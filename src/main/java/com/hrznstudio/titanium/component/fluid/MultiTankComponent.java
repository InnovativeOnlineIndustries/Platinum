/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.component.fluid;

import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.client.IScreenAddonProvider;
import com.hrznstudio.titanium.component.IComponentHandler;
import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.component.sideness.ICapabilityHolder;
import com.hrznstudio.titanium.component.sideness.IFacingComponent;
import com.hrznstudio.titanium.container.addon.IContainerAddon;
import com.hrznstudio.titanium.container.addon.IContainerAddonProvider;
import com.hrznstudio.titanium.util.FacingUtil;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class MultiTankComponent<T extends IComponentHarness> implements IScreenAddonProvider, IContainerAddonProvider,
    ICapabilityHolder<MultiTankComponent.MultiTankCapabilityHandler<T>>, IComponentHandler {

    private final LinkedHashSet<FluidTankComponent<T>> tanks;
    private final HashMap<FacingUtil.Sideness, LazyOptional<MultiTankCapabilityHandler<T>>> lazyOptionals;

    public MultiTankComponent() {
        tanks = new LinkedHashSet<>();
        this.lazyOptionals = new HashMap<>();
        lazyOptionals.put(null, LazyOptional.empty());
        for (FacingUtil.Sideness value : FacingUtil.Sideness.values()) {
            lazyOptionals.put(value, LazyOptional.empty());
        }
    }

    @Override
    public void add(Object... component) {
        Arrays.stream(component).filter(this::accepts).forEach(tank -> {
            this.tanks.add((FluidTankComponent<T>) tank);
            rebuildCapability(new FacingUtil.Sideness[]{null});
            rebuildCapability(FacingUtil.Sideness.values());
        });
    }

    private boolean accepts(Object component) {
        return component instanceof FluidTankComponent;
    }

    private void rebuildCapability(FacingUtil.Sideness[] sides) {
        for (FacingUtil.Sideness side : sides) {
            lazyOptionals.get(side).invalidate();
            lazyOptionals.put(side, LazyOptional.of(() -> new MultiTankCapabilityHandler<>(getHandlersForSide(side))));
        }
    }

    private List<FluidTankComponent<T>> getHandlersForSide(FacingUtil.Sideness sideness) {
        if (sideness == null) {
            return new ArrayList<>(tanks);
        }
        List<FluidTankComponent<T>> handlers = new ArrayList<>();
        for (FluidTankComponent<T> tankHandler : tanks) {
            if (tankHandler instanceof IFacingComponent) {
                if (((IFacingComponent) tankHandler).getFacingModes().containsKey(sideness) &&
                        ((IFacingComponent) tankHandler).getFacingModes().get(sideness).allowsConnection()) {
                    handlers.add(tankHandler);
                }
            } else {
                handlers.add(tankHandler);
            }
        }
        return handlers;
    }

    @Nonnull
    @Override
    public LazyOptional<MultiTankCapabilityHandler<T>> getCapabilityForSide(@Nullable FacingUtil.Sideness sideness) {
        return lazyOptionals.get(sideness);
    }

    @Override
    public boolean handleFacingChange(String handlerName, FacingUtil.Sideness facing, int mode) {
        for (FluidTankComponent<T> tankHandler : tanks) {
            if (tankHandler.getName().equals(handlerName) && tankHandler instanceof IFacingComponent) {
                ((IFacingComponent) tankHandler).getFacingModes().put(facing, ((IFacingComponent) tankHandler).getValidFacingModes()[mode]);
                rebuildCapability(new FacingUtil.Sideness[]{facing});
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<LazyOptional<MultiTankCapabilityHandler<T>>> getLazyOptionals() {
        return this.lazyOptionals.values();
    }

    public HashSet<FluidTankComponent<T>> getTanks() {
        return tanks;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        List<IFactory<? extends IScreenAddon>> addons = new ArrayList<>();
        for (FluidTankComponent<T> tank : tanks) {
            addons.addAll(tank.getScreenAddons());
        }
        return addons;
    }

    @Override
    public List<IFactory<? extends IContainerAddon>> getContainerAddons() {
        List<IFactory<? extends IContainerAddon>> addons = new ArrayList<>();
        for (FluidTankComponent<T> tank : tanks) {
            addons.addAll(tank.getContainerAddons());
        }
        return addons;
    }

    public static class MultiTankCapabilityHandler<T extends IComponentHarness> extends CombinedStorage<FluidVariant, FluidTankComponent<T>> {

        public MultiTankCapabilityHandler(List<FluidTankComponent<T>> tanks) {
            super(tanks);
        }

        public boolean isEmpty() {
            return parts.isEmpty();
        }
    }
}
