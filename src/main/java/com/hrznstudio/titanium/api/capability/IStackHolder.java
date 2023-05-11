/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.api.capability;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public interface IStackHolder extends Component {

    Supplier<ItemStack> getHolder();

    void setHolder(Supplier<ItemStack> stack);

}
