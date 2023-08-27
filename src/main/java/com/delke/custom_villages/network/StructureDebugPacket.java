package com.delke.custom_villages.network;

import com.delke.custom_villages.client.BuildablePiece;
import com.delke.custom_villages.client.ClientEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * @author Bailey Delker
 * @created 06/20/2023 - 7:15 AM
 * @project structures-1.18.2
 */
public class StructureDebugPacket {
    @Nullable
    private final CompoundTag tag;
    private final BlockPos min;
    private final BlockPos max;
    private final Rotation rotation;

    public StructureDebugPacket(@Nullable CompoundTag tag, BlockPos one, BlockPos two, Rotation rotation) {
        this.tag = tag;
        this.min = one;
        this.max = two;
        this.rotation = rotation;

        if (tag != null) {
            this.tag.putString("rotation", rotation.toString());
        }
    }

    public StructureDebugPacket(FriendlyByteBuf buf) {
        tag = buf.readAnySizeNbt();
        min = buf.readBlockPos();
        max = buf.readBlockPos();
        rotation = getRotation(tag);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
        buf.writeBlockPos(min);
        buf.writeBlockPos(max);
    }

    private Rotation getRotation(CompoundTag tag) {
        String r = "";
        if (tag != null) {
            r = tag.getString("rotation");
        }

        return switch (r) {
            case "CLOCKWISE_90" -> Rotation.CLOCKWISE_90;
            case "CLOCKWISE_180" -> Rotation.CLOCKWISE_180;
            case "COUNTERCLOCKWISE_90" -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    /*
        Only handle data on client (one way packet)
     */
    public static void handle(StructureDebugPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    BuildablePiece piece = new BuildablePiece(msg.tag, BoundingBox.fromCorners(msg.min, msg.max), msg.rotation);

                    if (!ClientEvents.pieces.contains(piece)) {
                        ClientEvents.pieces.add(piece);
                        System.out.println("Client - Adding new Box ");
                    }
                })
        );
        ctx.get().setPacketHandled(true);
    }
}