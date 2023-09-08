package com.delke.custom_villages.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Bailey Delker
 * @created 09/01/2023 - 2:20 PM
 * @project structures-1.18.2
 */
public class ClearPacket {

    public ClearPacket() {}

    public ClearPacket(FriendlyByteBuf buf) {}

    public void write(FriendlyByteBuf buf) {}

    /*
        Only handle on server
     */
    //TODO Refactor this
    public static void handle(ClearPacket msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null) {

                ServerLevel level = (ServerLevel) ctx.getSender().level;

                for (int x = 50; x > -50; x--) {
                    for (int y = -50; y > -60; y--) {
                        for (int z = 50; z > -50; z--) {
                            BlockPos pos = new BlockPos(x, y, z);
                            BlockState state = level.getBlockState(pos);

                            if (!state.isAir()) {
                                level.destroyBlock(pos, false, null);
                            }
                        }
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
