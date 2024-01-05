package com.delke.custom_villages.network;

import com.delke.custom_villages.structures.StructureHandler;
import com.delke.custom_villages.structures.pieces.BuildablePiece;
import com.delke.custom_villages.structures.pieces.placing.BuildablePiecePlacement;
import com.delke.custom_villages.structures.villagestructure.VillageStructureInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class DeleteStructurePiecePacket {
    public DeleteStructurePiecePacket() {
        System.out.println("Testing Packet");
    }

    public DeleteStructurePiecePacket(FriendlyByteBuf buf) {}

    public void write(FriendlyByteBuf buf) {}

    /*
        Only handle on server
     */
    public static void handle(DeleteStructurePiecePacket msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null) {
                ChunkPos pos = new ChunkPos(0, 0);

                VillageStructureInstance instance = StructureHandler.getInstance(pos);
                PieceGeneratorSupplier.Context<?> s_context = instance.getContext();

                ChunkAccess chunk = ctx.getSender().level.getChunk(pos.x, pos.z);

                BuildablePiecePlacement.placer.removePiece();

                List<BuildablePiece> t = BuildablePiecePlacement.placer.getPieces();
                List<StructurePiece> n = new ArrayList<>(t);

                instance.savePieces(chunk, n);
            }
        });
        ctx.setPacketHandled(true);
    }

}
