package com.delke.custom_villages;

import com.delke.custom_villages.client.ClientEvents;
import com.delke.custom_villages.network.Network;
import com.delke.custom_villages.network.StructureDebugPacket;
import com.delke.custom_villages.structures.STStructures;
import com.delke.custom_villages.structures.VillageBuildablePiece;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bailey Delker
 * @created 06/20/2023 - 7:15 AM
 * @project structures-1.18.2
 */
@Mod(Main.MODID)
public class Main {
    public static final String MODID = "structure_tutorial";
    public static final Map<ChunkPos, List<StructureStart>> chunkMap = new HashMap<>();

    public Main() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::CommonSetup);
        bus.addListener(this::ClientSetup);

        STStructures.DEFERRED_REGISTRY_STRUCTURE.register(bus);
        STStructures.REGISTER.register(bus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void ClientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    private void CommonSetup(final FMLCommonSetupEvent event) {
        Network.init();
    }

    /*
        Common / Server events
     */
    @SubscribeEvent
    public void WorldTick(TickEvent.WorldTickEvent event) {
        if (event.side.isServer()) {
            ServerLevel level = (ServerLevel)event.world;
            ServerPlayer player = level.getRandomPlayer();

            if (player == null) {
                return;
            }

            sendUnloadedChunks(level, player);
        }
    }

    private void sendUnloadedChunks(ServerLevel level, ServerPlayer player) {
        ChunkPos chunkPos = new ChunkPos(player.getOnPos());

        int radius = (16 * 8);

        int X_MIN = chunkPos.getMiddleBlockX() - radius;
        int X_MAX = chunkPos.getMiddleBlockX() + radius;

        int Z_MIN = chunkPos.getMiddleBlockZ() - radius;
        int Z_MAX = chunkPos.getMiddleBlockZ() + radius;

        for (int x = X_MIN; x < X_MAX; x += 9) {
            for (int z = Z_MIN; z < Z_MAX; z += 9) {
                ChunkPos finalChunkPos = chunkPos;

                chunkMap.computeIfAbsent(chunkPos, list -> {
                    SectionPos sectionpos = SectionPos.of(finalChunkPos, 0);

                    Map<ConfiguredStructureFeature<?, ?>, LongSet> r = level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();

                    List<StructureStart> starts = new ArrayList<>();

                    for (Map.Entry<ConfiguredStructureFeature<?, ?>, LongSet> entry : r.entrySet()) {
                         starts.addAll(startsForFeature(level, sectionpos, entry.getKey()));
                    }

                    for (StructureStart start : starts) {
                        sendStartDebug(player, start);
                    }
                    return new ArrayList<>();
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

    private void sendStartDebug(ServerPlayer player, StructureStart start) {
        StructureDebugPacket packet = new StructureDebugPacket(null, start.getBoundingBox(), Rotation.NONE);
        Network.INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);

        for (StructurePiece piece : start.getPieces()) {
            CompoundTag tag = null;

            if (piece instanceof VillageBuildablePiece buildablePiece) {
                tag = buildablePiece.getStructureData();
            }

            packet = new StructureDebugPacket(tag, piece.getBoundingBox(), piece.getRotation());

            Network.INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }
}
