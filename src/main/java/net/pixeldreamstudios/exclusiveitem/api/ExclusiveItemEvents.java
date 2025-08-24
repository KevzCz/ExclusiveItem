package net.pixeldreamstudios.exclusiveitem.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ExclusiveItemEvents {
    @FunctionalInterface
    public interface ShouldAddToStorage {
        boolean shouldAdd(ServerPlayerEntity player, ItemStack stack);
    }

    public static final Event<ShouldAddToStorage> SHOULD_ADD_TO_STORAGE =
            EventFactory.createArrayBacked(ShouldAddToStorage.class, listeners -> (player, stack) -> {
                for (ShouldAddToStorage l : listeners) {
                    if (!l.shouldAdd(player, stack)) return false;
                }
                return true;
            });

    private ExclusiveItemEvents() {}
}
