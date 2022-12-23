/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.recipe.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.hrznstudio.titanium.Titanium;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;


public class ContentExistsCondition implements ConditionJsonProvider {
    public static final ResourceLocation NAME = new ResourceLocation(Titanium.MODID, "content_exists");

    private final Registry<?> forgeRegistry;
    private final ResourceLocation contentName;

    public ContentExistsCondition(Registry<?> forgeRegistry, ResourceLocation contentName) {
        this.forgeRegistry = forgeRegistry;
        this.contentName = contentName;
    }

    @Override
    public void writeParameters(JsonObject json) {
        json.addProperty("registry", getForgeRegistry().key().location().toString());
        json.addProperty("name", getContentName().toString());
    }

    @Override
    public ResourceLocation getConditionId() {
        return NAME;
    }

    public static boolean test(JsonObject json) {
        String registryName = GsonHelper.getAsString(json, "registry");
        Registry<?> forgeRegistry = Registry.REGISTRY.get(new ResourceLocation(registryName));
        if (forgeRegistry == null) {
            throw new JsonParseException("Didn't Find Registry for registry: " + registryName);
        }

        return forgeRegistry.containsKey(new ResourceLocation(GsonHelper.getAsString(json, "name")));
    }

    public Registry<?> getForgeRegistry() {
        return forgeRegistry;
    }

    public ResourceLocation getContentName() {
        return this.contentName;
    }
}
