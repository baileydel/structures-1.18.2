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

    private BuildablePiece createPiece() {
        if (ResetStructurePacket.STRUCTURE_FEATURE != null) {
            WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));

            worldgenrandom.setLargeFeatureSeed(ResetStructurePacket.SEED, ResetStructurePacket.STATIC_START.x, ResetStructurePacket.STATIC_START.z);
            JigsawConfiguration jigsawconfiguration = (JigsawConfiguration) ResetStructurePacket.STRUCTURE_FEATURE.config;

            StructureManager structuremanager = ResetStructurePacket.STRUCTURE_MANAGER;

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