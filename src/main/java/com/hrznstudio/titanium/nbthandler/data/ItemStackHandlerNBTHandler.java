/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.nbthandler.data;


import com.hrznstudio.titanium.api.INBTHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemStackHandlerNBTHandler implements INBTHandler<ItemStackHandler> {
    @Override
    public boolean isClassValid(Class<?> aClass) {
        return ItemStackHandler.class.isAssignableFrom(aClass);
    }

    @Override
    public boolean storeToNBT(@Nonnull CompoundTag compound, @Nonnull String name, @Nonnull ItemStackHandler object) {
        compound.put(name, object.serializeNBT());
        return true;
    }

    @Override
    public ItemStackHandler readFromNBT(@Nonnull CompoundTag compound, @Nonnull String name, @Nullable ItemStackHandler current) {
        if (compound.contains(name)) {
            if (current == null) current = new ItemStackHandler();
            current.deserializeNBT(compound.getCompound(name));
            return current;
        }
        return current;
    }
}
