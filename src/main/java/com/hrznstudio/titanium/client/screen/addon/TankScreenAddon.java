/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.client.screen.addon;

import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.api.client.assets.types.ITankAsset;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.hrznstudio.titanium.component.fluid.FluidTankComponent;
import com.hrznstudio.titanium.network.locator.ILocatable;
import com.hrznstudio.titanium.network.messages.ButtonClickNetworkMessage;
import com.hrznstudio.titanium.util.AssetUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.fabricators_of_create.porting_lib.mixin.client.accessor.AbstractContainerScreenAccessor;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import me.alphamode.forgetags.Tags;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BucketItem;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TankScreenAddon extends BasicScreenAddon {

    private SingleSlotStorage<FluidVariant> tank;
    private ITankAsset asset;
    private FluidTankComponent.Type type;

    public TankScreenAddon(int posX, int posY, SingleSlotStorage<FluidVariant> tank, FluidTankComponent.Type type) {
        super(posX, posY);
        this.tank = tank;
        this.type = type;
    }

    @Override
    public void drawBackgroundLayer(PoseStack stack, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        asset = IAssetProvider.getAsset(provider, type.getAssetType());
        Rectangle area = asset.getArea();
        FluidStack fluidStack = new FluidStack(tank.getResource(), tank.getAmount());
        if (!fluidStack.isEmpty()) {
            double stored = tank.getAmount();
            double capacity = tank.getCapacity();
            int topBottomPadding = asset.getFluidRenderPadding(Direction.UP) + asset.getFluidRenderPadding(Direction.DOWN);
            int offset = (int) ((stored / capacity) * (area.height - topBottomPadding));
            TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluidStack.getType());
            if (sprite != null) {
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
                Color color = new Color(FluidVariantRendering.getColor(fluidStack.getType()));
                RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
                RenderSystem.enableBlend();
                Screen.blit(stack, this.getPosX() + guiX + asset.getFluidRenderPadding(Direction.WEST),
                    this.getPosY() + guiY + asset.getFluidRenderPadding(Direction.UP) + (fluidStack.getFluid().getAttributes().isGaseous() ? 0 : (area.height - topBottomPadding) - offset),
                    0,
                    (int) (area.getWidth() - asset.getFluidRenderPadding(Direction.EAST) - asset.getFluidRenderPadding(Direction.WEST)),
                    offset,
                    sprite);
                RenderSystem.disableBlend();
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
        }
        RenderSystem.setShaderColor(1, 1, 1, 1);
        ITankAsset asset = IAssetProvider.getAsset(provider, type.getAssetType());
        AssetUtil.drawAsset(stack, screen, asset, guiX + getPosX(), guiY + getPosY());
    }

    @Override
    public void drawForegroundLayer(PoseStack stack, Screen screen, IAssetProvider provider, int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {}

    @Override
    public List<Component> getTooltipLines() {
        List<Component> strings = new ArrayList<>();
        strings.add(new net.minecraft.network.chat.TextComponent(ChatFormatting.GOLD + new TranslatableComponent("tooltip.titanium.tank.fluid").getString()).append((tank.getAmount() <= 0L || tank.isResourceBlank()) ? new TranslatableComponent("tooltip.titanium.tank.empty").withStyle(ChatFormatting.WHITE) : FluidVariantAttributes.getName(tank.getResource())).withStyle(ChatFormatting.WHITE));
        strings.add(new TranslatableComponent("tooltip.titanium.tank.amount").withStyle(ChatFormatting.GOLD).append(new TextComponent(ChatFormatting.WHITE + new DecimalFormat().format(tank.getAmount()) + ChatFormatting.GOLD + "/" + ChatFormatting.WHITE + new DecimalFormat().format(tank.getCapacity()) + ChatFormatting.DARK_AQUA + "mb")));
        if (!Minecraft.getInstance().player.containerMenu.getCarried().isEmpty() && ContainerItemContext.ofPlayerCursor(Minecraft.getInstance().player, Minecraft.getInstance().player.containerMenu).find(FluidStorage.ITEM) != null) {
            Storage<FluidVariant> iFluidHandlerItem = ContainerItemContext.ofPlayerCursor(Minecraft.getInstance().player, Minecraft.getInstance().player.containerMenu).find(FluidStorage.ITEM);
            if (iFluidHandlerItem != null) {
                boolean isBucket = Minecraft.getInstance().player.containerMenu.getCarried().getItem() instanceof BucketItem;
                long amount = isBucket ? FluidConstants.BUCKET : Long.MAX_VALUE;
                boolean canFillFromItem = false;
                boolean canDrainFromItem = false;
                try (Transaction t = TransferUtil.getTransaction()) {
                    if (isBucket) {
                        canFillFromItem = StorageUtil.move(iFluidHandlerItem, tank, v -> true, amount, t) == FluidConstants.BUCKET;
                        canDrainFromItem = StorageUtil.move(tank, iFluidHandlerItem, v -> true, amount, t) == FluidConstants.BUCKET;
                    } else {
                        canFillFromItem = StorageUtil.move(iFluidHandlerItem, tank, v -> true, amount, t) > 0;
                        canDrainFromItem = StorageUtil.move(tank, iFluidHandlerItem, v -> true, amount, t) > 0;
                    }
                }
                if (canFillFromItem)
                    strings.add(new TranslatableComponent("tooltip.titanium.tank.can_fill_from_item").withStyle(ChatFormatting.BLUE));
                if (canDrainFromItem)
                    strings.add(new TranslatableComponent("tooltip.titanium.tank.can_drain_from_item").withStyle(ChatFormatting.GOLD));
                if (canFillFromItem)
                    strings.add(new TranslatableComponent("tooltip.titanium.tank.action_fill").withStyle(ChatFormatting.DARK_GRAY));
                if (canDrainFromItem)
                    strings.add(new TranslatableComponent("tooltip.titanium.tank.action_drain").withStyle(ChatFormatting.DARK_GRAY));
                if (!canDrainFromItem && !canFillFromItem) {
                    strings.add(new TranslatableComponent("tooltip.titanium.tank.no_action").withStyle(ChatFormatting.RED));
                }
            }
        } else {
            strings.add(new TranslatableComponent("tooltip.titanium.tank.no_tank").withStyle(ChatFormatting.DARK_GRAY));
        }
        return strings;
    }

    @Override
    public int getXSize() {
        return asset != null ? asset.getArea().width : 0;
    }

    @Override
    public int getYSize() {
        return asset != null ? asset.getArea().height : 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!Minecraft.getInstance().player.containerMenu.getCarried().isEmpty() && ContainerItemContext.ofPlayerCursor(Minecraft.getInstance().player, Minecraft.getInstance().player.containerMenu).find(FluidStorage.ITEM) != null) {
            Screen screen = Minecraft.getInstance().screen;
            if (screen instanceof AbstractContainerScreen && ((AbstractContainerScreen) screen).getMenu() instanceof ILocatable) {
                if (!isMouseOver(mouseX - ((AbstractContainerScreenAccessor) screen).port_lib$getGuiLeft(), mouseY - ((AbstractContainerScreenAccessor) screen).port_lib$getGuiTop()))
                    return false;
                Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(SoundEvents.UI_BUTTON_CLICK, SoundSource.PLAYERS, 1f, 1f, Minecraft.getInstance().player.blockPosition())); //getPosition
                ILocatable locatable = (ILocatable) ((AbstractContainerScreen) screen).getMenu();
                CompoundTag compoundNBT = new CompoundTag();
                if (tank instanceof FluidTankComponent) {
                    compoundNBT.putString("Name", ((FluidTankComponent<?>) tank).getName());
                } else {
                    compoundNBT.putBoolean("Invalid", true);
                }
                Storage<FluidVariant> iFluidHandlerItem = ContainerItemContext.ofPlayerCursor(Minecraft.getInstance().player, Minecraft.getInstance().player.containerMenu).find(FluidStorage.ITEM);
                if (iFluidHandlerItem != null) {
                    boolean isBucket = Minecraft.getInstance().player.containerMenu.getCarried().getItem() instanceof BucketItem;
                    long amount = isBucket ? FluidConstants.BUCKET : Long.MAX_VALUE;
                    boolean canFillFromItem = false;
                    boolean canDrainFromItem = false;
                    try (Transaction t = TransferUtil.getTransaction()) {
                        if (isBucket) {
                            canFillFromItem = StorageUtil.move(iFluidHandlerItem, tank, v -> true, amount, t) == FluidConstants.BUCKET;
                            canDrainFromItem = StorageUtil.move(tank, iFluidHandlerItem, v -> true, amount, t) == FluidConstants.BUCKET;
                        } else {
                            canFillFromItem = StorageUtil.move(iFluidHandlerItem, tank, v -> true, amount, t) > 0;
                            canDrainFromItem = StorageUtil.move(tank, iFluidHandlerItem, v -> true, amount, t) > 0;
                        }
                    }
                    if (canFillFromItem && button == 0) compoundNBT.putBoolean("Fill", true);
                    if (canDrainFromItem && button == 1) compoundNBT.putBoolean("Fill", false);
                }
                Titanium.NETWORK.get().sendToServer(new ButtonClickNetworkMessage(locatable.getLocatorInstance(), -3, compoundNBT));
                return true;
            }
        }
        return false;
    }
}
