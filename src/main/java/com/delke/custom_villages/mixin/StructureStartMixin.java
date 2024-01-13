package com.delke.custom_villages.mixin;

import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/*
This mixin makes the pieceContainer in StructureStart.class Mutable
Allowing us to change pieces within.
 */
@Mixin(StructureStart.class)
public abstract class StructureStartMixin {

    @Shadow @Mutable @Final private PiecesContainer pieceContainer;
}
