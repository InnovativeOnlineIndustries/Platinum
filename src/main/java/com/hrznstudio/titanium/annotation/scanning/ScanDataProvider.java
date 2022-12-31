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
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
            .registerTypeAdapter(ModFileScanData.AnnotationData.class, new AnnotationDataSerializer())
            .create();

    public static Map<String, ModFileScanData> ALL_SCAN_DATA = new HashMap<>();

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


    public static ModFileScanData generateScanData(Stream<Path> files, ModContainer modFile) {
        var startTime = System.currentTimeMillis();
        var data = new ModFileScanData();
        var classFiles = files.filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(".class"))
                .toList();

        for (var path : classFiles) {
            try {
                var reader = new ClassReader(Files.newInputStream(path));
                var node = new ModScanClassVisitor(data);
                reader.accept(node, 0);
                node.buildModData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // testing GSON
        var jsonStr = GSON.toJson(data);
        try {
            Files.writeString(Paths.get("scan_data.json"),jsonStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("Scanning {} took {}ms", modFile.getMetadata().getId(), (System.currentTimeMillis() - startTime));
        return data;
    }
}
