package com.delke.custom_villages.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

@OnlyIn(Dist.CLIENT)
public class RenderingUtil {
    public static void renderBoundingBox(PoseStack stack, BoundingBox box) {
        PoseStack posestack = RenderSystem.getModelViewStack();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        posestack.pushPose();
        posestack.mulPoseMatrix(stack.last().pose());
        RenderSystem.applyModelViewMatrix();
        Vec3 vec3 = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        RenderSystem.disableTexture();
        RenderSystem.disableBlend();

        double minX = box.minX() - vec3.x();
        double minY = box.minY() - vec3.y();
        double minZ = box.minZ() - vec3.z();

        double maxX = box.maxX() + 1 - vec3.x();
        double maxY = box.maxY() + 1 - vec3.y();
        double maxZ = box.maxZ() + 1 - vec3.z();

        RenderSystem.lineWidth(2.0F);
        bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        renderVerticalLine(minX, minY, maxY, minZ);
        renderVerticalLine(minX, minY, maxY, maxZ, 0F, 1F, 0F, 1F);
        renderVerticalLine(maxX, minY, maxY, maxZ);
        renderVerticalLine(maxX, minY, maxY, minZ);

        renderHorizontalLine(minX, maxX, minY, minZ, minZ);
        renderHorizontalLine(minX, maxX, maxY, minZ, minZ);
        renderHorizontalLine(minX, minX, minY, minZ, maxZ, 0F, 0F, 1F, 1F);
        renderHorizontalLine(minX, minX, maxY, minZ, maxZ);

        renderHorizontalLine(minX, maxX, minY, maxZ, maxZ, 1F, 0F, 0F, 1F);
        renderHorizontalLine(minX, maxX, maxY, maxZ, maxZ);
        renderHorizontalLine(maxX, maxX, maxY, minZ, maxZ);
        renderHorizontalLine(maxX, maxX, minY, minZ, maxZ);

        tesselator.end();
        RenderSystem.lineWidth(2.0F);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private static void renderVerticalLine(double x, double minH, double maxH, double z) {
        renderVerticalLine(x, minH, maxH, z, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderVerticalLine(double x, double minH, double maxH, double z, float r, float g, float b, float a) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        bufferbuilder.vertex(x, minH, z).color(r, g, b, 0).endVertex();
        bufferbuilder.vertex(x, minH, z).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(x, maxH, z).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(x, maxH, z).color(r, g, b, 0).endVertex();
    }

    // Coloring for horizontal line
    private static void renderHorizontalLine(double minX, double maxX, double h, double minZ, double maxZ) {
        renderHorizontalLine(minX, maxX, h, minZ, maxZ, 1F, 1F, 1F, 0.5F);
    }

    private static void renderHorizontalLine(double minX, double maxX, double h, double minZ, double maxZ, float r, float g, float b, float a) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        bufferbuilder.vertex(minX, h, minZ).color(r, g, b, 0.0F).endVertex();
        bufferbuilder.vertex(minX, h, minZ).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(maxX, h, maxZ).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(maxX, h, maxZ).color(r, g, b, 0.0F).endVertex();
    }

    public static void renderBlock(PoseStack matrix, BlockPos pos, BlockState state) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null) {
            BlockRenderDispatcher renderer = Minecraft.getInstance().getBlockRenderer();
            Camera camera = mc.gameRenderer.getMainCamera();
            Vec3 vec3 = camera.getPosition();

            double d0 = vec3.x();
            double d1 = vec3.y();
            double d2 = vec3.z();

            matrix.pushPose();
            matrix.translate(
                    (double)pos.getX() - d0,
                    (double)pos.getY() - d1,
                    (double)pos.getZ() - d2
            );

            renderer.renderSingleBlock(
                    state,
                    matrix,
                    mc.renderBuffers().crumblingBufferSource(),
                    15728880,
                    OverlayTexture.WHITE_OVERLAY_V,
                    EmptyModelData.INSTANCE
            );
            matrix.popPose();
        }
    }

    public static void renderCustomSlot(ItemStack itemstack, int x, int y) {
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
}
