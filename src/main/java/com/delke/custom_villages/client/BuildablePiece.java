package com.delke.custom_villages.client;

import com.delke.custom_villages.client.render.RenderingUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bailey Delker
 * @created 08/20/2023 - 1:55 PM
 * @project structures-1.18.2
 */
@OnlyIn(Dist.CLIENT)
public class BuildablePiece {
    private final BoundingBox box;

    @Nullable
    private final ModPalette palette;

    private final Rotation rotation;

    /*
        STOP-SHIP
        When a block is changed inside a bounding box, we should update it.
        Either use event bus's and make remaining compatible with multiple pieces / structures at the same time
        or constantly check in rendering

        either way constantly checking in rendering will always be slower
     */
    private final Map<Block, List<StructureTemplate.StructureBlockInfo>> remaining = new HashMap<>();

    public BuildablePiece(CompoundTag tag, BoundingBox box, Rotation rotation) {
        this.box = box;
        this.rotation = rotation;

        if (tag != null) {
            this.palette = new ModPalette(tag);
        }
        else {
            this.palette = null;
        }
    }

    public void renderGui(PoseStack stack) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && box.getCenter().closerThan(player.getOnPos(), mc.options.renderDistance * 16)) {
            Font font = mc.font;
            Vec3 vec3 = player.getViewVector(1.0F).normalize();

            BoundingBox expanded = box.inflatedBy(3);

            // Points representing corners of the 3D box
            Vec3 boxMin = new Vec3(expanded.minX(), expanded.minY(), expanded.minZ());
            Vec3 boxMax = new Vec3(expanded.maxX(), expanded.maxY(), expanded.maxZ());
            Vec3 playerPos = new Vec3(player.getX(), player.getEyeY(), player.getZ());

            boolean isLookingAtBox = false;

            for (int face = 0; face < 6; face++) {
                Vec3 normal;
                Vec3 pointOnFace;

                switch (face) {
                    case 0: // +X face
                        normal = new Vec3(1, 0, 0);
                        pointOnFace = boxMax;
                        break;
                    case 1: // -X face
                        normal = new Vec3(-1, 0, 0);
                        pointOnFace = boxMin;
                        break;
                    case 2: // +Y face
                        normal = new Vec3(0, 1, 0);
                        pointOnFace = boxMax;
                        break;
                    case 3: // -Y face
                        normal = new Vec3(0, -1, 0);
                        pointOnFace = boxMin;
                        break;
                    case 4: // +Z face
                        normal = new Vec3(0, 0, 1);
                        pointOnFace = boxMax;
                        break;
                    case 5: // -Z face
                    default:
                        normal = new Vec3(0, 0, -1);
                        pointOnFace = boxMin;
                        break;
                }

                double t = (pointOnFace.subtract(playerPos)).dot(normal) / vec3.dot(normal);

                if (t > 0) {
                    Vec3 scaledVec3 = new Vec3(vec3.x * t, vec3.y * t, vec3.z * t);
                    Vec3 intersection = playerPos.add(scaledVec3);

                    if (intersection.x >= boxMin.x && intersection.x <= boxMax.x &&
                            intersection.y >= boxMin.y && intersection.y <= boxMax.y &&
                            intersection.z >= boxMin.z && intersection.z <= boxMax.z) {
                        isLookingAtBox = true;
                        break;
                    }
                }
            }

            if (player.getBoundingBox().intersects(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()) || isLookingAtBox) {
                int y = mc.getWindow().getGuiScaledHeight() / 3;

                for (Map.Entry<Block, List<StructureTemplate.StructureBlockInfo>> entry : remaining.entrySet()) {
                    int x = mc.getWindow().getGuiScaledWidth() - 30;
                    font.drawShadow(stack, entry.getValue().size() + "", x - 18, y + 3, 23721831);
                    renderCustomSlot(new ItemStack(entry.getKey().asItem()), x, y);
                    y += 18;
                }
            }
        }
    }

    public void renderWorld(PoseStack stack) {
        RenderingUtil.renderBoundingBox(stack, box);

        if (palette != null) {
            checkBlocks();

            for (List<StructureTemplate.StructureBlockInfo> infos : remaining.values()) {
                for (StructureTemplate.StructureBlockInfo info : infos) {
                    BlockState state = info.state;
                    BlockPos minPos = new BlockPos(box.minX(), box.minY(), box.minZ());

                    BlockPos relativePos = info.pos;

                    switch (rotation) {
                        case COUNTERCLOCKWISE_90:
                            relativePos = new BlockPos(relativePos.getZ(), relativePos.getY(), relativePos.getX());
                            break;
                        case CLOCKWISE_180:
                            relativePos = new BlockPos(relativePos.getZ(), relativePos.getY(), -relativePos.getX());
                            break;
                        case CLOCKWISE_90:
                            relativePos = new BlockPos(-relativePos.getX(), relativePos.getY(), -relativePos.getZ());
                            break;

                    }

                    // Translate to world position
                    BlockPos actualPos = minPos.offset(relativePos);

                    if (!state.isAir()) {
                        RenderingUtil.renderBlock(stack, state, actualPos);
                    }
                }
            }
        }
    }

    private void renderCustomSlot(ItemStack itemstack, int x, int y) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null) {
            ItemRenderer itemRenderer = mc.getItemRenderer();
            Font font = mc.font;

            int imageWidth = 176;
            itemRenderer.blitOffset = 100.0F;

            //Render item icon
            RenderSystem.enableDepthTest();
            itemRenderer.renderAndDecorateItem(mc.player, itemstack, x, y, x + y * imageWidth);

            //Render item string count
            PoseStack posestack = new PoseStack();
            if (itemstack.getCount() != 1) {
                String s = String.valueOf(itemstack.getCount());
                posestack.translate(0.0D, 0.0D, itemRenderer.blitOffset + 200.0F);
                MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

                float stringX = (x + 19f - 2 - font.width(s));
                float stringY = (y + 6f + 3);
                font.drawInBatch(s,stringX,stringY,16777215, true, posestack.last().pose(), multibuffersource$buffersource, false, 0, 15728880);

                multibuffersource$buffersource.endBatch();
            }
            itemRenderer.blitOffset = 0.0F;
        }
    }

    private void checkBlocks() {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        if (level != null && palette != null) {
            remaining.clear();
            remaining.putAll(palette.getCache());

            List<Pair<BlockPos, BlockState>> blocks = getAllPlacedBlocks();
            for (Pair<BlockPos, BlockState> pair : blocks) {
                List<StructureTemplate.StructureBlockInfo> t = remaining.get(pair.getSecond().getBlock());

                if (t != null) {
                    List<StructureTemplate.StructureBlockInfo> list = new ArrayList<>(t);
                    List<StructureTemplate.StructureBlockInfo> remove = new ArrayList<>();

                    for (StructureTemplate.StructureBlockInfo info : list) {
                        BlockPos minPos = new BlockPos(box.minX(), box.minY(), box.minZ());
                        BlockPos pos = minPos.offset(info.pos);

                        if (pos.equals(pair.getFirst())) {
                            remove.add(info);
                        }
                    }

                    if (remove.size() > 0) {
                        list.removeAll(remove);
                        remaining.put(pair.getSecond().getBlock(), list);
                    }
                }
            }
        }
    }

    private List<Pair<BlockPos, BlockState>> getAllPlacedBlocks() {
        List<Pair<BlockPos, BlockState>> states = new ArrayList<>();
        Level level = Minecraft.getInstance().level;

        if (level != null) {
            for (int x = box.minX(); x <= box.maxX(); x++) {
                for (int y = box.minY(); y <= box.maxY(); y++) {
                    for (int z = box.minZ(); z <= box.maxZ(); z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = level.getBlockState(pos);

                        if (!state.isAir()) {
                            states.add(Pair.of(pos, state));
                        }
                    }
                }
            }
        }
        return states;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BuildablePiece piece) {
            return box.equals(piece.box);
        }
        return false;
    }

    public BoundingBox getBox() {
        return this.box;
    }
}
