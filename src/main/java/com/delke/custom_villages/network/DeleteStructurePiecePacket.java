package com.delke.custom_villages.network;

import com.delke.custom_villages.structures.StructureHandler;
import com.delke.custom_villages.structures.pieces.BuildablePiece;
import com.delke.custom_villages.structures.pieces.placing.BuildablePiecePlacement;
import com.delke.custom_villages.structures.villagestructure.VillageStructureInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Bailey Delker
 * @created 09/01/2023 - 10:56 PM
 * @project structures-1.18.2
 */
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
                if (ctx.getSender() != null) {
                    VillageStructureInstance instance = StructureHandler.getInstance(new ChunkPos(0, 0));
                    ChunkPos pos = instance.getChunkPos();

                    PieceGeneratorSupplier.Context<JigsawConfiguration> s_context = (PieceGeneratorSupplier.Context<JigsawConfiguration>) instance.getContext();

                    ChunkAccess chunk = ctx.getSender().level.getChunk(pos.x, pos.z);

                    BuildablePiecePlacement.placer.removePiece();

                    List<BuildablePiece> t = BuildablePiecePlacement.placer.getPieces();

                    List<StructurePiece> n = new ArrayList<>(t);

                    System.out.println(t);

                    instance.savePieces(chunk, n);

                }
            }
        });
        ctx.setPacketHandled(true);
    }

    private static void print(List<StructurePiece> pieces) {
        for (StructurePiece piece : pieces) {
            System.out.println(piece);
        }
    }


}
