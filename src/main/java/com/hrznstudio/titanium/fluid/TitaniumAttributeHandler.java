/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.fluid;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record TitaniumAttributeHandler(Properties properties, ResourceLocation still, ResourceLocation flowing, Function<FluidVariant, Integer> color) implements FluidVariantAttributeHandler {
    public static final class Properties {
        private String descriptionId;
        private int lightLevel = 0,
            density = 1000,
            temperature = 300,
            viscosity = 1000;

        private Properties() {}

        /**
         * Creates a new instance of the properties.
         *
         * @return the property holder instance
         */
        public static Properties create() {
            return new Properties();
        }

        /**
         * Sets the identifier representing the name of the fluid type.
         *
         * @param descriptionId  the identifier representing the name of the fluid type
         * @return the property holder instance
         */
        public Properties descriptionId(String descriptionId) {
            this.descriptionId = descriptionId;
            return this;
        }

        /**
         * Sets the light level emitted by the fluid.
         *
         * @param lightLevel the light level emitted by the fluid
         * @return the property holder instance
         * @throws IllegalArgumentException if light level is not between [0,15]
         */
        public Properties lightLevel(int lightLevel) {
            if (lightLevel < 0 || lightLevel > 15)
                throw new IllegalArgumentException("The light level should be between [0,15].");
            this.lightLevel = lightLevel;
            return this;
        }

        /**
         * Sets the density of the fluid.
         *
         * @param density the density of the fluid
         * @return the property holder instance
         */
        public Properties density(int density) {
            this.density = density;
            return this;
        }

        /**
         * Sets the temperature of the fluid.
         *
         * @param temperature the temperature of the fluid
         * @return the property holder instance
         */
        public Properties temperature(int temperature) {
            this.temperature = temperature;
            return this;
        }

        /**
         * Sets the viscosity, or thickness, of the fluid.
         *
         * @param viscosity the viscosity of the fluid
         * @return the property holder instance
         * @throws IllegalArgumentException if viscosity is negative
         */
        public Properties viscosity(int viscosity) {
            if (viscosity < 0)
                throw new IllegalArgumentException("The viscosity should never be negative.");
            this.viscosity = viscosity;
            return this;
        }
    }

    @Override
    public int getViscosity(FluidVariant variant, @Nullable Level world) {
        return properties.viscosity;
    }

    @Override
    public int getLuminance(FluidVariant variant) {
        return properties.lightLevel;
    }

    @Override
    public int getTemperature(FluidVariant variant) {
        return properties.temperature;
    }

    @Override
    public boolean isLighterThanAir(FluidVariant variant) {
        return properties.density <= 0;
    }

    @Environment(EnvType.CLIENT)
    public record TitaniumClientAttributeHandler(TitaniumAttributeHandler handler) implements FluidVariantRenderHandler {

        @Override
        public int getColor(FluidVariant fluidVariant, @Nullable BlockAndTintGetter view, @Nullable BlockPos pos) {
            return handler.color().apply(fluidVariant);
        }
    }
}
