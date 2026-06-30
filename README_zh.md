# GuidedMod (引导模式选择与模组物理开关)

[English](README.md) | 简体中文

---

一个功能强大、设计精美且客制化程度极高的 Minecraft 客户端引导模组。基于 **NeoForge 1.21.1** 开发。

该模组专为整合包制作者设计，能够在游戏首次启动时进行拦截引导，提供“原版模式”与“大改模式”的卡片选择。模组通过物理重命名 `.jar` 文件名（加减 `.disabled` 后缀）的形式控制目标模组的加载，同时支持后续中转“选装模组开关页”进行细粒度的滑块微调，最终通过防误触安全警告页与 5 秒倒计时重启引导完成配置。

---

## 🌟 核心特色

1. **三维旋转 Trial Chamber 清晰全景图背景**：
   * 采用 1.21 官方全景背景，屏蔽了原版模糊，界面切换响应平滑，生命周期管理防止冲突。
2. **经典的双卡片引导交互 (`GuideScreen`)**：
   * 提供 **“原版模式”**（绿色主题）与 **“大改模式”**（紫色主题）两个主卡片选择，支持悬停高亮、发光边框以及立体点击音效。
3. **独立的选装模组中转界面 (`OptionalModsScreen`)**：
   * **胶囊滑块开关**：将常规按钮重构为高端的 iOS/Material 样式滑块式开关，滑块状态与绿/灰文字实时联动。
   * **文件缺失自检**：如果配置文件里写了该选装模组，但 `mods` 文件夹里找不到对应的 Jar 文件，则会显示为灰色轨道、左侧显示 `缺失` 且不可点击。
   * **高级悬停提示 (Hover Tooltip)**：鼠标悬停在列表行上时，会自动弹出半透明信息框，展示控制的文件名（如 `控制文件: jei-1.21.1-19.0.0.jar`）或未检测到提示，具备出界自反弹避让算法。
   * **翻页支持**：支持鼠标滚轮或点击物理翻页键（▲ / ▼）进行上下滚动，带页数进度指示。
4. **防误触双重确认机制 (`ConfirmModeScreen`)**：
   * 确认切换界面中，警告信息采用**高醒目度红色字**渲染。
   * 按钮布局符合防误触设计：**左侧为绿色的“★ 取消 (推荐) ★”按钮（默认选中），右侧为红色的“确认切换”按钮**，强力阻断玩家误点。
5. **一键锁死与免打扰**：
   * 支持 `one_time_only=true`。玩家配置完成后，游戏主菜单左上角的“模式调整”按钮将被自动隐藏，且启动时会自动静默跳过引导界面，防止二次修改。
6. **万无一失的防误伤与清洗算法**：
   * **前缀/模糊智能清洗**：自动剔除文件名开头常见的版本标记（如 `[1.21.1]`、`(1.20)`、`mc1.21-`），进行精准的 **starts-with 前缀比对**。
   * **超短词条防御**：强制限制配置词条长度必须 $\ge 3$ 个字符，过滤掉诸如 `-`、`.` 或单字母等容易导致误伤的非法输入，并自动过滤 `- jei` 等 bullet-point 项目符。

---

## ⚙️ 配置文件说明

模组在首次运行或更新时会在客户端 `config` 文件夹下生成以下配置文件：

### 1. `config/guidedmod-mods.txt` (模组控制列表)
控制两部分模组，以 `[optional]` 关键字为界：
```text
# 第一部分：受主模式（原版/大改）控制的模组列表
# 点击“原版模式”时禁用这些模组，点击“大改模式”时启用它们。
# Example:
# appleskin
# journeymap

[optional]
# 第二部分：由选装中转界面控制的独立开关模组列表
# 无论选择何种模式，在最后确认前，都会弹出独立的滑块开关让玩家选择是否启用。
# Example:
# justenoughitems
```

### 2. `config/guidedmod-ui-text.txt` (UI 文本汉化与配置)
本模组界面所有的文字、卡片描述（支持自适应 1-6 行）、警告信息（支持自适应 1-6 行）以及底部的红字提示均可以在此文件中配置：
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

## 🛠️ 编译与开发

需要 Java 21 和 Gradle 环境。

### 编译 Jar 包：
在项目根目录下执行以下命令：
```bash
# Windows
.\gradlew.bat build

# Linux/macOS
./gradlew build
```
编译产物输出于 `build/libs/guidedmod-1.0.1-SNAPSHOT.jar`。

---

## 📝 许可证

本模组源码采用 [MIT License](LICENSE) 协议开源。
