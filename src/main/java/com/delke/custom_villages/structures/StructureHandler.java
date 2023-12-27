package com.delke.custom_villages.structures;

import com.delke.custom_villages.network.StructureDebugPacket;
import com.delke.custom_villages.structures.villagestructure.VillageStructure;
import com.delke.custom_villages.structures.villagestructure.VillageStructureInstance;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bailey Delker
 * @created 11/15/2023 - 3:35 PM
 * @project structures-1.18.2
 */
public class StructureHandler {
    public static Map<ChunkPos, VillageStructureInstance> INSTANCES = new HashMap<>();

    public static VillageStructureInstance getInstance(ChunkPos pos) {
        return StructureHandler.INSTANCES.get(pos);
    }

    public static void sendUnloadedChunks(ServerLevel level, ServerPlayer player) {
        ChunkPos chunkPos = new ChunkPos(player.getOnPos());

        int radius = (16 * 8);

        int X_MIN = chunkPos.getMiddleBlockX() - radius;
        int X_MAX = chunkPos.getMiddleBlockX() + radius;

        int Z_MIN = chunkPos.getMiddleBlockZ() - radius;
        int Z_MAX = chunkPos.getMiddleBlockZ() + radius;

        for (int x = X_MIN; x < X_MAX; x += 9) {
            for (int z = Z_MIN; z < Z_MAX; z += 9) {
                ChunkPos finalChunkPos = chunkPos;

                StructureHandler.INSTANCES.computeIfAbsent(chunkPos, (list) -> {
                    SectionPos sectionpos = SectionPos.of(finalChunkPos, 0);
                    ChunkAccess access = level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_REFERENCES);

                    Map<ConfiguredStructureFeature<?, ?>, LongSet> r = access.getAllReferences();

                    for (Map.Entry<ConfiguredStructureFeature<?, ?>, LongSet> entry : r.entrySet()) {
                        List<StructureStart> s = StructureHandler.startsForFeature(level, sectionpos, entry.getKey());

                        for (StructureStart start : s) {
                            if (start.getFeature().feature instanceof VillageStructure) {
                                StructureDebugPacket.send(player, start);
                                VillageStructureInstance instance = new VillageStructureInstance(start, access);
                                instance.createContext(level, start);
                                return instance;
                            }
                        }
                    }
                    return null;
                });

                chunkPos = new ChunkPos(new BlockPos(x, 0, z));
            }
        }
    }

    public static List<StructureStart> startsForFeature(ServerLevel level, SectionPos sectionPos, ConfiguredStructureFeature<?, ?> feature) {
        LongSet longset = level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(feature);
        ImmutableList.Builder<StructureStart> builder = ImmutableList.builder();
        level.structureFeatureManager().fillStartsForFeature(feature, longset, builder::add);
        return builder.build();
    }
}
