package com.example.guidedmod;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.example.guidedmod.config.ModManager;
import com.example.guidedmod.config.TextConfig;

@Mod(GuidedMod.MOD_ID)
public class GuidedMod {
    public static final String MOD_ID = "guidedmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public GuidedMod(IEventBus modEventBus) {
        LOGGER.info("Guided Mod Initialized!");
        try {
            // Proactively create/load configurations on mod startup
            TextConfig.load();
            ModManager.readTargetMods();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize configuration files", e);
        }
    }
}
