package com.telepathicgrunt.structuretutorial.mixin;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Bailey Delker
 * @created 08/02/2023 - 5:45 PM
 * @project structures-1.18.2
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

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

