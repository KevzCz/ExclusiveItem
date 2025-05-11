package net.pixeldreamstudios.exclusiveitem.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.pixeldreamstudios.exclusiveitem.config.ExclusiveItemConfig;
import net.pixeldreamstudios.exclusiveitem.network.ModPackets;
import net.pixeldreamstudios.exclusiveitem.network.RequestExclusiveItemsPayload;
import net.pixeldreamstudios.exclusiveitem.network.SyncExclusiveItemsPayload;
import org.lwjgl.glfw.GLFW;

public class ExclusiveItemClient implements ClientModInitializer {
    public static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        if (!ExclusiveItemConfig.INSTANCE.guiEnabled) return;

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.exclusive_item.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.exclusive_item"
        ));
        PayloadTypeRegistry.playS2C().register(SyncExclusiveItemsPayload.ID, SyncExclusiveItemsPayload.CODEC);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                if (client.player != null && client.world != null && ClientPlayNetworking.canSend(ModPackets.REQUEST_EXCLUSIVE_ITEMS)) {
                    client.setScreen(new ReclaimScreen());
                    ClientPlayNetworking.send(new RequestExclusiveItemsPayload());
                    }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SyncExclusiveItemsPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientExclusiveItemStorage.handleSync(payload.data());
            });
        });
    }
}
