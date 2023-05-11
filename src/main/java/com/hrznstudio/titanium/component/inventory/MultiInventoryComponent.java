/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.component.inventory;

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
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import javax.annotation.Nonnull;
import java.util.*;


public class MultiInventoryComponent<T extends IComponentHarness> implements IScreenAddonProvider, IContainerAddonProvider,
    ICapabilityHolder<MultiInventoryComponent.MultiInvCapabilityHandler<T>>, IComponentHandler {

    private final LinkedHashSet<InventoryComponent<T>> inventoryHandlers;
    private final Map<FacingUtil.Sideness, LazyOptional<MultiInvCapabilityHandler<T>>> lazyOptionals;

    public MultiInventoryComponent() {
        this.inventoryHandlers = new LinkedHashSet<>();
        this.lazyOptionals = new HashMap<>();
        lazyOptionals.put(null, LazyOptional.empty());
        for (FacingUtil.Sideness value : FacingUtil.Sideness.values()) {
            lazyOptionals.put(value, LazyOptional.empty());
        }
    }

    @Override
    public void add(Object... component) {
        Arrays.stream(component).filter(this::accepts).forEach(inventoryComponent -> {
            this.inventoryHandlers.add((InventoryComponent<T>) inventoryComponent);
            rebuildCapability(FacingUtil.Sideness.values());
        });
    }

    private boolean accepts(Object component) {
        return component instanceof InventoryComponent;
    }

    private void rebuildCapability(FacingUtil.Sideness[] sides) {
        for (FacingUtil.Sideness side : sides) {
            lazyOptionals.get(side).invalidate();
            lazyOptionals.put(side, LazyOptional.of(() -> new MultiInvCapabilityHandler<>(getHandlersForSide(side))));
        }
    }

    private List<InventoryComponent<T>> getHandlersForSide(FacingUtil.Sideness sideness) {
        if (sideness == null) {
            return new ArrayList<>(inventoryHandlers);
        }
        List<InventoryComponent<T>> handlers = new ArrayList<>();
        for (InventoryComponent<T> inventoryHandler : inventoryHandlers) {
            if (inventoryHandler instanceof IFacingComponent) {
                if (((IFacingComponent) inventoryHandler).getFacingModes().containsKey(sideness) &&
                        ((IFacingComponent) inventoryHandler).getFacingModes().get(sideness).allowsConnection()) {
                    handlers.add(inventoryHandler);
                }
            } else {
                handlers.add(inventoryHandler);
            }
        }
        return handlers;
    }

    @Nonnull
    @Override
    public LazyOptional<MultiInvCapabilityHandler<T>> getCapabilityForSide(FacingUtil.Sideness sideness) {
        return lazyOptionals.get(sideness);
    }

    @Override
    public boolean handleFacingChange(String handlerName, FacingUtil.Sideness facing, int mode) {
        for (InventoryComponent<T> inventoryHandler : inventoryHandlers) {
            if (inventoryHandler.getName().equals(handlerName) && inventoryHandler instanceof IFacingComponent) {
                ((IFacingComponent) inventoryHandler).getFacingModes().put(facing, ((IFacingComponent) inventoryHandler).getValidFacingModes()[mode]);
                rebuildCapability(new FacingUtil.Sideness[]{facing});
                return true;
            }
        }
        return false;
    }

    public HashSet<InventoryComponent<T>> getInventoryHandlers() {
        return inventoryHandlers;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        List<IFactory<? extends IScreenAddon>> addons = new ArrayList<>();
        inventoryHandlers.forEach(posInvHandler -> addons.addAll(posInvHandler.getScreenAddons()));
        return addons;
    }

    @Override
    public List<IFactory<? extends IContainerAddon>> getContainerAddons() {
        List<IFactory<? extends IContainerAddon>> addons = new ArrayList<>();
        for (InventoryComponent<T> inventoryComponent: inventoryHandlers) {
            addons.addAll(inventoryComponent.getContainerAddons());
        }
        return addons;
    }

    public static class MultiInvCapabilityHandler<T extends IComponentHarness> extends ItemStackHandler {

        private final List<InventoryComponent<T>> inventoryHandlers;
        private int slotAmount;

        public MultiInvCapabilityHandler(List<InventoryComponent<T>> inventoryHandlers) {
            this.inventoryHandlers = inventoryHandlers;
            this.slotAmount = 0;
            for (InventoryComponent<T> inventoryHandler : this.inventoryHandlers) {
                slotAmount += inventoryHandler.getSlots();
            }
        }

        @Override
        public int getSlots() {
            return slotAmount;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            for (int i = 0; i < getSlots(); i++) {
                InventoryComponent<T> handler = getFromSlot(i);
                if (handler != null) {
                    if (handler.getInsertPredicate().test(resource.toStack((int) maxAmount), i)) {
                        return handler.insert(resource, maxAmount, transaction);
                    } else {
                        return maxAmount;
                    }
                }
            }
            return super.insert(resource, maxAmount, transaction);
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            for (int i = 0; i < getSlots(); i++) {
                InventoryComponent<T> handler = getFromSlot(i);
                if (handler != null) {
                    int relativeSlot = getRelativeSlot(handler, i);
                    if (!handler.getExtractPredicate().test(handler.getStackInSlot(relativeSlot), relativeSlot))
                        return 0;
                    return handler.extract(resource, maxAmount, transaction);
                }
            }
            return super.extract(resource, maxAmount, transaction);
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            InventoryComponent<T> handler = getFromSlot(slot);
            if (handler != null) {
                return handler.getStackInSlot(getRelativeSlot(handler, slot));
            }
            return super.getStackInSlot(slot);
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            InventoryComponent<T> handler = getFromSlot(slot);
            if (handler != null) {
                handler.setStackInSlot(getRelativeSlot(handler, slot), stack);
            }
            super.setStackInSlot(slot, stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            InventoryComponent<T> handler = getFromSlot(slot);
            if (handler != null) {
                handler.getSlotLimit(getRelativeSlot(handler, slot));
            }
            return super.getSlotLimit(slot);
        }

        public InventoryComponent<T> getFromSlot(int slot) {
            for (InventoryComponent<T> handler : inventoryHandlers) {
                slot -= handler.getSlots();
                if (slot < 0) {
                    return handler;
                }
            }
            return null;
        }

        public int getRelativeSlot(InventoryComponent<T> handler, int slot) {
            for (InventoryComponent<T> h : inventoryHandlers) {
                if (h.equals(handler)) return slot;
                slot -= h.getSlots();
            }
            return 0;
        }
    }

    @Override
    public Collection<LazyOptional<MultiInvCapabilityHandler<T>>> getLazyOptionals() {
        return lazyOptionals.values();
    }
}
