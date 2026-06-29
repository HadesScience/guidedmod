package com.example.guidedmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

public class OptionalModsScreen extends Screen {
    private final Screen parent;
    private final boolean targetOverhaulMode;
    private final net.minecraft.client.renderer.PanoramaRenderer panorama = new net.minecraft.client.renderer.PanoramaRenderer(net.minecraft.client.gui.screens.TitleScreen.CUBE_MAP);
    private int originalBlur;
    private boolean hasCapturedBlur = false;

    private List<ModItem> optionalItems;
    private int startIndex = 0;

    public static class ModItem {
        public final String targetName;
        public final String jarFileName;
        public boolean enabled;
        public final boolean originalEnabled;

        public ModItem(String targetName, String jarFileName, boolean enabled) {
            this.targetName = targetName;
            this.jarFileName = jarFileName;
            this.enabled = enabled;
            this.originalEnabled = enabled;
        }
    }

    public OptionalModsScreen(Screen parent, boolean targetOverhaulMode) {
        super(Component.literal("Optional Mod Selection"));
        this.parent = parent;
        this.targetOverhaulMode = targetOverhaulMode;
        this.optionalItems = com.example.guidedmod.config.ModManager.getOptionalModItems();
    }

    public Screen getParentScreen() {
        return this.parent;
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

        int buttonWidth = 110;
        int buttonHeight = 20;
        int spacing = 20;

        // Next Button
        String nextText = com.example.guidedmod.config.TextConfig.get("optional_next_button", "下一步 (Next)");
        this.addRenderableWidget(Button.builder(Component.literal(nextText).withStyle(net.minecraft.ChatFormatting.GREEN), button -> {
            Minecraft.getInstance().setScreen(new ConfirmModeScreen(this.parent, this.targetOverhaulMode, this.optionalItems));
        }).bounds(this.width / 2 - buttonWidth - spacing / 2, this.height / 2 + 95, buttonWidth, buttonHeight).build());

        // Back Button
        String backText = com.example.guidedmod.config.TextConfig.get("optional_back_button", "返回 (Back)");
        this.addRenderableWidget(Button.builder(Component.literal(backText), button -> {
            Minecraft.getInstance().setScreen(this.parent);
        }).bounds(this.width / 2 + spacing / 2, this.height / 2 + 95, buttonWidth, buttonHeight).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.panorama.render(guiGraphics, mouseX, mouseY, partialTick, 1.0F);
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000); // dark overlay

        // Title & Subtitle
        String titleText = com.example.guidedmod.config.TextConfig.get("optional_title", "选装模组开关");
        String subtitleText = com.example.guidedmod.config.TextConfig.get("optional_subtitle", "请选择您需要随模式一同开启的附加模组：");
        guiGraphics.drawCenteredString(this.font, titleText, this.width / 2, this.height / 2 - 110, 0xFF5DADE2);
        guiGraphics.drawCenteredString(this.font, subtitleText, this.width / 2, this.height / 2 - 95, 0xFFAAAAAA);

        int listWidth = 320;
        int listHeight = 146; // 5 items * 24px + 4 * 6px spacing
        int listX = this.width / 2 - listWidth / 2;
        int startY = this.height / 2 - 70;

        // Draw List Background & Border
        guiGraphics.fill(listX - 5, startY - 5, listX + listWidth + 5, startY + listHeight + 5, 0x9918181A);
        guiGraphics.renderOutline(listX - 5, startY - 5, listWidth + 10, listHeight + 10, 0xFF3A3A3F);

        // Draw visible rows
        int visibleCount = Math.min(5, optionalItems.size() - startIndex);
        for (int i = 0; i < visibleCount; i++) {
            int itemIndex = startIndex + i;
            ModItem item = optionalItems.get(itemIndex);
            int itemY = startY + i * 30; // 24px height + 6px spacing

            // Row background
            guiGraphics.fill(listX, itemY, listX + listWidth, itemY + 24, 0x33FFFFFF);
            guiGraphics.renderOutline(listX, itemY, listWidth, 24, 0x33FFFFFF);

            // Mod name
            guiGraphics.drawString(this.font, item.targetName, listX + 8, itemY + 8, 0xFFFFFFFF, false);

            // Slider Switch Layout
            int switchX = listX + listWidth - 46;
            int switchY = itemY + 4;
            int switchW = 36;
            int switchH = 16;

            // Draw State Text to the left of the switch
            if (item.jarFileName == null) {
                guiGraphics.drawString(this.font, "缺失", switchX - 30, switchY + 4, 0xFFAAAAAA, false);
            } else if (item.enabled) {
                guiGraphics.drawString(this.font, "开启", switchX - 30, switchY + 4, 0xFF2ECC71, false);
            } else {
                guiGraphics.drawString(this.font, "关闭", switchX - 30, switchY + 4, 0xFF7F8C8D, false);
            }

            // Draw Switch Track and Knob
            if (item.jarFileName == null) {
                // Missing mod
                guiGraphics.fill(switchX, switchY, switchX + switchW, switchY + switchH, 0x44FFFFFF);
                guiGraphics.renderOutline(switchX, switchY, switchW, switchH, 0x33FFFFFF);
                int knobX = switchX + 2;
                guiGraphics.fill(knobX, switchY + 2, knobX + 12, switchY + 14, 0x55FFFFFF);
            } else if (item.enabled) {
                // Enabled (Green track, knob on the right)
                guiGraphics.fill(switchX, switchY, switchX + switchW, switchY + switchH, 0xFF2ECC71);
                guiGraphics.renderOutline(switchX, switchY, switchW, switchH, 0xFF27AE60);
                int knobX = switchX + 22;
                guiGraphics.fill(knobX, switchY + 2, knobX + 12, switchY + 14, 0xFFFFFFFF);
            } else {
                // Disabled (Gray track, knob on the left)
                guiGraphics.fill(switchX, switchY, switchX + switchW, switchY + switchH, 0xFF7F8C8D);
                guiGraphics.renderOutline(switchX, switchY, switchW, switchH, 0xFF95A5A6);
                int knobX = switchX + 2;
                guiGraphics.fill(knobX, switchY + 2, knobX + 12, switchY + 14, 0xFFFFFFFF);
            }
        }

        // Draw Scroll Buttons
        if (optionalItems.size() > 5) {
            int upX = listX + listWidth + 10;
            int upY = startY;
            int downX = listX + listWidth + 10;
            int downY = startY + listHeight - 20;

            boolean upHovered = mouseX >= upX && mouseX < upX + 20 && mouseY >= upY && mouseY < upY + 20;
            guiGraphics.fill(upX, upY, upX + 20, upY + 20, upHovered ? 0x88FFFFFF : 0x44FFFFFF);
            guiGraphics.renderOutline(upX, upY, 20, 20, 0xFF55555A);
            guiGraphics.drawCenteredString(this.font, "▲", upX + 10, upY + 6, 0xFFFFFFFF);

            boolean downHovered = mouseX >= downX && mouseX < downX + 20 && mouseY >= downY && mouseY < downY + 20;
            guiGraphics.fill(downX, downY, downX + 20, downY + 20, downHovered ? 0x88FFFFFF : 0x44FFFFFF);
            guiGraphics.renderOutline(downX, downY, 20, 20, 0xFF55555A);
            guiGraphics.drawCenteredString(this.font, "▼", downX + 10, downY + 6, 0xFFFFFFFF);

            String progress = (startIndex + 1) + "-" + Math.min(startIndex + 5, optionalItems.size()) + "/" + optionalItems.size();
            guiGraphics.drawCenteredString(this.font, progress, upX + 10, upY + 30, 0xFFAAAAAA);
        }

        // Draw Hover Tooltips
        String hoveredTooltip = null;
        for (int i = 0; i < visibleCount; i++) {
            int itemIndex = startIndex + i;
            ModItem item = optionalItems.get(itemIndex);
            int itemY = startY + i * 30;

            if (mouseX >= listX && mouseX < listX + listWidth && mouseY >= itemY && mouseY < itemY + 24) {
                hoveredTooltip = item.jarFileName != null ? "控制文件: " + item.jarFileName : "状态: 配置文件中未检测到对应的 Jar 包";
                break;
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (hoveredTooltip != null) {
            int textWidth = this.font.width(hoveredTooltip);
            int boxX = mouseX + 12;
            int boxY = mouseY - 12;
            int boxW = textWidth + 8;
            int boxH = 16;

            if (boxX + boxW > this.width) {
                boxX = mouseX - boxW - 8;
            }

            guiGraphics.fill(boxX, boxY, boxX + boxW, boxY + boxH, 0xF2000000);
            guiGraphics.renderOutline(boxX, boxY, boxW, boxH, 0xFF55555A);
            guiGraphics.drawString(this.font, hoveredTooltip, boxX + 4, boxY + 4, 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            int listWidth = 320;
            int itemHeight = 24;
            int spacing = 6;
            int listX = this.width / 2 - listWidth / 2;
            int startY = this.height / 2 - 70;

            // Check visible items toggles
            int visibleCount = Math.min(5, optionalItems.size() - startIndex);
            for (int i = 0; i < visibleCount; i++) {
                int itemIndex = startIndex + i;
                int itemY = startY + i * (itemHeight + spacing);

                int btnX = listX + listWidth - 46;
                int btnY = itemY + 4;
                int btnW = 36;
                int btnH = 16;

                if (mouseX >= btnX - 35 && mouseX < btnX + btnW && mouseY >= btnY - 2 && mouseY < btnY + btnH + 2) {
                    ModItem item = optionalItems.get(itemIndex);
                    if (item.jarFileName != null) {
                        item.enabled = !item.enabled;
                        playClickSound();
                    }
                    return true;
                }
            }

            // Check scroll buttons
            if (optionalItems.size() > 5) {
                int upX = listX + listWidth + 10;
                int upY = startY;
                int downX = listX + listWidth + 10;
                int downY = startY + 146 - 20;

                if (mouseX >= upX && mouseX < upX + 20 && mouseY >= upY && mouseY < upY + 20) {
                    scrollUp();
                    playClickSound();
                    return true;
                }
                if (mouseX >= downX && mouseX < downX + 20 && mouseY >= downY && mouseY < downY + 20) {
                    scrollDown();
                    playClickSound();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0) {
            scrollUp();
        } else if (scrollY < 0) {
            scrollDown();
        }
        return true;
    }

    private void scrollUp() {
        if (startIndex > 0) {
            startIndex--;
        }
    }

    private void scrollDown() {
        if (startIndex < Math.max(0, optionalItems.size() - 5)) {
            startIndex++;
        }
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F)
        );
    }
}
