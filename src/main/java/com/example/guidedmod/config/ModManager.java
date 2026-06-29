package com.example.guidedmod.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class ModManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String CONFIG_FILE = "config/guidedmod-mods.txt";
    private static final String MODS_DIR = "mods";

    private static final List<String> MAIN_TARGETS = new ArrayList<>();
    private static final List<String> OPTIONAL_TARGETS = new ArrayList<>();

    public static List<String> readTargetMods() {
        Path configPath = Paths.get(CONFIG_FILE);
        MAIN_TARGETS.clear();
        OPTIONAL_TARGETS.clear();

        if (!Files.exists(configPath)) {
            try {
                Files.createDirectories(configPath.getParent());
                List<String> defaultLines = List.of(
                    "# List of mods to be disabled in Vanilla Mode and enabled in Overhaul Mode.",
                    "# Put one mod ID or jar prefix per line.",
                    "# Comments start with #, empty lines are ignored.",
                    "# Example:",
                    "# appleskin",
                    "# journeymap",
                    "",
                    "[optional]",
                    "# Mods in this section are controlled individually on the setup screen.",
                    "# Example:",
                    "# justenoughitems"
                );
                Files.write(configPath, defaultLines);
                LOGGER.info("Created default config file at {}", configPath.toAbsolutePath());
            } catch (IOException e) {
                LOGGER.error("Failed to create default config file", e);
            }
        }

        try (Stream<String> stream = Files.lines(configPath)) {
            List<String> lines = stream.toList();
            boolean optionalSection = false;
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                    continue;
                }
                if (line.equalsIgnoreCase("[optional]") || line.equalsIgnoreCase("[extra]")) {
                    optionalSection = true;
                    continue;
                }

                // Strip leading bullet points if present (e.g., "- jei", "* optifine", or "-jei")
                if (line.startsWith("- ") || line.startsWith("* ")) {
                    line = line.substring(2).trim();
                } else if (line.startsWith("-") || line.startsWith("*")) {
                    line = line.substring(1).trim();
                }

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Ignore extremely short or wildcard-like keywords to prevent accidental mass matching
                if (line.length() < 3) {
                    LOGGER.warn("Skipping too short or invalid target keyword: '{}'", line);
                    continue;
                }

                if (optionalSection) {
                    OPTIONAL_TARGETS.add(line.toLowerCase());
                } else {
                    MAIN_TARGETS.add(line.toLowerCase());
                }
            }
            LOGGER.info("Read {} main targets and {} optional targets", MAIN_TARGETS.size(), OPTIONAL_TARGETS.size());
        } catch (IOException e) {
            LOGGER.error("Failed to read config file", e);
        }
        return MAIN_TARGETS;
    }

    private static String cleanFilename(String filename) {
        String name = filename.toLowerCase();
        if (name.endsWith(".jar.disabled")) {
            name = name.substring(0, name.length() - ".jar.disabled".length());
        } else if (name.endsWith(".jar")) {
            name = name.substring(0, name.length() - ".jar".length());
        }
        // Remove leading brackets and parentheses like [1.21.1], (1.20)
        name = name.replaceAll("^[\\[\\(][^\\]\\)]+[\\]\\)]", "");
        // Remove version prefixes like "mc1.21-", "1.21.1-", "mc-1.20-"
        name = name.replaceAll("^(mc)?[-_]?\\d+\\.\\d+(\\.\\d+)?[-_]?", "");
        return name.trim();
    }

    public static boolean applyMode(boolean overhaulMode) {
        List<String> targets = readTargetMods();
        if (targets.isEmpty()) {
            LOGGER.warn("No target mods found in config file.");
            return false;
        }

        Path modsPath = Paths.get(MODS_DIR);
        if (!Files.exists(modsPath) || !Files.isDirectory(modsPath)) {
            LOGGER.error("Mods directory does not exist: {}", modsPath.toAbsolutePath());
            return false;
        }

        boolean changed = false;
        try (Stream<Path> files = Files.list(modsPath)) {
            List<Path> allFiles = files.collect(Collectors.toList());
            for (Path file : allFiles) {
                String filename = file.getFileName().toString();
                String lowerName = filename.toLowerCase();

                // Skip our own mod to prevent disabling it
                if (lowerName.contains("guidedmod")) {
                    continue;
                }

                // Check if the file matches any target in the list using clean and strict matching
                boolean isMatch = false;
                String cleanedName = cleanFilename(filename);
                for (String target : targets) {
                    // 1. Strict start-with prefix match
                    if (cleanedName.startsWith(target)) {
                        isMatch = true;
                        break;
                    }
                    // 2. Contains match fallback ONLY if target length is >= 4 characters to prevent false positives
                    if (target.length() >= 4 && cleanedName.contains(target)) {
                        isMatch = true;
                        break;
                    }
                }

                if (isMatch) {
                    if (overhaulMode) {
                        // Enable: rename .jar.disabled back to .jar
                        if (filename.endsWith(".jar.disabled")) {
                            String newName = filename.substring(0, filename.length() - ".disabled".length());
                            Path targetPath = modsPath.resolve(newName);
                            Files.move(file, targetPath);
                            LOGGER.info("Enabled mod: {} -> {}", filename, newName);
                            changed = true;
                        }
                    } else {
                        // Disable: rename .jar to .jar.disabled
                        if (filename.endsWith(".jar")) {
                            String newName = filename + ".disabled";
                            Path targetPath = modsPath.resolve(newName);
                            Files.move(file, targetPath);
                            LOGGER.info("Disabled mod: {} -> {}", filename, newName);
                            changed = true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to list/move mods in mods directory", e);
            return false;
        }
        return changed;
    }

    public static void disableSelf() {
        Path modsDir = Paths.get(MODS_DIR);
        if (!Files.exists(modsDir)) {
            return;
        }
        try (var stream = Files.list(modsDir)) {
            stream.forEach(path -> {
                String filename = path.getFileName().toString();
                if (filename.endsWith(".jar") && filename.toLowerCase().contains("guidedmod")) {
                    String absoluteJarPath = path.toAbsolutePath().toString();
                    String disabledName = filename + ".disabled";
                    
                    try {
                        String os = System.getProperty("os.name").toLowerCase();
                        if (os.contains("win")) {
                            // On Windows, the running JAR is locked by the JVM. 
                            // We spawn a background cmd process that waits 3 seconds for Minecraft to shut down, then renames the JAR.
                            String cmd = String.format("cmd.exe /c timeout /t 3 /nobreak && move /y \"%s\" \"%s\"", 
                                absoluteJarPath, 
                                path.resolveSibling(disabledName).toAbsolutePath().toString()
                            );
                            Runtime.getRuntime().exec(cmd);
                            LOGGER.info("Scheduled self-disabling for Windows: {}", cmd);
                        } else {
                            // On Unix-based systems (Linux/macOS), files can be renamed even if they are open/active.
                            Files.move(path, path.resolveSibling(disabledName));
                            LOGGER.info("Disabled self directly on non-Windows OS.");
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to disable self by renaming jar", e);
                    }
                }
            });
        } catch (IOException e) {
            LOGGER.error("Failed to list mods folder for self-disabling", e);
        }
    }

    public static boolean hasOptionalTargets() {
        readTargetMods();
        return !OPTIONAL_TARGETS.isEmpty();
    }

    public static List<com.example.guidedmod.client.OptionalModsScreen.ModItem> getOptionalModItems() {
        List<com.example.guidedmod.client.OptionalModsScreen.ModItem> list = new ArrayList<>();
        // Make sure we have loaded the targets
        readTargetMods();
        
        Path modsPath = Paths.get(MODS_DIR);
        if (!Files.exists(modsPath)) {
            for (String target : OPTIONAL_TARGETS) {
                list.add(new com.example.guidedmod.client.OptionalModsScreen.ModItem(target, null, false));
            }
            return list;
        }
        try (var stream = Files.list(modsPath)) {
            List<Path> files = stream.toList();
            for (String target : OPTIONAL_TARGETS) {
                String lowerTarget = target.toLowerCase();
                Path foundFile = null;
                boolean enabled = true;
                
                // Search for enabled jar
                for (Path file : files) {
                    String filename = file.getFileName().toString();
                    if (filename.endsWith(".jar") && !filename.toLowerCase().contains("guidedmod")) {
                        String cleanedName = cleanFilename(filename);
                        if (cleanedName.startsWith(lowerTarget) || (lowerTarget.length() >= 4 && cleanedName.contains(lowerTarget))) {
                            foundFile = file;
                            enabled = true;
                            break;
                        }
                    }
                }
                // If not found, search for disabled jar
                if (foundFile == null) {
                    for (Path file : files) {
                        String filename = file.getFileName().toString();
                        if (filename.endsWith(".jar.disabled") && !filename.toLowerCase().contains("guidedmod")) {
                            String cleanedName = cleanFilename(filename);
                            if (cleanedName.startsWith(lowerTarget) || (lowerTarget.length() >= 4 && cleanedName.contains(lowerTarget))) {
                                foundFile = file;
                                enabled = false;
                                break;
                            }
                        }
                    }
                }
                
                // Always add the item, passing null jarFileName if not found
                list.add(new com.example.guidedmod.client.OptionalModsScreen.ModItem(
                    target, 
                    foundFile != null ? foundFile.getFileName().toString() : null, 
                    foundFile != null && enabled
                ));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to list mods for optional selection", e);
            for (String target : OPTIONAL_TARGETS) {
                list.add(new com.example.guidedmod.client.OptionalModsScreen.ModItem(target, null, false));
            }
        }
        return list;
    }

    public static boolean applyOptionalToggles(List<com.example.guidedmod.client.OptionalModsScreen.ModItem> optionalItems) {
        boolean changed = false;
        Path modsPath = Paths.get(MODS_DIR);
        for (com.example.guidedmod.client.OptionalModsScreen.ModItem item : optionalItems) {
            if (item.jarFileName != null && item.enabled != item.originalEnabled) {
                if (item.enabled) {
                    // Enable: rename .jar.disabled back to .jar
                    Path src = modsPath.resolve(item.jarFileName);
                    String newName = item.jarFileName.substring(0, item.jarFileName.length() - ".disabled".length());
                    Path dest = modsPath.resolve(newName);
                    try {
                        Files.move(src, dest);
                        changed = true;
                        LOGGER.info("Enabled optional mod: {} -> {}", item.jarFileName, newName);
                    } catch (IOException e) {
                        LOGGER.error("Failed to enable optional mod: " + item.jarFileName, e);
                    }
                } else {
                    // Disable: rename .jar to .jar.disabled
                    Path src = modsPath.resolve(item.jarFileName);
                    String newName = item.jarFileName + ".disabled";
                    Path dest = modsPath.resolve(newName);
                    try {
                        Files.move(src, dest);
                        changed = true;
                        LOGGER.info("Disabled optional mod: {} -> {}", item.jarFileName, newName);
                    } catch (IOException e) {
                        LOGGER.error("Failed to disable optional mod: " + item.jarFileName, e);
                    }
                }
            }
        }
        return changed;
    }
}
