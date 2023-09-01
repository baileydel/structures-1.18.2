package com.delke.custom_villages;

import com.delke.custom_villages.structures.pieces.BuildablePiece;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

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

    public static boolean hasPiece(ChunkPos pos, ConfiguredStructureFeature<?, ?> feature, String location) {
        List<StructureStart> starts = startMap.get(pos);

        for (StructureStart start : starts) {
            if (start.getFeature().equals(feature)) {
                for (StructurePiece piece : start.getPieces()) {
                    if (piece instanceof BuildablePiece vbp) {
                        if (vbp.getElement().toString().split("\\[")[2].replace("]]", "").equals(location)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
