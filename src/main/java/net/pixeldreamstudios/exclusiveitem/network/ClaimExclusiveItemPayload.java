package net.pixeldreamstudios.exclusiveitem.network;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.pixeldreamstudios.exclusiveitem.ExclusiveItemMod.MOD_ID;

public record ClaimExclusiveItemPayload(NbtCompound itemNbt) implements CustomPayload {
    public static final Id<ClaimExclusiveItemPayload> ID =
            new Id<>(Identifier.of(MOD_ID, "claim_exclusive_item"));

    public static final PacketCodec<PacketByteBuf, ClaimExclusiveItemPayload> CODEC =
            PacketCodec.of(ClaimExclusiveItemPayload::write, ClaimExclusiveItemPayload::read);

    @Override
    public Id<ClaimExclusiveItemPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeNbt(itemNbt);
    }

    public static ClaimExclusiveItemPayload read(PacketByteBuf buf) {
        return new ClaimExclusiveItemPayload(buf.readNbt());
    }
}
