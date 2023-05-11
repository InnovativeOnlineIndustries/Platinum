/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.module;

import com.hrznstudio.titanium.block.BasicBlock;
import com.hrznstudio.titanium.block.BasicTileBlock;
import io.github.fabricators_of_create.porting_lib.event.common.ModsLoadedCallback;
import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DeferredRegistryHelper {

    private final String modId;
    private final HashMap<ResourceKey<? extends Registry<?>>, List<RegistryObject<?>>> registries;
    private final HashMap<ResourceKey<? extends Registry<?>>, Runnable> callbacks;

    public DeferredRegistryHelper(String modId) {
        this.modId = modId;
        this.registries = new HashMap<>();
        this.callbacks = new HashMap<>();
    }

    private  <T> RegistryObject<T> register(ResourceKey<? extends Registry<T>> key, String name, Supplier<T> object) {
        T value = Registry.register((Registry<T>) Registry.REGISTRY.get(key.location()), new ResourceLocation(modId, name), object.get());
        if (registries.get(key) == null) {
            this.registries.put(key, new ArrayList<>());
        }
        RegistryObject<T> reg = new RegistryObject<>(new ResourceLocation(modId, name), () -> value, key);
        this.registries.get(key).add(reg);
        return reg;
    }

    public <T> RegistryObject<T> registerGeneric(ResourceKey<? extends Registry<T>> key, String name, Supplier<T> object) {
        return this.register(key, name, object);
    }

    public <T> void addRegistryCallback(ResourceKey<? extends Registry<T>> key, Runnable callback) {
        callbacks.put(key, callback);
    }

    public RegistryObject<BlockEntityType<?>> registerBlockEntityType(String name, Supplier<BlockEntityType<?>> object) {
        ResourceKey<Registry<BlockEntityType<?>>> key = Registry.BLOCK_ENTITY_TYPE_REGISTRY;
        List<RegistryObject<?>> deferredRegister = registries.get(key);
        if (deferredRegister == null) {
            this.registries.put(key, new ArrayList<>());
            deferredRegister = registries.get(key);
        }
        BlockEntityType<?> value = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation(modId, name), object.get());
        RegistryObject<BlockEntityType<?>> obj = new RegistryObject<>(new ResourceLocation(modId, name), () -> value, key);
        deferredRegister.add(obj);
        return obj;
    }

    public RegistryObject<EntityType<?>> registerEntityType(String name, Supplier<EntityType<?>> object) {
        ResourceKey<Registry<EntityType<?>>> key = Registry.ENTITY_TYPE_REGISTRY;
        List<RegistryObject<?>> deferredRegister = registries.get(key);
        if (deferredRegister == null) {
            this.registries.put(key, new ArrayList<>());
            deferredRegister = registries.get(key);
        }
        EntityType<?> value = Registry.register(Registry.ENTITY_TYPE, new ResourceLocation(modId, name), object.get());
        RegistryObject<EntityType<?>> obj = new RegistryObject<>(new ResourceLocation(modId, name), () -> value, Registry.ENTITY_TYPE_REGISTRY);
        deferredRegister.add(obj);
        return obj;
    }

    public RegistryObject<Block> registerBlockWithItem(String name, Supplier<? extends BasicBlock> blockSupplier){
        RegistryObject<Block> blockRegistryObject = registerGeneric(Registry.BLOCK_REGISTRY, name, blockSupplier::get);
        registerGeneric(Registry.ITEM_REGISTRY, name, () -> new BlockItem(blockRegistryObject.get(), new Item.Properties().tab(((BasicBlock) blockRegistryObject.get()).getItemGroup())));
        return blockRegistryObject;
    }

    public RegistryObject<Block> registerBlockWithItem(String name, Supplier<? extends Block> blockSupplier, Function<RegistryObject<Block>, Supplier<Item>> itemSupplier){
        ResourceKey<Registry<Block>> blockKey = Registry.BLOCK_REGISTRY;
        List<RegistryObject<?>> blockRegister = registries.get(blockKey);
        ResourceKey<Registry<Item>> itemKey = Registry.ITEM_REGISTRY;
        List<RegistryObject<?>> itemRegister = registries.get(itemKey);

        if (blockRegister == null) {
            this.registries.put(blockKey, new ArrayList<>());
            blockRegister = registries.get(blockKey);
        }

        if (itemRegister == null) {
            this.registries.put(itemKey, new ArrayList<>());
            itemRegister = registries.get(itemKey);
        }

        Block blockValue = Registry.register(Registry.BLOCK, new ResourceLocation(modId, name), blockSupplier.get());
        RegistryObject<Block> block = new RegistryObject<>(new ResourceLocation(modId, name), () -> blockValue, blockKey);
        blockRegister.add(block);
        Registry.register(Registry.ITEM, new ResourceLocation(modId, name), itemSupplier.apply(block).get());
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
