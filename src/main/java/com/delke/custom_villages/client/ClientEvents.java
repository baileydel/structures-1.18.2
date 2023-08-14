package com.delke.custom_villages.client;

import ca.weblite.objc.mappers.StructureMapping;
import com.delke.custom_villages.Main;
import com.delke.custom_villages.network.StructureDebugPacket;
import com.delke.custom_villages.structures.STStructures;
import com.delke.custom_villages.structures.SkyStructures;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.VoidStartPlatformFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.delke.custom_villages.Main.MODID;
import static net.minecraft.core.Registry.TEMPLATE_POOL_REGISTRY;
import static net.minecraft.tags.BiomeTags.IS_TAIGA;

/**
 * @author Bailey Delker
 * @created 07/30/2023 - 12:16 PM
 * @project structures-1.18.2
 */

@OnlyIn(Dist.CLIENT)
public class ClientEvents {

    boolean loaded = false;
    @SubscribeEvent
    public void OnKey(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (!loaded && mc.player != null && event.getKey() == 67 && mc.getSingleplayerServer() != null && mc.level != null) {
            ServerLevel level = mc.getSingleplayerServer().getLevel(mc.level.dimension());

            if (level != null) {
                ConfiguredStructureFeature<?, ?> structureFeature = makeStructure();

                StructureManager structureManager = level.getStructureManager();
                StructureFeatureManager featureManager = level.structureFeatureManager();
                RegistryAccess registryAccess = level.registryAccess();

                long seed = -234892394;

                ChunkAccess chunkAccess = level.getChunk(0, 0);
                ChunkPos chunkPos = new ChunkPos(0, 0);

                SectionPos sectionPos = SectionPos.of(chunkPos, 0);

                ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();

                if (tryGenerateStructure(structureFeature, chunkGenerator, featureManager, registryAccess, structureManager, seed, chunkAccess, chunkPos, sectionPos)) {
                    System.out.println("dub");
                }
                else {
                    System.out.println("hm");
                }
            }
        }
    }

    private ConfiguredStructureFeature<?, ?> makeStructure() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null && mc.getSingleplayerServer() != null) {
            ServerLevel level = mc.getSingleplayerServer().getLevel(mc.level.dimension());

            if (level != null) {
                StructureTemplatePool pool = level.registryAccess().registryOrThrow(TEMPLATE_POOL_REGISTRY).get(new ResourceLocation(MODID, "sky_fan"));

                if (pool != null) {
                    Holder<StructureTemplatePool> holder = Holder.direct(pool);
                    return STStructures.SKY_STRUCTURES.get().configured(new JigsawConfiguration(holder, 6), IS_TAIGA);
                }
            }
        }
        return null;
    }

    private boolean tryGenerateStructure(ConfiguredStructureFeature<?, ?> configuredstructurefeature, ChunkGenerator generator, StructureFeatureManager p_208018_, RegistryAccess p_208019_, StructureManager p_208020_, long p_208021_, ChunkAccess p_208022_, ChunkPos p_208023_, SectionPos p_208024_) {
        int i = fetchReferences(p_208018_, p_208022_, p_208024_, configuredstructurefeature);
        HolderSet<Biome> holderset = configuredstructurefeature.biomes();

        Predicate<Holder<Biome>> predicate = (p_211672_) -> holderset.contains(this.adjustBiome(p_211672_));

        StructureStart structurestart = configuredstructurefeature.generate(p_208019_, generator, generator.getBiomeSource(), p_208020_, p_208021_, p_208023_, i, p_208022_, predicate);
        if (structurestart.isValid()) {
            p_208018_.setStartForFeature(p_208024_, configuredstructurefeature, structurestart, p_208022_);
            return true;
        } else {
            return false;
        }
    }

    private static int fetchReferences(StructureFeatureManager p_207977_, ChunkAccess p_207978_, SectionPos p_207979_, ConfiguredStructureFeature<?, ?> p_207980_) {
        StructureStart structurestart = p_207977_.getStartForFeature(p_207979_, p_207980_, p_207978_);
        return structurestart != null ? structurestart.getReferences() : 0;
    }

    protected Holder<Biome> adjustBiome(Holder<Biome> p_204385_) {
        return p_204385_;
    }

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
                                RenderingUtil.renderBlock(event.getPoseStack(), state, pos);
                            }
                        }
                    }
                }
            }
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
