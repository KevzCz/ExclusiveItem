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

    @FunctionalInterface
    public interface OnItemBound {
        void onBound(ServerPlayerEntity player, ItemStack stack, boolean wasNewlyBound);
    }

    @FunctionalInterface
    public interface OnItemUnbound {
        void onUnbound(ServerPlayerEntity player, ItemStack stack);
    }

    @FunctionalInterface
    public interface OnBindingAttemptFailed {
        void onFailed(ServerPlayerEntity player, ItemStack stack, BindingResult.BindingFailureReason reason);
    }

    @FunctionalInterface
    public interface ShouldAllowBinding {
        boolean shouldAllow(ServerPlayerEntity player, ItemStack stack);
    }

    public static final Event<ShouldAddToStorage> SHOULD_ADD_TO_STORAGE =
            EventFactory.createArrayBacked(ShouldAddToStorage.class, listeners -> (player, stack) -> {
                for (ShouldAddToStorage l : listeners) {
                    if (!l.shouldAdd(player, stack)) return false;
                }
                return true;
            });

    public static final Event<OnItemBound> ON_ITEM_BOUND =
            EventFactory.createArrayBacked(OnItemBound.class, listeners -> (player, stack, wasNewlyBound) -> {
                for (OnItemBound l : listeners) {
                    l.onBound(player, stack, wasNewlyBound);
                }
            });

    public static final Event<OnItemUnbound> ON_ITEM_UNBOUND =
            EventFactory.createArrayBacked(OnItemUnbound.class, listeners -> (player, stack) -> {
                for (OnItemUnbound l : listeners) {
                    l.onUnbound(player, stack);
                }
            });

    public static final Event<OnBindingAttemptFailed> ON_BINDING_ATTEMPT_FAILED =
            EventFactory.createArrayBacked(OnBindingAttemptFailed.class, listeners -> (player, stack, reason) -> {
                for (OnBindingAttemptFailed l : listeners) {
                    l.onFailed(player, stack, reason);
                }
            });

    public static final Event<ShouldAllowBinding> SHOULD_ALLOW_BINDING =
            EventFactory.createArrayBacked(ShouldAllowBinding.class, listeners -> (player, stack) -> {
                for (ShouldAllowBinding l : listeners) {
                    if (!l.shouldAllow(player, stack)) return false;
                }
                return true;
            });

    private ExclusiveItemEvents() {}
}

