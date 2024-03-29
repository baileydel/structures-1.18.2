package com.delke.custom_villages.network;

import com.delke.custom_villages.structures.StructureHandler;
import com.delke.custom_villages.structures.pieces.BuildablePiece;
import com.delke.custom_villages.structures.pieces.placing.BuildablePiecePlacement;
import com.delke.custom_villages.structures.villagestructure.VillageStructureInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class AddPieceStructurePacket {
    public AddPieceStructurePacket() {
        System.out.println("[Client] - Adding Piece");
    }

    public AddPieceStructurePacket(FriendlyByteBuf buf) {}

    public void write(FriendlyByteBuf buf) {}

    /*
        Only handle on server
     */
    //TODO Refactor this
    public static void handle(AddPieceStructurePacket msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();

        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null) {
                VillageStructureInstance instance = StructureHandler.getInstance(new ChunkPos(0, 0));
                ChunkPos pos = instance.getChunkPos();

                PieceGeneratorSupplier.Context<?> s_context = instance.getContext();

                System.out.println(s_context.config());

                List<BuildablePiece> t = BuildablePiecePlacement.getPieces(s_context, new BlockPos(8, 5, 8), false, true);

                assert t != null;

                List<StructurePiece> n = new ArrayList<>(t);

                System.out.println(t);

                instance.savePieces(n);
            }
        });

        ctx.setPacketHandled(true);
    }
}