package net.pixeldreamstudios.exclusiveitem.network;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ExclusiveItemServerHooks {
    private ExclusiveItemServerHooks() {}

    public static void beforeSyncAfterClaim(ServerPlayerEntity player, ItemStack claimed) {}
}
