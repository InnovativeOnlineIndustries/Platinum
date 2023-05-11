/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.tab;

import io.github.fabricators_of_create.porting_lib.util.LazyItemGroup;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class TitaniumTab extends LazyItemGroup {
    protected Supplier<ItemStack> stackSupplier;

    public TitaniumTab(String label, Supplier<ItemStack> stackSupplier) {
        super(label);
        this.stackSupplier = stackSupplier;
    }

    @Override
    public ItemStack makeIcon() {
        return stackSupplier.get();
    }

    @Override
    public ItemStack getIconItem() {
        return stackSupplier.get();
    }
}
