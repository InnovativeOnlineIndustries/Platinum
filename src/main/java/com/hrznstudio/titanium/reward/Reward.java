/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.reward;

import com.google.gson.JsonParser;
import com.hrznstudio.titanium.util.URLUtil;
import net.fabricmc.api.EnvType;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Reward {

    private final ResourceLocation resourceLocation;
    private final URL contributorsURL;
    private String unlocalizedName;
    private List<UUID> players;
    private Supplier<Consumer<EnvType>> register;
    private String[] options;

    public Reward(ResourceLocation resourceLocation, URL contributorsURL, Supplier<Consumer<EnvType>> register, String[] options) {
        this.resourceLocation = resourceLocation;
        this.contributorsURL = contributorsURL;
        this.players = getPlayers(contributorsURL);
        this.register = register;
        this.options = options;
    }

    private static List<UUID> getPlayers(URL url) {
        try {
            List<UUID> players = new ArrayList<>();
            new JsonParser().parse(URLUtil.readUrl(url)).getAsJsonObject().get("uuid").getAsJsonArray().forEach(jsonElement -> players.add(UUID.fromString(jsonElement.getAsString())));
            return players;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Reward setUnlocalizedName(String unlocalizedName) {
        this.unlocalizedName = unlocalizedName;
        return this;
    }

    public void register(EnvType dist) {
        this.register.get().accept(dist);
    }

    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public String[] getOptions() {
        return options;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public boolean isPlayerValid(UUID uuid) {
        return players.contains(uuid);
    }
}
