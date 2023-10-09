package com.delke.custom_villages.network;

import com.delke.custom_villages.VillageStructureStartWrapper;
import com.delke.custom_villages.mixin.StructureStartAccessor;
import com.delke.custom_villages.structures.pieces.BuildablePiece;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
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
                List<StructureStart> starts = VillageStructureStartWrapper.startMap.get(AddPieceStructurePacket.STATIC_START);

                if (starts != null) {
                    for (StructureStart start : starts) {
                        ChunkPos pos = start.getChunkPos();
                        ChunkAccess chunk = ctx.getSender().level.getChunk(pos.x, pos.z);

                        List<StructurePiece> newList = new ArrayList<>(start.getPieces());

                        if (VillageStructureStartWrapper.hasPiece(newList, "structure_tutorial:house_two")) {
                            newList = VillageStructureStartWrapper.removePiece(start, "structure_tutorial:house_two");
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

    private BuildablePiece createPiece() {
        if (AddPieceStructurePacket.STRUCTURE_FEATURE != null) {
            WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));

            worldgenrandom.setLargeFeatureSeed(AddPieceStructurePacket.SEED, AddPieceStructurePacket.STATIC_START.x, AddPieceStructurePacket.STATIC_START.z);
            JigsawConfiguration jigsawconfiguration = (JigsawConfiguration) AddPieceStructurePacket.STRUCTURE_FEATURE.config;

            StructureManager structuremanager = AddPieceStructurePacket.STRUCTURE_MANAGER;

            StructureFeature.bootstrap();
            Rotation rotation = Rotation.getRandom(worldgenrandom);
            StructureTemplatePool structuretemplatepool = jigsawconfiguration.startPool().value();
            StructurePoolElement structurepoolelement = structuretemplatepool.getRandomTemplate(worldgenrandom);

            BlockPos pos = new BlockPos(30, 173, 0);

            return new BuildablePiece(structuremanager, structurepoolelement, pos, structurepoolelement.getGroundLevelDelta(), rotation, structurepoolelement.getBoundingBox(structuremanager, pos, rotation));
        }
        return null;
    }
}