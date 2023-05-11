/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.recipe.generator;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

public abstract class TitaniumRecipeProvider extends FabricRecipeProvider {

    public TitaniumRecipeProvider(FabricDataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void generateRecipes(Consumer<FinishedRecipe> p_176532_) {
        register(p_176532_);
    }

    public abstract void register(Consumer<FinishedRecipe> consumer);

    @Override
    public String getName() {
        return "Titanium Recipe";
    }
}
