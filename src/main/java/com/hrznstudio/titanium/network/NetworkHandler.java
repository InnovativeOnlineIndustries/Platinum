/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.network;

import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.InvocationTargetException;

public class NetworkHandler {

    private SimpleChannel network;
    private int i;

    public NetworkHandler(String modid) {
        i = 0;
        network = new SimpleChannel(new ResourceLocation(modid, "network"));
        network.initServerListener();
        EnvExecutor.runWhenOn(EnvType.CLIENT, () -> network::initClientListener);
    }

    public SimpleChannel get() {
        return network;
    }

    public <REQ extends Message> void registerMessage(Class<REQ> message) {
        network.registerS2CPacket(message, i++,
                buffer -> {
                    try {
                        REQ req = message.getConstructor().newInstance();
                        req.fromBytes(buffer);
                        return req;
                    } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public void sendToNearby(Level world, BlockPos pos, int distance, Message message) {
        world.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(distance)).forEach(playerEntity -> {
            network.sendToClient(message, playerEntity);
        });
    }
}
