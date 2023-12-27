package com.delke.custom_villages.structures.pieces.placing;

import com.delke.custom_villages.structures.pieces.BuildablePiece;
import com.google.common.collect.Queues;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

import java.util.*;

import static com.delke.custom_villages.structures.pieces.placing.BuildablePiecePlacement.createAABB;

public class Placer {
   static final Logger LOGGER = LogUtils.getLogger();

   private final Registry<StructureTemplatePool> pools;
   private final int maxDepth;
   private final PieceFactory factory;
   private final ChunkGenerator chunkGenerator;
   private final StructureManager structureManager;
   private final List<BuildablePiece> pieces;
   private final Random random;
   final Deque<BuildablePiecePlacement.PieceState> placing = Queues.newArrayDeque();
   private final List<String> t;

   BuildablePiece origin;

   public Placer(Registry<StructureTemplatePool> p_210323_, int maxDepth, PieceFactory p_210325_, ChunkGenerator p_210326_, StructureManager p_210327_, List<BuildablePiece> list, Random p_210329_) {
      this.pools = p_210323_;
      this.maxDepth = maxDepth;
      this.factory = p_210325_;
      this.chunkGenerator = p_210326_;
      this.structureManager = p_210327_;
      this.pieces = list;
      this.random = p_210329_;
      this.origin = list.get(0);
      this.t = piecesToStringList();
   }

   public List<String> piecesToStringList() {
      List<String> c = new ArrayList<>();

      for (BuildablePiece piece : this.pieces) {
         c.add(piece.getElement().toString().split("Left\\[")[1].split("]]")[0]);
      }

      return c;
   }

   void tryPlacingChildren(BuildablePiece originPiece, MutableObject<VoxelShape> shape, int depth, boolean p_210337_, LevelHeightAccessor chunk) {
      StructurePoolElement originPool = originPiece.getElement();

      BlockPos blockpos = originPiece.getPosition();
      Rotation rotation = originPiece.getRotation();

      StructureTemplatePool.Projection projection = originPool.getProjection();
      boolean isRigid = projection == StructureTemplatePool.Projection.RIGID;
      BoundingBox boundingbox = originPiece.getBoundingBox();
      int i = boundingbox.minY();

      List<StructureTemplate.StructureBlockInfo> parentJigsawBlocks = originPool.getShuffledJigsawBlocks(this.structureManager, blockpos, rotation, this.random);
      for (StructureTemplate.StructureBlockInfo parentBlock : parentJigsawBlocks) {
         // Get a valid pool either target pool, or its fallback pool.
         StructureTemplatePool targetPool = getTargetPoolOrFallback(parentBlock);

         // Skip current jigsaw block if there is no valid target pool, or valid fallback pool..
         if (targetPool == null) {
            continue;
         }

         Direction direction = JigsawBlock.getFrontFacing(parentBlock.state);
         BlockPos blockpos1 = parentBlock.pos;
         BlockPos blockpos2 = blockpos1.relative(direction);

         int j = blockpos1.getY() - i;
         int k = -1;
         boolean flag1 = boundingbox.isInside(blockpos2);

         List<StructurePoolElement> childElements = targetPool.getShuffledTemplates(this.random);
         for (StructurePoolElement childElement : childElements) {
            if (childElement == EmptyPoolElement.INSTANCE) {
               continue;
            }

            for (Rotation rotation1 : Rotation.getShuffled(this.random)) {

               List<StructureTemplate.StructureBlockInfo> childBlocks = childElement.getShuffledJigsawBlocks(this.structureManager, BlockPos.ZERO, rotation1, this.random);
               for (StructureTemplate.StructureBlockInfo childBlock : childBlocks) {
                  String name = childBlock.nbt.getString("name");

                  // Don't add if we already have that in our pieces...
                  if (t.contains(name)) {
                     continue;
                  }

                  if (JigsawBlock.canAttach(parentBlock, childBlock)) {
                     BlockPos blockpos3 = childBlock.pos;
                     BlockPos blockpos4 = blockpos2.subtract(blockpos3);
                     BoundingBox boundingbox2 = childElement.getBoundingBox(this.structureManager, blockpos4, rotation1);

                     StructureTemplatePool.Projection childProj = childElement.getProjection();
                     boolean isChildRigid = childProj == StructureTemplatePool.Projection.RIGID;

                     int j1 = blockpos3.getY();
                     int k1 = j - j1 + JigsawBlock.getFrontFacing(parentBlock.state).getStepY();
                     int i1 = boundingbox2.minY();

                     int l1 = getl1(isRigid, isChildRigid, i1, k, k1, j1, blockpos1, chunk);
                     int i2 = l1 - i1;

                     BoundingBox boundingbox3 = boundingbox2.moved(0, i2, 0);

                     int l = idkyet(rotation1, childElement, childBlocks, false);

                     if (l > 0) {
                        int j2 = Math.max(l + 1, boundingbox3.maxY() - boundingbox3.minY());
                        boundingbox3.encapsulate(new BlockPos(boundingbox3.minX(), boundingbox3.minY() + j2, boundingbox3.minZ()));
                     }

                     BlockPos blockpos5 = blockpos4.offset(0, i2, 0);

                     // Add Piece
                     MutableObject<VoxelShape> shape1 = combineShape(shape, flag1, boundingbox);

                     if (!Shapes.joinIsNotEmpty(shape1.getValue(), Shapes.create(AABB.of(boundingbox3).deflate(0.25D)), BooleanOp.ONLY_SECOND)) {
                        shape1.setValue(Shapes.joinUnoptimized(shape1.getValue(), Shapes.create(AABB.of(boundingbox3)), BooleanOp.ONLY_FIRST));

                        int i3 = originPiece.getGroundLevelDelta();
                        int k2 = isChildRigid ? (i3 - k1) : (childElement.getGroundLevelDelta());

                        int l2 = getl2(isRigid, flag1, i, k, k1, l1, j, j1, blockpos1, chunk);

                        BuildablePiece placing = this.factory.create(this.structureManager, childElement, blockpos5, k2, rotation1, boundingbox3);

                        originPiece.addJunction(new JigsawJunction(blockpos2.getX(), l2 - j + i3, blockpos2.getZ(), k1, childProj));
                        placing.addJunction(new JigsawJunction(blockpos1.getX(), l2 - j1 + k2, blockpos1.getZ(), -k1, projection));

                        t.add(name);
                        this.pieces.add(placing);

                        AABB aabb = createAABB(placing.getBoundingBox(), 6);
                        //TODO LETS GO THIS PLACES ANOTHER CHILD
                        tryPlacingChildren(placing, new MutableObject<>(Shapes.join(Shapes.create(aabb), Shapes.create(AABB.of(boundingbox)), BooleanOp.ONLY_FIRST)), 7, false, chunk);
                     }
                  }
               }
            }
         }
      }
   }

   private StructureTemplatePool getTargetPoolOrFallback(StructureTemplate.StructureBlockInfo block) {
      Optional<StructureTemplatePool> targetPool = this.pools.getOptional(new ResourceLocation(block.nbt.getString("pool")));

      // Check if the target pool is present and contains items
      if (isValidPool(targetPool)) {
         return targetPool.orElse(null);
      }

      Optional<StructureTemplatePool> fallback = this.pools.getOptional(targetPool.get().getFallback());

      if (isValidPool(fallback)) {
         return fallback.orElse(null);
      }

      return null;
   }

   private MutableObject<VoxelShape> combineShape(MutableObject<VoxelShape> shape, boolean flag1, BoundingBox boundingbox) {
      if (flag1 && shape.getValue() == null) {
         shape.setValue(Shapes.create(AABB.of(boundingbox)));
      }
      return shape;
   }

   boolean isValidPool(Optional<StructureTemplatePool> input) {
      return input.filter(this::isValidPool).isPresent();
   }

   boolean isValidPool(StructureTemplatePool input) {
      // Check if the pool has anything
      if (input.size() != 0) {
         return true;
      }
      // If main pool doesn't have anything check fallback pool..
      else {
         ResourceLocation fallbackResource = input.getFallback();
         Optional<StructureTemplatePool> fallback = this.pools.getOptional(fallbackResource);

         return fallback.isPresent() && fallback.get().size() != 0;
      }
   }

   public int getl1(boolean flag, boolean flag2, int i, int k, int k1, int j1, BlockPos blockpos1, LevelHeightAccessor chunk) {
      int l1;

      if (flag && flag2) {
         l1 = i + k1;
      }
      else {
         if (k == -1) {
            k = this.chunkGenerator.getFirstFreeHeight(blockpos1.getX(), blockpos1.getZ(), Heightmap.Types.WORLD_SURFACE_WG, chunk);
         }
         l1 = k - j1;
      }
      return l1;
   }

   public int getl2(boolean flag, boolean flag2, int i, int k, int k1, int l1, int j, int j1, BlockPos blockpos1, LevelHeightAccessor chunk) {
      int l2;

      if (flag) {
         l2 = i + j;
      }
      else if (flag2) {
         l2 = l1 + j1;
      }
      else {
         if (k == -1) {
            k = this.chunkGenerator.getFirstFreeHeight(blockpos1.getX(), blockpos1.getZ(), Heightmap.Types.WORLD_SURFACE_WG, chunk);
         }
         l2 = k + k1 / 2;
      }

      return l2;
   }

   public int idkyet(Rotation rotation1, StructurePoolElement structurepoolelement1, List<StructureTemplate.StructureBlockInfo> list1, boolean p_210337_) {
      int l;
      BoundingBox boundingbox1 = structurepoolelement1.getBoundingBox(this.structureManager, BlockPos.ZERO, rotation1);

      if (p_210337_ && boundingbox1.getYSpan() <= 16) {
         l = list1.stream().mapToInt((p_210332_) -> {
            if (!boundingbox1.isInside(p_210332_.pos.relative(JigsawBlock.getFrontFacing(p_210332_.state)))) {
               return 0;
            }
            else {
               ResourceLocation resourcelocation2 = new ResourceLocation(p_210332_.nbt.getString("pool"));
               Optional<StructureTemplatePool> optional2 = this.pools.getOptional(resourcelocation2);

               Optional<StructureTemplatePool> optional3 = optional2.flatMap((p_210344_) -> this.pools.getOptional(p_210344_.getFallback()));
               int j3 = optional2.map((p_210342_) -> p_210342_.getMaxSize(this.structureManager)).orElse(0);
               int k3 = optional3.map((p_210340_) -> p_210340_.getMaxSize(this.structureManager)).orElse(0);

               return Math.max(j3, k3);
            }

         }).max().orElse(0);
      }
      else {
         l = 0;
      }
      return l;
   }

   public List<BuildablePiece> getPieces() {
      return pieces;
   }

   public void removePiece() {
      int size = this.pieces.size();

      if (size > 1) {
         BuildablePiece piece = this.pieces.get(size - 1);

         String k = piece.getElement().toString().split("]]")[0].split("Left\\[")[1];

         if (!Objects.equals(piece, origin)) {
            this.t.remove(k);
            this.pieces.remove(piece);
         }
      }
   }
}