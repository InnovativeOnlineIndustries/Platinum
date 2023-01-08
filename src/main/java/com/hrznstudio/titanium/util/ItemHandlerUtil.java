/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.util;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;


public class ItemHandlerUtil {

    @Nonnull
    public static ItemStack getFirstItem(Storage<ItemVariant> handler) {
        ItemStack stack = TransferUtil.getItems(handler, 1).get(0);
        return stack != null ? stack : ItemStack.EMPTY;
    }

    public static boolean isEmpty(Storage<ItemVariant> handler) {
        try(Transaction transaction = TransferUtil.getTransaction()){
            for (StorageView<ItemVariant> view : handler.iterable(transaction))
                if (view.isResourceBlank() || view.getAmount() != 0L)
                    return false;
        }
        return true;
    }

    public static boolean isEmpty(ItemStackHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

}
