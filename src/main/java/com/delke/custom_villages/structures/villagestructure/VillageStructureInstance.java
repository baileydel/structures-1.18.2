package com.delke.custom_villages.structures.villagestructure;

import com.delke.custom_villages.mixin.StructureStartAccessor;
import com.delke.custom_villages.structures.pieces.BuildablePiecePlacement;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.List;
import java.util.Map;

/**
 * @author Bailey Delker
 * @created 11/08/2023 - 4:37 PM
 * @project structures-1.18.2
 * @description This class is a wrapper for StructureStart which holds information about a structure
 */
public class VillageStructureInstance {
    private final List<StructurePiece> pieces;
    private final StructureStart start;
    private final VillageEconomy economy;

    public VillageStructureInstance(StructureStart start) {
        this.start = start;
        this.economy = new VillageEconomy();
        this.pieces = start.getPieces();
    }

    public List<StructurePiece> getPieces() {
        return pieces;
    }

    private void addPiece(String piece) {

    }

    private void removePiece(String piece) {

    }

    private void getNextPiece() {

    }

    public void savePieces(ChunkAccess chunk) {
        ((StructureStartAccessor) (Object) start).setPieceContainer(new PiecesContainer(pieces));
        chunk.setAllStarts(Map.of(start.getFeature(), start));
    }

    public ChunkPos getChunkPos() {
        return start.getChunkPos();
    }

    public PieceGeneratorSupplier.Context<JigsawConfiguration> getContext() {
        return BuildablePiecePlacement.CONTEXT;
    }
}
