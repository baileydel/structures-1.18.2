package com.delke.custom_villages.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Bailey Delker
 * @created 06/20/2023 - 7:15 AM
 * @project structures-1.18.2
 */
public class AddPieceStructurePacket {


    public AddPieceStructurePacket() {
        System.out.println("Client - Trying to send ForcePacket");
    }

    public AddPieceStructurePacket(FriendlyByteBuf buf) {}

    public void write(FriendlyByteBuf buf) {}

    /*
        Only handle on server
     */
    //TODO Refactor this
    public static void handle(AddPieceStructurePacket msg, Supplier<NetworkEvent.Context> context) {

    }

}