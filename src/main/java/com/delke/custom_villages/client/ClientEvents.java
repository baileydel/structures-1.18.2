package com.delke.custom_villages.client;

import com.delke.custom_villages.ModStructureManager;
import com.delke.custom_villages.client.render.RenderBuildablePiece;
import com.delke.custom_villages.network.ClearPacket;
import com.delke.custom_villages.network.ForcePacket;
import com.delke.custom_villages.network.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

import static com.delke.custom_villages.network.ForcePacket.STATIC_START;

/**
 * @author Bailey Delker
 * @created 07/30/2023 - 12:16 PM
 * @project structures-1.18.2
 */
@OnlyIn(Dist.CLIENT)
public class ClientEvents {
    public static final List<RenderBuildablePiece> pieces = new ArrayList<>();
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
            ModStructureManager.startMap.remove(STATIC_START);
            pieces.clear();
        }

        if (event.getKey() == 90) {
            if (ModStructureManager.hasPiece(STATIC_START, ForcePacket.makeStructure(), "structure_tutorial:road")) {
                System.out.println("dub");
            }
        }

        if (event.getKey() == 82) {
            Network.INSTANCE.sendToServer(new ClearPacket());
        }
    }

    @SubscribeEvent
    public void RenderGui(RenderGameOverlayEvent event) {
        /*
            STOP-SHIP
            Do Block check
                if block state is wrong on structure make it yellow
                if structure contains incorrect block make it red

           Start development of actual structures, not one piece.
                Add pieces to a structure after generation
                Remove pieces of a structure after generation
         */

        for (RenderBuildablePiece piece : pieces) {
            piece.renderGui(event.getMatrixStack());
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
}
