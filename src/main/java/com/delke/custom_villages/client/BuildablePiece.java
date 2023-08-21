package com.delke.custom_villages.client;

import com.delke.custom_villages.client.render.RenderingUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Bailey Delker
 * @created 08/20/2023 - 1:55 PM
 * @project structures-1.18.2
 */

@OnlyIn(Dist.CLIENT)
public class BuildablePiece {
    private final BoundingBox box;
    private BuildablePiece.Palette palette;

    public BuildablePiece(BoundingBox box, CompoundTag tag) {
        this.box = box;

        if (tag != null) {
            ListTag paletteTag = (ListTag)tag.get("palette");
            ListTag blocksTag = (ListTag)tag.get("blocks");

            if (paletteTag != null && blocksTag != null) {
                 this.palette = loadPalette(paletteTag, blocksTag);
            }
        }
    }

    public void renderGui(PoseStack stack) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        Player player = mc.player;

        if (player != null) {
            Vec3 vec3 = player.getViewVector(1.0F).normalize();

            BoundingBox expanded = box.inflatedBy(3);

            // Points representing corners of the 3D box
            Vec3 boxMin = new Vec3(expanded.minX(), expanded.minY(), expanded.minZ());
            Vec3 boxMax = new Vec3(expanded.maxX(), expanded.maxY(), expanded.maxZ());

            Vec3 playerPos = new Vec3(player.getX(), player.getEyeY(), player.getZ());

            boolean isLookingAtBox = false;

            for (int face = 0; face < 6; face++) {
                Vec3 normal;
                Vec3 pointOnFace;

                switch (face) {
                    case 0: // +X face
                        normal = new Vec3(1, 0, 0);
                        pointOnFace = boxMax;
                        break;
                    case 1: // -X face
                        normal = new Vec3(-1, 0, 0);
                        pointOnFace = boxMin;
                        break;
                    case 2: // +Y face
                        normal = new Vec3(0, 1, 0);
                        pointOnFace = boxMax;
                        break;
                    case 3: // -Y face
                        normal = new Vec3(0, -1, 0);
                        pointOnFace = boxMin;
                        break;
                    case 4: // +Z face
                        normal = new Vec3(0, 0, 1);
                        pointOnFace = boxMax;
                        break;
                    case 5: // -Z face
                    default:
                        normal = new Vec3(0, 0, -1);
                        pointOnFace = boxMin;
                        break;
                }

                double t = (pointOnFace.subtract(playerPos)).dot(normal) / vec3.dot(normal);

                if (t > 0) {
                    Vec3 scaledVec3 = new Vec3(vec3.x * t, vec3.y * t, vec3.z * t);
                    Vec3 intersection = playerPos.add(scaledVec3);

                    if (intersection.x >= boxMin.x && intersection.x <= boxMax.x &&
                            intersection.y >= boxMin.y && intersection.y <= boxMax.y &&
                            intersection.z >= boxMin.z && intersection.z <= boxMax.z) {
                        isLookingAtBox = true;
                        break;
                    }
                }
            }

            if (player.getBoundingBox().intersects(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()) || isLookingAtBox) {
                int y = mc.getWindow().getGuiScaledHeight() / 3;

                for (Map.Entry<Block, List<StructureTemplate.StructureBlockInfo>> entry : palette.getCache().entrySet()) {
                    font.drawShadow(stack, entry.getValue().size() + "", 50 + 18, y + 3, 23721831);
                    renderCustomSlot(new ItemStack(entry.getKey().asItem()), 50, y);
                    y += 18;
                }
            }
        }
    }

    public void renderWorld(PoseStack stack) {
        for (StructureTemplate.StructureBlockInfo info : palette.blocks()) {
            BlockState state = info.state;
            BlockPos pos = new BlockPos(box.minX(), box.minY(), box.minZ()).offset(info.pos);

            if (!state.isAir()) {
                RenderingUtil.renderBlock(stack, state, pos);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BuildablePiece piece) {
            return box.equals(piece.box);
        }
        return false;
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

        Palette(List<StructureTemplate.StructureBlockInfo> list) {
            this.blocks = list;

            for (StructureTemplate.StructureBlockInfo info : list) {
               List<StructureTemplate.StructureBlockInfo> t = cache.getOrDefault(info.state.getBlock(), new ArrayList<>());

               t.add(info);

               cache.put(info.state.getBlock(), t);
            }
        }

        public List<StructureTemplate.StructureBlockInfo> blocks() {
            return this.blocks;
        }

        public Map<Block, List<StructureTemplate.StructureBlockInfo>> getCache() {
            return this.cache;
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

    private void renderCustomSlot(ItemStack itemstack, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();
        Font font = mc.font;

        int imageWidth = 176;
        itemRenderer.blitOffset = 100.0F;

        //Render item icon
        RenderSystem.enableDepthTest();
        itemRenderer.renderAndDecorateItem(mc.player, itemstack, x, y, x + y * imageWidth);

        //Render item string count
        PoseStack posestack = new PoseStack();
        if (itemstack.getCount() != 1) {
            String s = String.valueOf(itemstack.getCount());
            posestack.translate(0.0D, 0.0D, itemRenderer.blitOffset + 200.0F);
            MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

            float stringX = (x + 19f - 2 - font.width(s));
            float stringY = (y + 6f + 3);
            font.drawInBatch(s,stringX,stringY,16777215, true, posestack.last().pose(), multibuffersource$buffersource, false, 0, 15728880);

            multibuffersource$buffersource.endBatch();
        }
        itemRenderer.blitOffset = 0.0F;
    }
}
