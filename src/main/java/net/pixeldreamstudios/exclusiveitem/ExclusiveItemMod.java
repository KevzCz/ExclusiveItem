package net.pixeldreamstudios.exclusiveitem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.pixeldreamstudios.exclusiveitem.config.ExclusiveItemConfig;
import net.pixeldreamstudios.exclusiveitem.network.ClaimExclusiveItemPayload;
import net.pixeldreamstudios.exclusiveitem.network.RequestExclusiveItemsPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExclusiveItemMod implements ModInitializer {
	public static final String MOD_ID = "exclusive-item";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Exclusive Item mod initialized.");

		// Load config
		ExclusiveItemConfig.INSTANCE.load();

		// Register commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			ExclusiveItemCommands.register(dispatcher, registryAccess);
		});

		// Reset dev bypasses on shutdown
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			ExclusiveItemCommands.clearDevBypass();
		});

		// Register payload codecs
		PayloadTypeRegistry.playC2S().register(RequestExclusiveItemsPayload.ID, RequestExclusiveItemsPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ClaimExclusiveItemPayload.ID, ClaimExclusiveItemPayload.CODEC);

		// Handle item sync request
		ServerPlayNetworking.registerGlobalReceiver(RequestExclusiveItemsPayload.ID, (payload, context) -> {
			context.player().server.execute(() -> {
				ExclusiveItemStorage.syncToClient(context.player());
			});
		});

		// Handle item claim
		ServerPlayNetworking.registerGlobalReceiver(ClaimExclusiveItemPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			context.player().server.execute(() -> {
				ItemStack claimed;
				try {
					RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, player.getRegistryManager());
					claimed = ItemStack.CODEC.parse(ops, payload.itemNbt()).result().orElseThrow();
				} catch (Exception e) {
					player.sendMessage(Text.literal("§cFailed to decode claimed item."), false);
					e.printStackTrace();
					return;
				}

				if (!ExclusiveItemUtil.isExclusiveItem(claimed)) {
					player.sendMessage(Text.literal("§cInvalid exclusive item."), false);
					return;
				}

				if (!ExclusiveItemUtil.isOwner(claimed, player)) {
					player.sendMessage(Text.literal("§cYou do not own this item."), false);
					return;
				}

				// Check required items
				boolean allItemsPresent = true;
				List<ItemStack> requiredItems = ExclusiveItemConfig.INSTANCE.getRequiredItemStacks();
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
					player.sendMessage(Text.literal("§cMissing one or more required items."), false);
					return;
				}

				// Check XP requirement
				int xpRequired = ExclusiveItemConfig.INSTANCE.requiredXpLevels;
				if (player.experienceLevel < xpRequired) {
					player.sendMessage(Text.literal("§cYou need at least " + xpRequired + " XP levels."), false);
					return;
				}

				// Consume required items
				for (ItemStack required : requiredItems) {
					for (int i = 0; i < player.getInventory().size(); i++) {
						ItemStack invStack = player.getInventory().getStack(i);
						if (ItemStack.areItemsAndComponentsEqual(invStack, required) && invStack.getCount() >= required.getCount()) {
							invStack.decrement(required.getCount());
							break;
						}
					}
				}

				// Consume XP
				if (xpRequired > 0) {
					player.addExperienceLevels(-xpRequired);
				}

				// Give claimed item
				if (!player.getInventory().insertStack(claimed.copy())) {
					player.dropItem(claimed.copy(), false);
				}

				player.sendMessage(Text.literal("§aYou successfully claimed: " + claimed.getName().getString()), false);

				// Sync remaining exclusive items
				ExclusiveItemStorage.syncToClient(player);
			});
		});
	}
}
