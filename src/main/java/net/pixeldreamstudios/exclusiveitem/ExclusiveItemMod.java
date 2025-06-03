package net.pixeldreamstudios.exclusiveitem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.pixeldreamstudios.exclusiveitem.config.ExclusiveItemConfig;
import net.pixeldreamstudios.exclusiveitem.item.ModItemGroups;
import net.pixeldreamstudios.exclusiveitem.item.ModItems;
import net.pixeldreamstudios.exclusiveitem.network.ServerNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExclusiveItemMod implements ModInitializer {
	public static final String MOD_ID = "exclusive-item";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Exclusive Item mod initialized.");
		ExclusiveItemConfig.INSTANCE.load();
		ModItems.registerItems();
		ModItemGroups.registerItemGroups();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			ExclusiveItemCommands.register(dispatcher, registryAccess);
		});
		ServerNetwork.register();
		CooldownHandler.register();
	}
}
