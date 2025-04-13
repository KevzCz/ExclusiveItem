package net.pixeldreamstudios.exclusiveitem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExclusiveItemMod implements ModInitializer {
	public static final String MOD_ID = "exclusive-item";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Exclusive Item mod initialized.");
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			ExclusiveItemCommands.register(dispatcher, registryAccess);
		});
		// Reset devBypass on server stop (or world unload)
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			ExclusiveItemCommands.clearDevBypass();
		});
	}
}
