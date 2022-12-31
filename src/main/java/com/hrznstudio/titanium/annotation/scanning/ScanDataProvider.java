package com.hrznstudio.titanium.annotation.scanning;

import com.google.gson.*;
import com.hrznstudio.titanium.Titanium;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.util.LoaderUtil;
import net.fabricmc.loader.impl.util.SystemProperties;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.minecraft.util.GsonHelper;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Probably optimize this or just remove it.
 */
@Deprecated
public class ScanDataProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger("Scan Data Provider");
    private static final List<String> SKIP = List.of("minecraft", "fabricloader", "java");
    public static final Map<String, ModFileScanData> ALL_SCAN_DATA;

    public static void init() {
        for (ModContainer modContainer : FabricLoader.getInstance().getAllMods()) {
            ALL_SCAN_DATA.put(modContainer.getMetadata().getId(), generateScanData(modContainer.getRootPaths().stream().flatMap(path -> {
                try {
                    return Files.walk(path);
                } catch (IOException e) {
                    return Stream.empty();
                }
            }), modContainer));
        }
    }

    public static ModFileScanData getModScanData(String modId) {
        return ALL_SCAN_DATA.get(modId);
    }
}
