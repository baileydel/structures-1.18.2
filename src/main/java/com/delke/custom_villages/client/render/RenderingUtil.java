package com.delke.custom_villages.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.EmptyModelData;

/**
 * @author Bailey Delker
 * @created 07/30/2023 - 12:18 PM
 * @project structures-1.18.2
 */
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

        renderVerticalLine(minX, minY, maxY, minZ, 1.0F, 1.0F, 1.0F, 1.0F);
        renderVerticalLine(minX, minY, maxY, maxZ);

        renderVerticalLine(maxX, minY, maxY, maxZ, 0.0F, 1.0F, 0.0F, 1.0F);
        renderVerticalLine(maxX, minY, maxY, minZ);

        renderHorizontalLine(minX, maxX, minY, minZ, minZ);
        renderHorizontalLine(minX, maxX, maxY, minZ, minZ);

        renderHorizontalLine(minX, minX, minY, minZ, maxZ);
        renderHorizontalLine(minX, minX, maxY, minZ, maxZ);

        renderHorizontalLine(minX, maxX, minY, maxZ, maxZ);
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
        renderVerticalLine(x, minH, maxH, z, 1.0F, 0.0F, 0.0F, 1.0F);
    }

    private static void renderVerticalLine(double x, double minH, double maxH, double z, float r, float g, float b, float a) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        bufferbuilder.vertex(x, minH, z).color(r, g, b, 0).endVertex();
        bufferbuilder.vertex(x, minH, z).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(x, maxH, z).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(x, maxH, z).color(r, g, b, 0).endVertex();
    }

    private static void renderHorizontalLine(double minX, double maxX, double h, double minZ, double maxZ) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        bufferbuilder.vertex(minX, h, minZ).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(minX, h, minZ).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.vertex(maxX, h, maxZ).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.vertex(maxX, h, maxZ).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
    }

    public static void renderBlock(PoseStack matrix, BlockState state, BlockPos pos) {
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
}
