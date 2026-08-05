package ru.obabok.liquidesp.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class LiquidespClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(LavaEsp::render);
    }
}
