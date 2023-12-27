package com.delke.custom_villages.network;

import com.delke.custom_villages.structures.StructureHandler;
import com.delke.custom_villages.structures.villagestructure.VillageStructure;
import com.delke.custom_villages.structures.villagestructure.VillageStructureInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Bailey Delker
 * @created 09/01/2023 - 2:20 PM
 * @project structures-1.18.2
 */

public class GenerateStructurePacket {
    public int x;
    public int z;

    public GenerateStructurePacket(int chunkX, int chunkZ) {
        x = chunkX;
        z = chunkZ;
    }

    public GenerateStructurePacket(FriendlyByteBuf buf) {
        x = buf.readInt();
        z = buf.readInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(z);
    }

    /*
        Only handle on server
     */
    //TODO Refactor this
    public static void handle(GenerateStructurePacket msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null) {
                StructureHandler.INSTANCES.clear();

                ConfiguredStructureFeature<?, ?> feature = VillageStructureInstance.makeStructure();

                if (feature != null) {
                    ServerLevel level = ctx.getSender().getLevel();
                    ChunkPos pos = new ChunkPos(msg.x, msg.z);
                    ChunkAccess chunk = level.getChunk(msg.x, msg.z);

                    chunk.setAllStarts(Map.of());

                    StructureManager STRUCTURE_MANAGER = level.getStructureManager();
                    StructureFeatureManager featureManager = level.structureFeatureManager();
                    RegistryAccess REGISTRY_ACCESS = featureManager.registryAccess();

                    SectionPos sectionPos = SectionPos.of(pos, 0);
                    ChunkGenerator generator = level.getChunkSource().getGenerator();
                    StructureStart start = tryGenerateStructure(feature, generator, featureManager, REGISTRY_ACCESS, STRUCTURE_MANAGER, VillageStructure.SEED, chunk, pos, sectionPos);

                    chunk.setStartForFeature(feature, start);
                    chunk.addReferenceForFeature(feature, ChunkPos.asLong(0, 0));

                    WorldgenRandom worldgenrandom = new WorldgenRandom(new XoroshiroRandomSource(VillageStructure.SEED));

                    if (start != StructureStart.INVALID_START) {
                        BoundingBox box = getWritableArea(chunk);

                        featureManager.startsForFeature(sectionPos, feature).forEach(
                                (structureStart) -> structureStart.placeInChunk(level, featureManager, generator, worldgenrandom, box, pos)
                        );
                    }
                }
            }
        });
        ctx.setPacketHandled(true);

        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null) {

                ServerLevel level = (ServerLevel) ctx.getSender().level;

                for (int x = 50; x > -50; x--) {
                    for (int y = -50; y > -60; y--) {
                        for (int z = 50; z > -50; z--) {
                            BlockPos pos = new BlockPos(x, y, z);
                            BlockState state = level.getBlockState(pos);

                            if (!state.isAir()) {
                                level.destroyBlock(pos, false, null);
                            }
                        }
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }


    private static BoundingBox getWritableArea(ChunkAccess chunkAccess) {
        ChunkPos chunkpos = chunkAccess.getPos();
        int i = chunkpos.getMinBlockX();
        int j = chunkpos.getMinBlockZ();
        LevelHeightAccessor levelheightaccessor = chunkAccess.getHeightAccessorForGeneration();
        int k = levelheightaccessor.getMinBuildHeight() + 1;
        int l = levelheightaccessor.getMaxBuildHeight() - 1;
        return new BoundingBox(i, k, j, i + 15, l, j + 15);
    }


    private static StructureStart tryGenerateStructure(ConfiguredStructureFeature<?, ?> configuredstructurefeature, ChunkGenerator generator, StructureFeatureManager featureManager, RegistryAccess access, StructureManager p_208020_, long p_208021_, ChunkAccess chunkAccess, ChunkPos chunkPos, SectionPos sectionPos) {
        try {
            int i = fetchReferences(featureManager, chunkAccess, sectionPos, configuredstructurefeature);

            Predicate<Holder<Biome>> predicate = (w) -> true;

            StructureStart structurestart = configuredstructurefeature.generate(access, generator, generator.getBiomeSource(), p_208020_, p_208021_, chunkPos, i, chunkAccess, predicate);

            if (structurestart.isValid()) {
                featureManager.setStartForFeature(sectionPos, configuredstructurefeature, structurestart, chunkAccess);
                return structurestart;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return StructureStart.INVALID_START;
    }

    private static int fetchReferences(StructureFeatureManager p_207977_, ChunkAccess p_207978_, SectionPos p_207979_, ConfiguredStructureFeature<?, ?> p_207980_) {
        StructureStart structurestart = p_207977_.getStartForFeature(p_207979_, p_207980_, p_207978_);
        return structurestart != null ? structurestart.getReferences() : 0;
    }
}
