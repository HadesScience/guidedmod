package com.example.guidedmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;

public class InfoScreen extends Screen {
    private final String message;
    private final Screen parent;
    private final net.minecraft.client.renderer.PanoramaRenderer panorama = new net.minecraft.client.renderer.PanoramaRenderer(net.minecraft.client.gui.screens.TitleScreen.CUBE_MAP);
    private int originalBlur;
    private boolean hasCapturedBlur = false;

    public InfoScreen(String message, Screen parent) {
        super(Component.literal("Info"));
        this.message = message;
        this.parent = parent;
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
        this.addRenderableWidget(Button.builder(Component.literal(com.example.guidedmod.config.TextConfig.get("info_confirm_button", "确定 (Confirm)")), button -> {
            Minecraft.getInstance().setScreen(this.parent);
        }).bounds(this.width / 2 - buttonWidth / 2, this.height / 2 + 20, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw the rotating panorama background
        this.panorama.render(guiGraphics, mouseX, mouseY, partialTick, 1.0F);

        // Draw custom overlay without blur (semi-transparent black mask)
        guiGraphics.fill(0, 0, this.width, this.height, 0x80000000);

        // Render message
        guiGraphics.drawCenteredString(this.font, this.message, this.width / 2, this.height / 2 - 10, 0xFFFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true; // Esc can close this screen and return to parent
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }
}
