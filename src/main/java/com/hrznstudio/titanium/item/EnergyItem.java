/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.item;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleBatteryItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class EnergyItem extends BasicItem implements SimpleBatteryItem {
    private final long capacity;
    private final long input;
    private final long output;

    public EnergyItem(String name, int capacity, int input, int output, Properties properties) {
        super(name, properties.stacksTo(1));
        this.capacity = capacity;
        this.input = input;
        this.output = output;
    }

    public EnergyItem(String name, Properties properties, int capacity, int throughput) {
        this(name, capacity, throughput, throughput, properties);
    }

    public long getEnergyCapacity() {
        return capacity;
    }

    public long getEnergyMaxInput() {
        return input;
    }

    public long getEnergyMaxOutput() {
        return output;
    }

    @Override
    public boolean hasTooltipDetails(@Nullable Key key) {
        return key == Key.SHIFT || super.hasTooltipDetails(key);
    }

    @Override
    public void addTooltipDetails(@Nullable Key key, @Nonnull ItemStack stack, @Nonnull List<Component> tooltip, boolean advanced) {
        super.addTooltipDetails(key, stack, tooltip, advanced);
        if (key == Key.SHIFT) {
            getEnergyStorage(stack).ifPresent(storage ->
                tooltip.add(
                    new TextComponent("").withStyle(ChatFormatting.YELLOW)
                        .append("Energy: ").withStyle(ChatFormatting.RED)
                        .append(String.valueOf(storage.getAmount())).withStyle(ChatFormatting.YELLOW)
                        .append("/").withStyle(ChatFormatting.RED)
                        .append(String.valueOf(storage.getCapacity())).withStyle(ChatFormatting.RESET)));
        }
    }


    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getEnergyStorage(stack).isPresent();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(getEnergyStorage(stack).map(storage -> 1 - (double) storage.getAmount() / (double) storage.getCapacity()).orElse(0.0) * 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00E93232;
    }

    public Optional<EnergyStorage> getEnergyStorage(ItemStack stack) {
        return Optional.ofNullable(EnergyStorage.ITEM.find(stack, ContainerItemContext.withInitial(stack)));
    }
}
