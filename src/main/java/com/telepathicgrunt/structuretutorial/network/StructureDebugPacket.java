package com.telepathicgrunt.structuretutorial.network;

import com.telepathicgrunt.structuretutorial.client.ClientEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StructureDebugPacket {
   private BlockPos one;
   private BlockPos two;

   public StructureDebugPacket(BlockPos one, BlockPos two) {
      this.one = one;
      this.two = two;
   }

   public StructureDebugPacket(FriendlyByteBuf buf) {
      one = buf.readBlockPos();
      two = buf.readBlockPos();
   }

   public void write(FriendlyByteBuf buf) {
      buf.writeBlockPos(one);
      buf.writeBlockPos(two);
   }

   public static void handle(StructureDebugPacket msg, Supplier<NetworkEvent.Context> ctx) {
      ctx.get().enqueueWork(() ->
              DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                 BlockPos o = msg.one;
                 BlockPos t = msg.two;
                 BoundingBox box = new BoundingBox(o.getX(), o.getY(), o.getZ(), t.getX(), t.getY(), t.getZ());

                 if (!ClientEvents.boxes.contains(box)) {
                    System.out.println("Client - Adding new Box " + box);
                    ClientEvents.boxes.add(box);
                 }
              })
      );
      ctx.get().setPacketHandled(true);
   }
}