package com.delke.custom_villages.client;

import com.delke.custom_villages.client.render.RenderingUtil;
import com.delke.custom_villages.network.ForcePacket;
import com.delke.custom_villages.network.Network;
import com.delke.custom_villages.network.StructureDebugPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.client.gui.GuiComponent.fill;

/**
 * @author Bailey Delker
 * @created 07/30/2023 - 12:16 PM
 * @project structures-1.18.2
 */

@OnlyIn(Dist.CLIENT)
public class ClientEvents {
    private final List<BuildablePiece> pieces = new ArrayList<>();

    @SubscribeEvent
    public void OnKey(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && event.getKey() == 61 && mc.level != null) {
            System.out.println("Client - Trying to send ForcePacket");
            Network.INSTANCE.sendToServer(new ForcePacket());
        }
    }

    @SubscribeEvent
    public void TitleScreen(ScreenEvent event) {
        if (event.getScreen() instanceof TitleScreen) {
            if (pieces.size() > 0) {
                System.out.println("Clearing");
                pieces.clear();
            }
        }
    }

    @SubscribeEvent
    public void RenderGui(RenderGameOverlayEvent event) {
        /*
            STOP-SHIP
            Only render is close to the piece ect
         */
        for (BuildablePiece piece : pieces) {
            piece.renderGui(event.getMatrixStack());
        }
    }

    @SubscribeEvent
    public void RenderLevelStageEvent(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && mc.level != null && event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS)) {
            for (Pair<CompoundTag, BoundingBox> pair : StructureDebugPacket.data) {
                BoundingBox box = pair.getSecond();

                // Render Bounding Box for every Structure.
                if (box.getCenter().closerThan(player.getOnPos(), mc.options.renderDistance * 16)) {
                    RenderingUtil.renderBoundingBox(event.getPoseStack(), box);
                }

                CompoundTag tag = pair.getFirst();
                if (tag != null) {
                    BuildablePiece piece = new BuildablePiece(box, tag);
                    if (!pieces.contains(piece)) {
                        System.out.println("Client - Adding new piece");
                        pieces.add(piece);
                    }
                }
            }

            for (BuildablePiece piece : pieces) {
                piece.renderWorld(event.getPoseStack());
            }
        }
    }
}
