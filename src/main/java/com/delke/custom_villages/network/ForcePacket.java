package com.delke.custom_villages.network;

import com.delke.custom_villages.Main;
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

import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.delke.custom_villages.Main.MODID;

public class ForcePacket {
    public static final ChunkPos STATIC_START = new ChunkPos(0, 0);
    private static boolean loaded = false;

    public ForcePacket() {}

    public ForcePacket(FriendlyByteBuf buf) {}

    public void write(FriendlyByteBuf buf) {}

    /*
        Only ever handle data on client (one way packet)
     */
    public static void handle(ForcePacket msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            System.out.println("server handling");

            if (ctx.getSender() != null) {
                ServerLevel level = ctx.getSender().getLevel();
                ConfiguredStructureFeature<?, ?> structureFeature = makeStructure();
                ChunkAccess chunkAccess = level.getChunk(STATIC_START.x, STATIC_START.z);

                if (structureFeature != null) {

                    Main.posList.remove(STATIC_START);

                    long seed = -234892394;
                    StructureManager structureManager = level.getStructureManager();
                    StructureFeatureManager featureManager = level.structureFeatureManager();
                    RegistryAccess registryAccess = level.registryAccess();

                    SectionPos sectionPos = SectionPos.of(STATIC_START, 0);
                    ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
                    StructureStart start = tryGenerateStructure(structureFeature, chunkGenerator, featureManager, registryAccess, structureManager, seed, chunkAccess, STATIC_START, sectionPos);

                    chunkAccess.setStartForFeature(structureFeature,start );
                    chunkAccess.addReferenceForFeature(structureFeature, ChunkPos.asLong(0, 0));

                    WorldgenRandom worldgenrandom = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.seedUniquifier()));

                    if (start != StructureStart.INVALID_START) {
                        BoundingBox box = getWritableArea(chunkAccess);

                        featureManager.startsForFeature(sectionPos, structureFeature).forEach((p_211647_) -> p_211647_.placeInChunk(level, featureManager, chunkGenerator, worldgenrandom, box, STATIC_START));
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

            Predicate<Holder<Biome>> predicate = (w) -> {
                System.out.println(w);
                return true;
            };

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