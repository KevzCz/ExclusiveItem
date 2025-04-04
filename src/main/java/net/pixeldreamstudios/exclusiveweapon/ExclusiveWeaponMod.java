package net.pixeldreamstudios.exclusiveweapon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExclusiveWeaponMod implements ModInitializer {
	public static final String MOD_ID = "exclusive-weapon";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Exclusive Weapon mod initialized.");
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
			ExclusiveWeaponCommands.register(dispatcher);
		});
	}
}
