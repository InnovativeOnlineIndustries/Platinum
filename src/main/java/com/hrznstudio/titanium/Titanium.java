/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium;

import com.hrznstudio.titanium._impl.creative.CreativeFEGeneratorBlock;
import com.hrznstudio.titanium._impl.test.AssetTestBlock;
import com.hrznstudio.titanium._impl.test.MachineTestBlock;
import com.hrznstudio.titanium._impl.test.TestBlock;
import com.hrznstudio.titanium._impl.test.TwentyFourTestBlock;
import com.hrznstudio.titanium._impl.test.recipe.TestSerializableRecipe;
import com.hrznstudio.titanium.annotation.scanning.ScanDataProvider;
import com.hrznstudio.titanium.block.tile.PoweredTile;
import com.hrznstudio.titanium.command.RewardCommand;
import com.hrznstudio.titanium.command.RewardGrantCommand;
import com.hrznstudio.titanium.container.BasicAddonContainer;
import com.hrznstudio.titanium.fabric.ItemExistsCondition;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.network.NetworkHandler;
import com.hrznstudio.titanium.network.locator.LocatorTypes;
import com.hrznstudio.titanium.network.messages.ButtonClickNetworkMessage;
import com.hrznstudio.titanium.network.messages.TileFieldNetworkMessage;
import com.hrznstudio.titanium.recipe.condition.ContentExistsCondition;
import com.hrznstudio.titanium.recipe.serializer.GenericSerializer;
import com.hrznstudio.titanium.recipe.shapelessenchant.ShapelessEnchantSerializer;
import com.hrznstudio.titanium.reward.Reward;
import com.hrznstudio.titanium.reward.RewardManager;
import com.hrznstudio.titanium.reward.RewardSyncMessage;
import com.hrznstudio.titanium.reward.storage.RewardWorldStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.reborn.energy.api.EnergyStorage;


public class Titanium extends ModuleController implements ModInitializer {

    public static final String MODID = "titanium";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static NetworkHandler NETWORK = new NetworkHandler(MODID);

    public void onInitialize() {
        NETWORK.registerMessage(ButtonClickNetworkMessage.class);
        NETWORK.registerMessage(RewardSyncMessage.class);
        NETWORK.registerMessage(TileFieldNetworkMessage.class);
        commonSetup();
        ServerPlayConnectionEvents.JOIN.register(this::onPlayerLoggedIn);
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStart);
//        EventManager.mod(RegisterCapabilitiesEvent.class).process(CapabilityItemStackHolder::register).subscribe();
        ResourceConditions.register(ContentExistsCondition.NAME, ContentExistsCondition::test);

        EnergyStorage.SIDED.registerFallback((world, pos, state, blockEntity, context) -> {
            if (blockEntity instanceof PoweredTile<?> poweredTile)
                return poweredTile.getEnergyStorage();
            return null;
        });

        ItemExistsCondition.init();
        ScanDataProvider.init();
    }

    @Override
    public void onPreInit() {
        super.onPreInit();
    }

    @Override
    public void onInit() {
        super.onInit();
    }

    @Override
    protected void initModules() {
        BasicAddonContainer.TYPE = getRegistries().registerGeneric(Registry.MENU.key(), "addon_container", () -> (MenuType) new ExtendedScreenHandlerType<>(BasicAddonContainer::create));
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) { //ENABLE IN DEV
            getRegistries().registerGeneric(Registry.RECIPE_SERIALIZER.key(), "shapeless_enchant", () -> (RecipeSerializer) new ShapelessEnchantSerializer());
            TestSerializableRecipe.SERIALIZER = getRegistries().registerGeneric(Registry.RECIPE_SERIALIZER.key(), "test_serializer", () -> new GenericSerializer<>(TestSerializableRecipe.class, TestSerializableRecipe.RECIPE_TYPE));
            TestSerializableRecipe.RECIPE_TYPE = getRegistries().registerGeneric(Registry.RECIPE_TYPE.key(), "", () -> new RecipeType() {
                @Override
                public String toString() {
                    return MODID + ":test_recipe_type";
                }
            });
            TestBlock.TEST = getRegistries().registerBlockWithTile("block_test", () -> (TestBlock) new TestBlock());
            TwentyFourTestBlock.TEST = getRegistries().registerBlockWithTile("block_twenty_four_test", () -> (TwentyFourTestBlock) new TwentyFourTestBlock());
            AssetTestBlock.TEST = getRegistries().registerBlockWithTile("block_asset_test", () -> (AssetTestBlock) new AssetTestBlock());
            MachineTestBlock.TEST = getRegistries().registerBlockWithTile("machine_test", () -> (MachineTestBlock) new MachineTestBlock());
            CreativeFEGeneratorBlock.INSTANCE = getRegistries().registerBlockWithTile("creative_generator", () -> new CreativeFEGeneratorBlock());
        }
    }

    @Override
    public void onPostInit() {
        super.onPostInit();
    }

    private void commonSetup() {
        RewardManager.get().getRewards().values().forEach(rewardGiver -> rewardGiver.getRewards().forEach(reward -> reward.register(EnvType.SERVER)));
        LocatorTypes.register();
    }

    private void onPlayerLoggedIn(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        server.execute(() -> {
            RewardWorldStorage storage = RewardWorldStorage.get(server.getLevel(Level.OVERWORLD));
            if (!storage.getConfiguredPlayers().contains(handler.getPlayer().getUUID())) {
                for (ResourceLocation collectRewardsResourceLocation : RewardManager.get().collectRewardsResourceLocations(handler.getPlayer().getUUID())) {
                    Reward reward = RewardManager.get().getReward(collectRewardsResourceLocation);
                    storage.add(handler.getPlayer().getUUID(), reward.getResourceLocation(), reward.getOptions()[0]);
                }
                storage.getConfiguredPlayers().add(handler.getPlayer().getUUID());
                storage.setDirty();
            }
            CompoundTag nbt = storage.serializeSimple();
            server.getPlayerList().getPlayers().forEach(serverPlayerEntity -> Titanium.NETWORK.get().sendToClient(new RewardSyncMessage(nbt), serverPlayerEntity));
        });
    }

    private void onServerStart(MinecraftServer server) {
        RewardCommand.register(server.getCommands().getDispatcher());
        RewardGrantCommand.register(server.getCommands().getDispatcher());
    }
}
