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
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "structure_tutorial";
    public static final Logger LOGGER = LogManager.getLogger();
    public static Map<ChunkPos, List<StructureStart>> poses = new HashMap<>();
    public static List<BoundingBox> box = new ArrayList<>();

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

            int radius = (16 * 8);

            ChunkPos chunkPos = new ChunkPos(player.getOnPos());

            int X_MIN = chunkPos.getMiddleBlockX() - radius;
            int X_MAX = chunkPos.getMiddleBlockX() + radius;

            int Z_MIN = chunkPos.getMiddleBlockZ() - radius;
            int Z_MAX = chunkPos.getMiddleBlockZ() + radius;

            for (int x = X_MIN; x < X_MAX; x += 9) {
                for (int z = Z_MIN; z < Z_MAX; z += 9) {
                    BlockPos pos = new BlockPos(x, (int)player.getY(), z);

                    if (!poses.containsKey(chunkPos)) {
                        SectionPos sectionpos = SectionPos.of(pos);
                        Map<ConfiguredStructureFeature<?, ?>, LongSet> r = level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();

                        for (Map.Entry<ConfiguredStructureFeature<?, ?>, LongSet> entry : r.entrySet()) {
                            List<StructureStart> starts = startsForFeature(level, SectionPos.of(pos), entry.getKey());

                            poses.put(chunkPos, starts);

                            for (StructureStart start : starts) {
                                if (start.getPieces().size() > 0 && !box.contains(start.getBoundingBox())) {
                                    box.add(start.getBoundingBox());
                                    sendStructureDebug(player, null, start.getBoundingBox());

                                    for (StructurePiece piece : start.getPieces()) {
                                        if (piece instanceof VillageBuildablePiece dub) {
                                            sendStructureDebug(player, dub.getStructureData(), piece.getBoundingBox());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    chunkPos = new ChunkPos(pos);
                }
            }
        }
    }

    public static List<StructureStart> startsForFeature(ServerLevel level, SectionPos sectionPos, ConfiguredStructureFeature<?, ?> feature) {
        LongSet longset = level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(feature);
        ImmutableList.Builder<StructureStart> builder = ImmutableList.builder();
        level.structureFeatureManager().fillStartsForFeature(feature, longset, builder::add);
        return builder.build();
    }

    private void sendStructureDebug(ServerPlayer player, @Nullable CompoundTag tag, BoundingBox box) {
        System.out.println("Server - StructureData " + box);

        BlockPos one = new BlockPos(box.minX(), box.minY(), box.minZ());
        BlockPos two = new BlockPos(box.maxX(), box.maxY(), box.maxZ());

        Network.INSTANCE.sendTo(new StructureDebugPacket(tag, one, two), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
