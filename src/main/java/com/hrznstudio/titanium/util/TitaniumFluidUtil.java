/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.util;

import com.mojang.datafixers.util.Pair;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * This class exists because @{@link FluidUtil}'s tryEmptyContainer doesn't work properly
 */
public class TitaniumFluidUtil {

    @Nonnull
    public static Pair<Boolean, ContainerItemContext> tryEmptyContainer(@Nonnull ItemStack container, Storage<FluidVariant> fluidDestination, long maxAmount, boolean doDrain) {
        ContainerItemContext context = ContainerItemContext.withInitial(container);
        Storage<FluidVariant> fluidStorage = context.find(FluidStorage.ITEM);
        if (fluidStorage != null) {
            try (Transaction t = TransferUtil.getTransaction()) {
                long amountMoved = StorageUtil.move(fluidStorage, fluidDestination, v -> true, maxAmount, t);
                t.commit();
                return amountMoved != 0 ? Pair.of(true, context) : Pair.of(false, ContainerItemContext.withInitial(container));
            }
        }
        return Pair.of(false, ContainerItemContext.withInitial(container));
    }

}
