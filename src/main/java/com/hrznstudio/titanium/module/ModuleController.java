/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.module;

import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.annotation.config.ConfigFile;
import com.hrznstudio.titanium.annotation.plugin.FeaturePlugin;
import com.hrznstudio.titanium.config.AnnotationConfigManager;
import com.hrznstudio.titanium.plugin.PluginManager;
import com.hrznstudio.titanium.plugin.PluginPhase;
import com.hrznstudio.titanium.util.AnnotationUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.api.fml.event.config.ModConfigEvent;

public abstract class ModuleController {
    private final String modid;
    private final AnnotationConfigManager configManager;
    private final PluginManager modPluginManager;
    private final DeferredRegistryHelper deferredRegistryHelper;

    public ModuleController(String modid) {
        this.modid = modid;
        this.configManager = new AnnotationConfigManager(modid);
        this.modPluginManager = new PluginManager(modid, FeaturePlugin.FeaturePluginType.MOD, featurePlugin -> FabricLoader.getInstance().isModLoaded(featurePlugin.value()), true);
        this.modPluginManager.execute(PluginPhase.CONSTRUCTION);
        this.deferredRegistryHelper = new DeferredRegistryHelper(this.modid);
        onPreInit();
        onInit();
        onPostInit();
    }

    public ModuleController() {
        this(Titanium.MODID);
    }

    private void addConfig(AnnotationConfigManager.Type type) {
        for (Class configClass : type.getConfigClass()) {
            if (configManager.isClassManaged(configClass)) return;
        }
        configManager.add(type);
    }

    public void onPreInit() {
        this.modPluginManager.execute(PluginPhase.PRE_INIT);
    }

    public void onInit() {
        initModules();

        this.modPluginManager.execute(PluginPhase.INIT);
    }

    public void onPostInit() {
        AnnotationUtil.getFilteredAnnotatedClasses(ConfigFile.class, modid).forEach(aClass -> {
            ConfigFile annotation = (ConfigFile) aClass.getAnnotation(ConfigFile.class);
            addConfig(AnnotationConfigManager.Type.of(annotation.type(), aClass).setName(annotation.value()));
        });
        ModConfigEvent.LOADING.register(c -> {
            if (c.getModId().equals(modid)){
                configManager.inject(c.getSpec());
                this.modPluginManager.execute(PluginPhase.CONFIG_LOAD);
            }
        });
        ModConfigEvent.RELOADING.register(c -> {
            if (c.getModId().equals(modid)){
                configManager.inject(c.getSpec());
                this.modPluginManager.execute(PluginPhase.CONFIG_RELOAD);
            }
        });
        this.modPluginManager.execute(PluginPhase.POST_INIT);
    }

    protected abstract void initModules();

    public DeferredRegistryHelper getRegistries() {
        return deferredRegistryHelper;
    }
}
