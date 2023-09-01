package com.delke.custom_villages.network;

import com.delke.custom_villages.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

/**
 * @author Bailey Delker
 * @created 06/20/2023 - 7:15 AM
 * @project structures-1.18.2
 */
public class Network {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Main.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        INSTANCE.registerMessage(0, StructureDebugPacket.class, StructureDebugPacket::write, StructureDebugPacket::new, StructureDebugPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(1, ForcePacket.class, ForcePacket::write, ForcePacket::new, ForcePacket::handle);
        INSTANCE.registerMessage(2, ClearPacket.class, ClearPacket::write, ClearPacket::new, ClearPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}
