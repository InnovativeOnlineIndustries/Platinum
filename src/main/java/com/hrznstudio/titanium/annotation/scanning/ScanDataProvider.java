package com.hrznstudio.titanium.annotation.scanning;

import com.google.gson.*;
import com.hrznstudio.titanium.Titanium;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.GsonHelper;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        try {
            var modsDir = Paths.get("mods");
            List<Path> pathsToScan = new ArrayList<>(Files.list(modsDir).toList());

            pathsToScan.addAll(FabricLoader.getInstance().getModContainer(Titanium.MODID).get().getRootPaths());

            try (var modStream = pathsToScan.stream()) {
                modStream.filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .forEach(path -> {
                        try (var fs = FileSystems.newFileSystem(path)) {
                            var modJson = fs.getPath("fabric.mod.json");
                            if (Files.exists(modJson) && path.toString().endsWith(".jar")) {
                                JsonObject json = JsonParser.parseReader(Files.newBufferedReader(fs.getPath("fabric.mod.json"))).getAsJsonObject();
                                var files = Files.walk(fs.getPath("/"));
                                ALL_SCAN_DATA.put(GsonHelper.getAsString(json, "id"), generateScanData(files, path));
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to read " + path.getFileName(), e);
                        }
                    });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan mods", e);
        }
    }

    public static ModFileScanData getModScanData(String modId) {
        return ALL_SCAN_DATA.get(modId);
    }


    public static ModFileScanData generateScanData(Stream<Path> files, Path modFile) {
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

        LOGGER.info("Scanning {} took {}ms", modFile.getFileName().toString(), (System.currentTimeMillis() - startTime));
        return data;
    }
}
