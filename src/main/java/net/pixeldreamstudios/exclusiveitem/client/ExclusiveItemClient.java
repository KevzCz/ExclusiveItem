package net.pixeldreamstudios.exclusiveitem.client;

import net.fabricmc.api.ClientModInitializer;
import net.pixeldreamstudios.exclusiveitem.network.ClientNetwork;

public class ExclusiveItemClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        ClientNetwork.register();
    }
}
