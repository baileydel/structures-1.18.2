package com.delke.custom_villages.network;

import com.delke.custom_villages.client.ClientEvents;
import com.delke.custom_villages.client.render.RenderBuildablePiece;
import com.delke.custom_villages.structures.pieces.BuildablePiece;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author Bailey Delker
 * @created 06/20/2023 - 7:15 AM
 * @project structures-1.18.2
 */
public class GetStructurePacket {
    @Nullable
    private final CompoundTag tag;
    private final BoundingBox pieceBox;
    private final Rotation rotation;
    private final String name;

    public GetStructurePacket(String name, @Nullable CompoundTag tag, BoundingBox pieceBox, Rotation rotation) {
        System.out.println("Server - Sending new Structure - " + rotation);

        this.tag = tag;
        this.name = name;
        this.rotation = rotation;
        this.pieceBox = pieceBox;


        if (tag != null) {
            this.tag.putString("rotation", rotation.toString());
        }
    }

    public GetStructurePacket(FriendlyByteBuf buf) {
        tag = buf.readAnySizeNbt();
        name = buf.readUtf();
        rotation = getRotation();
        pieceBox = getBoundingBox(buf);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
        buf.writeUtf(name);
        writeBoundingBox(pieceBox, buf);
    }

    private Rotation getRotation() {
        String rot = "";
        if (tag != null) {
            rot = tag.getString("rotation");
        }

        return switch (rot) {
            case "CLOCKWISE_90" -> Rotation.CLOCKWISE_90;
            case "CLOCKWISE_180" -> Rotation.CLOCKWISE_180;
            case "COUNTERCLOCKWISE_90" -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    private void writeBoundingBox(BoundingBox box, FriendlyByteBuf buf) {
        BlockPos min = new BlockPos(box.minX(), box.minY(), box.minZ());
        BlockPos max = new BlockPos(box.maxX(), box.maxY(), box.maxZ());

        buf.writeBlockPos(min);
        buf.writeBlockPos(max);
    }

    private BoundingBox getBoundingBox(FriendlyByteBuf buf) {
        BlockPos min = buf.readBlockPos();
        BlockPos max = buf.readBlockPos();

        return BoundingBox.fromCorners(min, max);
    }

    /*
        Only handle data on client (one way packet)
     */
    public static void handle(GetStructurePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    RenderBuildablePiece piece = new RenderBuildablePiece(msg.name, msg.tag, msg.pieceBox, msg.rotation);

                    if (!ClientEvents.pieces.contains(piece)) {
                        ClientEvents.pieces.add(piece);
                        System.out.println("Client - \n");
                    }
                })
        );
        ctx.get().setPacketHandled(true);
    }

    public static void send(ServerPlayer player, StructureStart start) {
        GetStructurePacket packet = new GetStructurePacket("village", null, start.getBoundingBox(), Rotation.NONE);
        Network.INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);

        List<StructurePiece> pieces = start.getPieces();

        for (StructurePiece piece : pieces) {
            CompoundTag tag = null;
            String name = "";

            if (piece instanceof BuildablePiece buildablePiece) {
                name = buildablePiece.getName();
                tag = buildablePiece.getStructureData();
            }

            packet = new GetStructurePacket(name, tag, piece.getBoundingBox(), piece.getRotation());

            Network.INSTANCE.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }
}