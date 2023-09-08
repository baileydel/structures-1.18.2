package com.delke.custom_villages.mixin;

import com.delke.custom_villages.structures.pieces.BuildablePiece;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Bailey Delker
 * @created 08/31/2023 - 11:19 AM
 * @project structures-1.18.2
 */
@Mixin(StructureStart.class)
public abstract class StructureStartMixin {

    @Shadow @Mutable @Final private PiecesContainer pieceContainer;

    /*
            TODO This is only on creation of a structure
             we want to make removing and adding pieces to a structure dynamic
             TODO Instead of removing them after generation, how about we see how they are built....
             and then add and remove from there

             But this is good for testing, to even see if this is how we should do it
         */
    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(ConfiguredStructureFeature<?, ?> p_210077_, ChunkPos p_210078_, int p_210079_, PiecesContainer p_210080_, CallbackInfo ci) {
        boolean has = false;

        for (StructurePiece piece : p_210080_.pieces()) {
            has = piece instanceof BuildablePiece;

            if (has) {
                break;
            }
        }

        if (has) {
            this.pieceContainer = new PiecesContainer(p_210080_.pieces());

            for (StructurePiece piece : pieceContainer.pieces()) {
                if (piece instanceof BuildablePiece vbp) {
                    if (vbp.getElement().toString().split("\\[")[2].replace("]]", "").equals("structure_tutorial:house_two")) {
                        break;
                    }
                }
            }

            //this.pieces.remove(piece1);

        }
    }

   

    public void setPieceContainer(PiecesContainer pieceContainer) {
        this.pieceContainer = pieceContainer;
    }
}
