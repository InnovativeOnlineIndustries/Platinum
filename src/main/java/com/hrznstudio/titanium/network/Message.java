/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.network;

import me.pepperbell.simplenetworking.C2SPacket;
import me.pepperbell.simplenetworking.S2CPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.concurrent.Executor;

public abstract class Message implements Serializable, S2CPacket, C2SPacket {

    protected abstract void handleMessage(Executor executor, @Nullable Player sender, PacketListener packetListener, PacketSender packetSender, SimpleChannel channel);

    public final void fromBytes(FriendlyByteBuf buf) {
        try {
            Class<?> clazz = getClass();
            for (Field f : clazz.getDeclaredFields()) {
                if (!f.isAccessible()) f.setAccessible(true);
                Class<?> type = f.getType();
                if (CompoundSerializableDataHandler.acceptField(f, type))
                    CompoundSerializableDataHandler.readField(f, type, buf, this);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error at reading packet " + this, e);
        }
    }

    public final void encode(FriendlyByteBuf buf) {
        try {
            Class<?> clazz = getClass();
            for (Field f : clazz.getDeclaredFields()) {
                if (!f.isAccessible()) f.setAccessible(true);
                Class<?> type = f.getType();
                if (CompoundSerializableDataHandler.acceptField(f, type))
                    CompoundSerializableDataHandler.writeField(f, type, buf, this);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error at writing packet " + this, e);
        }
    }

    @Override
    public void handle(Minecraft minecraft, ClientPacketListener clientPacketListener, PacketSender packetSender, SimpleChannel simpleChannel) {
        handleMessage(minecraft, null, clientPacketListener, packetSender, simpleChannel);
    }

    @Override
    public void handle(MinecraftServer minecraftServer, ServerPlayer serverPlayer, ServerGamePacketListenerImpl serverGamePacketListener, PacketSender packetSender, SimpleChannel simpleChannel) {
        handleMessage(minecraftServer, serverPlayer, serverGamePacketListener, packetSender, simpleChannel);
    }
}
