/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.container.referenceholder;

import com.hrznstudio.titanium.component.fluid.FluidTankComponent;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.minecraft.core.Registry;
import net.minecraft.world.inventory.ContainerData;

public class FluidTankReferenceHolder implements ContainerData {
    private final FluidTankComponent<?> fluidTank;
    private int fluidAmount = -1;
    private int fluidId = -1;

    public FluidTankReferenceHolder(FluidTankComponent<?> fluidTank) {
        this.fluidTank = fluidTank;
    }

    @Override
    public int get(int index) {
        FluidStack fluidStack = this.fluidTank.getFluid();
        if (fluidStack.isEmpty()) {
            return -1;
        } else if (index == 0) {
            return Registry.FLUID.getId(fluidStack.getFluid());
        } else {
            return (int) fluidStack.getAmount();
        }
    }

    @Override
    public void set(int index, int value) {
        if (index == 0) {
            fluidId = value;
        } else {
            fluidAmount = value;
        }

        if (fluidAmount >= 0 && fluidId >= 0) {
            fluidTank.setFluid(new FluidStack(Registry.FLUID.byId(fluidId), fluidAmount));
        } else {
            fluidTank.setFluid(FluidStack.EMPTY);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
