package com.example.guidedmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;

public class RestartScreen extends Screen {
    private final String message;
    private int tickCounter = 0;
    private static final int DELAY_TICKS = 100; // 5 seconds (20 ticks/sec)
    private final net.minecraft.client.renderer.PanoramaRenderer panorama = new net.minecraft.client.renderer.PanoramaRenderer(net.minecraft.client.gui.screens.TitleScreen.CUBE_MAP);
    private int originalBlur;
    private boolean hasCapturedBlur = false;

    public RestartScreen(String message) {
        super(Component.literal("Restarting"));
        this.message = message;
    }

    @Override
    protected void init() {
        super.init();
        if (!this.hasCapturedBlur) {
            this.originalBlur = Minecraft.getInstance().options.menuBackgroundBlurriness().get();
            this.hasCapturedBlur = true;
        }
        Minecraft.getInstance().options.menuBackgroundBlurriness().set(0);
    }

    @Override
    public void tick() {
        super.tick();
        tickCounter++;
        if (tickCounter >= DELAY_TICKS) {
            if (this.hasCapturedBlur) {
                Minecraft.getInstance().options.menuBackgroundBlurriness().set(this.originalBlur);
            }
            Minecraft.getInstance().stop();
        }
    }

    @Override
    public void removed() {
        if (this.hasCapturedBlur) {
            Minecraft.getInstance().options.menuBackgroundBlurriness().set(this.originalBlur);
        }
        super.removed();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw the rotating panorama background
        this.panorama.render(guiGraphics, mouseX, mouseY, partialTick, 1.0F);

        // Draw custom overlay without blur (semi-transparent black mask)
        guiGraphics.fill(0, 0, this.width, this.height, 0x80000000);

        // Success message
        guiGraphics.drawCenteredString(this.font, this.message, this.width / 2, this.height / 2 - 20, 0xFF2ECC71);

        // Restart warning/instruction
        guiGraphics.drawCenteredString(this.font, com.example.guidedmod.config.TextConfig.get("restart_info", "请手动重新启动游戏以使更改生效！"), this.width / 2, this.height / 2, 0xFFFFFFFF);

        // Countdown timer
        int remainingSeconds = Math.max(0, (DELAY_TICKS - tickCounter) / 20 + 1);
        String formatStr = com.example.guidedmod.config.TextConfig.get("restart_closing_format", "游戏将在 %d 秒后自动关闭... (Closing in %ds...)");
        guiGraphics.drawCenteredString(this.font, String.format(formatStr, remainingSeconds, remainingSeconds), this.width / 2, this.height / 2 + 25, 0xAAAAAA);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // Prevent closing via ESC to ensure restart action occurs
    }
}
