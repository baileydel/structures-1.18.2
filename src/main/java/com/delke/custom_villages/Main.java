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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
    public static List<ChunkPos> posList = new ArrayList<>();

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

            List<ChunkPos> posList1 = getUnloadedChunks(player);
            List<BoundingBox> boxes = new ArrayList<>();

            for (ChunkPos chunkPos : posList1) {
                BlockPos pos = new BlockPos(chunkPos.x, (int) player.getY(), chunkPos.z);
                SectionPos sectionpos = SectionPos.of(pos);

                Map<ConfiguredStructureFeature<?, ?>, LongSet> r = level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();

                for (Map.Entry<ConfiguredStructureFeature<?, ?>, LongSet> entry : r.entrySet()) {
                    List<StructureStart> starts = startsForFeature(level, SectionPos.of(pos), entry.getKey());

                    for (StructureStart start : starts) {
                        if (start.getPieces().size() > 0) {
                            if (!boxes.contains(start.getBoundingBox())) {
                                boxes.add(start.getBoundingBox());

                                sendStructureDebug(player, null, start.getBoundingBox(), Rotation.NONE);

                                for (StructurePiece piece : start.getPieces()) {
                                    if (piece instanceof VillageBuildablePiece dub) {
                                        if (!boxes.contains(piece.getBoundingBox())) {
                                            boxes.add(piece.getBoundingBox());
                                            sendStructureDebug(player, dub.getStructureData(), piece.getBoundingBox(), piece.getRotation());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private List<ChunkPos> getUnloadedChunks(Player player) {
        List<ChunkPos> posList = new ArrayList<>();
        int radius = (16 * 8);

        ChunkPos chunkPos = new ChunkPos(player.getOnPos());

        int X_MIN = chunkPos.getMiddleBlockX() - radius;
        int X_MAX = chunkPos.getMiddleBlockX() + radius;

        int Z_MIN = chunkPos.getMiddleBlockZ() - radius;
        int Z_MAX = chunkPos.getMiddleBlockZ() + radius;

        for (int x = X_MIN; x < X_MAX; x += 9) {
            for (int z = Z_MIN; z < Z_MAX; z += 9) {
                if (!Main.posList.contains(chunkPos)) {
                    Main.posList.add(chunkPos);
                    posList.add(chunkPos);
                }
                chunkPos = new ChunkPos(x, z);
            }
        }
        return posList;
    }

    public static List<StructureStart> startsForFeature(ServerLevel level, SectionPos sectionPos, ConfiguredStructureFeature<?, ?> feature) {
        LongSet longset = level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(feature);
        ImmutableList.Builder<StructureStart> builder = ImmutableList.builder();
        level.structureFeatureManager().fillStartsForFeature(feature, longset, builder::add);
        return builder.build();
    }

    private void sendStructureDebug(ServerPlayer player, @Nullable CompoundTag tag, BoundingBox box, Rotation rotation) {
        System.out.println("Server - StructureData " + box);

        BlockPos min = new BlockPos(box.minX(), box.minY(), box.minZ());
        BlockPos max = new BlockPos(box.maxX(), box.maxY(), box.maxZ());

        Network.INSTANCE.sendTo(new StructureDebugPacket(tag, min, max, rotation), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
