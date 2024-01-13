package com.delke.custom_villages.client;

import com.delke.custom_villages.client.render.RenderingUtil;
import com.delke.custom_villages.structures.StructureHandler;
import com.delke.custom_villages.structures.villagestructure.VillageStructureInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ClientBuildablePiece {
    public static ClientBuildablePiece CUR = null;
    private final BoundingBox box;

    //TODO Palette can never be empty, only line this for now because we have to send the structures overall bounding box
    @Nullable
    private final ModPalette palette;
    private final Rotation rotation;
    private final String name;

    /*
    For Gui Rendering.
    */
    private final Map<Block, Integer> placed = new HashMap<>();

    /*
    Original copy of all rotated blocks
     */
    private final HashSet<Pair<BlockPos, BlockState>> relativeToWorld = new HashSet<>();

    /*
    This is used to render relative to the world.
     */
    private HashSet<Pair<BlockPos, BlockState>> blocksToRender = new HashSet<>();

    public ClientBuildablePiece(String name, CompoundTag tag, BoundingBox box, Rotation rotation) {
        this.box = box;
        this.rotation = rotation;
        this.name = name;

        if (tag != null) {
            this.palette = new ModPalette(tag);
            rotate();

            this.blocksToRender = new HashSet<>(blocksToRender);
        }
        else {
            palette = null;
        }
    }

    public void renderGui(PoseStack stack) {
        CUR = this;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Font font = mc.font;

        int y = mc.getWindow().getGuiScaledHeight() / 3;
        int x = mc.getWindow().getGuiScaledWidth() - 30;

        if (player != null && this.palette != null) {
            font.draw(stack, name, x - 18, y - 8, 23721831);

            for (Map.Entry<Block, List<StructureTemplate.StructureBlockInfo>> entry : palette.getCache().entrySet()) {
                Block block = entry.getKey();
                int place = placed.getOrDefault(block, 0);

                int size = entry.getValue().size();
                int amount = size - place;

                font.draw(stack, amount + "", x - 18, y + 8, 23721831);

                ItemRenderer itemRenderer = mc.getItemRenderer();
                itemRenderer.renderAndDecorateFakeItem(new ItemStack(block.asItem()), x, y);

                y += 18;
            }
        }
    }

    public void renderWorld(PoseStack stack) {
        RenderingUtil.renderBoundingBox(stack, box);

        if (palette != null) {
            checkBlocks();

            for (Pair<BlockPos, BlockState> pair : blocksToRender) {
                RenderingUtil.renderBlock(stack, pair.getFirst(), pair.getSecond());
            }
        }
    }

    //TODO Move this to mod palette
    private void rotate() {
        assert palette != null;

        BlockPos worldPos = new BlockPos(box.minX(), box.minY(), box.minZ());

        for (Map.Entry<Block, List<StructureTemplate.StructureBlockInfo>> entry : palette.getCache().entrySet()) {
            for (StructureTemplate.StructureBlockInfo info : entry.getValue()) {
                BlockState state = info.state;
                BlockPos relativePos = info.pos;

                relativePos = switch (rotation) {
                    case CLOCKWISE_90 -> new BlockPos(-relativePos.getZ() + box.getXSpan() - 1 , relativePos.getY(), relativePos.getX());
                    case COUNTERCLOCKWISE_90 -> new BlockPos(relativePos.getZ(), relativePos.getY(), -relativePos.getX() + box.getZSpan() - 1);

                    case CLOCKWISE_180 -> new BlockPos(-relativePos.getX() + box.getXSpan() - 1, relativePos.getY(), -relativePos.getZ() + box.getZSpan() - 1);
                    default -> info.pos;
                };

                state = state.rotate(Minecraft.getInstance().level, relativePos, rotation);

                // Translate to world position
                BlockPos actualPos = worldPos.offset(relativePos);

                relativeToWorld.add(Pair.of(actualPos, state));
            }
        }
    }

    private void checkBlocks() {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        if (level != null && palette != null) {
            for (Pair<BlockPos, BlockState> pair : relativeToWorld) {
                BlockState worldBlock = level.getBlockState(pair.getFirst());
                BlockState shouldBe = pair.getSecond();

                boolean same = shouldBe.equals(worldBlock);

                if (same && blocksToRender.contains(pair)) {
                    int i;
                    if (placed.get(shouldBe.getBlock()) == null)  {
                        i = 0;
                    }
                    else {
                        i = placed.get(shouldBe.getBlock());
                    }

                    placed.put(shouldBe.getBlock(), i + 1);
                    blocksToRender.remove(pair);
                }
                else if (!same && !blocksToRender.contains(pair)) {
                    int i;

                    if (placed.get(shouldBe.getBlock()) == null)  {
                        i = 0;
                    }
                    else {
                        i = placed.get(shouldBe.getBlock());
                    }

                    blocksToRender.add(pair);

                    if (i > 0) {
                        placed.put(shouldBe.getBlock(), i - 1);
                    }
                }
            }
        }
    }

    public boolean isLookingAtBox() {
        if (palette == null) {
            return false;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        assert player != null;

        // Get player's view vector and expanded box boundaries
        Vec3 viewVector = player.getViewVector(1.0F).normalize();
        BoundingBox expandedBox = box.inflatedBy(2);

        // Define box corners directly from expanded box
        Vec3 boxMin =  new Vec3(expandedBox.minX(), expandedBox.minY(), expandedBox.minZ());
        Vec3 boxMax = new Vec3(expandedBox.maxX(), expandedBox.maxY(), expandedBox.maxZ());
        Vec3 playerPos = player.position(); // Use position() for player's coordinates

        // Iterate through box faces efficiently
        for (Vec3 normal : new Vec3[]{new Vec3(1, 0, 0), new Vec3(-1, 0, 0),
                new Vec3(0, 1, 0), new Vec3(0, -1, 0),
                new Vec3(0, 0, 1), new Vec3(0, 0, -1)}) {
            Vec3 pointOnFace = normal.dot(boxMax) > 0 ? boxMax : boxMin; // Choose point based on normal
            double t = (pointOnFace.subtract(playerPos)).dot(normal) / viewVector.dot(normal);

            if (t > 0) {
                Vec3 intersection = playerPos.add(viewVector.scale(t)); // Combine scaling and adding

                if (expandedBox.intersects(new BoundingBox(new BlockPos(intersection)))) { // Use contains() for concise intersection check
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isPlayerInside() {
        Player player = Minecraft.getInstance().player;
        return palette != null && player != null && player.getBoundingBox().intersects(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClientBuildablePiece piece) {
            return box.equals(piece.box);
        }
        return false;
    }

    public BoundingBox getBox() {
        return this.box;
    }


    public void move(BlockPos pos) {
        int x = box.getCenter().getX();
        int z = box.getCenter().getZ();

        int offsetX = pos.getX() - x;
        int offsetZ = pos.getZ() - z;

        VillageStructureInstance instance = StructureHandler.INSTANCES.get(new ChunkPos(0, 0));

        if (instance != null) {
            instance.movePiece("structuremod:tent_hunter", offsetX, 0, offsetZ);

            ClientEvents.pieces.clear();
            rotate();
        }


    }
}
