package com.delke.custom_villages.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

//TODO This should really only be a palette for the structure
    // the buildable piece should contain all of the placement info
public class ModPalette {
    private List<StructureTemplate.StructureBlockInfo> blocks;
    private final Map<Block, List<StructureTemplate.StructureBlockInfo>> cache = Maps.newHashMap();

    public ModPalette(@Nonnull CompoundTag tag) {
        ListTag paletteTag = (ListTag) tag.get("palette");
        ListTag blocksTag = (ListTag)tag.get("blocks");

        if (paletteTag != null && blocksTag != null) {
            SimplePalette structuretemplate$simplepalette = new SimplePalette();

            for (int i = 0; i < paletteTag.size(); ++i) {
                structuretemplate$simplepalette.addMapping(NbtUtils.readBlockState(paletteTag.getCompound(i)), i);
            }

            List<StructureTemplate.StructureBlockInfo> list2 = Lists.newArrayList();
            List<StructureTemplate.StructureBlockInfo> list = Lists.newArrayList();
            List<StructureTemplate.StructureBlockInfo> list1 = Lists.newArrayList();

            BlockPos blockpos;
            for (int j = 0; j < blocksTag.size(); ++j) {
                CompoundTag compoundtag = blocksTag.getCompound(j);
                ListTag listtag = compoundtag.getList("pos", 3);

                blockpos = new BlockPos(listtag.getInt(0), listtag.getInt(1), listtag.getInt(2));
                BlockState blockstate = structuretemplate$simplepalette.stateFor(compoundtag.getInt("state"));

                if (blockstate != null) {
                    CompoundTag compoundtag1;

                    if (compoundtag.contains("nbt")) {
                        compoundtag1 = compoundtag.getCompound("nbt");
                    }
                    else {
                        compoundtag1 = null;
                    }

                    StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo = new StructureTemplate.StructureBlockInfo(blockpos, blockstate, compoundtag1);
                    addToLists(structuretemplate$structureblockinfo, list2, list, list1);
                }
            }

            blocks = buildInfoList(list2, list, list1);

            for (StructureTemplate.StructureBlockInfo info : blocks) {
                List<StructureTemplate.StructureBlockInfo> t = cache.getOrDefault(info.state.getBlock(), new ArrayList<>());

                t.add(info);

                cache.put(info.state.getBlock(), t);
            }
        }
    }

    public List<StructureTemplate.StructureBlockInfo> blocks() {
        return this.blocks;
    }

    //TODO Process the jigsaw blocks
    public Map<Block, List<StructureTemplate.StructureBlockInfo>> getCache() {
        this.cache.remove(Blocks.AIR);
        return this.cache;
    }

    public List<StructureTemplate.StructureBlockInfo> blocks(Block block) {
        return this.cache.computeIfAbsent(block, (cur) -> this.blocks.stream().filter((p_163818_) -> p_163818_.state.is(cur)).collect(Collectors.toList()));
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
