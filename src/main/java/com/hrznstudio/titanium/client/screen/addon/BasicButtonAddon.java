/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.client.screen.addon;

import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.component.button.ButtonComponent;
import com.hrznstudio.titanium.network.locator.ILocatable;
import com.hrznstudio.titanium.network.messages.ButtonClickNetworkMessage;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.fabricators_of_create.porting_lib.mixin.client.accessor.AbstractContainerScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.Collections;
import java.util.List;

public class BasicButtonAddon extends BasicScreenAddon {

    private ButtonComponent button;

    public BasicButtonAddon(ButtonComponent buttonComponent) {
        super(buttonComponent.getPosX(), buttonComponent.getPosY());
        this.button = buttonComponent;
    }

    @Override
    public void drawBackgroundLayer(PoseStack stack, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {}

    @Override
    public void drawForegroundLayer(PoseStack stack, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {}

    @Override
    public List<Component> getTooltipLines() {
        return Collections.emptyList();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof AbstractContainerScreen && ((AbstractContainerScreen) screen).getMenu() instanceof ILocatable) {
            if (!isMouseOver(mouseX - ((AbstractContainerScreenAccessor) screen).port_lib$getGuiLeft(), mouseY - ((AbstractContainerScreenAccessor) screen).port_lib$getGuiTop()))
                return false;
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 1f, 1f, RandomSource.create(), Minecraft.getInstance().player.blockPosition())); //getPosition
            ILocatable locatable = (ILocatable) ((AbstractContainerScreen) screen).getMenu();
            Titanium.NETWORK.get().sendToServer(new ButtonClickNetworkMessage(locatable.getLocatorInstance(), this.button.getId(), new CompoundTag()));
        }
        return true;
    }

    @Override
    public int getXSize() {
        return button.getSizeX();
    }

    @Override
    public int getYSize() {
        return button.getSizeY();
    }

    public ButtonComponent getButton() {
        return button;
    }
}
