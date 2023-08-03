package com.telepathicgrunt.structuretutorial;

import com.telepathicgrunt.structuretutorial.client.ClientEvents;
import com.telepathicgrunt.structuretutorial.network.Network;
import com.telepathicgrunt.structuretutorial.network.StructureDebugPacket;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraftforge.client.event.RenderLevelStageEvent;
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

import java.util.*;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "structure_tutorial";
    public static final Logger LOGGER = LogManager.getLogger();
    List<BoundingBox> boxes = new ArrayList<>();

    public Main() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::CommonSetup);
        bus.addListener(this::ClientSetup);

        STStructures.DEFERRED_REGISTRY_STRUCTURE.register(bus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void ClientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }

    private void CommonSetup(final FMLCommonSetupEvent event) {
        Network.init();
    }



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
                        sendBox(player, start.getBoundingBox());

                        for (StructurePiece piece : start.getPieces()) {
                            sendBox(player, piece.getBoundingBox());
                        }
                    }
                }
            }
        }
    }

    private void sendBox(ServerPlayer player, BoundingBox box) {
        if (!boxes.contains(box)) {
            System.out.println("Server - Adding & Sending new Box " + box);
            boxes.add(box);

            BlockPos one = new BlockPos(box.minX(), box.minY(), box.minZ());
            BlockPos two = new BlockPos(box.maxX(), box.maxY(), box.maxZ());

            Network.INSTANCE.sendTo(new StructureDebugPacket(one, two), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }
}
