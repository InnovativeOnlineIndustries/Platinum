/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.component.inventory;

import com.google.common.collect.Lists;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.client.IScreenAddonProvider;
import com.hrznstudio.titanium.client.screen.addon.SlotsScreenAddon;
import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.container.addon.IContainerAddon;
import com.hrznstudio.titanium.container.addon.IContainerAddonProvider;
import com.hrznstudio.titanium.container.addon.SlotContainerAddon;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.util.ItemStackUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public class InventoryComponent<T extends IComponentHarness> extends ItemStackHandler implements IScreenAddonProvider,
        IContainerAddonProvider {

    private final String name;
    private int xPos;
    private int yPos;
    private int xSize;
    private int ySize;
    private T componentHarness;
    private BiPredicate<ItemStack, Integer> insertPredicate;
    private BiPredicate<ItemStack, Integer> extractPredicate;
    private BiConsumer<ItemStack, Integer> onSlotChanged;
    private Map<Integer, Integer> slotAmountFilter;
    private Map<Integer, ItemStack> slotToStackRenderMap;
    private boolean colorGuiEnabled;
    private Map<Integer, Color> slotToColorRenderMap;
    private int slotLimit;
    private Function<Integer, Pair<Integer, Integer>> slotPosition;
    private Predicate<Integer> slotVisiblePredicate;

    public InventoryComponent(String name, int xPos, int yPos, int size) {
        this.name = name;
        this.xPos = xPos;
        this.yPos = yPos;
        this.setSize(size);
        this.setRange(size, 1);
        this.insertPredicate = (stack, integer) -> true;
        this.extractPredicate = (stack, integer) -> true;
        this.onSlotChanged = (stack, integer) -> {
        };
        this.slotAmountFilter = new HashMap<>();
        this.slotToStackRenderMap = new HashMap<>();
        this.colorGuiEnabled = false;
        this.slotToColorRenderMap = new HashMap<>();
        this.slotLimit = 64;
        this.slotPosition = integer -> Pair.of(18 * (integer % xSize), 18 * (integer / xSize));
        this.slotVisiblePredicate = integer -> true;
    }

    /**
     * Defines how many slots/row and slots/column
     *
     * @param x How many slots there are horizontally
     * @param y How many slots there are vertically
     * @return itself
     */
    public InventoryComponent<T> setRange(int x, int y) {
        this.xSize = x;
        this.ySize = y;
        return this;
    }

    /**
     * Sets the tile where the inventory is to allow markForUpdate automatically
     *
     * @param componentHarness the object which owns this component
     * @return itself
     */
    public InventoryComponent<T> setComponentHarness(T componentHarness) {
        this.componentHarness = componentHarness;
        return this;
    }

    /**
     * Sets the predicate input filter to filter what items go into which slot.
     *
     * @param predicate A bi predicate where the itemstack is the item trying to be inserted and the slot where is trying to be inserted to
     * @return itself
     */
    public InventoryComponent<T> setInputFilter(BiPredicate<ItemStack, Integer> predicate) {
        this.insertPredicate = predicate;
        return this;
    }

    /**
     * Sets the predicate output filter to filter what can be extracted from which slot.
     *
     * @param predicate A bi predicate where the itemstack is the item trying to be extracted and the slot where is trying to be extracted
     * @return itself
     */
    public InventoryComponent<T> setOutputFilter(BiPredicate<ItemStack, Integer> predicate) {
        this.extractPredicate = predicate;
        return this;
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        long inserted = 0;
        updateSnapshots(transaction);
        for (int i = 0; i < getSlots(); i++) {
            ItemStack held = stacks[i];
            if (held.isEmpty()) { // just throw in a full stack
                int toFill = (int) Math.min(getStackLimit(i, resource), maxAmount);
                maxAmount -= toFill;
                inserted += toFill;
                ItemStack stack = resource.toStack(toFill);
                contentsChangedInternal(i, stack, transaction);
            } else if (ItemStackUtil.canItemStacksStack(held, resource.toStack())) { // already filled, but can stack
                int max = getStackLimit(i, resource); // total possible
                int canInsert = max - held.getCount(); // room available
                int actuallyInsert = Math.min(canInsert, (int) maxAmount);
                if (actuallyInsert > 0) {
                    maxAmount -= actuallyInsert;
                    inserted += actuallyInsert;
                    held = held.copy();
                    held.grow(actuallyInsert);
                    contentsChangedInternal(i, held, transaction);
                }
            }
            if (maxAmount == 0)
                break;
        }
        return inserted;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (this.componentHarness != null) {
            componentHarness.markComponentDirty();
        }
        onSlotChanged.accept(getStackInSlot(slot), slot);
    }

    public String getName() {
        return name;
    }

    public int getXPos() {
        return xPos;
    }

    public void setXPos(int xPos) {
        this.xPos = xPos;
    }

    public int getYPos() {
        return yPos;
    }

    public void setYPos(int yPos) {
        this.yPos = yPos;
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public T getComponentHarness() {
        return componentHarness;
    }

    public BiPredicate<ItemStack, Integer> getInsertPredicate() {
        return insertPredicate;
    }

    public BiPredicate<ItemStack, Integer> getExtractPredicate() {
        return extractPredicate;
    }

    public BiConsumer<ItemStack, Integer> getOnSlotChanged() {
        return onSlotChanged;
    }

    public InventoryComponent<T> setColorGuiEnabled(boolean colorGuiEnabled) {
        this.colorGuiEnabled = colorGuiEnabled;
        return this;
    }

    public boolean isColorGuiEnabled() {
        return colorGuiEnabled;
    }

    public Map<Integer, Color> getSlotToColorRenderMap() {
        return slotToColorRenderMap;
    }

    /**
     * Sets the predicate slot changed that gets triggered when a slot is changed.
     *
     * @param onSlotChanged A bi predicate where the itemstack and slot changed
     * @return itself
     */
    public InventoryComponent<T> setOnSlotChanged(BiConsumer<ItemStack, Integer> onSlotChanged) {
        this.onSlotChanged = onSlotChanged;
        return this;
    }

    /**
     * Sets the limit amount for a specific slot, this limit has priority instead of the slot limit for all the slots
     *
     * @param slot  The slot to set the limit to
     * @param limit The limit for the slot
     * @return itself
     */
    public InventoryComponent<T> setSlotLimit(int slot, int limit) {
        this.slotAmountFilter.put(slot, limit);
        return this;
    }

    /**
     * @param slot The slot to render the stack in
     * @param stack The Itemstack to render in the slot
     * @return itself
     */
    public InventoryComponent<T> setSlotToItemStackRender(int slot, ItemStack stack) {
        this.slotToStackRenderMap.put(slot, stack);
        return this;
    }

    /**
     * @param slot Slot which color should be set.
     * @param color The color of the slot
     * @return Returns the inventorycomponent
     */
    public InventoryComponent<T> setSlotToColorRender(int slot, int color) {
        if (!this.colorGuiEnabled) {
            this.setColorGuiEnabled(true);
        }
        this.slotToColorRenderMap.put(slot, new Color(color));
        return this;
    }

    /**
     * @param slot Slot which color should be set.
     * @param color The color of the slot
     * @return Returns the inventorycomponent
     */
    public InventoryComponent<T> setSlotToColorRender(int slot, DyeColor color) {
        if (!this.colorGuiEnabled) {
            this.setColorGuiEnabled(true);
        }
        this.slotToColorRenderMap.put(slot, new Color(color.getMaterialColor().col));
        return this;
    }

    /**
     * @param slot Slot which color should be set.
     * @param color The color of the slot
     * @return Returns the inventorycomponent
     */
    public InventoryComponent<T> setSlotToColorRender(int slot, Color color) {
        if (!this.colorGuiEnabled) {
            this.setColorGuiEnabled(true);
        }
        this.slotToColorRenderMap.put(slot, color);
        return this;
    }

    /**
     * @param slot Slot to get the Render Stack for
     * @return Returns the Itemstack to be rendered
     */
    public ItemStack getItemStackForSlotRendering(int slot) {
        return this.slotToStackRenderMap.getOrDefault(slot, ItemStack.EMPTY);
    }

    /**
     * @param slot Slot to get the Render Color for
     * @return Returns the Color to be rendered
     */
    public Color getColorForSlotRendering(int slot) {
        return this.slotToColorRenderMap.get(slot);
    }

    /**
     * Sets the default limit for all the slots
     *
     * @param limit The default limit for all the slot that don't have specific limit
     * @return itself
     */
    public InventoryComponent<T> setSlotLimit(int limit) {
        this.slotLimit = limit;
        return this;
    }

    /**
     * Gets the predicate to check if a slot is enabled
     *
     * @return predicate
     */
    public Predicate<Integer> getSlotVisiblePredicate() {
        return slotVisiblePredicate;
    }

    /**
     * Sets the slot enabled predicate that allows to disable/enable slots
     *
     * @param slotVisiblePredicate a int predicate that checks slot id
     * @return itself
     */
    public InventoryComponent<T> setSlotVisiblePredicate(Predicate<Integer> slotVisiblePredicate) {
        this.slotVisiblePredicate = slotVisiblePredicate;
        return this;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slotAmountFilter.getOrDefault(slot, this.slotLimit);
    }

    public Function<Integer, Pair<Integer, Integer>> getSlotPosition() {
        return slotPosition;
    }

    public InventoryComponent<T> setSlotPosition(Function<Integer, Pair<Integer, Integer>> slotPosition) {
        this.slotPosition = slotPosition;
        return this;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemVariant variant, long amount) {
        return insertPredicate.test(variant.toStack((int) amount), slot);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        List<IFactory<? extends IScreenAddon>> addons = new ArrayList<>();
        addons.add(() -> new SlotsScreenAddon<>(this));
        return addons;
    }

    @Override
    public List<IFactory<? extends IContainerAddon>> getContainerAddons() {
        return Lists.newArrayList(
                () -> new SlotContainerAddon(this, xPos, yPos, slotPosition)
        );
    }
}
