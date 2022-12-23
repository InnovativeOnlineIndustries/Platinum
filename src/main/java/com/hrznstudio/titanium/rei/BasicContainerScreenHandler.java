package com.hrznstudio.titanium.rei;

import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.container.BasicContainerScreen;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZonesProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BasicContainerScreenHandler<T extends AbstractContainerMenu> implements ExclusionZonesProvider<BasicContainerScreen<T>> {
    @Override
    public Collection<Rectangle> provide(BasicContainerScreen<T> containerScreen) {
        List<Rectangle> rectangles = new ArrayList<>();
        for (Object o : containerScreen.getAddons()) {
            if (o instanceof BasicScreenAddon) {
                BasicScreenAddon addon = (BasicScreenAddon) o;
                rectangles.add(new Rectangle(containerScreen.getX() + addon.getPosX(), containerScreen.getY() + addon.getPosY(), addon.getXSize(), addon.getYSize()));
            }
        }
        return rectangles;
    }
}
