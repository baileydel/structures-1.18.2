package com.telepathicgrunt.structuretutorial.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * @author Bailey Delker
 * @created 07/30/2023 - 12:18 PM
 * @project structures-1.18.2
 */
public class RenderingUtil {
    public static void renderBoundingBox(PoseStack stack, BoundingBox box) {
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.mulPoseMatrix(stack.last().pose());
        RenderSystem.applyModelViewMatrix();
        Vec3 vec3 =  Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        RenderSystem.disableTexture();
        RenderSystem.disableBlend();

        double minX = box.minX() - vec3.x();
        double minH = box.minY() - vec3.y();
        double minZ = box.minZ() - vec3.z();

        double maxX = box.maxX() + 1 - vec3.x();
        double maxH = box.maxY() + 1 - vec3.y();
        double maxZ = box.maxZ() + 1 - vec3.z();

        RenderSystem.lineWidth(2.0F);
        bufferbuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        renderVerticalLine(minX, minH, maxH, minZ);
        renderVerticalLine(minX, minH, maxH, maxZ);

        renderVerticalLine(maxX, minH, maxH, maxZ);
        renderVerticalLine(maxX, minH, maxH, minZ);

        renderHorizontalLine(minX, maxX, minH, minZ, minZ);
        renderHorizontalLine(minX, maxX, maxH, minZ, minZ);

        renderHorizontalLine(minX, minX, minH, minZ, maxZ);
        renderHorizontalLine(minX, minX, maxH, minZ, maxZ);

        renderHorizontalLine(minX, maxX, minH, maxZ, maxZ);
        renderHorizontalLine(minX, maxX, maxH, maxZ, maxZ);

        renderHorizontalLine(maxX, maxX, maxH, minZ, maxZ);
        renderHorizontalLine(maxX, maxX, minH, minZ, maxZ);

        tesselator.end();
        RenderSystem.lineWidth(2.0F);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private static void renderVerticalLine(double x, double minH, double maxH, double z) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        bufferbuilder.vertex(x, minH, z).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(x, minH, z).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.vertex(x, maxH, z).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.vertex(x, maxH, z).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
    }

    private static void renderHorizontalLine(double minX, double maxX, double h, double minZ, double maxZ) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();

        bufferbuilder.vertex(minX, h, minZ).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(minX, h, minZ).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.vertex(maxX, h, maxZ).color(1.0F, 0.0F, 0.0F, 0.5F).endVertex();
        bufferbuilder.vertex(maxX, h, maxZ).color(1.0F, 0.0F, 0.0F, 0.0F).endVertex();
    }

    public static void renderHitOutline(PoseStack stack, VertexConsumer vertexConsumer, Entity entity, double p_109641_, double p_109642_, double p_109643_, BlockPos p_109644_, BlockState p_109645_) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            renderShape(stack, vertexConsumer, p_109645_.getShape(level, p_109644_, CollisionContext.of(entity)), (double)p_109644_.getX() - p_109641_, (double)p_109644_.getY() - p_109642_, (double)p_109644_.getZ() - p_109643_);
        }
    }

    private static void renderShape(PoseStack stack, VertexConsumer vertexConsumer, VoxelShape shape, double p_109786_, double p_109787_, double p_109788_) {
        PoseStack.Pose posestack$pose = stack.last();
        shape.forAllEdges((p_194324_, p_194325_, p_194326_, p_194327_, p_194328_, p_194329_) -> {
            float f = (float)(p_194327_ - p_194324_);
            float f1 = (float)(p_194328_ - p_194325_);
            float f2 = (float)(p_194329_ - p_194326_);
            float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
            f /= f3;
            f1 /= f3;
            f2 /= f3;
            vertexConsumer.vertex(posestack$pose.pose(), (float)(p_194324_ + p_109786_), (float)(p_194325_ + p_109787_), (float)(p_194326_ + p_109788_)).color(1.0F, 0.0F, 0.0F, 0.5F).normal(posestack$pose.normal(), f, f1, f2).endVertex();
            vertexConsumer.vertex(posestack$pose.pose(), (float)(p_194327_ + p_109786_), (float)(p_194328_ + p_109787_), (float)(p_194329_ + p_109788_)).color(1.0F, 0.0F, 0.0F, 0.5F).normal(posestack$pose.normal(), f, f1, f2).endVertex();
        });
    }
}
