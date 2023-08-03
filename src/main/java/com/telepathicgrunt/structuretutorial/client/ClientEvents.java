package com.telepathicgrunt.structuretutorial.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bailey Delker
 * @created 07/30/2023 - 12:16 PM
 * @project structures-1.18.2
 */
public class ClientEvents {
    public static List<BoundingBox> boxes = new ArrayList<>();

    @SubscribeEvent
    public void RenderLevelStageEvent(RenderLevelStageEvent event) {
        Player player = Minecraft.getInstance().player;

        if (player != null && event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS)) {
            for (BoundingBox box : boxes) {
                if (box.getCenter().closerThan(player.getOnPos(), Minecraft.getInstance().options.renderDistance * 16)) {
                    RenderingUtil.renderBoundingBox(event.getPoseStack(), box);
                }
            }
        }
    }
}
