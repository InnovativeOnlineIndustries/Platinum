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
import io.github.fabricators_of_create.porting_lib.util.FluidUtil;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

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


    public static boolean interactWithFluidStorage(Storage<FluidVariant> storage, Player player, InteractionHand hand) {
        // Check if hand is a fluid container.
        Storage<FluidVariant> handStorage = ContainerItemContext.ofPlayerHand(player, hand).find(FluidStorage.ITEM);
        if (handStorage == null) return false;

        // Try to fill hand first, otherwise try to empty it.
        Item handItem = player.getItemInHand(hand).getItem();
        return moveWithSound(storage, handStorage, player, true, handItem) || moveWithSound(handStorage, storage, player, false, handItem);
    }

    private static boolean moveWithSound(Storage<FluidVariant> from, Storage<FluidVariant> to, Player player, boolean fill, Item handItem) {
        try (Transaction outer = Transaction.openOuter()){
            for (StorageView<FluidVariant> view : from.iterable(outer)) {
                if (view.isResourceBlank()) continue;
                FluidVariant resource = view.getResource();
                long maxExtracted;

                // check how much can be extracted
                try (Transaction extractionTestTransaction = Transaction.openNested(outer)) {
                    maxExtracted = view.extract(resource, Long.MAX_VALUE, extractionTestTransaction);
                    extractionTestTransaction.abort();
                }

                try (Transaction transferTransaction = Transaction.openNested(outer)) {
                    // check how much can be inserted
                    long accepted = to.insert(resource, maxExtracted, transferTransaction);

                    // extract it, or rollback if the amounts don't match
                    if (accepted > 0 && view.extract(resource, accepted, transferTransaction) == accepted) {
                        transferTransaction.commit();

                        SoundEvent sound = fill ? FluidVariantAttributes.getFillSound(resource) : FluidVariantAttributes.getEmptySound(resource);

                        // Temporary workaround to use the correct sound for water bottles.
                        // TODO: Look into providing a proper item-aware fluid sound API.
                        if (resource.isOf(Fluids.WATER)) {
                            if (fill && handItem == Items.GLASS_BOTTLE) sound = SoundEvents.BOTTLE_FILL;
                            if (!fill && handItem == Items.POTION) sound = SoundEvents.BOTTLE_EMPTY;
                        }

                        player.playNotifySound(sound, SoundSource.BLOCKS, 1, 1);

                        return true;
                    }
                }
            }
        }


        return false;
    }
}
