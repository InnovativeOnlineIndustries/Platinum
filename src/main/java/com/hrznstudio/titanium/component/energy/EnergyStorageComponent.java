/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.component.energy;

import com.google.common.collect.Lists;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.client.IScreenAddonProvider;
import com.hrznstudio.titanium.client.screen.addon.EnergyBarScreenAddon;
import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.container.addon.IContainerAddon;
import com.hrznstudio.titanium.container.addon.IContainerAddonProvider;
import com.hrznstudio.titanium.container.addon.IntReferenceHolderAddon;
import com.hrznstudio.titanium.container.referenceholder.FunctionReferenceHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import javax.annotation.Nonnull;
import java.util.List;

public class EnergyStorageComponent<T extends IComponentHarness> extends SimpleEnergyStorage implements
    IScreenAddonProvider, IContainerAddonProvider {

    private final int xPos;
    private final int yPos;

    protected T componentHarness;

    public EnergyStorageComponent(long maxCapacity, int xPos, int yPos) {
        this(maxCapacity, maxCapacity, xPos, yPos);
    }

    public EnergyStorageComponent(long maxCapacity, long maxIO, int xPos, int yPos) {
        this(maxCapacity, maxIO, maxIO, xPos, yPos);
    }

    public EnergyStorageComponent(long maxCapacity, long maxReceive, long maxExtract, int xPos, int yPos) {
        super(maxCapacity, maxReceive, maxExtract);
        this.xPos = xPos;
        this.yPos = yPos;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        long amount = super.insert(maxAmount, transaction);
        if (amount > 0) {
            this.update();
        }
        return amount;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        long amount = super.extract(maxAmount, transaction);
        if (amount > 0) {
            this.update();
        }
        return amount;
    }

    public void setEnergyStored(long energy) {
        if (energy > this.getCapacity()) {
            this.amount = this.getCapacity();
        } else {
            this.amount = Math.max(energy, 0);
        }
        this.update();
    }

    @Override
    @Nonnull
    @Environment(EnvType.CLIENT)
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        return Lists.newArrayList(
            () -> new EnergyBarScreenAddon(xPos, yPos, this)
        );
    }

    @Override
    @Nonnull
    public List<IFactory<? extends IContainerAddon>> getContainerAddons() {
        return Lists.newArrayList(
            () -> new IntReferenceHolderAddon(new FunctionReferenceHolder(this::setEnergyStored, this::getAmount))
        );
    }

    public void setComponentHarness(T componentHarness) {
        this.componentHarness = componentHarness;
    }

    private void update() {
        if (this.componentHarness != null) {
            this.componentHarness.markComponentForUpdate(true);
        }
    }

    public int getX() {
        return xPos;
    }

    public int getY() {
        return yPos;
    }

    @Override
    protected void onFinalCommit() {
        update();
    }
}

