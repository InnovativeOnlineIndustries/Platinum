/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.container.impl;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.BooleanSupplier;

public class DisableableItemHandlerSlot extends SlotItemHandler {
    private final BooleanSupplier isDisabled;

    public DisableableItemHandlerSlot(ItemStackHandler itemHandler, int index, int xPosition, int yPosition, BasicInventoryContainer basicInventoryContainer) {
        this(itemHandler, index, xPosition, yPosition, basicInventoryContainer::isDisabled);
    }

    public DisableableItemHandlerSlot(ItemStackHandler itemHandler, int index, int xPosition, int yPosition, BooleanSupplier isDisabled) {
        super(itemHandler, index, xPosition, yPosition);
        this.isDisabled = isDisabled;
    }

    @Override
    public boolean isActive() {
        return !isDisabled.getAsBoolean();
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return !isDisabled.getAsBoolean();
    }
}
