package com.delke.custom_villages.client;

import com.delke.custom_villages.VillageStructureStartWrapper;
import com.delke.custom_villages.client.render.RenderBuildablePiece;
import com.delke.custom_villages.network.AddPieceStructurePacket;
import com.delke.custom_villages.network.DeleteStructurePiecePacket;
import com.delke.custom_villages.network.Network;
import com.delke.custom_villages.network.ResetStructurePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
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
    public static final List<RenderBuildablePiece> pieces = new ArrayList<>();
    private int tick = 21;
    private boolean sentPacket = false;

    @SubscribeEvent
    public void OnKey(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            switch (event.getKey()) {
                case 61 ->  // + Generate Structure
                        sendPacket(new AddPieceStructurePacket());
                case 82 -> // R Clear Blocks, and reset structure..
                        sendPacket(new ResetStructurePacket());
                case 45 -> // G Delete a Piece
                        sendPacket(new DeleteStructurePiecePacket());
            }
        }
    }

    @SubscribeEvent
    public void ClientTick(TickEvent.ClientTickEvent event) {
        if (tick <= 20) {
            tick++;
        }

        if (tick == 20) {
            clear();
        }
    }

    private void clear() {
        System.out.println("Clearing Pieces & Structure Starts");
        pieces.clear();
        VillageStructureStartWrapper.startMap.clear();
        sentPacket = false;
    }

    @SubscribeEvent
    public void RenderGui(RenderGameOverlayEvent event) {
        /*
            STOP-SHIP
            Do Block check
                if block state is wrong on structure make it yellow
                if structure contains incorrect block make it red
         */

        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null) {
            if (tick <= 20) {
                Font font = Minecraft.getInstance().font;
                font.drawShadow(event.getMatrixStack(), tick + "", 50F, 50F, 23721831);
            }

            for (RenderBuildablePiece piece : pieces) {
                piece.renderGui(event.getMatrixStack());
            }
        }
    }

    @SubscribeEvent
    public void RenderLevelStageEvent(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && mc.level != null && event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS)) {
            for (RenderBuildablePiece piece : pieces) {
                BoundingBox box = piece.getBox();

                if (box.getCenter().closerThan(player.getOnPos(), mc.options.renderDistance * 16)) {
                    piece.renderWorld(event.getPoseStack());
                }
            }
        }
    }

    private void sendPacket(Object packet) {
        if (!sentPacket) {
            Network.INSTANCE.sendToServer(packet);
            this.tick = 0;
            sentPacket = true;
        }
    }
}
