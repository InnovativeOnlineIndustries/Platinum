/*
 * This file is part of Titanium
 * Copyright (C) 2019, Horizon Studio <contact@hrznstudio.com>, All rights reserved.
 *
 * This means no, you cannot use this code. This is licensed for sole use by Horizon and it's partners, you MUST be granted specific written permission from Horizon to use this code.
 */

package com.hrznstudio.titanium.config;

import com.hrznstudio.titanium.annotation.config.ConfigVal;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnnotationConfigManager {

    public List<Type> configClasses;
    public HashMap<Field, ForgeConfigSpec.ConfigValue> cachedConfigValues;

    public AnnotationConfigManager() {
        configClasses = new ArrayList<>();
        cachedConfigValues = new HashMap<>();
    }

    public void add(Type type) {
        configClasses.add(type);
        // SCANNING CLASSES
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        for (Class configClass : type.configClass) {
            scanClass(configClass, builder);
        }
        // REGISTERING CONFIG
        if (type.fileName.isEmpty()) ModLoadingContext.get().registerConfig(type.type, builder.build());
        else ModLoadingContext.get().registerConfig(type.type, builder.build(), type.fileName);
    }

    private void scanClass(Class configClass, ForgeConfigSpec.Builder builder) {
        builder.push(configClass.getSimpleName());
        try {
            for (Field field : configClass.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(ConfigVal.class)) {
                    if (field.getType().isPrimitive() || field.getType().equals(String.class)) {
                        ConfigVal value = field.getAnnotation(ConfigVal.class);
                        ForgeConfigSpec.ConfigValue configValue = null;
                        if (!value.comment().isEmpty()) builder.comment(value.comment());

                        if (field.isAnnotationPresent(ConfigVal.InRangeDouble.class))
                            configValue = builder.defineInRange(value.value().isEmpty() ? field.getName() : value.value(), (Double) field.get(null),
                                    field.getAnnotation(ConfigVal.InRangeDouble.class).min(), field.getAnnotation(ConfigVal.InRangeDouble.class).max());
                        if (field.isAnnotationPresent(ConfigVal.InRangeLong.class))
                            configValue = builder.defineInRange(value.value().isEmpty() ? field.getName() : value.value(), (Long) field.get(null),
                                    field.getAnnotation(ConfigVal.InRangeLong.class).min(), field.getAnnotation(ConfigVal.InRangeLong.class).max());
                        if (field.isAnnotationPresent(ConfigVal.InRangeInt.class))
                            configValue = builder.defineInRange(value.value().isEmpty() ? field.getName() : value.value(), (Integer) field.get(null),
                                    field.getAnnotation(ConfigVal.InRangeInt.class).min(), field.getAnnotation(ConfigVal.InRangeInt.class).max());

                        if (configValue == null)
                            configValue = builder.define(value.value().isEmpty() ? field.getName() : value.value(), field.get(null));
                        cachedConfigValues.put(field, configValue);
                    } else {
                        scanClass(field.getType(), builder);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        builder.pop();
    }

    public void inject() {
        // MODIFYING CLASSES VALUES FROM CONFIG
        cachedConfigValues.forEach((field, configValue) -> {
            try {
                field.set(null, configValue.get());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean isClassManaged(Class clazz) {
        for (Type configClass : configClasses) {
            for (Class aClass : configClass.configClass) {
                if (clazz.equals(aClass)) return true;
            }
        }
        return false;
    }

    public static class Type {
        private ModConfig.Type type;
        private Class[] configClass;
        private String fileName;

        public Type(ModConfig.Type type, Class... configClass) {
            this.type = type;
            this.configClass = configClass;
            this.fileName = "";
        }

        public Type setName(String name) {
            this.fileName = name;
            return this;
        }

        public Class[] getConfigClass() {
            return configClass;
        }
    }
}
