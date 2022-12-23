/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.itemstack;

import com.google.common.collect.Lists;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.client.IScreenAddonProvider;
import com.hrznstudio.titanium.container.addon.IContainerAddon;
import com.hrznstudio.titanium.container.addon.IContainerAddonProvider;
import com.hrznstudio.titanium.network.IButtonHandler;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemStackHarness implements IContainerAddonProvider, IScreenAddonProvider, IButtonHandler {
    private final ItemStack itemStack;
    private final IButtonHandler buttonHandler;
    private final ItemApiLookup<?, Void>[] capabilities;
    private final IScreenAddonProvider defaultProvider;

    public ItemStackHarness(ItemStack itemStack, IScreenAddonProvider defaultProvider, IButtonHandler buttonHandler, ItemApiLookup<?, Void>... capabilities) {
        this.itemStack = itemStack;
        this.defaultProvider = defaultProvider;
        this.buttonHandler = buttonHandler;
        this.capabilities = capabilities;
    }

    @Override
    @Nonnull
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        List<IFactory<? extends IScreenAddon>> screenAddons = Lists.newArrayList();
        if (defaultProvider != null) screenAddons.addAll(defaultProvider.getScreenAddons());
        for (ItemApiLookup<?, Void> capability : capabilities) {
            Object cap = capability.find(itemStack, null);
            if (cap instanceof IScreenAddonProvider provider)
                screenAddons.addAll(provider.getScreenAddons());
        }
        return screenAddons;
    }

    @Override
    @Nonnull
    public List<IFactory<? extends IContainerAddon>> getContainerAddons() {
        List<IFactory<? extends IContainerAddon>> containerAddons = Lists.newArrayList();
        for (ItemApiLookup<?, Void> capability : capabilities) {
            Object cap = capability.find(itemStack, null);
            if (cap instanceof IContainerAddonProvider provider)
                containerAddons.addAll(provider.getContainerAddons());
        }
        return containerAddons;
    }

    @Override
    public void handleButtonMessage(int id, Player playerEntity, CompoundTag compound) {
        if (buttonHandler != null) {
            buttonHandler.handleButtonMessage(id, playerEntity, compound);
        }
    }
}
