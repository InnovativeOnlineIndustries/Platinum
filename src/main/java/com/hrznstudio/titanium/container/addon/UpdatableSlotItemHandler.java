/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.container.addon;

import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import org.apache.commons.lang3.tuple.Pair;

public class UpdatableSlotItemHandler extends SlotItemHandler {

    private boolean enabled;

    public UpdatableSlotItemHandler(ItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
        this.enabled = true;
    }

    public void update() {
        if (this.getItemHandler() instanceof InventoryComponent) {
            Pair<Integer, Integer> pos = ((InventoryComponent<?>) this.getItemHandler()).getSlotPosition().apply(this.getSlotIndex());
            this.x = ((InventoryComponent<?>) this.getItemHandler()).getXPos() + pos.getLeft();
            this.y = ((InventoryComponent<?>) this.getItemHandler()).getYPos() + pos.getRight();
            this.enabled = ((InventoryComponent<?>) this.getItemHandler()).getSlotVisiblePredicate().test(this.getSlotIndex());
        }
    }

    @Override
    public boolean isActive() {
        return enabled;
    }
}
