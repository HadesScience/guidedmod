package com.example.guidedmod.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class TextConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TEXT_CONFIG_FILE = "config/guidedmod-ui-text.txt";
    private static final Properties PROPERTIES = new Properties();

    static {
        load();
    }

    public static void load() {
        Path path = Paths.get(TEXT_CONFIG_FILE);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                // Write default properties file using UTF-8
                try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                    writer.write("# Guided Mod UI Text Configuration\n");
                    writer.write("# You can change the text displayed in the startup guide screens here.\n\n");
                    writer.write("title=★ 游戏启动引导 / Game Mode Setup ★\n");
                    writer.write("subtitle=请选择您要启动的模式。完成后游戏会自动关闭，请手动重启应用更改。\n\n");
                    writer.write("vanilla_title=原版模式\n");
                    writer.write("vanilla_subtitle=Vanilla Mode\n");
                    writer.write("vanilla_desc1=禁用指定模组\n");
                    writer.write("vanilla_desc2=保持原版纯净体验\n");
                    writer.write("vanilla_desc3=适合原版服务器\n\n");
                    writer.write("overhaul_title=大改模式\n");
                    writer.write("overhaul_subtitle=Overhaul Mode\n");
                    writer.write("overhaul_desc1=启用所有模组\n");
                    writer.write("overhaul_desc2=加载完整模组体验\n");
                    writer.write("overhaul_desc3=包含所有大改模组\n\n");
                    writer.write("restart_success_vanilla=已成功切换到原版模式！\n");
                    writer.write("restart_success_overhaul=已成功切换到大改模式！\n");
                    writer.write("restart_info=请手动重新启动游戏以使更改生效！\n");
                    writer.write("restart_closing_format=游戏将在 %d 秒后自动关闭... (Closing in %ds...)\n\n");
                    writer.write("info_already_vanilla=当前已是原版模式，无需更改。\n");
                    writer.write("info_already_overhaul=当前已是大改模式，无需更改。\n");
                    writer.write("info_confirm_button=确定 (Confirm)\n\n");
                    writer.write("# Whether to only show the guide screen once (true/false)\n");
                    writer.write("show_only_once=true\n\n");
                    writer.write("# Text for the button on the main menu title screen\n");
                    writer.write("main_menu_button=模式调整\n\n");
                    writer.write("# Safety confirmation screen text\n");
                    writer.write("warning_title=⚠️ 安全警告 / Safety Warning\n");
                    writer.write("warning_line1=检测到模式切换请求。切换模式可能会导致您的\n");
                    writer.write("warning_line2=已有存档发生不可逆的损坏或物品/方块丢失！\n");
                    writer.write("warning_line3=建议在继续前备份您的存档。是否确认切换？\n");
                    writer.write("confirm_switch_button=确认切换\n");
                    writer.write("cancel_switch_button=取消\n\n");
                    writer.write("# Whether this setup screen is a one-time only action. If true, the mod will display a warning note\n");
                    writer.write("# at the bottom of the selection page, and will hide the \"Mode Select\" button on the main menu after selection.\n");
                    writer.write("one_time_only=true\n\n");
                    writer.write("# Note to display at the bottom if one_time_only is true\n");
                    writer.write("one_time_note=* 注意：切换并重启后将隐藏主页的调整按钮，您将无法再次更改。\n\n");
                    writer.write("# Optional Mode sub-screen settings\n");
                    writer.write("optional_title=选装模组开关\n");
                    writer.write("optional_subtitle=请选择您需要随模式一同开启的附加模组：\n");
                    writer.write("optional_next_button=下一步 (Next)\n");
                    writer.write("optional_back_button=返回 (Back)\n");
                }
                LOGGER.info("Created default UI text config file at {}", path.toAbsolutePath());
            } catch (IOException e) {
                LOGGER.error("Failed to create default UI text config file", e);
            }
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            PROPERTIES.load(reader);
            LOGGER.info("Successfully loaded UI text config");
        } catch (IOException e) {
            LOGGER.error("Failed to load UI text config file", e);
        }

        // Auto-append missing properties to existing file if they don't exist
        try {
            boolean hasOneTimeOnly = hasPropertyInFile(path, "one_time_only");
            boolean hasOneTimeNote = hasPropertyInFile(path, "one_time_note");
            boolean hasShowOnlyOnce = hasPropertyInFile(path, "show_only_once");
            boolean hasMainMenuButton = hasPropertyInFile(path, "main_menu_button");
            boolean hasWarningTitle = hasPropertyInFile(path, "warning_title");
            boolean hasOptionalTitle = hasPropertyInFile(path, "optional_title");
            boolean hasOptionalNext = hasPropertyInFile(path, "optional_next_button");

            if (!hasOneTimeOnly || !hasOneTimeNote || !hasShowOnlyOnce || !hasMainMenuButton || !hasWarningTitle || !hasOptionalTitle || !hasOptionalNext) {
                try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND)) {
                    writer.write("\n# Automatically appended newly introduced configuration options below\n");
                    if (!hasShowOnlyOnce) {
                        writer.write("show_only_once=true\n");
                        PROPERTIES.setProperty("show_only_once", "true");
                    }
                    if (!hasMainMenuButton) {
                        writer.write("main_menu_button=模式调整\n");
                        PROPERTIES.setProperty("main_menu_button", "模式调整");
                    }
                    if (!hasWarningTitle) {
                        writer.write("warning_title=⚠️ 安全警告 / Safety Warning\n");
                        writer.write("warning_line1=检测到模式切换请求。切换模式可能会导致您的\n");
                        writer.write("warning_line2=已有存档发生不可逆的损坏或物品/方块丢失！\n");
                        writer.write("warning_line3=建议在继续前备份您的存档。是否确认切换？\n");
                        writer.write("confirm_switch_button=确认切换\n");
                        writer.write("cancel_switch_button=取消\n");
                        PROPERTIES.setProperty("warning_title", "⚠️ 安全警告 / Safety Warning");
                        PROPERTIES.setProperty("warning_line1", "检测到模式切换请求。切换模式可能会导致您的");
                        PROPERTIES.setProperty("warning_line2", "已有存档发生不可逆的损坏或物品/方块丢失！");
                        PROPERTIES.setProperty("warning_line3", "建议在继续前备份您的存档。是否确认切换？");
                        PROPERTIES.setProperty("confirm_switch_button", "确认切换");
                        PROPERTIES.setProperty("cancel_switch_button", "取消");
                    }
                    if (!hasOneTimeOnly) {
                        writer.write("one_time_only=true\n");
                        PROPERTIES.setProperty("one_time_only", "true");
                    }
                    if (!hasOneTimeNote) {
                        writer.write("one_time_note=* 注意：切换并重启后将隐藏主页的调整按钮，您将无法再次更改。\n");
                        PROPERTIES.setProperty("one_time_note", "* 注意：切换并重启后将隐藏主页的调整按钮，您将无法再次更改。");
                    }
                    if (!hasOptionalTitle) {
                        writer.write("optional_title=选装模组开关\n");
                        writer.write("optional_subtitle=请选择您需要随模式一同开启的附加模组：\n");
                        PROPERTIES.setProperty("optional_title", "选装模组开关");
                        PROPERTIES.setProperty("optional_subtitle", "请选择您需要随模式一同开启的附加模组：");
                    }
                    if (!hasOptionalNext) {
                        writer.write("optional_next_button=下一步 (Next)\n");
                        writer.write("optional_back_button=返回 (Back)\n");
                        PROPERTIES.setProperty("optional_next_button", "下一步 (Next)");
                        PROPERTIES.setProperty("optional_back_button", "返回 (Back)");
                    }
                }
                LOGGER.info("Appended missing configurations to existing UI text config file.");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to auto-update configuration file", e);
        }
    }

    private static boolean hasPropertyInFile(Path path, String key) {
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (line.trim().startsWith(key + "=")) {
                    return true;
                }
            }
        } catch (IOException e) {
            // ignore
        }
        return false;
    }

    public static String get(String key, String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }
}
