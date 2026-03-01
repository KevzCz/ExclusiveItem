package net.pixeldreamstudios.exclusiveitem;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static net.pixeldreamstudios.exclusiveitem.api.ExclusiveItemConstants.DEFAULT_COOLDOWN_TICKS;

public class CooldownHandler {
    private static final Map<UUID, PlayerCooldownData> cooldownTimers = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<Map.Entry<UUID, PlayerCooldownData>> iterator = cooldownTimers.entrySet().iterator();
            
            while (iterator.hasNext()) {
                Map.Entry<UUID, PlayerCooldownData> entry = iterator.next();
                UUID playerId = entry.getKey();
                PlayerCooldownData data = entry.getValue();
                
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
                if (player == null) {
                    iterator.remove();
                    continue;
                }
                
                ItemStack main = player.getMainHandStack();
                if (!ExclusiveItemUtil.isExclusiveItem(main)) {
                    iterator.remove();
                    continue;
                }

                if (!shouldApplyCooldown(main, player)) {
                    iterator.remove();
                    continue;
                }

                UUID itemId = ExclusiveItemUtil.getExclusiveID(main);
                if (itemId == null) {
                    continue;
                }

                int ticksLeft = data.getCooldown(itemId);
                if (ticksLeft <= 0) {
                    player.getItemCooldownManager().set(main.getItem(), DEFAULT_COOLDOWN_TICKS);
                    data.setCooldown(itemId, DEFAULT_COOLDOWN_TICKS);
                } else {
                    data.setCooldown(itemId, ticksLeft - 1);
                }
            }
        });
    }

    private static boolean shouldApplyCooldown(ItemStack stack, ServerPlayerEntity player) {
        boolean bindOnUse = ExclusiveItemUtil.shouldBindOnUse(stack);
        boolean isOwned = ExclusiveItemUtil.isOwned(stack);
        boolean isOwner = ExclusiveItemUtil.isOwner(stack, player);

        if (bindOnUse) {
            return isOwned && !isOwner;
        }
        return !isOwner;
    }

    public static void trackPlayer(UUID playerId) {
        cooldownTimers.putIfAbsent(playerId, new PlayerCooldownData());
    }

    private static class PlayerCooldownData {
        private final Map<UUID, Integer> itemCooldowns = new HashMap<>();

        public int getCooldown(UUID itemId) {
            return itemCooldowns.getOrDefault(itemId, 0);
        }

        public void setCooldown(UUID itemId, int ticks) {
            if (ticks <= 0) {
                itemCooldowns.remove(itemId);
            } else {
                itemCooldowns.put(itemId, ticks);
            }
        }
    }
}
