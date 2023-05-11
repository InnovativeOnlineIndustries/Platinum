/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.network.messages;

import com.hrznstudio.titanium.block.tile.BasicTile;
import com.hrznstudio.titanium.network.Message;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;


public class TileFieldNetworkMessage extends Message {

    private BlockPos pos;
    private CompoundTag data;

    public TileFieldNetworkMessage(BlockPos pos, CompoundTag data) {
        this.pos = pos;
        this.data = data;
    }

    public TileFieldNetworkMessage() {
    }

    @Override
    protected void handleMessage(Executor executor, @Nullable Player sender, PacketListener packetListener, PacketSender packetSender, SimpleChannel channel) {
        executor.execute(() -> {
            BlockEntity entity = Minecraft.getInstance().player.getCommandSenderWorld().getBlockEntity(pos);
            if (entity instanceof BasicTile){
                ((BasicTile<?>) entity).handleSyncObject(data);
            }
        });
    }
}
