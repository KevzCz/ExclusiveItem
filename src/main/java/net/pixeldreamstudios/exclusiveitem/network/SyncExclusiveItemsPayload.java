package net.pixeldreamstudios.exclusiveitem.network;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.pixeldreamstudios.exclusiveitem.ExclusiveItemMod.MOD_ID;

public record SyncExclusiveItemsPayload(NbtCompound data) implements CustomPayload {
    public static final Id<SyncExclusiveItemsPayload> ID =
            new Id<>(Identifier.of(MOD_ID, "sync_exclusive_items"));

    public static final PacketCodec<PacketByteBuf, SyncExclusiveItemsPayload> CODEC =
            PacketCodec.of(SyncExclusiveItemsPayload::write, SyncExclusiveItemsPayload::read);

    @Override
    public Id<SyncExclusiveItemsPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeNbt(data);
    }

    public static SyncExclusiveItemsPayload read(PacketByteBuf buf) {
        return new SyncExclusiveItemsPayload(buf.readNbt());
    }
}
