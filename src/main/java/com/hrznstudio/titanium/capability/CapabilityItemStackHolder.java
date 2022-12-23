/*
 * This file is part of Titanium
 * Copyright (C) 2022, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.capability;

import com.hrznstudio.titanium.Titanium;
import com.hrznstudio.titanium.api.capability.IStackHolder;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.minecraft.resources.ResourceLocation;

public class CapabilityItemStackHolder {
    public static final ComponentKey<IStackHolder> ITEMSTACK_HOLDER_CAPABILITY = ComponentRegistry.getOrCreate(new ResourceLocation(Titanium.MODID, "itemstack_holder"), IStackHolder.class);

}
