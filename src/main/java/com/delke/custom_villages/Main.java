package com.delke.custom_villages;

import com.delke.custom_villages.client.ClientEvents;
import com.delke.custom_villages.network.Network;
import com.delke.custom_villages.network.StructureDebugPacket;
import com.delke.custom_villages.structures.VillageBuildablePiece;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.StructureFeatureManager;
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
import java.util.List;
import java.util.Map;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "structure_tutorial";
    public static final Logger LOGGER = LogManager.getLogger();

    private static final List<Pair<CompoundTag, BoundingBox>> boxes = new ArrayList<>();

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

            if (player != null) {
                StructureFeatureManager featureManager = level.structureFeatureManager();
                BlockPos pos = player.getOnPos();

                for (Map.Entry<ConfiguredStructureFeature<?, ?>, LongSet> entry : featureManager.getAllStructuresAt(pos).entrySet()) {
                    StructureStart start = featureManager.getStructureAt(pos, entry.getKey());

                    if (start.getPieces().size() > 0) {
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
    }

    private void sendStructureDebug(ServerPlayer player, @Nullable CompoundTag tag, BoundingBox box) {
        Pair<CompoundTag, BoundingBox> data = Pair.of(tag, box);
        if (!boxes.contains(data)) {
            boxes.add(data);

            System.out.println("Server - StructureData " + data);

            BlockPos one = new BlockPos(box.minX(), box.minY(), box.minZ());
            BlockPos two = new BlockPos(box.maxX(), box.maxY(), box.maxZ());

            Network.INSTANCE.sendTo(new StructureDebugPacket(tag, one, two), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }
}
