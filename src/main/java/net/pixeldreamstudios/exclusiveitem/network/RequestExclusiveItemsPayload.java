package net.pixeldreamstudios.exclusiveitem.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.pixeldreamstudios.exclusiveitem.ExclusiveItemMod.MOD_ID;

public record RequestExclusiveItemsPayload() implements CustomPayload {
    public static final Id<RequestExclusiveItemsPayload> ID =
            new Id<>(Identifier.of(MOD_ID, "request_exclusive_items"));

    public static final PacketCodec<PacketByteBuf, RequestExclusiveItemsPayload> CODEC =
            PacketCodec.of(RequestExclusiveItemsPayload::write, buf -> new RequestExclusiveItemsPayload());

    @Override
    public Id<RequestExclusiveItemsPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        // Nothing to write for now
    }
}
