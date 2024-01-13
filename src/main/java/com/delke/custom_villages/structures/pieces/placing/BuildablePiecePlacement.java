package com.delke.custom_villages.structures.pieces.placing;

import com.delke.custom_villages.structures.pieces.BuildablePiece;
import com.google.common.collect.Lists;
import net.minecraft.core.*;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class BuildablePiecePlacement {
   public static Placer placer;

   public static List<BuildablePiece> getPieces(PieceGeneratorSupplier.Context<?> context, BlockPos pos, boolean p_210288_, boolean p_210289_) {
      PieceFactory factory = BuildablePiece::new;

      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      JigsawConfiguration jigsawconfiguration = (JigsawConfiguration) context.config();

      // Don't continue if max depth is 0
      if (jigsawconfiguration.maxDepth() == 0) {
         return null;
      }

      StructureTemplatePool structuretemplatepool = jigsawconfiguration.startPool().value();
      StructurePoolElement structurepoolelement = structuretemplatepool.getRandomTemplate(worldgenrandom);

      if (structurepoolelement == EmptyPoolElement.INSTANCE) {
         return List.of();
      }

      worldgenrandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
      RegistryAccess registryaccess = context.registryAccess();

      ChunkGenerator chunkgenerator = context.chunkGenerator();
      StructureManager structuremanager = context.structureManager();
      LevelHeightAccessor chunk = context.heightAccessor();
      Predicate<Holder<Biome>> predicate = context.validBiome();
      StructureFeature.bootstrap();
      Registry<StructureTemplatePool> registry = registryaccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
      Rotation rotation = Rotation.getRandom(worldgenrandom);

      BuildablePiece initPiece = factory.create(structuremanager, structurepoolelement, pos, structurepoolelement.getGroundLevelDelta(), rotation, structurepoolelement.getBoundingBox(structuremanager, pos, rotation));

      BoundingBox boundingbox = initPiece.getBoundingBox();
      int i = (boundingbox.maxX() + boundingbox.minX()) / 2;
      int j = (boundingbox.maxZ() + boundingbox.minZ()) / 2;
      int k;

      if (p_210289_) {
         k = pos.getY() + chunkgenerator.getFirstFreeHeight(i, j, Heightmap.Types.WORLD_SURFACE_WG, chunk);
      }
      else {
         k = pos.getY();
      }

      if (!predicate.test(chunkgenerator.getNoiseBiome(QuartPos.fromBlock(i), QuartPos.fromBlock(k), QuartPos.fromBlock(j)))) {
         return List.of();
      }

      int l = boundingbox.minY() + initPiece.getGroundLevelDelta();
      initPiece.move(0, k - l, 0);

      List<BuildablePiece> list = Lists.newArrayList(initPiece);

      // Only place if max depth is higher than 0
      if (jigsawconfiguration.maxDepth() > 0) {
         AABB aabb = new AABB(i - 80, k - 80, j - 80, i + 80 + 1, k + 80 + 1, j + 80 + 1);

         if (placer == null) {
            placer = new Placer(registry, 9, factory, chunkgenerator, structuremanager, list, worldgenrandom);
         }

         placer.placing.addLast(new PieceState(initPiece, new MutableObject<>(Shapes.join(Shapes.create(aabb), Shapes.create(AABB.of(boundingbox)), BooleanOp.ONLY_FIRST)), 0));

         while (!placer.placing.isEmpty()) {
            PieceState piecestate = placer.placing.removeFirst();

            placer.tryPlacingChildren(piecestate.piece, piecestate.free, piecestate.depth, p_210288_, chunk);
         }
      }
      return placer.getPieces();
   }

   public static Optional<PieceGenerator<JigsawConfiguration>> addPieces(PieceGeneratorSupplier.Context<JigsawConfiguration> context, PieceFactory factory, BlockPos pos, boolean p_210288_, boolean p_210289_) {
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      JigsawConfiguration jigsawconfiguration = context.config();
      StructureTemplatePool structuretemplatepool = jigsawconfiguration.startPool().value();
      StructurePoolElement structurepoolelement = structuretemplatepool.getRandomTemplate(worldgenrandom);

      if (structurepoolelement == EmptyPoolElement.INSTANCE) {
         return Optional.empty();
      }

      worldgenrandom.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
      RegistryAccess registryaccess = context.registryAccess();

      ChunkGenerator chunkgenerator = context.chunkGenerator();
      StructureManager structuremanager = context.structureManager();
      LevelHeightAccessor levelheightaccessor = context.heightAccessor();
      Predicate<Holder<Biome>> predicate = context.validBiome();
      StructureFeature.bootstrap();
      Registry<StructureTemplatePool> registry = registryaccess.registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
      Rotation rotation = Rotation.getRandom(worldgenrandom);
      BuildablePiece buildablePiece = factory.create(structuremanager, structurepoolelement, pos, structurepoolelement.getGroundLevelDelta(), rotation, structurepoolelement.getBoundingBox(structuremanager, pos, rotation));
      BoundingBox boundingbox = buildablePiece.getBoundingBox();
      int i = (boundingbox.maxX() + boundingbox.minX()) / 2;
      int j = (boundingbox.maxZ() + boundingbox.minZ()) / 2;
      int k;

      if (p_210289_) {
         k = pos.getY() + chunkgenerator.getFirstFreeHeight(i, j, Heightmap.Types.WORLD_SURFACE_WG, levelheightaccessor);
      }
      else {
         k = pos.getY();
      }

      if (!predicate.test(chunkgenerator.getNoiseBiome(QuartPos.fromBlock(i), QuartPos.fromBlock(k), QuartPos.fromBlock(j)))) {
         return Optional.empty();
      }

      int l = boundingbox.minY() + buildablePiece.getGroundLevelDelta();
      buildablePiece.move(0, k - l, 0);

      List<BuildablePiece> list = Lists.newArrayList(buildablePiece);

      if (jigsawconfiguration.maxDepth() > 0) {
         AABB aabb = createAABB(boundingbox, k);
         Placer jigsawplacement$placer = new Placer(registry, 5, factory, chunkgenerator, structuremanager, list, worldgenrandom);
         jigsawplacement$placer.placing.addLast(new PieceState(buildablePiece, new MutableObject<>(Shapes.join(Shapes.create(aabb), Shapes.create(AABB.of(boundingbox)), BooleanOp.ONLY_FIRST)), 0));

         while (!jigsawplacement$placer.placing.isEmpty()) {
            PieceState jigsawplacement$piecestate = jigsawplacement$placer.placing.removeFirst();
            //TODO This adds the next piece
            jigsawplacement$placer.tryPlacingChildren(jigsawplacement$piecestate.piece, jigsawplacement$piecestate.free, jigsawplacement$piecestate.depth, p_210288_, levelheightaccessor);
         }
      }

      return Optional.of((builder, context2) -> list.forEach(builder::addPiece));
   }

   public static AABB createAABB(BoundingBox boundingbox, int k) {
      int i = (boundingbox.maxX() + boundingbox.minX()) / 2;
      int j = (boundingbox.maxZ() + boundingbox.minZ()) / 2;

      return new AABB(i - 80, k - 80, j - 80, i + 80 + 1, k + 80 + 1, j + 80 + 1);
   }
}