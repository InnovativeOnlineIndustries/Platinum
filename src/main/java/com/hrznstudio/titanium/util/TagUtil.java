/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.util;

import com.hrznstudio.titanium._impl.TagConfig;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.*;
import java.util.stream.Stream;

public class TagUtil {

    public static <T> boolean hasTag(Registry<T> registry, T type, TagKey<T> tag) {
        Optional<List<T>> tags = registry.getTag(tag).map(holders -> holders.stream().map(Holder::value).toList());
        if (tags.isPresent())
            return tags.get().contains(type);
        return false;
    }

    public static Stream<Pair<TagKey<Block>, HolderSet.Named<Block>>> getAllBlockTags() {
        return Registry.BLOCK.getTags();
    }

    public static Stream<Pair<TagKey<Item>, HolderSet.Named<Item>>> getAllItemTags() {
        return Registry.ITEM.getTags();
    }

    public static Stream<Pair<TagKey<Fluid>, HolderSet.Named<Fluid>>> getAllFluidTags() {
        return Registry.FLUID.getTags();
    }

    public static <T> Collection<T> getAllEntries(Registry<T> registry, TagKey<T>... tags) {
        if (tags.length == 0)
            return Collections.emptyList();
        if (tags.length == 1)
            return registry.getTag(tags[0]).map(holders -> holders.stream().map(Holder::value).toList()).orElse(Collections.emptyList()); //getAllElements
        List<T> list = new ArrayList<>();
        for (TagKey<T> tag : tags) {
            list.addAll(registry.getTag(tag).map(holders -> holders.stream().map(Holder::value).toList()).orElse(Collections.emptyList())); //getAllElements
        }
        return list;
    }

    public static <T> Collection<T> getAllEntries(Registry<T> registry, TagKey<T> tag) {
        return registry.getTag(tag).map(holders -> holders.stream().map(Holder::value).toList()).orElse(Collections.emptyList());
    }

    public static <T> TagKey<T> getOrCreateTag(Registry<T> registry, ResourceLocation resourceLocation) {
        /*
        if (registry.tags().stream().anyMatch(ts -> ts.getKey().location().equals(resourceLocation))) {

        }
        return collection.getTagOrEmpty(resourceLocation);
        */
        return TagKey.create(registry.key(), resourceLocation);
    }

    public static TagKey<Item> getItemTag(ResourceLocation resourceLocation) {
        /*
        if (ItemTags.getAllTags().getAvailableTags().contains(resourceLocation)) {
            return ItemTags.getAllTags().getTag(resourceLocation);
        }
        return ItemTags.create(resourceLocation);
        */

        return Registry.ITEM.tags.entrySet().stream().filter(items -> items.getKey().location().equals(resourceLocation)).map(Map.Entry::getKey).findFirst().orElse(getOrCreateTag(Registry.ITEM, resourceLocation));
    }

    public static TagKey<Block> getBlockTag(ResourceLocation resourceLocation) {
        /*if (BlockTags.getAllTags().getAvailableTags().contains(resourceLocation)) {
            return BlockTags.getAllTags().getTag(resourceLocation);
        }
        return BlockTags.create(resourceLocation);*/
        return Registry.BLOCK.tags.entrySet().stream().filter(items -> items.getKey().location().equals(resourceLocation)).map(Map.Entry::getKey).findFirst().orElse(getOrCreateTag(Registry.BLOCK, resourceLocation));

    }

    public static TagKey<EntityType<?>> getEntityTypeTag(ResourceLocation resourceLocation) {
        /*if (EntityTypeTags.getAllTags().getAvailableTags().contains(resourceLocation)) {
            return EntityTypeTags.getAllTags().getTag(resourceLocation);
        }
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, resourceLocation);*/
        return Registry.ENTITY_TYPE.tags.entrySet().stream().filter(items -> items.getKey().location().equals(resourceLocation)).map(Map.Entry::getKey).findFirst().orElse(getOrCreateTag(Registry.ENTITY_TYPE, resourceLocation));

    }

    public static TagKey<Fluid> getFluidTag(ResourceLocation resourceLocation) {
        /*if (FluidTags.getAllTags().getAvailableTags().contains(resourceLocation)) {
            return FluidTags.getAllTags().getTag(resourceLocation);
        }
        Registry.FLUID_REGISTRY.cast()
        return FluidTags.create(resourceLocation);*/
        return Registry.FLUID.tags.entrySet().stream().filter(items -> items.getKey().location().equals(resourceLocation)).map(Map.Entry::getKey).findFirst().orElse(getOrCreateTag(Registry.FLUID, resourceLocation));

    }

    public static ItemStack getItemWithPreference(TagKey<Item> tagKey){
        Optional<HolderSet.Named<Item>> item = Registry.ITEM.getTag(tagKey);
        if (item.isEmpty()) return ItemStack.EMPTY;
        List<Item> elements = item.get().stream().map(Holder::value).toList();
        for (String modid : TagConfig.ITEM_PREFERENCE) {
            for (Item allElement : elements) {
                if (Registry.ITEM.getKey(allElement).getNamespace().equalsIgnoreCase(modid)) return new ItemStack(allElement);
            }
        }
        return new ItemStack(elements.get(0));
    }
}
