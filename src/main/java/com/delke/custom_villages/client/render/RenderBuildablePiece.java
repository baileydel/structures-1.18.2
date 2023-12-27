package com.delke.custom_villages.client.render;

import com.delke.custom_villages.client.ModPalette;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

/**
 * @author Bailey Delker
 * @created 08/20/2023 - 1:55 PM
 * @project structures-1.18.2
 */
@OnlyIn(Dist.CLIENT)
public class RenderBuildablePiece {
    private final BoundingBox box;

    //TODO Palette can never be empty, only line this for now because we have to send the structures overall bounding box
    @Nullable
    private final ModPalette palette;
    private final Rotation rotation;

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
    private final HashSet<Pair<BlockPos, BlockState>> blocksToRender = new HashSet<>();

    public RenderBuildablePiece(CompoundTag tag, BoundingBox box, Rotation rotation) {
        this.box = box;
        this.rotation = rotation;

        if (tag != null) {
            this.palette = new ModPalette(tag);
            rotate();
        }
        else {
            this.palette = null;
        }
    }

    public void renderGui(PoseStack stack) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Font font = mc.font;

        if (player != null && this.palette != null) {
            int y = mc.getWindow().getGuiScaledHeight() / 3;
            for (Map.Entry<Block, List<StructureTemplate.StructureBlockInfo>> entry : palette.getCache().entrySet()) {
                int size = entry.getValue().size() - placed.get(entry.getKey());

                int x = mc.getWindow().getGuiScaledWidth() - 30;
                font.drawShadow(stack, size + "", x - 18, y + 3, 23721831);

                RenderingUtil.renderCustomSlot(new ItemStack(entry.getKey().asItem()), x, y);

                y += 18;
            }
        }
    }

    public boolean isPlayerInside() {
        Player player = Minecraft.getInstance().player;
        return palette != null && player != null && player.getBoundingBox().intersects(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
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


    //TODO Remove this, it's only in rendering
    private void rotate() {
        assert palette != null;

        for (Map.Entry<Block, List<StructureTemplate.StructureBlockInfo>> entry : palette.getCache().entrySet()) {
            for (StructureTemplate.StructureBlockInfo info : entry.getValue()) {
                BlockState state = info.state;
                BlockPos minPos = new BlockPos(box.minX(), box.minY(), box.minZ());
                BlockPos relativePos = info.pos;

                relativePos = switch (rotation) {
                    case COUNTERCLOCKWISE_90 ->
                            new BlockPos(relativePos.getZ(), relativePos.getY(), relativePos.getX());
                    case CLOCKWISE_180 ->
                            new BlockPos(relativePos.getX(), relativePos.getY(), -relativePos.getZ() + box.getZSpan() - 1);
                    case CLOCKWISE_90 ->
                            new BlockPos(-relativePos.getZ() + box.getXSpan() - 1, relativePos.getY(), relativePos.getX());
                    default -> info.pos;
                };

                state = state.rotate(Minecraft.getInstance().level, relativePos, rotation);

                // Translate to world position
                BlockPos actualPos = minPos.offset(relativePos);

                relativeToWorld.add(Pair.of(actualPos, state));
            }
        }
    }

    private void checkBlocks() {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        if (level != null && palette != null) {
            for (Pair<BlockPos, BlockState> pair : relativeToWorld) {
                BlockState state = level.getBlockState(pair.getFirst());

                boolean same = pair.getSecond().equals(state);
                Block block = pair.getSecond().getBlock();

                if (same && blocksToRender.contains(pair)) {
                    placed.compute(block, (b, i) -> {
                        if (i != null) {
                            return i + 1;
                        }
                        return 0;
                    });

                    blocksToRender.remove(pair);
                }
                else if (!same && !blocksToRender.contains(pair)) {
                    blocksToRender.add(pair);

                    placed.compute(block, (b, i) -> {
                        if (i != null) {
                            return i - 1;
                        }
                        return 0;
                    });
                }
            }
        }
    }

    public boolean isLookingAtBox() {
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RenderBuildablePiece piece) {
            return box.equals(piece.box);
        }
        return false;
    }

    public BoundingBox getBox() {
        return this.box;
    }
}
