package com.hrznstudio.titanium;

import com.hrznstudio.titanium.datagenerator.loot.TitaniumLootTableProvider;
import com.hrznstudio.titanium.datagenerator.model.BlockItemModelGeneratorProvider;
import com.hrznstudio.titanium.fabric.NonNullLazy;
import com.hrznstudio.titanium.recipe.generator.titanium.JsonRecipeSerializerProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.hrznstudio.titanium.Titanium.MODID;

public class TitaniumData implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        NonNullLazy<List<Block>> blocksToProcess = NonNullLazy.of(() ->
            Registry.BLOCK
                .stream()
                .filter(basicBlock -> Optional.ofNullable(Registry.BLOCK.getKey(basicBlock))
                    .map(ResourceLocation::getNamespace)
                    .filter(MODID::equalsIgnoreCase)
                    .isPresent())
                .collect(Collectors.toList())
        );
        generator.addProvider(true, new BlockItemModelGeneratorProvider(generator, MODID, blocksToProcess));
        generator.addProvider(true, new TitaniumLootTableProvider(generator, blocksToProcess));
        generator.addProvider(true, new JsonRecipeSerializerProvider(generator, MODID));
    }
}
