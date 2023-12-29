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



/*
This mixin makes the pieceContainer in StructureStart.class Mutable
Allowing us to change pieces within.
 */
@Mixin(StructureStart.class)
public abstract class StructureStartMixin {

    @Shadow @Mutable @Final private PiecesContainer pieceContainer;


    //TODO What does this do?
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
        }
    }
}
