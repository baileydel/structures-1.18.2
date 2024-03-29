package com.delke.custom_villages.structures.pieces;

import com.delke.custom_villages.structures.StructureRegistry;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/*
   This is only used for village structures,
 */
public class BuildablePiece extends StructurePiece {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final StructurePoolElement element;
    protected BlockPos position;
    private final int groundLevelDelta;
    protected final Rotation rotation;
    private final List<JigsawJunction> junctions = Lists.newArrayList();
    private final StructureManager structureManager;
    private CompoundTag tag;
    private final String name;

    public BuildablePiece(StructureManager structureManager, StructurePoolElement element, BlockPos pos, int groundLevelDelta, Rotation rotation, BoundingBox boundingBox) {
        super(StructureRegistry.VILLAGE_BUILDABLE_PIECE.get(), 0, boundingBox);
        this.structureManager = structureManager;
        this.element = element;
        this.position = pos;
        this.groundLevelDelta = groundLevelDelta;
        this.rotation = rotation;
        this.name = "";
    }

    public BuildablePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(StructureRegistry.VILLAGE_BUILDABLE_PIECE.get(), tag);
        this.structureManager = context.structureManager();
        this.position = new BlockPos(tag.getInt("PosX"), tag.getInt("PosY"), tag.getInt("PosZ"));
        this.groundLevelDelta = tag.getInt("ground_level_delta");
        DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, context.registryAccess());
        this.element = StructurePoolElement.CODEC.parse(dynamicops, tag.getCompound("pool_element")).resultOrPartial(LOGGER::error).orElseThrow(() -> new IllegalStateException("Invalid pool element found"));
        this.rotation = Rotation.valueOf(tag.getString("rotation"));
        this.boundingBox = this.element.getBoundingBox(this.structureManager, this.position, this.rotation);
        ListTag listtag = tag.getList("junctions", 10);
        this.junctions.clear();
        listtag.forEach((p_204943_) -> this.junctions.add(JigsawJunction.deserialize(new Dynamic<>(dynamicops, p_204943_))));
        this.name = "";
    }

    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag nbt) {
        nbt.putInt("PosX", this.position.getX());
        nbt.putInt("PosY", this.position.getY());
        nbt.putInt("PosZ", this.position.getZ());
        nbt.putInt("ground_level_delta", this.groundLevelDelta);
        DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, context.registryAccess());
        StructurePoolElement.CODEC.encodeStart(dynamicops, this.element).resultOrPartial(LOGGER::error).ifPresent((p_163125_) -> nbt.put("pool_element", p_163125_));
        nbt.putString("rotation", this.rotation.name());
        ListTag listtag = new ListTag();

        for (JigsawJunction jigsawjunction : this.junctions) {
            listtag.add(jigsawjunction.serialize(dynamicops).getValue());
        }

        nbt.put("junctions", listtag);

        if (!nbt.contains("block_data") && getStructureData() != null) {
            nbt.put("block_data", getStructureData());
        }
    }

    public void postProcess(@NotNull WorldGenLevel p_192409_, @NotNull StructureFeatureManager p_192410_, @NotNull ChunkGenerator p_192411_, @NotNull Random p_192412_, @NotNull BoundingBox p_192413_, @NotNull ChunkPos p_192414_, @NotNull BlockPos p_192415_) {
        this.place(p_192409_, p_192410_, p_192411_, p_192412_, p_192413_, p_192415_, false);
    }

    public void place(@NotNull WorldGenLevel genLevel, @NotNull StructureFeatureManager featureManager, @NotNull ChunkGenerator chunkGenerator, @NotNull Random random, @NotNull BoundingBox boundingBox, @NotNull BlockPos blockPos, boolean idk) {
        //this.element.place(this.structureManager, genLevel, featureManager, chunkGenerator, this.position, blockPos , this.rotation, boundingBox, random, idk);
    }

    public void move(int x, int y, int z) {
        super.move(x, y, z);
        this.position = this.position.offset(x, y, z);
    }

    public void move(int x, int z) {
        this.boundingBox = this.boundingBox.moved(x, 0, z);
        this.position = new BlockPos(boundingBox.maxX(), boundingBox.minY(), boundingBox.minZ());
    }

    public @NotNull Rotation getRotation() {
        return this.rotation;
    }

    public StructurePoolElement getElement() {
        return this.element;
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public int getGroundLevelDelta() {
        return this.groundLevelDelta;
    }

    public void addJunction(JigsawJunction junction) {
        this.junctions.add(junction);
    }

    //TODO Retrieve tag from saved data
    @Nullable
    public CompoundTag getStructureData() {
        tag = new CompoundTag();
        String[] spl = element.toString().split("\\[");
        Optional<StructureTemplate> template =  structureManager.get(new ResourceLocation(spl[spl.length - 1].replace("]]", "")));

        template.ifPresent(structureTemplate -> tag = structureTemplate.save(tag));

        if (!tag.isEmpty()) {
            CompoundTag tag = new CompoundTag();

            ListTag listTag1 = this.tag.getList("blocks", 10);
            ListTag listTag2 = (ListTag)this.tag.get("palette");

            tag.put("blocks", listTag1);

            if (listTag2 != null) {
                tag.put("palette", listTag2);
            }
            return tag;
        }
        return null;
    }

    public String getName() {
        return getElement().toString().split("Left\\[")[1].split("]]")[0];
    }

    public String toString() {
        return String.format("<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.position, this.rotation, this.element);
    }
}