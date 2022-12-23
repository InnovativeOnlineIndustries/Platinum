/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class RecipeUtil {

    public static <T extends Recipe<?>> List<T> getRecipes(Level world, RecipeType<T> recipeType) {
        Map<ResourceLocation, Recipe<?>> typedRecipes = world.getRecipeManager().recipes.get(recipeType);
        if (typedRecipes != null) {
            return (List<T>) typedRecipes.values().stream().collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static Collection<SmeltingRecipe> getCookingRecipes(Level world) {
        return getRecipes(world, RecipeType.SMELTING);
    }

    @Nullable
    public static SmeltingRecipe getSmelingRecipeFor(Level world, ItemStack stack) {
        return getCookingRecipes(world).stream().filter(furnaceRecipe -> furnaceRecipe.getIngredients().get(0).test(stack)).findFirst().orElse(null);
    }

}
