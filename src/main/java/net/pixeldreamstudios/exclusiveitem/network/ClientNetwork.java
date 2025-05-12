package net.pixeldreamstudios.exclusiveitem.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.pixeldreamstudios.exclusiveitem.client.ClientExclusiveItemStorage;
import net.pixeldreamstudios.exclusiveitem.config.ExclusiveItemConfig;

public class ClientNetwork {
    public static void register(){
        if (!ExclusiveItemConfig.INSTANCE.guiEnabled) return;
        PayloadTypeRegistry.playS2C().register(SyncExclusiveItemsPayload.ID, SyncExclusiveItemsPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(SyncExclusiveItemsPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientExclusiveItemStorage.handleSync(payload.data());
            });
        });
    }
}
