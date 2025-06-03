package net.pixeldreamstudios.exclusiveitem;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class CooldownHandler {
    private static final Map<UUID, Integer> cooldownTimers = new WeakHashMap<>();
    private static final int COOLDOWN_TICKS = 200;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ItemStack main = player.getMainHandStack();

                if (ExclusiveItemUtil.isExclusiveItem(main) && !ExclusiveItemUtil.isOwner(main, player)) {
                    UUID uuid = player.getUuid();
                    int ticksLeft = cooldownTimers.getOrDefault(uuid, 0);

                    if (ticksLeft <= 0) {

                        player.getItemCooldownManager().set(main.getItem(), COOLDOWN_TICKS);
                        cooldownTimers.put(uuid, COOLDOWN_TICKS);
                    } else {
                        cooldownTimers.put(uuid, ticksLeft - 1);
                    }
                } else {

                    cooldownTimers.remove(player.getUuid());
                }
            }
        });
    }
}
