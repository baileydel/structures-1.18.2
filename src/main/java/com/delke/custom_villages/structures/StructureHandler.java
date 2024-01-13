package com.delke.custom_villages.structures;

import com.delke.custom_villages.network.GetStructurePacket;
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

public class StructureHandler {
    public static Map<ChunkPos, VillageStructureInstance> INSTANCES = new HashMap<>();

    public static VillageStructureInstance getInstance(ChunkPos pos) {
        return StructureHandler.INSTANCES.get(pos);
    }

    //TODO In reality when we create a village structure we are going to save the structure pos in a file, rather than searching chunks
    // this was only good for searching for random spawns
    // we should really convert it the other way

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

                if (!StructureHandler.INSTANCES.containsKey(chunkPos)) {
                    SectionPos sectionpos = SectionPos.of(finalChunkPos, 0);
                    ChunkAccess access = level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_REFERENCES);

                    Map<ConfiguredStructureFeature<?, ?>, LongSet> r = access.getAllReferences();

                    for (Map.Entry<ConfiguredStructureFeature<?, ?>, LongSet> entry : r.entrySet()) {
                        List<StructureStart> s = StructureHandler.startsForFeature(level, sectionpos, entry.getKey());

                        for (StructureStart start : s) {
                            if (start.getFeature().feature instanceof VillageStructure) {
                                GetStructurePacket.send(player, start);
                                StructureHandler.INSTANCES.put(chunkPos, new VillageStructureInstance(level, start, access));
                            }
                        }
                    }
                }
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
