package net.pixeldreamstudios.exclusiveitem;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.mojang.brigadier.arguments.BoolArgumentType;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;

public class ExclusiveItemCommands {
    private static final HashSet<UUID> devBypass = new HashSet<>();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("exclusiveitem")
                .then(CommandManager.literal("add")
                        .requires(source -> source.hasPermissionLevel(2))
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
                        .requires(source -> source.hasPermissionLevel(2))
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
                .then(CommandManager.literal("remove")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            if (player == null) return 0;

                            ItemStack stack = player.getMainHandStack();
                            NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
                            NbtCompound nbt = component != null ? component.copyNbt() : new NbtCompound();

                            if (!nbt.contains("ExclusiveItem")) {
                                player.sendMessage(Text.literal("This item is not exclusive.").formatted(Formatting.YELLOW), false);
                                return 1;
                            }

                            nbt.remove("ExclusiveItem");
                            nbt.remove("exclusiveOwner");
                            nbt.remove("exclusiveOwnerName");
                            nbt.remove("requiredTag");

                            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                            player.sendMessage(Text.literal("Exclusive tag removed from item.").formatted(Formatting.RED), false);
                            return 1;
                        }))
                .then(CommandManager.literal("settag")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(argument("tag", StringArgumentType.word())
                                .then(argument("visible", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                                            if (player == null) return 0;

                                            String tag = StringArgumentType.getString(ctx, "tag");
                                            boolean visible = BoolArgumentType.getBool(ctx, "visible");


                                            ItemStack stack = player.getMainHandStack();
                                            NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
                                            NbtCompound nbt = component != null ? component.copyNbt() : new NbtCompound();

                                            if (!nbt.getBoolean("ExclusiveItem")) {
                                                player.sendMessage(Text.literal("This item is not exclusive. Use /exclusiveitem add first.")
                                                        .formatted(Formatting.YELLOW), false);
                                                return 1;
                                            }

                                            nbt.putString("requiredTag", tag);
                                            nbt.putBoolean("showRequiredTag", visible);
                                            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                                            player.sendMessage(Text.literal("Required tag set to: ")
                                                    .append(Text.literal(tag).formatted(Formatting.AQUA))
                                                    .append(Text.literal(" (Visible: " + visible + ")").formatted(Formatting.GRAY)), false);
                                            return 1;
                                        })
                                )
                        )
                )
                .then(CommandManager.literal("info")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            if (player == null) return 0;

                            ItemStack stack = player.getMainHandStack();
                            NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
                            NbtCompound nbt = component != null ? component.copyNbt() : new NbtCompound();

                            if (!nbt.getBoolean("ExclusiveItem")) {
                                player.sendMessage(Text.literal("This item is not exclusive.").formatted(Formatting.YELLOW), false);
                                return 1;
                            }

                            String owner = nbt.contains("exclusiveOwnerName") ? nbt.getString("exclusiveOwnerName") : "Unknown";
                            String tag = nbt.contains("requiredTag") ? nbt.getString("requiredTag") : "(None)";
                            boolean visible = !nbt.contains("showRequiredTag") || nbt.getBoolean("showRequiredTag");

                            player.sendMessage(Text.literal("=== Exclusive Item Info ===").formatted(Formatting.GRAY));
                            player.sendMessage(Text.literal("Owner: ").append(Text.literal(owner).formatted(Formatting.GOLD)));
                            player.sendMessage(Text.literal("Required Tag: ").append(Text.literal(tag).formatted(Formatting.AQUA)));
                            player.sendMessage(Text.literal("Tag Visible in Tooltip: ").append(Text.literal(String.valueOf(visible)).formatted(Formatting.GREEN)));
                            return 1;
                        })
                )

                .then(CommandManager.literal("givefake")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                                    if (player == null) return 0;

                                    ItemStackArgument itemArg = ItemStackArgumentType.getItemStackArgument(ctx, "item");
                                    ItemStack baseStack = itemArg.createStack(1, false);

                                    ItemStack exclusiveStack = ExclusiveItemUtil.createExclusiveItemOwnedBySomebodyElse(baseStack.getItem());
                                    player.getInventory().offerOrDrop(exclusiveStack);

                                    player.sendMessage(Text.literal("Given exclusive item owned by SomebodyElse")
                                            .formatted(Formatting.AQUA), false);
                                    return 1;
                                })
                        )
                )
                .then(CommandManager.literal("stored")
                        .executes(ctx -> {
                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                if (player == null) return 0;

                                List<ItemStack> stored = ExclusiveItemStorage.get(player);
                                if (stored.isEmpty()) {
                                    player.sendMessage(Text.literal("You have no stored exclusive items.")
                                            .formatted(Formatting.YELLOW), false);
                                    return 1;
                                }

                                player.sendMessage(Text.literal("=== Stored Exclusive Items ===").formatted(Formatting.GRAY), false);

                                for (int i = 0; i < stored.size(); i++) {
                                    ItemStack stack = stored.get(i);
                                    Text line = Text.literal("[" + i + "] ")
                                            .append(stack.getName().copy().formatted(Formatting.AQUA))
                                            .setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack))));
                                    player.sendMessage(line, false);
                                }

                                return 1;
                            }
                        )
                )
                .then(CommandManager.literal("overrideowner")
                        .requires(source -> source.hasPermissionLevel(2)) // op only
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(ctx -> {
                                    ServerPlayerEntity sender = ctx.getSource().getPlayerOrThrow();
                                    ServerPlayerEntity newOwner = EntityArgumentType.getPlayer(ctx, "target");
                                    ItemStack stack = sender.getMainHandStack();

                                    if (stack.isEmpty()) {
                                        sender.sendMessage(Text.literal("You must be holding an item to override.").formatted(Formatting.RED), false);
                                        return 0;
                                    }

                                    NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
                                    NbtCompound nbt = component != null ? component.copyNbt() : new NbtCompound();

                                    nbt.putBoolean("ExclusiveItem", true);
                                    nbt.putUuid("exclusiveOwner", newOwner.getUuid());
                                    nbt.putString("exclusiveOwnerName", newOwner.getName().getString());
                                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                                    // Optional: add it to the new owner's storage if they own it now
                                    if (ExclusiveItemUtil.isOwner(stack, newOwner)) {
                                        ExclusiveItemStorage.add(newOwner, stack);
                                    }

                                    sender.sendMessage(Text.literal("Exclusive item ownership overridden to ")
                                            .append(Text.literal(newOwner.getName().getString()).formatted(Formatting.AQUA)), false);
                                    return 1;
                                })
                        )
                )
                .then(CommandManager.literal("addall")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayer();
                            if (player == null) return 0;

                            int updated = 0;

                            for (int i = 0; i < player.getInventory().size(); i++) {
                                ItemStack stack = player.getInventory().getStack(i);
                                if (stack.isEmpty()) continue;

                                NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
                                NbtCompound nbt = component != null ? component.copyNbt() : new NbtCompound();

                                if (!nbt.getBoolean("ExclusiveItem")) {
                                    nbt.putBoolean("ExclusiveItem", true);
                                    stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                                }

                                ExclusiveItemUtil.bindToPlayer(stack, player);
                                updated++;
                            }

                            player.sendMessage(Text.literal("Tagged " + updated + " item(s) in your inventory as exclusive.")
                                    .formatted(Formatting.GREEN), false);
                            return 1;
                        })
                )

                .then(CommandManager.literal("givefromstorage")
                        .then(CommandManager.argument("index", IntegerArgumentType.integer(0))
                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                                    if (player == null) return 0;

                                    int index = IntegerArgumentType.getInteger(ctx, "index");
                                    List<ItemStack> stored = ExclusiveItemStorage.get(player);

                                    if (index < 0 || index >= stored.size()) {
                                        player.sendMessage(Text.literal("Invalid index.").formatted(Formatting.RED), false);
                                        return 1;
                                    }

                                    ItemStack copy = stored.get(index).copy();
                                    player.getInventory().offerOrDrop(copy);

                                    player.sendMessage(Text.literal("Given item from storage: ")
                                            .append(copy.toHoverableText())
                                            .formatted(Formatting.AQUA), false);
                                    return 1;
                                }))

                ));

    }

    public static boolean isBypassing(ServerPlayerEntity player) {
        return devBypass.contains(player.getUuid());
    }

    public static void clearDevBypass() {
        devBypass.clear();
    }

    public static boolean isBypassing(UUID uuid) {
        return devBypass.contains(uuid);
    }
}
