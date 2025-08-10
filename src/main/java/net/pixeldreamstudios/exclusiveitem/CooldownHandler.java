package net.pixeldreamstudios.exclusiveitem;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class CooldownHandler {
    private static final Map<UUID, Map<UUID, Integer>> cooldownTimers = new WeakHashMap<>();
    private static final int COOLDOWN_TICKS = 200;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ItemStack main = player.getMainHandStack();

                if (!ExclusiveItemUtil.isExclusiveItem(main)) {
                    cooldownTimers.remove(player.getUuid());
                    continue;
                }

                boolean bindOnUse = ExclusiveItemUtil.shouldBindOnUse(main);
                boolean isOwned   = ExclusiveItemUtil.isOwned(main);
                boolean isOwner   = ExclusiveItemUtil.isOwner(main, player);

                boolean shouldApplyCooldown;
                if (bindOnUse) {
                    if (!isOwned) {
                        shouldApplyCooldown = false;
                    } else {
                        shouldApplyCooldown = !isOwner;
                    }
                } else {
                    shouldApplyCooldown = !isOwner;
                }

                if (!shouldApplyCooldown) {
                    cooldownTimers.remove(player.getUuid());
                    continue;
                }

                UUID playerId = player.getUuid();
                UUID itemId = ExclusiveItemUtil.getExclusiveID(main);
                if (itemId == null) {
                    continue;
                }

                cooldownTimers.putIfAbsent(playerId, new WeakHashMap<>());
                Map<UUID, Integer> playerCooldowns = cooldownTimers.get(playerId);

                int ticksLeft = playerCooldowns.getOrDefault(itemId, 0);
                if (ticksLeft <= 0) {
                    player.getItemCooldownManager().set(main.getItem(), COOLDOWN_TICKS);
                    playerCooldowns.put(itemId, COOLDOWN_TICKS);
                } else {
                    playerCooldowns.put(itemId, ticksLeft - 1);
                }
            }
        });
    }
}
