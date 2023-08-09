package com.delke.custom_villages.client;

import com.delke.custom_villages.network.StructureDebugPacket;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Bailey Delker
 * @created 07/30/2023 - 12:16 PM
 * @project structures-1.18.2
 */

@OnlyIn(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public void RenderLevelStageEvent(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && mc.level != null && event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS)) {
            for (Pair<CompoundTag, BoundingBox> pair : StructureDebugPacket.data) {
                BoundingBox box = pair.getSecond();
                CompoundTag tag = pair.getFirst();

                if (box.getCenter().closerThan(player.getOnPos(), mc.options.renderDistance * 16)) {
                    RenderingUtil.renderBoundingBox(event.getPoseStack(), box);
                }

                if (tag != null) {
                    ListTag paletteTag = (ListTag)tag.get("palette");
                    ListTag blocksTag = (ListTag)tag.get("blocks");

                    if (paletteTag != null && blocksTag != null) {
                        Palette palette = loadPalette(paletteTag, blocksTag);

                        for (StructureTemplate.StructureBlockInfo info : palette.blocks()) {
                            BlockState state = info.state;
                            BlockPos pos = new BlockPos(box.minX(), box.minY(), box.minZ()).offset(info.pos);

                            if (!state.isAir()) {
                                renderBlock(event.getPoseStack(), state, pos);
                            }
                        }
                    }
                }
            }
        }
    }

    private void renderBlock(PoseStack matrix, BlockState state, BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null) {
            BlockRenderDispatcher renderer = Minecraft.getInstance().getBlockRenderer();

            Camera camera = mc.gameRenderer.getMainCamera();
            Vec3 vec3 = camera.getPosition();

            double d0 = vec3.x();
            double d1 = vec3.y();
            double d2 = vec3.z();


            matrix.pushPose();
            matrix.translate(
                    (double)pos.getX() - d0,
                    (double)pos.getY() - d1,
                    (double)pos.getZ() - d2
            );



            renderer.renderSingleBlock(
                    state,
                    matrix,
                    mc.renderBuffers().crumblingBufferSource(),
                    15728880,
                    OverlayTexture.WHITE_OVERLAY_V,
                    EmptyModelData.INSTANCE
            );
            matrix.popPose();
        }
    }


    private Palette loadPalette(ListTag p_74621_, ListTag p_74622_) {
        SimplePalette structuretemplate$simplepalette = new SimplePalette();

        for (int i = 0; i < p_74621_.size(); ++i) {
            structuretemplate$simplepalette.addMapping(NbtUtils.readBlockState(p_74621_.getCompound(i)), i);
        }

        List<StructureTemplate.StructureBlockInfo> list2 = Lists.newArrayList();
        List<StructureTemplate.StructureBlockInfo> list = Lists.newArrayList();
        List<StructureTemplate.StructureBlockInfo> list1 = Lists.newArrayList();

        BlockPos blockpos;
        for (int j = 0; j < p_74622_.size(); ++j) {
            CompoundTag compoundtag = p_74622_.getCompound(j);
            ListTag listtag = compoundtag.getList("pos", 3);
            blockpos = new BlockPos(listtag.getInt(0), listtag.getInt(1), listtag.getInt(2));
            BlockState blockstate = structuretemplate$simplepalette.stateFor(compoundtag.getInt("state"));
            CompoundTag compoundtag1;
            if (compoundtag.contains("nbt")) {
                compoundtag1 = compoundtag.getCompound("nbt");
            } else {
                compoundtag1 = null;
            }

            StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo = new StructureTemplate.StructureBlockInfo(blockpos, blockstate, compoundtag1);
            addToLists(structuretemplate$structureblockinfo, list2, list, list1);
        }

        List<StructureTemplate.StructureBlockInfo> list3 = buildInfoList(list2, list, list1);

        return new Palette(list3);
    }

    private static List<StructureTemplate.StructureBlockInfo> buildInfoList(List<StructureTemplate.StructureBlockInfo> p_74615_, List<StructureTemplate.StructureBlockInfo> p_74616_, List<StructureTemplate.StructureBlockInfo> p_74617_) {
        Comparator<StructureTemplate.StructureBlockInfo> comparator = Comparator.<StructureTemplate.StructureBlockInfo>comparingInt((p_74641_) -> p_74641_.pos.getY()).thenComparingInt((p_74637_) -> p_74637_.pos.getX()).thenComparingInt((p_74572_) -> p_74572_.pos.getZ());
        p_74615_.sort(comparator);
        p_74617_.sort(comparator);
        p_74616_.sort(comparator);
        List<StructureTemplate.StructureBlockInfo> list = Lists.newArrayList();
        list.addAll(p_74615_);
        list.addAll(p_74617_);
        list.addAll(p_74616_);
        return list;
    }

    private static void addToLists(StructureTemplate.StructureBlockInfo p_74574_, List<StructureTemplate.StructureBlockInfo> p_74575_, List<StructureTemplate.StructureBlockInfo> p_74576_, List<StructureTemplate.StructureBlockInfo> p_74577_) {
        if (p_74574_.nbt != null) {
            p_74576_.add(p_74574_);
        } else if (!p_74574_.state.getBlock().hasDynamicShape() && p_74574_.state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
            p_74575_.add(p_74574_);
        } else {
            p_74577_.add(p_74574_);
        }
    }

    public static final class Palette {
        private final List<StructureTemplate.StructureBlockInfo> blocks;
        private final Map<Block, List<StructureTemplate.StructureBlockInfo>> cache = Maps.newHashMap();

        Palette(List<StructureTemplate.StructureBlockInfo> p_74648_) {
            this.blocks = p_74648_;
        }

        public List<StructureTemplate.StructureBlockInfo> blocks() {
            return this.blocks;
        }

        public List<StructureTemplate.StructureBlockInfo> blocks(Block p_74654_) {
            return this.cache.computeIfAbsent(p_74654_, (p_74659_) -> this.blocks.stream().filter((p_163818_) -> p_163818_.state.is(p_74659_)).collect(Collectors.toList()));
        }
    }

    static class SimplePalette implements Iterable<BlockState> {
        public static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.defaultBlockState();
        private final IdMapper<BlockState> ids = new IdMapper<>(16);
        private int lastId;

        public int idFor(BlockState p_74670_) {
            int i = this.ids.getId(p_74670_);
            if (i == -1) {
                i = this.lastId++;
                this.ids.addMapping(p_74670_, i);
            }

            return i;
        }

        @Nullable
        public BlockState stateFor(int p_74668_) {
            BlockState blockstate = this.ids.byId(p_74668_);
            return blockstate == null ? DEFAULT_BLOCK_STATE : blockstate;
        }

        public Iterator<BlockState> iterator() {
            return this.ids.iterator();
        }

        public void addMapping(BlockState p_74672_, int p_74673_) {
            this.ids.addMapping(p_74672_, p_74673_);
        }
    }
}
