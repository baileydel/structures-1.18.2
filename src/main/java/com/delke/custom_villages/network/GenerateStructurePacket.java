package com.delke.custom_villages.network;

import com.delke.custom_villages.structures.villagestructure.VillageStructure;
import net.minecraft.client.Minecraft;
import net.minecraft.core.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.delke.custom_villages.Main.MODID;

/**
 * @author Bailey Delker
 * @created 09/01/2023 - 2:20 PM
 * @project structures-1.18.2
 */
public class GenerateStructurePacket {
    /*
   TODO this is all static things for now
   should be random or inputted by the person sending the packet
    */
    public static final ChunkPos STATIC_START = new ChunkPos(0, 0);
    public static long SEED = RandomSupport.seedUniquifier();
    public static RegistryAccess REGISTRY_ACCESS;
    public static ConfiguredStructureFeature<?, ?> STRUCTURE_FEATURE;
    public static StructureManager STRUCTURE_MANAGER;
    public static ChunkGenerator CHUNK_GENERATOR;
    public static ChunkAccess CHUNK;


    public GenerateStructurePacket() {}

    public GenerateStructurePacket(FriendlyByteBuf buf) {}

    public void write(FriendlyByteBuf buf) {}

    /*
        Only handle on server
     */
    //TODO Refactor this
    public static void handle(GenerateStructurePacket msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null) {
                VillageStructure.INSTANCES.clear();

                ServerLevel level = ctx.getSender().getLevel();
                STRUCTURE_FEATURE = makeStructure();

                if (STRUCTURE_FEATURE != null) {
                    CHUNK = level.getChunk(STATIC_START.x, STATIC_START.z);
                    CHUNK.setAllStarts(Map.of());

                    STRUCTURE_MANAGER = level.getStructureManager();
                    StructureFeatureManager featureManager = level.structureFeatureManager();
                    REGISTRY_ACCESS = featureManager.registryAccess();

                    SectionPos sectionPos = SectionPos.of(STATIC_START, 0);
                    CHUNK_GENERATOR = level.getChunkSource().getGenerator();
                    StructureStart start = tryGenerateStructure(STRUCTURE_FEATURE, CHUNK_GENERATOR, featureManager, REGISTRY_ACCESS, STRUCTURE_MANAGER, SEED, CHUNK, STATIC_START, sectionPos);

                    CHUNK.setStartForFeature(STRUCTURE_FEATURE, start);
                    CHUNK.addReferenceForFeature(STRUCTURE_FEATURE, ChunkPos.asLong(0, 0));

                    WorldgenRandom worldgenrandom = new WorldgenRandom(new XoroshiroRandomSource(SEED));

                    if (start != StructureStart.INVALID_START) {
                        BoundingBox box = getWritableArea(CHUNK);

                        featureManager.startsForFeature(sectionPos, STRUCTURE_FEATURE).forEach(
                                (structureStart) -> structureStart.placeInChunk(level, featureManager, CHUNK_GENERATOR, worldgenrandom, box, STATIC_START)
                        );
                    }
                }
            }
        });
        ctx.setPacketHandled(true);

        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null) {

                ServerLevel level = (ServerLevel) ctx.getSender().level;

                for (int x = 50; x > -50; x--) {
                    for (int y = -50; y > -60; y--) {
                        for (int z = 50; z > -50; z--) {
                            BlockPos pos = new BlockPos(x, y, z);
                            BlockState state = level.getBlockState(pos);

                            if (!state.isAir()) {
                                level.destroyBlock(pos, false, null);
                            }
                        }
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }


    private static BoundingBox getWritableArea(ChunkAccess p_187718_) {
        ChunkPos chunkpos = p_187718_.getPos();
        int i = chunkpos.getMinBlockX();
        int j = chunkpos.getMinBlockZ();
        LevelHeightAccessor levelheightaccessor = p_187718_.getHeightAccessorForGeneration();
        int k = levelheightaccessor.getMinBuildHeight() + 1;
        int l = levelheightaccessor.getMaxBuildHeight() - 1;
        return new BoundingBox(i, k, j, i + 15, l, j + 15);
    }

    private static ConfiguredStructureFeature<?, ?> makeStructure() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null && mc.getSingleplayerServer() != null) {
            ServerLevel level = mc.getSingleplayerServer().getLevel(mc.level.dimension());

            if (level != null) {
                ResourceKey<ConfiguredStructureFeature<?, ?>> th = ResourceKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, new ResourceLocation(MODID, "code_structure_sky_fan"));

                return level.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY).getOrThrow(th);
            }
        }
        return null;
    }

    private static StructureStart tryGenerateStructure(ConfiguredStructureFeature<?, ?> configuredstructurefeature, ChunkGenerator generator, StructureFeatureManager featureManager, RegistryAccess access, StructureManager p_208020_, long p_208021_, ChunkAccess chunkAccess, ChunkPos chunkPos, SectionPos sectionPos) {
        try {
            int i = fetchReferences(featureManager, chunkAccess, sectionPos, configuredstructurefeature);

            Predicate<Holder<Biome>> predicate = (w) -> true;

            //TODO HMMm
            StructureStart structurestart =  configuredstructurefeature.generate(access, generator, generator.getBiomeSource(), p_208020_, p_208021_, chunkPos, i, chunkAccess, predicate);


            if (structurestart.isValid()) {
                featureManager.setStartForFeature(sectionPos, configuredstructurefeature, structurestart, chunkAccess);
                return structurestart;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return StructureStart.INVALID_START;
    }

    private static int fetchReferences(StructureFeatureManager p_207977_, ChunkAccess p_207978_, SectionPos p_207979_, ConfiguredStructureFeature<?, ?> p_207980_) {
        StructureStart structurestart = p_207977_.getStartForFeature(p_207979_, p_207980_, p_207978_);
        return structurestart != null ? structurestart.getReferences() : 0;
    }
}
