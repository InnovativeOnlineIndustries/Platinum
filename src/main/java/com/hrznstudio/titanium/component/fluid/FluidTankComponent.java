/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.component.fluid;

import com.google.common.collect.Lists;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.IAssetType;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.client.IScreenAddonProvider;
import com.hrznstudio.titanium.api.client.assets.types.ITankAsset;
import com.hrznstudio.titanium.client.screen.addon.TankScreenAddon;
import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.container.addon.IContainerAddon;
import com.hrznstudio.titanium.container.addon.IContainerAddonProvider;
import com.hrznstudio.titanium.container.addon.IntArrayReferenceHolderAddon;
import com.hrznstudio.titanium.container.referenceholder.FluidTankReferenceHolder;
import io.github.fabricators_of_create.porting_lib.extensions.INBTSerializable;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FluidTankComponent<T extends IComponentHarness> extends FluidTank implements IScreenAddonProvider,
        IContainerAddonProvider, INBTSerializable<CompoundTag> {

    private final int posX;
    private final int posY;
    private String name;
    private T componentHarness;
    private Type tankType;
    private Action tankAction;
    private Runnable onContentChange;

    public FluidTankComponent(String name, long amount, int posX, int posY) {
        super(amount);
        this.posX = posX;
        this.posY = posY;
        this.name = name;
        this.tankType = Type.NORMAL;
        this.tankAction = Action.BOTH;
        this.onContentChange = () -> {
        };
    }

    /**
     * Sets the tile to be automatically marked dirty when the contents change
     *
     * @param componentHarness The tile where the tank is
     * @return itself
     */
    public FluidTankComponent<T> setComponentHarness(T componentHarness) {
        this.componentHarness = componentHarness;
        return this;
    }

    public T getComponentHarness() {
        return componentHarness;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        if (componentHarness != null) {
            componentHarness.markComponentForUpdate(true);
        }
        onContentChange.run();
    }

    public String getName() {
        return name;
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public Type getTankType() {
        return tankType;
    }

    public FluidTankComponent<T> setTankType(Type tankType) {
        this.tankType = tankType;
        return this;
    }

    public FluidTankComponent<T> setOnContentChange(Runnable onContentChange) {
        this.onContentChange = onContentChange;
        return this;
    }

    public Action getTankAction() {
        return tankAction;
    }

    public FluidTankComponent<T> setTankAction(Action tankAction) {
        this.tankAction = tankAction;
        return this;
    }

    @Override
    public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        return getTankAction().canFill() ? super.insert(insertedVariant, maxAmount, transaction) : 0;
    }

    public long fillForced(FluidStack resource, boolean simulate) {
        try (Transaction t = TransferUtil.getTransaction()) {
            long filled = super.insert(resource.getType(), resource.getAmount(), t);
            if (!simulate)
                t.commit();
            return filled;
        }
    }

    @Nonnull
    public FluidStack drainForced(FluidStack resource, boolean simulate) {
        if (resource.isEmpty() || !resource.isFluidEqual(stack)) {
            return FluidStack.EMPTY;
        }
        return drainForced(resource.getAmount(), simulate);
    }

    @Nonnull
    public FluidStack drainForced(long maxDrain, boolean simulate) {
        try (Transaction t = TransferUtil.getTransaction()) {
            FluidStack extracted = new FluidStack(stack, super.extract(stack.getType(), maxDrain, t));
            if (!simulate)
                t.commit();
            return extracted;
        }
    }

    @Override
    public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
        return getTankAction().canDrain() ? super.extract(extractedVariant, maxAmount, transaction) : 0;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        List<IFactory<? extends IScreenAddon>> addons = new ArrayList<>();
        addons.add(() -> new TankScreenAddon(posX, posY, this, tankType));
        return addons;
    }

    @Override
    public List<IFactory<? extends IContainerAddon>> getContainerAddons() {
        return Lists.newArrayList(
                () -> new IntArrayReferenceHolderAddon(new FluidTankReferenceHolder(this))
        );
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.writeToNBT(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.readFromNBT(nbt);
    }

    public enum Type {
        NORMAL(AssetTypes.TANK_NORMAL),
        SMALL(AssetTypes.TANK_SMALL);

        private final IAssetType<ITankAsset> assetType;

        Type(IAssetType<ITankAsset> assetType) {
            this.assetType = assetType;
        }

        public IAssetType<ITankAsset> getAssetType() {
            return assetType;
        }
    }

    public enum Action {
        FILL(true, false),
        DRAIN(false, true),
        BOTH(true, true),
        NONE(false, false);

        private final boolean fill;
        private final boolean drain;

        Action(boolean fill, boolean drain) {
            this.fill = fill;
            this.drain = drain;
        }

        public boolean canFill() {
            return fill;
        }

        public boolean canDrain() {
            return drain;
        }
    }
}
