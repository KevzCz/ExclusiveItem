package net.pixeldreamstudios.exclusiveitem;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pixeldreamstudios.exclusiveitem.item.ModItems;
import net.pixeldreamstudios.exclusiveitem.network.SyncExclusiveItemsPayload;

import java.util.*;

import static net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil.getExclusiveID;

public class ExclusiveItemStorage {
    private static final Map<UUID, List<ItemStack>> storage = new HashMap<>();

    public static void add(ServerPlayerEntity player, ItemStack stack) {
        if (!ExclusiveItemUtil.isOwner(stack, player)) return; // 💥 Ownership check

        List<ItemStack> list = storage.computeIfAbsent(player.getUuid(), k -> new ArrayList<>());
        UUID newId = getExclusiveID(stack);
        if (newId == null) return;

        if (stack.isOf(ModItems.BOOK_ITEM)) {
            for (ItemStack existing : list) {
                if (existing.isOf(ModItems.BOOK_ITEM)) {
                    return;
                }
            }
        }
        for (ItemStack existing : list) {
            UUID existingId = getExclusiveID(existing);
            if (newId.equals(existingId)) {
                return;
            }
        }
        ItemStack copy = stack.copy();
        copy.setCount(1);
        list.add(copy);
    }
    public static void syncToClient(ServerPlayerEntity player) {
        List<ItemStack> stored = ExclusiveItemStorage.get(player);
        RegistryWrapper.WrapperLookup registryLookup = player.getRegistryManager();

        NbtList list = new NbtList();
        for (ItemStack stack : stored) {
            ItemStack.CODEC.encodeStart(registryLookup.getOps(NbtOps.INSTANCE), stack)
                    .result()
                    .ifPresent(nbtElement -> list.add((NbtCompound) nbtElement));
        }

        NbtCompound compound = new NbtCompound();
        compound.put("ExclusiveItemStorage", list);

        player.networkHandler.sendPacket(new CustomPayloadS2CPacket(new SyncExclusiveItemsPayload(compound)));
    }


    public static List<ItemStack> get(ServerPlayerEntity player) {
        return storage.getOrDefault(player.getUuid(), Collections.emptyList());
    }

    public static void set(ServerPlayerEntity player, List<ItemStack> stacks) {
        storage.put(player.getUuid(), new ArrayList<>(stacks));
    }

    public static void loadFromNbt(ServerPlayerEntity player, NbtCompound nbt) {
        if (!nbt.contains("ExclusiveItemStorage", NbtList.COMPOUND_TYPE)) return;

        RegistryWrapper.WrapperLookup registryLookup = player.getRegistryManager();
        NbtList list = nbt.getList("ExclusiveItemStorage", NbtList.COMPOUND_TYPE);
        List<ItemStack> stacks = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            NbtCompound stackNbt = list.getCompound(i);
            ItemStack.CODEC.parse(registryLookup.getOps(NbtOps.INSTANCE), stackNbt).result().ifPresent(stacks::add);
        }

        set(player, stacks);
    }

    public static void saveToNbt(ServerPlayerEntity player, NbtCompound nbt) {
        List<ItemStack> stacks = get(player);
        RegistryWrapper.WrapperLookup registryLookup = player.getRegistryManager();

        NbtList list = new NbtList();
        for (ItemStack stack : stacks) {
            ItemStack.CODEC.encodeStart(registryLookup.getOps(NbtOps.INSTANCE), stack)
                    .result()
                    .ifPresent(nbtElement -> list.add((NbtCompound) nbtElement));
        }

        nbt.put("ExclusiveItemStorage", list);
    }

}
