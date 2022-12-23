/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.module;

import com.hrznstudio.titanium.block.BasicBlock;
import com.hrznstudio.titanium.block.BasicTileBlock;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class DeferredRegistryHelper {

    private final String modId;
    private final HashMap<ResourceKey<? extends Registry<?>>, LazyRegistrar<?>> registries;

    public DeferredRegistryHelper(String modId) {
        this.modId = modId;
        this.registries = new HashMap<>();
    }

    public <T> LazyRegistrar<T> addRegistry(ResourceKey<? extends Registry<T>> key) {
        LazyRegistrar<T> deferredRegister = LazyRegistrar.create(key, this.modId);
        deferredRegister.register();
        registries.put(key, deferredRegister);
        return deferredRegister;
    }

    private  <T> RegistryObject<T> register(ResourceKey<? extends Registry<T>> key, String name, Supplier<T> object) {
        LazyRegistrar<T> deferredRegister = (LazyRegistrar<T>)(Object)registries.get(key);
        if (deferredRegister == null) {
            this.addRegistry(key);
            deferredRegister = (LazyRegistrar<T>)(Object)registries.get(key);
        }
        return deferredRegister.register(name, object);
    }

    public <T> RegistryObject<T> registerGeneric(ResourceKey<? extends Registry<T>> key, String name, Supplier<T> object) {
        return this.register(key, name, object);
    }

    public RegistryObject<BlockEntityType<?>> registerBlockEntityType(String name, Supplier<BlockEntityType<?>> object) {
        ResourceKey<Registry<BlockEntityType<?>>> key = Registry.BLOCK_ENTITY_TYPE_REGISTRY;
        LazyRegistrar<BlockEntityType<?>> deferredRegister = (LazyRegistrar<BlockEntityType<?>>) (Object) registries.get(key);
        if (deferredRegister == null) {
            this.addRegistry(key);
            deferredRegister = (LazyRegistrar<BlockEntityType<?>>) (Object) registries.get(key);
        }
        return deferredRegister.register(name, object);
    }

    public RegistryObject<EntityType<?>> registerEntityType(String name, Supplier<EntityType<?>> object) {
        ResourceKey<Registry<EntityType<?>>> key = Registry.ENTITY_TYPE_REGISTRY;
        LazyRegistrar<EntityType<?>> deferredRegister = (LazyRegistrar<EntityType<?>>) (Object) registries.get(key);
        if (deferredRegister == null) {
            this.addRegistry(key);
            deferredRegister = (LazyRegistrar<EntityType<?>>) (Object) registries.get(key);
        }
        return deferredRegister.register(name, object);
    }

    public RegistryObject<Block> registerBlockWithItem(String name, Supplier<? extends BasicBlock> blockSupplier){
        RegistryObject<Block> blockRegistryObject = registerGeneric(Registry.BLOCK_REGISTRY, name, blockSupplier::get);
        registerGeneric(Registry.ITEM_REGISTRY, name, () -> new BlockItem(blockRegistryObject.get(), new Item.Properties().tab(((BasicBlock) blockRegistryObject.get()).getItemGroup())));
        return blockRegistryObject;
    }

    public RegistryObject<Block> registerBlockWithItem(String name, Supplier<? extends Block> blockSupplier, Function<RegistryObject<Block>, Supplier<Item>> itemSupplier){
        ResourceKey<Registry<Block>> blockKey = Registry.BLOCK_REGISTRY;
        LazyRegistrar<Block> blockRegister = (LazyRegistrar<Block>)(Object)registries.get(blockKey);
        ResourceKey<Registry<Item>> itemKey = Registry.ITEM_REGISTRY;
        LazyRegistrar<Item> itemRegister = (LazyRegistrar<Item>)(Object)registries.get(itemKey);

        if (blockRegister == null) {
            this.addRegistry(blockKey);
            blockRegister = (LazyRegistrar<Block>)(Object)registries.get(blockKey);
        }

        if (itemRegister == null) {
            this.addRegistry(itemKey);
            itemRegister = (LazyRegistrar<Item>)(Object)registries.get(itemKey);
        }

        RegistryObject<Block> block = blockRegister.register(name, blockSupplier);
        itemRegister.register(name, itemSupplier.apply(block));
        return block;
    }

    public Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> registerBlockWithTile(String name, Supplier<BasicTileBlock<?>> blockSupplier){
        RegistryObject<Block> blockRegistryObject = registerBlockWithItem(name, blockSupplier);
        return Pair.of(blockRegistryObject, registerBlockEntityType(name, () -> BlockEntityType.Builder.of(((BasicTileBlock<?>)blockRegistryObject.get()).getTileEntityFactory(), blockRegistryObject.get()).build(null)));
    }

    public Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> registerBlockWithTileItem(String name, Supplier<BasicTileBlock<?>> blockSupplier, Function<RegistryObject<Block>, Supplier<Item>> itemSupplier){
        RegistryObject<Block> blockRegistryObject = registerBlockWithItem(name, blockSupplier, itemSupplier);
        return Pair.of(blockRegistryObject, registerBlockEntityType(name, () -> BlockEntityType.Builder.of(((BasicTileBlock<?>)blockRegistryObject.get()).getTileEntityFactory(), blockRegistryObject.get()).build(null)));
    }
}
