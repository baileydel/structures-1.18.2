package com.delke.custom_villages.structures.villagestructure;

import com.delke.custom_villages.mixin.StructureStartAccessor;
import com.delke.custom_villages.structures.StructureHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.delke.custom_villages.Main.MODID;

public class VillageStructureInstance {
    private List<StructurePiece> pieces;
    private final StructureStart start;
    private final VillageEconomy economy;
    private final ChunkAccess chunk;

    private PieceGeneratorSupplier.Context<?> context;

    public VillageStructureInstance(StructureStart start, ChunkAccess chunk) {
        this.start = start;
        this.economy = new VillageEconomy();
        this.pieces = start.getPieces();
        this.chunk = chunk;
    }

    public List<StructurePiece> getPieces() {
        return pieces;
    }

    private void addPiece(String piece) {

    }

    private void removePiece(String piece) {

    }

    private void getNextPiece() {

    }

    public void savePieces(ChunkAccess chunk, List<StructurePiece> pieces) {
        StructureHandler.INSTANCES.clear();
        this.pieces = pieces;
        ((StructureStartAccessor) (Object) start).setPieceContainer(new PiecesContainer(pieces));
        chunk.setAllStarts(Map.of(start.getFeature(), start));
    }

    public ChunkPos getChunkPos() {
        return start.getChunkPos();
    }

    public PieceGeneratorSupplier.Context<?> getContext() {
        return this.context;
    }

    public static ConfiguredStructureFeature<?, ?> makeStructure() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null && mc.getSingleplayerServer() != null) {
            ServerLevel level = mc.getSingleplayerServer().getLevel(mc.level.dimension());

            if (level != null) {
                ResourceKey<ConfiguredStructureFeature<?, ?>> th = ResourceKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, new ResourceLocation(MODID, "code_structure_sky_fan"));

                return level.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY).getOrThrow(th);
            }
        }
        return null;
    }


    public void createContext(ServerLevel level, StructureStart start) {
        FeatureConfiguration config = start.getFeature().config;

        ChunkGenerator generator = level.getChunkSource().getGenerator();
        BiomeSource biomeSource = generator.getBiomeSource();
        StructureManager structureManager = level.getStructureManager();

        ChunkPos chunkPos = start.getChunkPos();
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);

        StructureFeatureManager featureManager = level.structureFeatureManager();
        RegistryAccess registryAccess = featureManager.registryAccess();

        Predicate<Holder<Biome>> predicate = (w) -> true;

        this.context = new PieceGeneratorSupplier.Context<>(generator, biomeSource, VillageStructure.SEED, chunkPos, config, chunk, predicate, structureManager, registryAccess);

        System.out.println(this.context);
    }
}
