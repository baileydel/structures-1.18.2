package com.delke.custom_villages.structures;

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

import static com.delke.custom_villages.STStructures.VILLAGE_BUILDABLE_PIECE;

/*
   This is only used for village structures,
 */
public class VillageBuildablePiece extends StructurePiece {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final StructurePoolElement element;
    protected BlockPos position;
    private final int groundLevelDelta;
    protected final Rotation rotation;
    private final List<JigsawJunction> junctions = Lists.newArrayList();
    private final StructureManager structureManager;
    private CompoundTag tag;

    public VillageBuildablePiece(StructureManager p_209910_, StructurePoolElement p_209911_, BlockPos p_209912_, int p_209913_, Rotation p_209914_, BoundingBox p_209915_) {
        super(VILLAGE_BUILDABLE_PIECE.get(), 0, p_209915_);
        this.structureManager = p_209910_;
        this.element = p_209911_;
        this.position = p_209912_;
        this.groundLevelDelta = p_209913_;
        this.rotation = p_209914_;
    }

    public VillageBuildablePiece(StructurePieceSerializationContext p_192406_, CompoundTag p_192407_) {
        super(VILLAGE_BUILDABLE_PIECE.get(), p_192407_);
        this.structureManager = p_192406_.structureManager();
        this.position = new BlockPos(p_192407_.getInt("PosX"), p_192407_.getInt("PosY"), p_192407_.getInt("PosZ"));
        this.groundLevelDelta = p_192407_.getInt("ground_level_delta");
        DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, p_192406_.registryAccess());
        this.element = StructurePoolElement.CODEC.parse(dynamicops, p_192407_.getCompound("pool_element")).resultOrPartial(LOGGER::error).orElseThrow(() -> new IllegalStateException("Invalid pool element found"));
        this.rotation = Rotation.valueOf(p_192407_.getString("rotation"));
        this.boundingBox = this.element.getBoundingBox(this.structureManager, this.position, this.rotation);
        ListTag listtag = p_192407_.getList("junctions", 10);
        this.junctions.clear();
        listtag.forEach((p_204943_) -> this.junctions.add(JigsawJunction.deserialize(new Dynamic<>(dynamicops, p_204943_))));
    }

    protected void addAdditionalSaveData(StructurePieceSerializationContext p_192425_, CompoundTag nbt) {
        nbt.putInt("PosX", this.position.getX());
        nbt.putInt("PosY", this.position.getY());
        nbt.putInt("PosZ", this.position.getZ());
        nbt.putInt("ground_level_delta", this.groundLevelDelta);
        DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, p_192425_.registryAccess());
        StructurePoolElement.CODEC.encodeStart(dynamicops, this.element).resultOrPartial(LOGGER::error).ifPresent((p_163125_) -> nbt.put("pool_element", p_163125_));
        nbt.putString("rotation", this.rotation.name());
        ListTag listtag = new ListTag();

        for (JigsawJunction jigsawjunction : this.junctions) {
            listtag.add(jigsawjunction.serialize(dynamicops).getValue());
        }

        nbt.put("junctions", listtag);

        if (!nbt.contains("custom") && getStructureData() != null) {
            nbt.put("custom", getStructureData());
        }
    }

    public void postProcess(@NotNull WorldGenLevel p_192409_, @NotNull StructureFeatureManager p_192410_, @NotNull ChunkGenerator p_192411_, @NotNull Random p_192412_, @NotNull BoundingBox p_192413_, @NotNull ChunkPos p_192414_, @NotNull BlockPos p_192415_) {
        this.place(p_192409_, p_192410_, p_192411_, p_192412_, p_192413_, p_192415_, false);
    }

    public void place(@NotNull WorldGenLevel genLevel, @NotNull StructureFeatureManager featureManager, @NotNull ChunkGenerator chunkGenerator, @NotNull Random random, @NotNull BoundingBox boundingBox, @NotNull BlockPos blockPos, boolean idk) {
        //this.element.place(this.structureManager, genLevel, featureManager, chunkGenerator, this.position, blockPos , this.rotation, boundingBox, random, idk);
    }

    public void move(int p_72616_, int p_72617_, int p_72618_) {
        super.move(p_72616_, p_72617_, p_72618_);
        this.position = this.position.offset(p_72616_, p_72617_, p_72618_);
    }

    public @NotNull Rotation getRotation() {
        return this.rotation;
    }

    public String toString() {
        return String.format("<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.position, this.rotation, this.element);
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

    public void addJunction(JigsawJunction p_209917_) {
        this.junctions.add(p_209917_);
    }

    public List<JigsawJunction> getJunctions() {
        return this.junctions;
    }

    //TODO Retrieve tag from saved data
    @Nullable
    public CompoundTag getStructureData() {
        this.tag = new CompoundTag();
        String[] spl = element.toString().split("\\[");
        Optional<StructureTemplate> template =  structureManager.get(new ResourceLocation(spl[spl.length - 1].replace("]]", "")));

        template.ifPresent(structureTemplate -> this.tag = structureTemplate.save(tag));
        if (!this.tag.isEmpty()) {
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
}