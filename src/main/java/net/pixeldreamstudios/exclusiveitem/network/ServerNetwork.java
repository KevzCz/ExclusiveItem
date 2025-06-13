package net.pixeldreamstudios.exclusiveitem.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemCommands;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemStorage;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemUtil;
import net.pixeldreamstudios.exclusiveitem.config.ExclusiveItemConfig;
import net.pixeldreamstudios.exclusiveitem.item.ModItems;

import java.util.Collections;
import java.util.List;

public class ServerNetwork {
    public static void register()
    {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            ExclusiveItemCommands.clearDevBypass();
        });

        PayloadTypeRegistry.playC2S().register(RequestExclusiveItemsPayload.ID, RequestExclusiveItemsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClaimExclusiveItemPayload.ID, ClaimExclusiveItemPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestExclusiveItemsPayload.ID, (payload, context) -> {
            context.player().server.execute(() -> {
                ExclusiveItemStorage.syncToClient(context.player());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ClaimExclusiveItemPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.player().server.execute(() -> {
                ItemStack claimed;
                try {
                    RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, player.getRegistryManager());
                    claimed = ItemStack.CODEC.parse(ops, payload.itemNbt()).result().orElseThrow();
                } catch (Exception e) {
                    player.sendMessage(
                            Text.translatable("exclusiveitem.message.decode_failed")
                                    .formatted(Formatting.RED),
                            false
                    );
                    e.printStackTrace();
                    return;
                }

                if (!ExclusiveItemUtil.isExclusiveItem(claimed)) {
                    player.sendMessage(
                            Text.translatable("exclusiveitem.message.invalid_exclusive_item")
                                    .formatted(Formatting.RED),
                            false
                    );
                    return;
                }

                if (!ExclusiveItemUtil.isOwner(claimed, player)) {
                    player.sendMessage(
                            Text.translatable("exclusiveitem.message.not_owner_item")
                                    .formatted(Formatting.RED),
                            false
                    );
                    return;
                }

                List<ItemStack> requiredItems;
                int xpRequired;

                if (claimed.isOf(ModItems.BOOK_ITEM)) {
                    ItemStack vanillaBook = new ItemStack(Items.BOOK);
                    vanillaBook.setCount(1);
                    requiredItems = List.of(vanillaBook);
                    xpRequired = 10;
                } else {
                    requiredItems = ExclusiveItemConfig.INSTANCE.getRequiredItemStacks();
                    xpRequired = ExclusiveItemConfig.INSTANCE.requiredXpLevels;
                }


                boolean allItemsPresent = true;
                for (ItemStack required : requiredItems) {
                    boolean matched = false;
                    for (int i = 0; i < player.getInventory().size(); i++) {
                        ItemStack invStack = player.getInventory().getStack(i);
                        if (ItemStack.areItemsAndComponentsEqual(invStack, required) && invStack.getCount() >= required.getCount()) {
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        allItemsPresent = false;
                        break;
                    }
                }

                if (!allItemsPresent) {
                    player.sendMessage(
                            Text.translatable("exclusiveitem.message.missing_required_items")
                                    .formatted(Formatting.RED),
                            false
                    );
                    return;
                }

                if (player.experienceLevel < xpRequired) {
                    player.sendMessage(
                            Text.translatable("exclusiveitem.message.insufficient_xp", xpRequired)
                                    .formatted(Formatting.RED),
                            false
                    );
                    return;
                }

                for (ItemStack required : requiredItems) {
                    for (int i = 0; i < player.getInventory().size(); i++) {
                        ItemStack invStack = player.getInventory().getStack(i);
                        if (ItemStack.areItemsAndComponentsEqual(invStack, required) && invStack.getCount() >= required.getCount()) {
                            invStack.decrement(required.getCount());
                            break;
                        }
                    }
                }

                if (xpRequired > 0) {
                    player.addExperienceLevels(-xpRequired);
                }

                if (!player.getInventory().insertStack(claimed.copy())) {
                    player.dropItem(claimed.copy(), false);
                }

                ExclusiveItemStorage.syncToClient(player);
            });
        });
    }
}
