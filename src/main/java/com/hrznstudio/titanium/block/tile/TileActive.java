/*
 * This file is part of Titanium
 * Copyright (C) 2019, Horizon Studio <contact@hrznstudio.com>, All rights reserved.
 *
 * This means no, you cannot steal this code. This is licensed for sole use by Horizon Studio and its subsidiaries, you MUST be granted specific written permission by Horizon Studio to use this code, thinking you have permission IS NOT PERMISSION!
 */

package com.hrznstudio.titanium.block.tile;

import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IGuiAddon;
import com.hrznstudio.titanium.api.client.IGuiAddonProvider;
import com.hrznstudio.titanium.block.BlockRotation;
import com.hrznstudio.titanium.block.BlockTileBase;
import com.hrznstudio.titanium.block.tile.fluid.MultiTankHandler;
import com.hrznstudio.titanium.block.tile.fluid.PosFluidTank;
import com.hrznstudio.titanium.block.tile.inventory.MultiInventoryHandler;
import com.hrznstudio.titanium.block.tile.inventory.PosInvHandler;
import com.hrznstudio.titanium.block.tile.progress.MultiProgressBarHandler;
import com.hrznstudio.titanium.block.tile.progress.PosProgressBar;
import com.hrznstudio.titanium.block.tile.sideness.IFacingHandler;
import com.hrznstudio.titanium.client.gui.asset.IAssetProvider;
import com.hrznstudio.titanium.container.ContainerTileBase;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileActive extends TileBase implements IGuiAddonProvider, ITickable, IInteractionObject {

    private MultiInventoryHandler multiInventoryHandler;
    private MultiProgressBarHandler multiProgressBarHandler;
    private MultiTankHandler multiTankHandler;

    private List<IFactory<? extends IGuiAddon>> guiAddons;

    public TileActive(BlockTileBase base) {
        super(base);
        this.guiAddons = new ArrayList<>();
    }

    @Override
    public boolean onActivated(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (multiTankHandler != null){
            return FluidUtil.interactWithFluidHandler(playerIn, hand, multiTankHandler.getCapabilityForSide(null));
        }
        return false;
    }

    @Override
    public void onNeighborChanged(Block blockIn, BlockPos fromPos) {

    }

    public void openGui(EntityPlayer player) {
        if (player instanceof EntityPlayerMP)
            Titanium.openGui(this, (EntityPlayerMP) player);
    }

    @Override
    public Container createContainer(InventoryPlayer inventoryPlayer, EntityPlayer entityPlayer) {
        return new ContainerTileBase<>(this, inventoryPlayer);
    }

    @Override
    public String getGuiID() {
        return "titanium:tilegui";
    }

    @Override
    public ITextComponent getName() {
        return new TextComponentString("what. pls");
    }

    @Override
    public ITextComponent getDisplayName() {
        return getName();
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return null;
    }

    /*
            Capability Handling
         */
    public void addInventory(PosInvHandler handler) {
        if (multiInventoryHandler == null) multiInventoryHandler = new MultiInventoryHandler();
        multiInventoryHandler.addInventory(handler.setTile(this));
    }

    public void addProgressBar(PosProgressBar posProgressBar) {
        if (multiProgressBarHandler == null) multiProgressBarHandler = new MultiProgressBarHandler();
        multiProgressBarHandler.addBar(posProgressBar.setTile(this));
    }

    public void addTank(PosFluidTank tank) {
        if (multiTankHandler == null) multiTankHandler = new MultiTankHandler();
        multiTankHandler.addTank(tank);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && multiInventoryHandler != null) {
            return LazyOptional.of(new NonNullSupplier<T>() {
                @Nonnull
                @Override
                public T get() {
                    return (T) multiInventoryHandler.getCapabilityForSide(side);
                }
            });
        }
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && multiTankHandler != null){
            return LazyOptional.of(new NonNullSupplier<T>() {
                @Nonnull
                @Override
                public T get() {
                    return (T) multiTankHandler.getCapabilityForSide(side);
                }
            });
        }
        return LazyOptional.empty();
    }

    public MultiInventoryHandler getMultiInventoryHandler() {
        return multiInventoryHandler;
    }

    /*
        Client
     */

    public void addGuiAddonFactory(IFactory<? extends IGuiAddon> factory) {
        this.guiAddons.add(factory);
    }

    @Override
    public List<IFactory<? extends IGuiAddon>> getGuiAddons() {
        List<IFactory<? extends IGuiAddon>> addons = new ArrayList<>(guiAddons);
        if (multiInventoryHandler != null) addons.addAll(multiInventoryHandler.getGuiAddons());
        if (multiProgressBarHandler != null) addons.addAll(multiProgressBarHandler.getGuiAddons());
        if (multiTankHandler != null) addons.addAll(multiTankHandler.getGuiAddons());
        return addons;
    }

    public IAssetProvider getAssetProvider() {
        return IAssetProvider.DEFAULT_PROVIDER;
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            if (multiProgressBarHandler != null) multiProgressBarHandler.update();
        }
    }

    public EnumFacing getFacingDirection() {
        return this.world.getBlockState(pos).get(BlockRotation.FACING);
    }

    public IFacingHandler getHandlerFromName(String string) {
        for (PosInvHandler handler : multiInventoryHandler.getInventoryHandlers()) {
            if (handler instanceof IFacingHandler && handler.getName().equalsIgnoreCase(string))
                return (IFacingHandler) handler;
        }
        for (PosFluidTank posFluidTank : multiTankHandler.getTanks()) {
            if (posFluidTank instanceof IFacingHandler && posFluidTank.getName().equalsIgnoreCase(string))
                return (IFacingHandler) posFluidTank;
        }
        return null;
    }
}