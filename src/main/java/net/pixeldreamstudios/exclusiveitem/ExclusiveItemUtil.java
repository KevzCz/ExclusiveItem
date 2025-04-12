package net.pixeldreamstudios.exclusiveitem;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;


public class ExclusiveItemUtil {

    public static boolean isExclusiveItem(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        return component != null && component.copyNbt().getBoolean("ExclusiveItem");
    }

    public static boolean isOwned(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        return component != null && component.copyNbt().contains("exclusiveOwner");
    }

    public static void bindToPlayer(ItemStack stack, PlayerEntity player) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = component != null ? component.copyNbt() : new NbtCompound();

        if (!nbt.contains("exclusiveOwner")) {
            nbt.putUuid("exclusiveOwner", player.getUuid());
            nbt.putString("exclusiveOwnerName", player.getName().getString());
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }
    }

    public static boolean isOwner(ItemStack stack, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer && ExclusiveItemCommands.isBypassing(serverPlayer)) return true;

        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return false;

        NbtCompound nbt = component.copyNbt();
        return nbt.containsUuid("exclusiveOwner") && player.getUuid().equals(nbt.getUuid("exclusiveOwner"));
    }


    public static String getOwnerName(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return "Nobody";
        NbtCompound nbt = component.copyNbt();
        return nbt.getString("exclusiveOwnerName");
    }
    public static ItemStack createExclusiveItemOwnedBySomebodyElse(Item item) {
        ItemStack stack = new ItemStack(item);

        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("ExclusiveItem", true);

        // Fake UUID (stable)
        UUID fakeOwnerUUID = UUID.nameUUIDFromBytes("SomebodyElse".getBytes());

        nbt.putUuid("exclusiveOwner", fakeOwnerUUID);
        nbt.putString("exclusiveOwnerName", "SomebodyElse");

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        return stack;
    }
}
