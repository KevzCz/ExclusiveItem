package net.pixeldreamstudios.exclusiveitem;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashSet;
import java.util.UUID;

public class ExclusiveItemCommands {
    private static final HashSet<UUID> devBypass = new HashSet<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("exclusiveitem")
                .then(CommandManager.literal("add")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            if (player == null) return 0;

                            ItemStack stack = player.getMainHandStack();
                            NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
                            NbtCompound nbt = component != null ? component.copyNbt() : new NbtCompound();

                            nbt.putBoolean("ExclusiveItem", true);
                            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                            player.sendMessage(Text.literal("Item tagged as Exclusive.").formatted(Formatting.GREEN), false);
                            return 1;
                        }))
                .then(CommandManager.literal("dev")
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            if (player == null) return 0;

                            UUID uuid = player.getUuid();
                            if (devBypass.contains(uuid)) {
                                devBypass.remove(uuid);
                                player.sendMessage(Text.literal("ExclusiveItem Dev Mode OFF").formatted(Formatting.RED), false);
                            } else {
                                devBypass.add(uuid);
                                player.sendMessage(Text.literal("ExclusiveItem Dev Mode ON").formatted(Formatting.AQUA), false);
                            }
                            return 1;
                        }))
        );
    }

    public static boolean isBypassing(ServerPlayerEntity player) {
        return devBypass.contains(player.getUuid());
    }

    public static boolean isBypassing(UUID uuid) {
        return devBypass.contains(uuid);
    }
}
