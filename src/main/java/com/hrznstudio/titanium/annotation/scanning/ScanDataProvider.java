package com.hrznstudio.titanium.annotation.scanning;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    static {
        var absoluteStartTime = System.currentTimeMillis();
        ALL_SCAN_DATA = FabricLoader.getInstance().getAllMods().stream()
            .filter(modContainer -> !(SKIP.contains(modContainer.getMetadata().getId()) || modContainer.getMetadata().containsCustomValue("skip-class-scan")))
            .map(modContainer -> {
                var startTime = System.currentTimeMillis();
                var paths = modContainer.getRootPaths();
                var data = new ModFileScanData();

                paths.stream()
                    .flatMap(path -> {
                        try {
                            return Files.walk(path);
                        } catch (IOException e) {
                            return Stream.empty();
                        }
                    })
                    .parallel()
                    .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".class"))
                    .forEach(path -> {
                        try {
                            var reader = new ClassReader(Files.newInputStream(path));
                            var node = new ModScanClassVisitor(data);
                            reader.accept(node, 0);
                            node.buildModData();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    });
                LOGGER.info("Scanning {} took {}ms", modContainer.getMetadata().getId(), (System.currentTimeMillis() - startTime));
                return new Pair<>(modContainer.getMetadata().getId(), data);
            })
            .collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond));
        LOGGER.info("Scanning took {}ms", (System.currentTimeMillis() - absoluteStartTime));
    }

    public static ModFileScanData getModScanData(String modId) {
        return ALL_SCAN_DATA.get(modId);
    }
}
