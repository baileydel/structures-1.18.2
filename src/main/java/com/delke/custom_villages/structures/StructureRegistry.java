package com.delke.custom_villages.structures;

import com.delke.custom_villages.Main;
import com.delke.custom_villages.structures.pieces.BuildablePiece;
import com.delke.custom_villages.structures.villagestructure.VillageStructure;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class StructureRegistry {
    public static final DeferredRegister<StructureFeature<?>> DEFERRED_REGISTRY_STRUCTURE = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, Main.MODID);

    public static final RegistryObject<StructureFeature<JigsawConfiguration>> SKY_STRUCTURES = DEFERRED_REGISTRY_STRUCTURE.register("road", VillageStructure::new);

    public static final DeferredRegister<StructurePieceType> REGISTER = DeferredRegister.create(Registry.STRUCTURE_PIECE_REGISTRY, Main.MODID);

    public static final RegistryObject<StructurePieceType> VILLAGE_BUILDABLE_PIECE = REGISTER.register("vbp", () -> BuildablePiece::new);

    public static void Register(IEventBus bus) {
        StructureRegistry.DEFERRED_REGISTRY_STRUCTURE.register(bus);
        StructureRegistry.REGISTER.register(bus);
    }
}
