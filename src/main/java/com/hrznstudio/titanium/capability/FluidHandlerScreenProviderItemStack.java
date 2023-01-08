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
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FluidHandlerScreenProviderItemStack extends SingleVariantItemStorage<FluidVariant> implements IScreenAddonProvider {

    final ContainerItemContext container;
    final long capacity;
    public FluidHandlerScreenProviderItemStack(@Nonnull ContainerItemContext context, long capacity) {
        super(context);
        this.container = context;
        this.capacity = capacity;
    }

    @Nonnull
    @Override
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        return new ArrayList<>();
    }

    @Override
    protected FluidVariant getBlankResource() {
        return FluidVariant.blank();
    }

    @Override
    protected FluidVariant getResource(ItemVariant currentVariant) {
        return FluidVariant.of(getFluid(currentVariant.getNbt()));
    }

    @Override
    protected long getAmount(ItemVariant currentVariant) {
        return !getResource(currentVariant).isBlank() ? getAmount(currentVariant.getNbt()) : 0;
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return capacity;
    }

    @Override
    protected ItemVariant getUpdatedVariant(ItemVariant currentVariant, FluidVariant newResource, long newAmount) {
        ItemStack stack = currentVariant.toStack();
        if (newResource.isBlank() || newAmount == 0) {
            if (stack.getTag() != null) {
                stack.getTag().remove("fluid");
            }
        } else {
            CompoundTag tag = stack.getOrCreateTag();
            CompoundTag fluidTag = new CompoundTag();
            fluidTag.putString("fluid_name", Registry.FLUID.getKey(newResource.getFluid()).toString());
            fluidTag.putLong("amount", newAmount);
            tag.put("fluid", fluidTag);
        }
        return ItemVariant.of(stack);
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    private Fluid getFluid(CompoundTag tag){
        if (tag != null && tag.contains("fluid")) {
            CompoundTag fluidTag = tag.getCompound("fluid");
            if (fluidTag.contains("fluid_name")){
                return Registry.FLUID.get(new ResourceLocation(fluidTag.getString("fluid_name")));
            }
        }
        return Fluids.EMPTY;
    }

    private long getAmount(CompoundTag tag){
        if (tag != null && tag.contains("fluid")) {
            CompoundTag fluidTag = tag.getCompound("fluid");
            if (fluidTag.contains("amount")){
                return fluidTag.getLong("amount");
            }
        }
        return 0;
    }
}
