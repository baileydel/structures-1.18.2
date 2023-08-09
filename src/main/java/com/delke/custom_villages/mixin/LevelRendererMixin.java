package com.delke.custom_villages.mixin;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.nio.charset.MalformedInputException;
import java.util.Objects;

/**
 * @author Bailey Delker
 * @created 08/02/2023 - 5:45 PM
 * @project structures-1.18.2
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow @Nullable private VertexBuffer skyBuffer;

    @Redirect(
            // the method this function is called in
            method = "renderChunkLayer",
            // target the invocation of System.out.println
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/shaders/Uniform;upload()V"
            )
    )
    private void renderChunkLayer(Uniform instance) {

    }
}

