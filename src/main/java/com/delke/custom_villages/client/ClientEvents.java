package com.delke.custom_villages.client;

import com.delke.custom_villages.network.ForcePacket;
import com.delke.custom_villages.network.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bailey Delker
 * @created 07/30/2023 - 12:16 PM
 * @project structures-1.18.2
 */
@OnlyIn(Dist.CLIENT)
public class ClientEvents {
    public static final List<BuildablePiece> pieces = new ArrayList<>();
    private boolean loaded = false;

    @SubscribeEvent
    public void OnKey(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null && event.getKey() == 61 && mc.level != null && !loaded) {
            loaded = true;
            System.out.println("Client - Trying to send ForcePacket");
            Network.INSTANCE.sendToServer(new ForcePacket());
        }

        if (event.getKey() == 45) {
            loaded = false;
            pieces.clear();
        }
    }

    @SubscribeEvent
    public void RenderGui(RenderGameOverlayEvent event) {
        /*
            STOP-SHIP
            Only render is close to the piece
            Do Block check
                if block state is wrong on structure make it yellow
                if structure contains incorrect block make it red

           Start development of actual structures, not one piece.
                Locate a piece in a structure (Start a structure off with 2 pieces)
                Add pieces to a structure after generation
                Remove pieces of a structure after generation
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

            for (BuildablePiece piece : pieces) {
                BoundingBox box = piece.getBox();

                if (box.getCenter().closerThan(player.getOnPos(), mc.options.renderDistance * 16)) {
                    piece.renderWorld(event.getPoseStack());
                }
            }
        }
    }
}
