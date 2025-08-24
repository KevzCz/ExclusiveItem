package net.pixeldreamstudios.exclusiveitem;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.exclusiveitem.item.ModItems;
import net.pixeldreamstudios.exclusiveitem.network.SyncExclusiveItemsPayload;

import java.util.List;
import java.util.UUID;

import static net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil.getExclusiveID;

public class ExclusiveItemStorage {
    public static void syncToClient(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        ExclusiveItemWorldStorage storage = ExclusiveItemWorldStorage.get(world);

        List<ItemStack> stacks = storage.getStacks(player.getRegistryManager(), player.getUuid());
        NbtList list = new NbtList();

        for (ItemStack stack : stacks) {
            try {
                NbtElement encoded = stack.encode(player.getRegistryManager());
                if (encoded instanceof NbtCompound compound) {
                    list.add(compound);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        NbtCompound compound = new NbtCompound();
        compound.put("ExclusiveItemStorage", list);
        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(new SyncExclusiveItemsPayload(compound)));
    }

    public static void add(ServerPlayerEntity player, ItemStack stack) {
        if (!ExclusiveItemUtil.isOwner(stack, player)) return;
        ServerWorld world = player.getServerWorld();
        ExclusiveItemWorldStorage storage = ExclusiveItemWorldStorage.get(world);
        List<ItemStack> stacks = storage.getStacks(player.getRegistryManager(), player.getUuid());

        UUID newId = getExclusiveID(stack);
        if (newId == null) return;

        if (stack.isOf(ModItems.BOOK_ITEM)) {
            for (ItemStack existing : stacks) {
                if (existing.isOf(ModItems.BOOK_ITEM)) return;
            }
        }

        for (ItemStack existing : stacks) {
            if (newId.equals(getExclusiveID(existing))) return;
        }

        ItemStack copy = stack.copy();
        copy.setCount(1);
        stacks.add(copy);
        storage.setStacks(player.getRegistryManager(), player.getUuid(), stacks);
    }
    public static void addAndSync(ServerPlayerEntity player, ItemStack stack) {
        add(player, stack);
        syncToClient(player);
    }

    public static void remove(ServerPlayerEntity player, ItemStack stack) {
        ServerWorld world = player.getServerWorld();
        ExclusiveItemWorldStorage storage = ExclusiveItemWorldStorage.get(world);
        List<ItemStack> stacks = storage.getStacks(player.getRegistryManager(), player.getUuid());

        UUID targetId = getExclusiveID(stack);
        if (targetId == null) return;

        stacks.removeIf(existing -> targetId.equals(getExclusiveID(existing)));

        storage.setStacks(player.getRegistryManager(), player.getUuid(), stacks);

        syncToClient(player);
    }
}
