/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.capability;

import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.client.IScreenAddonProvider;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantItemStorage;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FluidHandlerScreenProviderItemStack extends SingleVariantItemStorage<FluidVariant> implements IScreenAddonProvider {
    private final ContainerItemContext context;
    protected long capacity;

    public FluidHandlerScreenProviderItemStack(@Nonnull ContainerItemContext context, long capacity) {
        super(context);
        this.context = context;
        this.capacity = capacity;
    }

    @Nonnull
    @Override
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        return new ArrayList<>();
    }

    public FluidStack getFluid() {
        CompoundTag tagCompound = context.getItemVariant().getNbt();
        if (tagCompound == null || !tagCompound.contains("Fluid"))
            return FluidStack.EMPTY;
        return FluidStack.loadFluidStackFromNBT(tagCompound.getCompound("Fluid"));
    }

    @Override
    protected FluidVariant getBlankResource() {
        return FluidVariant.blank();
    }

    @Override
    protected FluidVariant getResource(ItemVariant currentVariant) {
        return getFluid().getType();
    }

    @Override
    protected long getAmount(ItemVariant currentVariant) {
        return getFluid().getAmount();
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return capacity;
    }

    @Override
    protected ItemVariant getUpdatedVariant(ItemVariant currentVariant, FluidVariant newResource, long newAmount) {
        return currentVariant;
    }

    @Override
    public long getCapacity() {
        return this.capacity;
    }
}
