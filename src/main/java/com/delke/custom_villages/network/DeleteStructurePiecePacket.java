package com.delke.custom_villages.network;

import com.delke.custom_villages.VillageStructureStartWrapper;
import com.delke.custom_villages.mixin.StructureStartAccessor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                List<StructureStart> starts = VillageStructureStartWrapper.startMap.get(ResetStructurePacket.STATIC_START);

                if (starts != null) {
                    for (StructureStart start : starts) {
                        ChunkPos pos = start.getChunkPos();
                        ChunkAccess chunk = ctx.getSender().level.getChunk(pos.x, pos.z);

                        List<StructurePiece> newList = new ArrayList<>(start.getPieces());

                        if (newList.size() > 0) {
                            newList.remove(newList.size() - 1);
                        }

                        print(newList);

                        ((StructureStartAccessor) (Object) start).setPieceContainer(new PiecesContainer(newList));
                        chunk.setAllStarts(Map.of(start.getFeature(), start));
                    }
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
