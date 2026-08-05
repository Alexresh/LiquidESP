package ru.obabok.liquidesp.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class LavaEspConfig {
    public int scanRadius = 24;
    public int updateIntervalMs = 500;
    public float markerSize = 1f;
    public boolean enabled = true;

    public int markerColor = 0x59FF4D00;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("lava-esp.json");

    private static LavaEspConfig INSTANCE;

    public static LavaEspConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                INSTANCE = GSON.fromJson(json, LavaEspConfig.class);
            }
        } catch (Exception e) {
            System.err.println("[LavaESP] Failed to load config: " + e.getMessage());
        }
        if (INSTANCE == null) INSTANCE = new LavaEspConfig();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(get()));
        } catch (Exception e) {
            System.err.println("[LavaESP] Failed to save config: " + e.getMessage());
        }
    }

    public float getColorR() { return ((markerColor >> 16) & 0xFF) / 255f; }
    public float getColorG() { return ((markerColor >> 8) & 0xFF) / 255f; }
    public float getColorB() { return (markerColor & 0xFF) / 255f; }
    public float getColorA() { return ((markerColor >> 24) & 0xFF) / 255f; }
}
