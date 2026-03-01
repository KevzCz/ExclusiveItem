package net.pixeldreamstudios.exclusiveitem.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemStorage;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemWorldStorage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ExclusiveItemQuery {
    
    private ExclusiveItemQuery() {}

    public static List<ItemStack> getPlayerBoundItems(ServerPlayerEntity player) {
        ExclusiveItemWorldStorage storage = ExclusiveItemWorldStorage.get(player.getServerWorld());
        return storage.getStacks(player.getRegistryManager(), player.getUuid());
    }

    public static Optional<ItemStack> findBoundItemById(ServerPlayerEntity player, UUID itemId) {
        return getPlayerBoundItems(player).stream()
                .filter(stack -> itemId.equals(ExclusiveItemUtil.getExclusiveID(stack)))
                .findFirst();
    }

    public static int countBoundItems(ServerPlayerEntity player) {
        return getPlayerBoundItems(player).size();
    }

    public static boolean hasBoundItem(ServerPlayerEntity player, UUID itemId) {
        return findBoundItemById(player, itemId).isPresent();
    }

    public static List<ItemStack> getBoundItemsByType(ServerPlayerEntity player, Class<?> itemClass) {
        return getPlayerBoundItems(player).stream()
                .filter(stack -> itemClass.isInstance(stack.getItem()))
                .collect(Collectors.toList());
    }

    public static boolean canPlayerAccessItem(ItemStack stack, PlayerEntity player) {
        if (!ExclusiveItemUtil.isExclusiveItem(stack)) {
            return true;
        }

        if (!ExclusiveItemUtil.isOwned(stack)) {
            return ExclusiveItemUtil.shouldBindOnUse(stack);
        }

        return ExclusiveItemUtil.isOwner(stack, player);
    }

    public static void reclaimItem(ServerPlayerEntity player, ItemStack stack) {
        if (ExclusiveItemUtil.isExclusiveItem(stack) && ExclusiveItemUtil.isOwner(stack, player)) {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            ExclusiveItemStorage.addAndSync(player, copy);
        }
    }

    public static void unbindItem(ServerPlayerEntity player, ItemStack stack) {
        if (ExclusiveItemUtil.isOwner(stack, player)) {
            ExclusiveItemAPI.Builder builder = new ExclusiveItemAPI.Builder(stack);
            builder.removeOwner().build();
            ExclusiveItemStorage.remove(player, stack);
            ExclusiveItemEvents.ON_ITEM_UNBOUND.invoker().onUnbound(player, stack);
        }
    }

    public static Optional<String> getBindingStatus(ItemStack stack, PlayerEntity player) {
        if (!ExclusiveItemUtil.isExclusiveItem(stack)) {
            return Optional.of("not_exclusive");
        }

        if (!ExclusiveItemUtil.isOwned(stack)) {
            BindingMode mode = ExclusiveItemAPI.getBindingMode(stack);
            return Optional.of(mode == BindingMode.ON_USE ? "unbound_on_use" : "unbound_on_pickup");
        }

        if (ExclusiveItemUtil.isOwner(stack, player)) {
            return Optional.of("owned");
        }

        return Optional.of("owned_by_other");
    }
}
