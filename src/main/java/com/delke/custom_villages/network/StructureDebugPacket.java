package com.delke.custom_villages.network;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class StructureDebugPacket {
    // List of all data for client
    public static final List<Pair<CompoundTag, BoundingBox>> data = new ArrayList<>();

    @Nullable
    private final CompoundTag tag;
    private final BlockPos one;
    private final BlockPos two;

    public StructureDebugPacket(@Nullable CompoundTag tag, BlockPos one, BlockPos two) {
        this.tag = tag;
        this.one = one;
        this.two = two;
    }

    public StructureDebugPacket(FriendlyByteBuf buf) {
        tag = buf.readAnySizeNbt();
        one = buf.readBlockPos();
        two = buf.readBlockPos();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
        buf.writeBlockPos(one);
        buf.writeBlockPos(two);
    }

    /*
        Only ever handle data on client (one way packet)
     */
    public static void handle(StructureDebugPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    Pair<CompoundTag, BoundingBox> pair = Pair.of(msg.tag, BoundingBox.fromCorners(msg.one, msg.two));

                    if (!data.contains(pair)) {
                        data.add(pair);

                        System.out.println("Client - Adding new Box " + pair);
                    }
                })
        );
        ctx.get().setPacketHandled(true);
    }
}