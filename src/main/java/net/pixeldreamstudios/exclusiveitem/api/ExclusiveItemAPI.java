package net.pixeldreamstudios.exclusiveitem.api;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;
import java.util.UUID;

import static net.pixeldreamstudios.exclusiveitem.api.ExclusiveItemConstants.*;

public final class ExclusiveItemAPI {
    
    private ExclusiveItemAPI() {}

    public static boolean isExclusiveItem(ItemStack stack) {
        return getNbtCompound(stack)
                .map(nbt -> nbt.getBoolean(NBT_EXCLUSIVE_ITEM))
                .orElse(false);
    }

    public static boolean hasOwner(ItemStack stack) {
        return getNbtCompound(stack)
                .map(nbt -> nbt.contains(NBT_EXCLUSIVE_OWNER))
                .orElse(false);
    }

    public static Optional<UUID> getOwnerId(ItemStack stack) {
        return getNbtCompound(stack)
                .filter(nbt -> nbt.containsUuid(NBT_EXCLUSIVE_OWNER))
                .map(nbt -> nbt.getUuid(NBT_EXCLUSIVE_OWNER));
    }

    public static Optional<String> getOwnerName(ItemStack stack) {
        return getNbtCompound(stack)
                .filter(nbt -> nbt.contains(NBT_EXCLUSIVE_OWNER_NAME))
                .map(nbt -> nbt.getString(NBT_EXCLUSIVE_OWNER_NAME));
    }

    public static Optional<UUID> getExclusiveId(ItemStack stack) {
        return getNbtCompound(stack)
                .filter(nbt -> nbt.containsUuid(NBT_EXCLUSIVE_ID))
                .map(nbt -> nbt.getUuid(NBT_EXCLUSIVE_ID));
    }

    public static BindingMode getBindingMode(ItemStack stack) {
        boolean bindOnUse = getNbtCompound(stack)
                .map(nbt -> nbt.getBoolean(NBT_ON_USE_BIND))
                .orElse(false);
        return BindingMode.fromBoolean(bindOnUse);
    }

    public static Optional<String> getRequiredTag(ItemStack stack) {
        return getNbtCompound(stack)
                .filter(nbt -> nbt.contains(NBT_REQUIRED_TAG))
                .map(nbt -> nbt.getString(NBT_REQUIRED_TAG));
    }

    public static boolean shouldShowRequiredTag(ItemStack stack) {
        return getNbtCompound(stack)
                .map(nbt -> !nbt.contains(NBT_SHOW_REQUIRED_TAG) || nbt.getBoolean(NBT_SHOW_REQUIRED_TAG))
                .orElse(true);
    }

    public static boolean canPlayerUseItem(ItemStack stack, PlayerEntity player) {
        if (!isExclusiveItem(stack)) {
            return true;
        }

        if (!hasOwner(stack)) {
            BindingMode mode = getBindingMode(stack);
            return mode == BindingMode.ON_USE;
        }

        return getOwnerId(stack)
                .map(ownerId -> ownerId.equals(player.getUuid()))
                .orElse(false) && hasRequiredTag(stack, player);
    }

    public static boolean hasRequiredTag(ItemStack stack, PlayerEntity player) {
        return getRequiredTag(stack)
                .map(tag -> player.getCommandTags().contains(tag))
                .orElse(true);
    }

    private static Optional<NbtCompound> getNbtCompound(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        return Optional.ofNullable(component).map(NbtComponent::copyNbt);
    }

    public static class Builder {
        private final ItemStack stack;
        private final NbtCompound nbt;

        public Builder(ItemStack stack) {
            this.stack = stack;
            NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
            this.nbt = component != null ? component.copyNbt() : new NbtCompound();
        }

        public Builder makeExclusive() {
            nbt.putBoolean(NBT_EXCLUSIVE_ITEM, true);
            return this;
        }

        public Builder setBindingMode(BindingMode mode) {
            nbt.putBoolean(NBT_ON_USE_BIND, mode.isBindOnUse());
            return this;
        }

        public Builder setOwner(UUID playerId, String playerName) {
            nbt.putUuid(NBT_EXCLUSIVE_OWNER, playerId);
            nbt.putString(NBT_EXCLUSIVE_OWNER_NAME, playerName);
            return this;
        }

        public Builder setExclusiveId(UUID id) {
            nbt.putUuid(NBT_EXCLUSIVE_ID, id);
            return this;
        }

        public Builder setRequiredTag(String tag, boolean visible) {
            nbt.putString(NBT_REQUIRED_TAG, tag);
            nbt.putBoolean(NBT_SHOW_REQUIRED_TAG, visible);
            return this;
        }

        public Builder removeOwner() {
            nbt.remove(NBT_EXCLUSIVE_OWNER);
            nbt.remove(NBT_EXCLUSIVE_OWNER_NAME);
            return this;
        }

        public ItemStack build() {
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
            return stack;
        }
    }
}
