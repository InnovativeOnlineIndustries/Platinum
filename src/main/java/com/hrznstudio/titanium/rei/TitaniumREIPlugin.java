package com.hrznstudio.titanium.rei;

import com.hrznstudio.titanium.client.screen.container.BasicContainerScreen;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;

public class TitaniumREIPlugin implements REIClientPlugin {
    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        zones.register(BasicContainerScreen.class, new BasicContainerScreenHandler());
    }
}
