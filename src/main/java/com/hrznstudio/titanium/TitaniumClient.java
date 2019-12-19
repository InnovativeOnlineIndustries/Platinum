/*
 * This file is part of Titanium
 * Copyright (C) 2019, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium;

import com.hrznstudio.titanium.block.BlockBase;
import com.hrznstudio.titanium.client.TitaniumModelLoader;
import com.hrznstudio.titanium.util.RayTraceUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;

public class TitaniumClient {
    public static void registerModelLoader() {
        //ModelLoaderRegistry.registerLoader(new ResourceLocation(Titanium.MODID, "model_loader"),new TitaniumModelLoader());
    }

    public static PlayerRenderer getPlayerRenderer(Minecraft minecraft) {
        return minecraft.getRenderManager().playerRenderer;
    }

    @OnlyIn(Dist.CLIENT)
    public static void blockOverlayEvent(DrawHighlightEvent event) {
        if (event.getTarget() instanceof BlockRayTraceResult) {
            BlockRayTraceResult traceResult = (BlockRayTraceResult) event.getTarget();
            BlockState og = Minecraft.getInstance().world.getBlockState(traceResult.getPos());
            if (og.getBlock() instanceof BlockBase && ((BlockBase) og.getBlock()).hasIndividualRenderVoxelShape()) {
                VoxelShape shape = RayTraceUtils.rayTraceVoxelShape(traceResult, Minecraft.getInstance().world, Minecraft.getInstance().player, 32, event.getPartialTicks());
                if (shape != null) {
                    event.setCanceled(true);
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    RenderSystem.lineWidth(Math.max(2.5F, (float) Minecraft.getInstance().func_228018_at_().getFramebufferWidth() / 1920.0F * 2.5F)); //MainWindow
                    RenderSystem.disableTexture();
                    RenderSystem.depthMask(false);
                    RenderSystem.matrixMode(5889);
                    RenderSystem.pushMatrix();
                    RenderSystem.scalef(1.0F, 1.0F, 0.999F);
                    ActiveRenderInfo info = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();
                    BlockPos blockpos = traceResult.getPos();
                    double d0 = info.getProjectedView().x;
                    double d1 = info.getProjectedView().y;
                    double d2 = info.getProjectedView().z;
                    //TODO Minecraft.getInstance().worldRenderer.drawShape(shape, (double) blockpos.getX() - d0, (double) blockpos.getY() - d1, (double) blockpos.getZ() - d2, 0.0F, 0.0F, 0.0F, 0.4F);
                    RenderSystem.popMatrix();
                    RenderSystem.matrixMode(5888);
                    RenderSystem.depthMask(true);
                    RenderSystem.enableTexture();
                    RenderSystem.disableBlend();
                }

            }
        }
    }
}
