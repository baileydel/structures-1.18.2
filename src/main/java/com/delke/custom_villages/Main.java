package com.delke.custom_villages;

import com.delke.custom_villages.client.ClientEvents;
import com.delke.custom_villages.network.Network;
import com.delke.custom_villages.structures.StructureHandler;
import com.delke.custom_villages.structures.StructureRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "structuremod";

    public Main() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::CommonSetup);
        bus.addListener(this::ClientSetup);

        ItemRegistry.ITEMS.register(bus);
        StructureRegistry.Register(bus);

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
            ServerLevel level = (ServerLevel) event.world;
            ServerPlayer player = level.getRandomPlayer();

            if (player == null) {
                return;
            }

            StructureHandler.sendUnloadedChunks(level, player);
        }
    }
}
