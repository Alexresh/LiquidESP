package ru.obabok.liquidesp.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public class LiquidespClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LevelExtractionEvents.END_EXTRACTION.register(LavaEsp::collect);
        LevelRenderEvents.END_MAIN.register(LavaEsp::render);
    }
}
