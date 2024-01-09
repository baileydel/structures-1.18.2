package com.delke.custom_villages.structures.pieces.placing;

import com.delke.custom_villages.structures.pieces.BuildablePiece;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;

public class PieceState {
      final BuildablePiece piece;
      final MutableObject<VoxelShape> free;
      final int depth;

      PieceState(BuildablePiece p_210311_, MutableObject<VoxelShape> p_210312_, int p_210313_) {
         this.piece = p_210311_;
         this.free = p_210312_;
         this.depth = p_210313_;
      }
   }