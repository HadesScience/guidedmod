# GuidedMod (Mode Selection & Mod Toggle Mod)

English | [简体中文](README_zh.md)

---

A powerful, premium, and highly customizable Minecraft client-side guide mod. Built on **NeoForge 1.21.1**.

GuidedMod is designed for modpack creators to intercept players on the first game startup, prompting them to choose between "Vanilla Mode" and "Overhaul Mode". The mod physically controls mod loading states by renaming `.jar` files (appending or removing the `.disabled` suffix). It also features an intermediate "Optional Mods Switch Screen" for fine-grained toggling, an anti-accidental-click safety warning screen, and a 5-second countdown restart sequence.

---

## 🌟 Core Features

1. **3D Rotating Panorama Background**:
   * Uses the official 1.21 Trial Chamber panorama cubemap with post-processing blur disabled for a clean, premium look.
2. **Bilingual Dual Cards Selection (`GuideScreen`)**:
   * Offers **"Vanilla Mode"** (Green theme) and **"Overhaul Mode"** (Purple theme) card selectors with hover highlights, outer glows, and custom click sounds.
3. **Independent Optional Mods Screen (`OptionalModsScreen`)**:
   * **Capsule Slider Switches**: Upgrades standard buttons to sleek iOS/Material-style slider switches linked dynamically to ON (green) / OFF (gray) states.
   * **Missing Mod Detection**: If an optional mod is listed in the config but its JAR is not physically present in the `mods` folder, the switch will render as a gray track, display `Missing`, and disable click operations.
   * **Hover Tooltips**: Hovering over any row displays a tooltip showing the exact JAR filename (e.g., `控制文件: jei-1.21.1-19.0.0.jar`) or missing status, featuring screen boundary-aware positioning.
   * **Page Scrolling**: Scroll through the list using the mouse wheel or the physical scroll buttons (▲ / ▼) with progress page tracking.
4. **Anti-Accidental-Click Confirmation (`ConfirmModeScreen`)**:
   * Warning details are rendered in **bold red text**.
   * Strategic button layout: **Green "★ Cancel (Recommended) ★" on the left (selected by default) and red "Confirm Switch" on the right**, strongly preventing accidental clicks.
5. **One-Time Interception Lock**:
   * Supports `one_time_only=true`. Once configured, the "Mode Adjust" button on the main menu is automatically hidden, and subsequent game launches will silently bypass the guide screen.
6. **Defensive Filename Matching & Cleaning**:
   * **Prefix & Version Cleaning**: Strips common filename version formats (like `[1.21.1]`, `(1.20)`, `mc1.21-`) before executing a **starts-with prefix comparison**.
   * **Accidental renaming prevention**: Restricts keywords to a minimum length of 3 characters, ignoring single-letter entries or standalone bullet symbols (such as `-` or `*`) to prevent catastrophic mass-renaming bugs.

---

## ⚙️ Configuration

The mod generates the following configuration files in the client's `config` folder:

### 1. `config/guidedmod-mods.txt`
Configure mods to control, separated by the `[optional]` header:
```text
# Section 1: Mods controlled by primary modes (Vanilla/Overhaul)
# When clicking "Vanilla Mode", these mods are disabled; "Overhaul Mode" enables them.
# Example:
# appleskin
# journeymap

[optional]
# Section 2: Optional mods controlled individually
# Regardless of the selected mode, an optional slider screen will display these switches.
# Example:
# justenoughitems
```

### 2. `config/guidedmod-ui-text.txt`
All screen texts, card descriptions (supports 1-6 lines), warnings (1-6 lines), and bottom notes can be customized here:
```properties
# Primary Guide Screen
title=模式选择 / Mode Selection
subtitle=请选择您要启动的模式。完成后游戏会自动关闭，请手动重启应用更改。

vanilla_title=原版模式
vanilla_subtitle=Vanilla Mode
vanilla_desc1=禁用指定模组
vanilla_desc2=保持原版纯净体验
vanilla_desc3=适合原版服务器

overhaul_title=大改模式
overhaul_subtitle=Overhaul Mode
overhaul_desc1=启用所有模组
overhaul_desc2=加载完整模组体验
overhaul_desc3=包含所有大改模组

# Optional Mode sub-screen settings
optional_title=选装模组开关
optional_subtitle=请选择您需要随模式一同开启的附加模组：
optional_next_button=下一步 (Next)
optional_back_button=返回 (Back)

# Final Warning Screen
warning_title=⚠️ 安全警告 / Safety Warning
warning_line1=检测到模式切换请求。切换模式可能会导致您的
warning_line2=已有存档发生不可逆的损坏或物品/方块丢失！
warning_line3=建议在继续前备份您的存档。是否确认切换？
confirm_switch_button=确认切换
cancel_switch_button=取消

# One-time Notice (Empty to hide)
one_time_only=true
one_time_note=* 注意：切换并重启后将隐藏主页的调整按钮，您将无法再次更改。
```

---

## 🛠️ Build and Compilation

Requires Java 21 and Gradle.

### Compile JAR:
Run the following in the project root:
```bash
# Windows
.\gradlew.bat build

# Linux/macOS
./gradlew build
```
The compiled jar will be located at `build/libs/guidedmod-1.0.1-SNAPSHOT.jar`.

---

## 📝 License

This project is licensed under the [MIT License](LICENSE).
