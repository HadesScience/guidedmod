package com.example.guidedmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;

public class ConfirmModeScreen extends Screen {
    private final Screen parent;
    private final boolean targetOverhaulMode;
    private final java.util.List<OptionalModsScreen.ModItem> optionalItems;
    private final net.minecraft.client.renderer.PanoramaRenderer panorama = new net.minecraft.client.renderer.PanoramaRenderer(net.minecraft.client.gui.screens.TitleScreen.CUBE_MAP);
    private int originalBlur;
    private boolean hasCapturedBlur = false;

    public ConfirmModeScreen(Screen parent, boolean targetOverhaulMode) {
        this(parent, targetOverhaulMode, new java.util.ArrayList<>());
    }

    public ConfirmModeScreen(Screen parent, boolean targetOverhaulMode, java.util.List<OptionalModsScreen.ModItem> optionalItems) {
        super(Component.literal("Confirm Mode Switch"));
        this.parent = parent;
        this.targetOverhaulMode = targetOverhaulMode;
        this.optionalItems = optionalItems;
    }

    @Override
    public void removed() {
        if (this.hasCapturedBlur) {
            Minecraft.getInstance().options.menuBackgroundBlurriness().set(this.originalBlur);
        }
        super.removed();
    }

    @Override
    protected void init() {
        super.init();
        if (!this.hasCapturedBlur) {
            this.originalBlur = Minecraft.getInstance().options.menuBackgroundBlurriness().get();
            this.hasCapturedBlur = true;
        }
        Minecraft.getInstance().options.menuBackgroundBlurriness().set(0);

        int buttonWidth = 120;
        int buttonHeight = 20;
        int spacing = 20;

        // Left button: Cancel (Recommended)
        String cancelText = "★ " + com.example.guidedmod.config.TextConfig.get("cancel_switch_button", "取消") + " (推荐) ★";
        this.addRenderableWidget(Button.builder(Component.literal(cancelText).withStyle(net.minecraft.ChatFormatting.GREEN), button -> {
            Minecraft.getInstance().setScreen(this.parent);
        }).bounds(this.width / 2 - buttonWidth - spacing / 2, this.height / 2 + 40, buttonWidth, buttonHeight).build());

        // Right button: Confirm
        String confirmText = com.example.guidedmod.config.TextConfig.get("confirm_switch_button", "确认切换");
        this.addRenderableWidget(Button.builder(Component.literal(confirmText).withStyle(net.minecraft.ChatFormatting.RED), button -> {
            executeSwitch();
        }).bounds(this.width / 2 + spacing / 2, this.height / 2 + 40, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.panorama.render(guiGraphics, mouseX, mouseY, partialTick, 1.0F);
        guiGraphics.fill(0, 0, this.width, this.height, 0x99000000); // slightly darker dark overlay

        // Draw Warning Title
        String warningTitle = com.example.guidedmod.config.TextConfig.get("warning_title", "⚠️ 安全警告 / Safety Warning");
        guiGraphics.drawCenteredString(this.font, warningTitle, this.width / 2, this.height / 2 - 50, 0xFFE74C3C); // red text for warning

        // Draw Warning Lines in red
        int yOffset = -20;
        int redColor = 0xFFE74C3C; // Red color for safety warning
        for (int i = 1; i <= 6; i++) {
            String line = com.example.guidedmod.config.TextConfig.get("warning_line" + i, "");
            if (i <= 3 && line.isEmpty()) {
                // Keep default compatibility fallback
                if (i == 1) line = "检测到模式切换请求。切换模式可能会导致您的";
                if (i == 2) line = "已有存档发生不可逆的损坏或物品/方块丢失！";
                if (i == 3) line = "建议在继续前备份您的存档。是否确认切换？";
            }
            if (!line.trim().isEmpty()) {
                guiGraphics.drawCenteredString(this.font, line, this.width / 2, this.height / 2 + yOffset, redColor);
                yOffset += 15;
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void executeSwitch() {
        boolean changed = com.example.guidedmod.config.ModManager.applyMode(this.targetOverhaulMode);
        boolean optionalChanged = com.example.guidedmod.config.ModManager.applyOptionalToggles(this.optionalItems);
        
        // Write flag
        try {
            java.nio.file.Path flagPath = java.nio.file.Paths.get("config/guidedmod-configured.flag");
            java.nio.file.Files.createDirectories(flagPath.getParent());
            java.nio.file.Files.writeString(flagPath, "configured=true");
        } catch (java.io.IOException e) {
            com.mojang.logging.LogUtils.getLogger().error("Failed to write configured flag", e);
        }

        // Get parent's parent screen (which is TitleScreen)
        Screen titleScreen = null;
        Screen current = this.parent;
        if (current instanceof OptionalModsScreen) {
            current = ((OptionalModsScreen) current).getParentScreen();
        }
        if (current instanceof GuideScreen) {
            titleScreen = ((GuideScreen) current).getParentScreen();
        }
        if (titleScreen == null) {
            titleScreen = this.parent;
        }

        if (changed || optionalChanged) {
            String successMsg = this.targetOverhaulMode ? 
                com.example.guidedmod.config.TextConfig.get("restart_success_overhaul", "已成功切换到大改模式！") :
                com.example.guidedmod.config.TextConfig.get("restart_success_vanilla", "已成功切换到原版模式！");
            Minecraft.getInstance().setScreen(new RestartScreen(successMsg));
        } else {
            String alreadyMsg = this.targetOverhaulMode ?
                com.example.guidedmod.config.TextConfig.get("info_already_overhaul", "当前已是大改模式，无需更改。") :
                com.example.guidedmod.config.TextConfig.get("info_already_vanilla", "当前已是原版模式，无需更改。");
            Minecraft.getInstance().setScreen(new InfoScreen(alreadyMsg, titleScreen));
        }
    }
}
