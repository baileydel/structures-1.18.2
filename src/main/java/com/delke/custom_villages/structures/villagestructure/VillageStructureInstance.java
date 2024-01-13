package com.delke.custom_villages.structures.villagestructure;

import com.delke.custom_villages.mixin.StructureStartAccessor;
import com.delke.custom_villages.structures.StructureHandler;
import com.delke.custom_villages.structures.pieces.BuildablePiece;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

import java.util.List;
import java.util.Map;

import static com.delke.custom_villages.Main.MODID;

public class VillageStructureInstance {
    private List<StructurePiece> pieces;
    private final StructureStart start;
    private PieceGeneratorSupplier.Context<?> context;
    private final ChunkAccess chunk;

    public VillageStructureInstance(ServerLevel level, StructureStart start, ChunkAccess chunk) {
        this.start = start;
        this.pieces = start.getPieces();
        this.chunk = chunk;
        createContext(level, start);
    }

    public void movePiece(String name, int x, int y, int z) {
        for (StructurePiece piece : this.pieces) {
            if (piece instanceof BuildablePiece buildablePiece) {
                if (buildablePiece.getName().equals(name)) {

                    buildablePiece.move(x, z);
                    savePieces(this.pieces);

                    break;
                }
            }
        }
    }

    public List<StructurePiece> getPieces() {
        return pieces;
    }

    public void savePieces(List<StructurePiece> pieces) {
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
                ResourceKey<ConfiguredStructureFeature<?, ?>> th = ResourceKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, new ResourceLocation(MODID, "new_village"));

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

        this.context = new PieceGeneratorSupplier.Context<>(generator, biomeSource, VillageStructure.SEED, chunkPos, config, chunk, (w) -> true, structureManager, registryAccess);
    }
}
