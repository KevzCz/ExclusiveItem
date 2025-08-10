package net.pixeldreamstudios.exclusiveitem;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

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
    public static boolean shouldBindOnUse(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return false;
        NbtCompound nbt = component.copyNbt();
        return nbt.contains("on_use_bind") && nbt.getBoolean("on_use_bind");
    }

    public static boolean ensureOwnedForUse(ItemStack stack, PlayerEntity player) {
        if (!isExclusiveItem(stack)) return true;
        if (isOwner(stack, player)) return true;

        if (shouldBindOnUse(stack)) {
            bindToPlayer(stack, player);
            return isOwner(stack, player);
        }

        return false;
    }

    public static void setBindOnUse(ItemStack stack, boolean value) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = component != null ? component.copyNbt() : new NbtCompound();
        nbt.putBoolean("on_use_bind", value);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
    public static void bindToPlayer(ItemStack stack, PlayerEntity player) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        NbtCompound nbt = component != null ? component.copyNbt() : new NbtCompound();

        boolean newlyBound = false;

        if (!nbt.contains("exclusiveOwner")) {
            nbt.putUuid("exclusiveOwner", player.getUuid());
            nbt.putString("exclusiveOwnerName", player.getName().getString());
            newlyBound = true;
        }

        if (!nbt.containsUuid("exclusiveID")) {
            nbt.putUuid("exclusiveID", UUID.randomUUID());
        }

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        if (player instanceof ServerPlayerEntity serverPlayer && isOwner(stack, player)) {
            ExclusiveItemStorage.add(serverPlayer, stack);

            if (newlyBound) {
                ServerWorld sw = serverPlayer.getServerWorld();
                BindEffects.play(sw, serverPlayer);
            }
        }
    }

    @Nullable
    public static UUID getExclusiveID(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return null;

        NbtCompound nbt = component.copyNbt();
        return nbt.containsUuid("exclusiveID") ? nbt.getUuid("exclusiveID") : null;
    }

    public static boolean isOwner(ItemStack stack, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer && ExclusiveItemCommands.isBypassing(serverPlayer)) return true;

        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return false;

        NbtCompound nbt = component.copyNbt();

        if (!nbt.containsUuid("exclusiveOwner") || !player.getUuid().equals(nbt.getUuid("exclusiveOwner"))) {
            return false;
        }


        if (nbt.contains("requiredTag")) {
            String requiredTag = nbt.getString("requiredTag");
            return player.getCommandTags().contains(requiredTag);
        }

        return true;
    }
    public static boolean shouldShowRequiredTag(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return true;
        NbtCompound nbt = component.copyNbt();
        return !nbt.contains("showRequiredTag") || nbt.getBoolean("showRequiredTag");
    }

    public static String getOwnerName(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return "Nobody";
        NbtCompound nbt = component.copyNbt();
        return nbt.getString("exclusiveOwnerName");
    }

    public static String getRequiredTag(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return null;
        NbtCompound nbt = component.copyNbt();
        return nbt.contains("requiredTag") ? nbt.getString("requiredTag") : null;
    }

    public static ItemStack createExclusiveItemOwnedBySomebodyElse(Item item) {
        ItemStack stack = new ItemStack(item);

        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("ExclusiveItem", true);

        UUID fakeOwnerUUID = UUID.nameUUIDFromBytes("SomebodyElse".getBytes());
        nbt.putUuid("exclusiveOwner", fakeOwnerUUID);
        nbt.putString("exclusiveOwnerName", "SomebodyElse");
        nbt.putString("requiredTag", "TagRequirement");

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        return stack;
    }
}
