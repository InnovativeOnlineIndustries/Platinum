/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.container.referenceholder;

import net.minecraft.world.inventory.DataSlot;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

public class FunctionReferenceHolder extends DataSlot {
    private final LongConsumer setter;
    private final LongSupplier getter;

    public FunctionReferenceHolder(LongConsumer setter, LongSupplier getter) {
        this.setter = setter;
        this.getter = getter;
    }

    @Override
    public int get() {
        return (int) getter.getAsLong();
    }

    @Override
    public void set(int i) {
        setter.accept(i);
    }
}
