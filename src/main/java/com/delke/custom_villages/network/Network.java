package com.delke.custom_villages.network;

import com.delke.custom_villages.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class Network {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Main.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        INSTANCE.registerMessage(0, GetStructurePacket.class, GetStructurePacket::write, GetStructurePacket::new, GetStructurePacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(1, AddPieceStructurePacket.class, AddPieceStructurePacket::write, AddPieceStructurePacket::new, AddPieceStructurePacket::handle);
        INSTANCE.registerMessage(2, GenerateStructurePacket.class, GenerateStructurePacket::write, GenerateStructurePacket::new, GenerateStructurePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(3, DeleteStructurePiecePacket.class, DeleteStructurePiecePacket::write, DeleteStructurePiecePacket::new, DeleteStructurePiecePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}
