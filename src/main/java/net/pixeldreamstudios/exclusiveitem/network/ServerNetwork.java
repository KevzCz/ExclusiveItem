package net.pixeldreamstudios.exclusiveitem.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemCommands;
import net.pixeldreamstudios.exclusiveitem.ExclusiveItemStorage;

public class ServerNetwork {
    public static void register() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> ExclusiveItemCommands.clearDevBypass());

        PayloadTypeRegistry.playC2S().register(RequestExclusiveItemsPayload.ID, RequestExclusiveItemsPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClaimExclusiveItemPayload.ID, ClaimExclusiveItemPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestExclusiveItemsPayload.ID, (payload, context) ->
                context.player().server.execute(() ->
                        ExclusiveItemStorage.syncToClient(context.player())
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(ClaimExclusiveItemPayload.ID, (payload, context) ->
                context.player().server.execute(() ->
                        ExclusiveItemClaimHandler.handleClaimPayload(context.player(), payload)
                )
        );
    }
}
