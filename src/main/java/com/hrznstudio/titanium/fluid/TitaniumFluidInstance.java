/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.fluid;

import com.hrznstudio.titanium.module.DeferredRegistryHelper;
import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;

import java.util.function.Function;

public class TitaniumFluidInstance {

    private TitaniumAttributeHandler attributeHandler;
    private RegistryObject<Fluid> flowingFluid;
    private RegistryObject<Fluid> sourceFluid;
    private RegistryObject<Item> bucketFluid;
    private RegistryObject<Block> blockFluid;
    private final String fluid;

    public TitaniumFluidInstance(DeferredRegistryHelper helper, TitaniumAttributeHandler.Properties fluidAttributes, ResourceLocation still, ResourceLocation flowing, Function<FluidVariant, Integer> color, String fluid, CreativeModeTab group) {
        this.fluid = fluid;
        this.sourceFluid = helper.registerGeneric(Registry.FLUID_REGISTRY, fluid, () -> new TitaniumFluid.Source(this));
        this.flowingFluid = helper.registerGeneric(Registry.FLUID_REGISTRY, fluid + "_flowing", () -> new TitaniumFluid.Flowing(this));
        this.bucketFluid = helper.registerGeneric(Registry.ITEM_REGISTRY, fluid + "_bucket", () -> new BucketItem(this.sourceFluid.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(group)));
        this.blockFluid = helper.registerGeneric(Registry.BLOCK_REGISTRY, fluid, () -> new LiquidBlock((FlowingFluid) sourceFluid.get(), Block.Properties.of(Material.WATER).noCollission().strength(100.0F).noLootTable()));
        helper.addRegistryCallback(Registry.FLUID_REGISTRY, () -> {
            TitaniumAttributeHandler handler = new TitaniumAttributeHandler(fluidAttributes, still, flowing, color);
            FluidVariantAttributes.register(sourceFluid.get(), handler);
            FluidVariantAttributes.register(flowingFluid.get(), handler);

            EnvExecutor.runWhenOn(EnvType.CLIENT, () ->() -> {
                FluidRenderHandlerRegistry.INSTANCE.register(sourceFluid.get(), flowingFluid.get(), new SimpleFluidRenderHandler(attributeHandler.still(), attributeHandler.flowing()));
                TitaniumAttributeHandler.TitaniumClientAttributeHandler clientAttributeHandler = new TitaniumAttributeHandler.TitaniumClientAttributeHandler(handler);
                FluidVariantRendering.register(sourceFluid.get(), clientAttributeHandler);
                FluidVariantRendering.register(flowingFluid.get(), clientAttributeHandler);
            });
        });
    }

    public RegistryObject<Fluid>  getFlowingFluid() {
        return flowingFluid;
    }

    public RegistryObject<Fluid>  getSourceFluid() {
        return sourceFluid;
    }

    public RegistryObject<Item> getBucketFluid() {
        return bucketFluid;
    }

    public RegistryObject<Block> getBlockFluid() {
        return blockFluid;
    }

    public String getFluid() {
        return fluid;
    }

    public TitaniumAttributeHandler getAttributeHandler() {
        return attributeHandler;
    }
}
