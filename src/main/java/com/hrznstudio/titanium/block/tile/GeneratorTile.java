/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.block.tile;

import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.component.energy.EnergyStorageComponent;
import com.hrznstudio.titanium.component.progress.ProgressBarComponent;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.EnergyStorageUtil;

import javax.annotation.Nonnull;

public abstract class GeneratorTile<T extends GeneratorTile<T>> extends PoweredTile<T> {

    @Save
    private ProgressBarComponent<T> progressBar;

    public GeneratorTile(BasicTileBlock<T> base, BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        super(base, blockEntityType, pos, state);
        this.addProgressBar(progressBar = getProgressBar()
                .setComponentHarness(this.getSelf())
                .setCanIncrease(tileEntity -> !isSmart() || this.getEnergyCapacity() - this.getEnergyStorage().getAmount() >= getEnergyProducedEveryTick())
                .setIncreaseType(false)
                .setOnStart(() -> {
                    progressBar.setMaxProgress(consumeFuel());
                    progressBar.setProgress(progressBar.getMaxProgress());
                    markForUpdate();
                })
                .setCanReset(tileEntity -> canStart() && progressBar.getProgress() == 0)
                .setOnTickWork(() -> this.getEnergyStorage().setEnergyStored(getEnergyProducedEveryTick() + this.getEnergyStorage().getAmount()))
        );
    }

    /**
     * Consumes fuel successfully
     *
     * @return the amount of ticks the fuel will last for
     */
    public abstract int consumeFuel();

    /**
     * Gets if the generator can start
     *
     * @return True if the generator can start
     */
    public abstract boolean canStart();

    /**
     * @return The amount of energy produced every tick
     */
    public abstract long getEnergyProducedEveryTick();

    /**
     * Gets the progress bar used for the generator
     *
     * @return The progress bar
     */
    public abstract ProgressBarComponent<T> getProgressBar();

    /**
     * Gets how big the energy buffer on the generator is
     *
     * @return The amount of energy that can be stored
     */
    public abstract long getEnergyCapacity();

    /**
     * Gets how much energy can be extracted every tick
     *
     * @return The amount of energy that can be extracted
     */
    public abstract long getExtractingEnergy();

    /**
     * Defines is the generator wastes power when generating or not
     *
     * @return true is if it efficient, false if not
     */
    public boolean isSmart() {
        return true;
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        super.serverTick(level, pos, state, blockEntity);
        for (Direction facing : Direction.values()) {
            BlockPos checking = this.worldPosition.relative(facing);
            EnergyStorage storage = EnergyStorage.SIDED.find(this.level, checking, facing.getOpposite());
            if (storage != null) {
                try (Transaction t = TransferUtil.getTransaction()) {
                    EnergyStorageUtil.move(this.getEnergyStorage(), storage, this.getExtractingEnergy(), t);
                    t.commit();
                }
            }
        }
    }

    @Nonnull
    @Override
    protected EnergyStorageComponent<T> createEnergyStorage() {
        return new EnergyStorageComponent<>(getEnergyCapacity(),0, getExtractingEnergy(), 10, 20);
    }
}
