package com.delke.custom_villages.structures.pieces.placing;

import com.delke.custom_villages.structures.pieces.BuildablePiece;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public interface PieceFactory {
    BuildablePiece create(StructureManager structureManager, StructurePoolElement poolElement, BlockPos pos, int p_210304_, Rotation rotation, BoundingBox boundingBox);
}