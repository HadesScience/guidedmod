package com.example.guidedmod.client;

import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import com.example.guidedmod.GuidedMod;

@EventBusSubscriber(modid = GuidedMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ClientEvents {
    private static boolean hasShownThisSession = false;

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof TitleScreen && !hasShownThisSession) {
            hasShownThisSession = true;
            
            // Check if we should only show the screen once
            boolean showOnlyOnce = Boolean.parseBoolean(com.example.guidedmod.config.TextConfig.get("show_only_once", "true"));
            if (showOnlyOnce) {
                java.nio.file.Path flagPath = java.nio.file.Paths.get("config/guidedmod-configured.flag");
                if (!java.nio.file.Files.exists(flagPath)) {
                    event.setNewScreen(new GuideScreen(event.getNewScreen()));
                }
            } else {
                event.setNewScreen(new GuideScreen(event.getNewScreen()));
            }
        }
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            // Check if one_time_only is true. If true, and user has already configured the mod,
            // we do NOT show the Mode Select button on the main menu.
            boolean oneTimeOnly = Boolean.parseBoolean(com.example.guidedmod.config.TextConfig.get("one_time_only", "true"));
            java.nio.file.Path flagPath = java.nio.file.Paths.get("config/guidedmod-configured.flag");
            if (oneTimeOnly && java.nio.file.Files.exists(flagPath)) {
                return;
            }

            int x = 10;
            int y = 10;
            int width = 80;
            int height = 20;
            String btnText = com.example.guidedmod.config.TextConfig.get("main_menu_button", "模式调整");
            event.addListener(net.minecraft.client.gui.components.Button.builder(
                net.minecraft.network.chat.Component.literal(btnText),
                button -> {
                    net.minecraft.client.Minecraft.getInstance().setScreen(new GuideScreen(event.getScreen()));
                }
            ).bounds(x, y, width, height).build());
        }
    }
}
