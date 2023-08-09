package com.delke.custom_villages;

import com.delke.custom_villages.structures.VillageBuildablePiece;
import com.delke.custom_villages.structures.SkyStructures;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class STStructures {

    /**
     * We are using the Deferred Registry system to register our structure as this is the preferred way on Forge.
     * This will handle registering the base structure for us at the correct time so we don't have to handle it ourselves.
     *
     * HOWEVER, do note that Deferred Registries only work for anything that is a Forge Registry. This means that
     * configured structures and configured features need to be registered directly to BuiltinRegistries as there
     * is no Deferred Registry system for them.
     */
    public static final DeferredRegister<StructureFeature<?>> DEFERRED_REGISTRY_STRUCTURE = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, Main.MODID);

    /**
     * Registers the base structure itself and sets what its path is. In this case,
     * this base structure will have the resourcelocation of structure_tutorial:sky_structures.
     */
    public static final RegistryObject<StructureFeature<?>> SKY_STRUCTURES = DEFERRED_REGISTRY_STRUCTURE.register("sky_structures", SkyStructures::new);

    public static final DeferredRegister<StructurePieceType> REGISTER = DeferredRegister.create(Registry.STRUCTURE_PIECE_REGISTRY, Main.MODID);

    // As RecipeType is an interface, an anonymous class will be created for registering
    // Vanilla RecipeTypes override #toString for debugging purposes, so it is omitted in this example
    // Assume some recipe ExampleRecipe
    public static final RegistryObject<StructurePieceType> VILLAGE_BUILDABLE_PIECE = REGISTER.register("vbp", () -> VillageBuildablePiece::new);
}
