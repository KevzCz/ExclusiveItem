package net.pixeldreamstudios.exclusiveitem.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;

import java.util.ArrayList;
import java.util.List;

public class ClientExclusiveItemStorage {
    private static final List<ItemStack> clientItems = new ArrayList<>();

    public static void handleSync(NbtCompound nbt) {
        clientItems.clear();
        if (nbt == null || !nbt.contains("ExclusiveItemStorage")) return;

        NbtElement raw = nbt.get("ExclusiveItemStorage");
        if (!(raw instanceof NbtList list)) return;

        var networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) {
            System.err.println("[ExclusiveItem] Network handler is null, skipping sync.");
            return;
        }

        DynamicRegistryManager registryManager = networkHandler.getRegistryManager();
        RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, registryManager);

        for (int i = 0; i < list.size(); i++) {
            NbtElement element = list.get(i);

            if (!(element instanceof NbtCompound compound)) continue;

            try {
                ItemStack.fromNbt(registryManager, compound).ifPresent(stack -> {
                    if (!stack.isEmpty()) {
                        clientItems.add(stack);
                    }
                });
            } catch (Exception e) {
                System.err.println("[ExclusiveItem] Failed to parse item stack from NBT:\n" + compound);
            }


        }
    }

    public static List<ItemStack> get() {
        return clientItems;
    }
}
