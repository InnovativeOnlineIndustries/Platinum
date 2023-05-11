/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.fabric;

import com.google.gson.JsonObject;
import com.hrznstudio.titanium.Titanium;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class ItemExistsCondition implements ConditionJsonProvider {
    public static final ResourceLocation ID = new ResourceLocation(Titanium.MODID, "item_exists");
    
    private final ResourceLocation item;

    public static void init() {
        ResourceConditions.register(ID, jsonObject -> {
            ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(jsonObject, "id"));
            return Registry.ITEM.getOptional(id).isPresent();
        });
    }

    public ItemExistsCondition(String namespace, String path) {
        this(new ResourceLocation(namespace, path));
    }

    public ItemExistsCondition(ResourceLocation item) {
        this.item = item;
    }

    @Override
    public ResourceLocation getConditionId() {
        return ID;
    }

    @Override
    public void writeParameters(JsonObject object) {
        object.addProperty("id", item.toString());
    }
}
