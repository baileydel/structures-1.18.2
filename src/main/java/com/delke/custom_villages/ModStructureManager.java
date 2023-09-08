package com.delke.custom_villages;

import com.delke.custom_villages.structures.pieces.BuildablePiece;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bailey Delker
 * @created 08/30/2023 - 2:36 PM
 * @project structures-1.18.2
 */
public class ModStructureManager {

    public static Map<ChunkPos, List<StructureStart>> startMap = new HashMap<>();


    @Nullable
    public static StructurePiece getPiece(List<StructurePiece> pieces, String location) {
        for (StructurePiece piece : pieces) {
            if (piece instanceof BuildablePiece vbp) {
                if (vbp.getElement().toString().split("\\[")[2].replace("]]", "").equals(location)) {
                    return piece;
                }
            }
        }
        return null;
    }

    public static boolean hasPiece(List<StructurePiece> pieces, String location) {
        return getPiece(pieces, location) != null;
    }

    public static boolean hasPiece(ChunkPos pos, ConfiguredStructureFeature<?, ?> feature, String location) {
        List<StructureStart> starts = startMap.get(pos);

        for (StructureStart start : starts) {
            if (start.getFeature().equals(feature)) {

                if (hasPiece(start.getPieces(), location)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<StructurePiece> removePiece(StructureStart start, String location) {
        List<StructurePiece> out = new ArrayList<>(start.getPieces());

        StructurePiece piece = getPiece(out, location);

        out.remove(piece);
        return out;
    }
}
