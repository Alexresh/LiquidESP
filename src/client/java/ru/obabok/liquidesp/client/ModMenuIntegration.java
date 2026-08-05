package ru.obabok.liquidesp.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            LavaEspConfig config = LavaEspConfig.get();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("Lava ESP Config"))
                    .setSavingRunnable(LavaEspConfig::save);

            ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            general.addEntry(entryBuilder.startBooleanToggle(
                            Component.literal("Enabled"), config.enabled)
                    .setDefaultValue(true)
                    .setTooltip(Component.literal("Enable/disable Lava ESP overlay"))
                    .setSaveConsumer(val -> config.enabled = val)
                    .build());

            general.addEntry(entryBuilder.startIntSlider(
                            Component.literal("Scan Radius"), config.scanRadius, 8, 64)
                    .setDefaultValue(24)
                    .setTooltip(Component.literal("Block search radius (lower = better performance)"))
                    .setSaveConsumer(val -> config.scanRadius = val)
                    .build());

            general.addEntry(entryBuilder.startIntSlider(
                            Component.literal("Update Interval (ms)"), config.updateIntervalMs, 100, 2000)
                    .setDefaultValue(500)
                    .setTooltip(Component.literal("How often to rescan blocks"))
                    .setSaveConsumer(val -> config.updateIntervalMs = val)
                    .build());

            general.addEntry(entryBuilder.startFloatField(
                            Component.literal("Marker Size"), config.markerSize)
                    .setDefaultValue(0.98f)
                    .setMin(0.1f).setMax(1.0f)
                    .setSaveConsumer(val -> config.markerSize = val)
                    .build());

            general.addEntry(entryBuilder.startAlphaColorField(
                            Component.literal("Marker Color"), config.markerColor)
                    .setDefaultValue(0x59FF4D00)
                    .setAlphaMode(true) // ← Включает alpha-канал в пикере!
                    .setTooltip(Component.literal("Overlay color with transparency"))
                    .setSaveConsumer(val -> config.markerColor = val)
                    .build());

            return builder.build();
        };
    }
}
