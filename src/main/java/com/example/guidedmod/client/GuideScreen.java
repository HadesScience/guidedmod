package com.example.guidedmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.gui.GuiGraphics;

public class GuideScreen extends Screen {
    private final Screen parent;
    private final net.minecraft.client.renderer.PanoramaRenderer panorama = new net.minecraft.client.renderer.PanoramaRenderer(net.minecraft.client.gui.screens.TitleScreen.CUBE_MAP);
    private int originalBlur;
    private boolean hasCapturedBlur = false;

    public GuideScreen(Screen parent) {
        super(Component.literal("Mode Selection"));
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
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Draw the rotating panorama background
        this.panorama.render(guiGraphics, mouseX, mouseY, partialTick, 1.0F);

        // Draw custom overlay without blur (semi-transparent black mask)
        guiGraphics.fill(0, 0, this.width, this.height, 0x80000000);

        // Main screen title
        guiGraphics.drawCenteredString(this.font, com.example.guidedmod.config.TextConfig.get("title", "★ 游戏启动引导 / Game Mode Setup ★"), this.width / 2, this.height / 2 - 100, 0xFFF1C40F);
        guiGraphics.drawCenteredString(this.font, com.example.guidedmod.config.TextConfig.get("subtitle", "请选择您要启动的模式。完成后游戏会自动关闭，请手动重启应用更改。"), this.width / 2, this.height / 2 - 85, 0xFFAAAAAA);

        // Setup layout coordinates
        int cardWidth = 140;
        int cardHeight = 160;
        int spacing = 30;

        int leftCardX = this.width / 2 - cardWidth - spacing / 2;
        int rightCardX = this.width / 2 + spacing / 2;
        int cardY = this.height / 2 - cardHeight / 2 + 10;

        // Hover checking
        boolean leftHovered = mouseX >= leftCardX && mouseX < leftCardX + cardWidth && mouseY >= cardY && mouseY < cardY + cardHeight;
        boolean rightHovered = mouseX >= rightCardX && mouseX < rightCardX + cardWidth && mouseY >= cardY && mouseY < cardY + cardHeight;

        // Draw backgrounds of cards
        int leftBgColor = leftHovered ? 0xDD1E2D24 : 0xBB18181A; // tinted dark green when hovered
        int rightBgColor = rightHovered ? 0xDD2B1D30 : 0xBB18181A; // tinted dark purple when hovered

        guiGraphics.fill(leftCardX, cardY, leftCardX + cardWidth, cardY + cardHeight, leftBgColor);
        guiGraphics.fill(rightCardX, cardY, rightCardX + cardWidth, cardY + cardHeight, rightBgColor);

        // Draw borders
        int leftBorderColor = leftHovered ? 0xFF2ECC71 : 0xFF3A3A3F;
        int rightBorderColor = rightHovered ? 0xFFA832E2 : 0xFF3A3A3F;

        guiGraphics.renderOutline(leftCardX, cardY, cardWidth, cardHeight, leftBorderColor);
        guiGraphics.renderOutline(rightCardX, cardY, cardWidth, cardHeight, rightBorderColor);

        int descColor = 0xFFCCCCCC;
        int descHoverColor = 0xFFFFFFFF;

        // Draw Left Card Content (Vanilla)
        int leftTitleColor = leftHovered ? 0xFF2ECC71 : 0xFFFFFFFF;
        guiGraphics.drawCenteredString(this.font, com.example.guidedmod.config.TextConfig.get("vanilla_title", "原版模式"), leftCardX + cardWidth / 2, cardY + 15, leftTitleColor);
        guiGraphics.drawCenteredString(this.font, com.example.guidedmod.config.TextConfig.get("vanilla_subtitle", "Vanilla Mode"), leftCardX + cardWidth / 2, cardY + 27, 0x88AAAAAA);

        int leftTextColor = leftHovered ? descHoverColor : descColor;
        int leftYOffset = 60;
        for (int i = 1; i <= 6; i++) {
            String desc = com.example.guidedmod.config.TextConfig.get("vanilla_desc" + i, "");
            if (i <= 3 && desc.isEmpty()) {
                // Keep default compatibility fallback
                if (i == 1) desc = "禁用指定模组";
                if (i == 2) desc = "保持原版纯净体验";
                if (i == 3) desc = "适合原版服务器";
            }
            if (!desc.trim().isEmpty()) {
                guiGraphics.drawCenteredString(this.font, desc, leftCardX + cardWidth / 2, cardY + leftYOffset, leftTextColor);
                leftYOffset += 15;
            }
        }

        // Draw Right Card Content (Overhaul)
        int rightTitleColor = rightHovered ? 0xFFD780FF : 0xFFFFFFFF;
        guiGraphics.drawCenteredString(this.font, com.example.guidedmod.config.TextConfig.get("overhaul_title", "大改模式"), rightCardX + cardWidth / 2, cardY + 15, rightTitleColor);
        guiGraphics.drawCenteredString(this.font, com.example.guidedmod.config.TextConfig.get("overhaul_subtitle", "Overhaul Mode"), rightCardX + cardWidth / 2, cardY + 27, 0x88AAAAAA);

        int rightTextColor = rightHovered ? descHoverColor : descColor;
        int rightYOffset = 60;
        for (int i = 1; i <= 6; i++) {
            String desc = com.example.guidedmod.config.TextConfig.get("overhaul_desc" + i, "");
            if (i <= 3 && desc.isEmpty()) {
                // Keep default compatibility fallback
                if (i == 1) desc = "启用所有模组";
                if (i == 2) desc = "加载完整模组体验";
                if (i == 3) desc = "包含所有大改模组";
            }
            if (!desc.trim().isEmpty()) {
                guiGraphics.drawCenteredString(this.font, desc, rightCardX + cardWidth / 2, cardY + rightYOffset, rightTextColor);
                rightYOffset += 15;
            }
        }

        // Draw footnote if one_time_only is true
        boolean oneTimeOnly = Boolean.parseBoolean(com.example.guidedmod.config.TextConfig.get("one_time_only", "true"));
        if (oneTimeOnly) {
            String note = com.example.guidedmod.config.TextConfig.get("one_time_note", "* 注意：切换并重启后将隐藏主页的调整按钮，您将无法再次更改。");
            if (!note.trim().isEmpty()) {
                guiGraphics.drawCenteredString(this.font, note, this.width / 2, this.height / 2 + 110, 0xFFE74C3C); // red color for caution
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            int cardWidth = 140;
            int cardHeight = 160;
            int spacing = 30;

            int leftCardX = this.width / 2 - cardWidth - spacing / 2;
            int rightCardX = this.width / 2 + spacing / 2;
            int cardY = this.height / 2 - cardHeight / 2 + 10;

            if (mouseX >= leftCardX && mouseX < leftCardX + cardWidth && mouseY >= cardY && mouseY < cardY + cardHeight) {
                playClickSound();
                selectMode(false);
                return true;
            }

            if (mouseX >= rightCardX && mouseX < rightCardX + cardWidth && mouseY >= cardY && mouseY < cardY + cardHeight) {
                playClickSound();
                selectMode(true);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(
            SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F)
        );
    }

    public Screen getParentScreen() {
        return this.parent;
    }

    private void selectMode(boolean overhaulMode) {
        if (!com.example.guidedmod.config.ModManager.hasOptionalTargets()) {
            Minecraft.getInstance().setScreen(new ConfirmModeScreen(this, overhaulMode, new java.util.ArrayList<>()));
        } else {
            Minecraft.getInstance().setScreen(new OptionalModsScreen(this, overhaulMode));
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false; // Prevent closing via ESC to force selection
    }
}
