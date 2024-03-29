/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.component.inventory;

import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.AssetTypes;
import com.hrznstudio.titanium.api.client.IAsset;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.block.tile.ActiveTile;
import com.hrznstudio.titanium.client.screen.addon.FacingHandlerScreenAddon;
import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.component.sideness.IFacingComponent;
import com.hrznstudio.titanium.component.sideness.SidedComponentManager;
import com.hrznstudio.titanium.util.FacingUtil;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotExposedStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.awt.*;
import java.util.HashMap;
import java.util.List;

public class SidedInventoryComponent<T extends IComponentHarness> extends InventoryComponent<T> implements IFacingComponent {

    private int color;
    private int facingHandlerX = 8;
    private int facingHandlerY = 84;
    private HashMap<FacingUtil.Sideness, FaceMode> facingModes;
    private HashMap<FacingUtil.Sideness, Integer> slotCache;
    private int position;
    private boolean hasFacingAddon;
    private FaceMode[] validFaceModes;

    public SidedInventoryComponent(String name, int xPos, int yPos, int size, int position) {
        super(name, xPos, yPos, size);
        this.color = DyeColor.WHITE.getFireworkColor();
        this.facingModes = new HashMap<>();
        this.slotCache = new HashMap<>();
        for (FacingUtil.Sideness value : FacingUtil.Sideness.values()) {
            this.facingModes.put(value, FaceMode.ENABLED);
        }
        this.position = position;
        this.setColorGuiEnabled(true);
        this.hasFacingAddon = true;
        this.validFaceModes = FaceMode.values();
    }

    public SidedInventoryComponent<T> disableFacingAddon() {
        this.hasFacingAddon = false;
        return this;
    }

    @Override
    public HashMap<FacingUtil.Sideness, FaceMode> getFacingModes() {
        return facingModes;
    }

    @Override
    public int getColor() {
        return new Color(color).getRGB();
    }

    public SidedInventoryComponent<T> setColor(int color) {
        this.color = color;
        return this;
    }

    public SidedInventoryComponent<T> setColor(DyeColor color) {
        this.color = color.getMaterialColor().col;
        return this;
    }

    @Override
    public Color getColorForSlotRendering(int slot) {
        return getSlotToColorRenderMap().getOrDefault(slot, new Color(color));
    }

    @Override
    public Rectangle getRectangle(IAsset asset) {
        int renderingOffset = 1;
        Rectangle rectangle = new Rectangle(getSlotPosition().apply(0).getLeft() - 1, getSlotPosition().apply(0).getRight() - 1, (int) asset.getArea().getWidth() + getSlotPosition().apply(0).getLeft(), (int) asset.getArea().getHeight() + getSlotPosition().apply(0).getRight());
        for (int i = 0; i < getSlots(); i++) {
            if (getSlotPosition().apply(i).getLeft() < rectangle.getX()) {
                rectangle.setLocation(getSlotPosition().apply(i).getLeft(), rectangle.y);
            }
            if (getSlotPosition().apply(i).getRight() < rectangle.getY()) {
                rectangle.setLocation(rectangle.x, getSlotPosition().apply(i).getRight());
            }
            if (getSlotPosition().apply(i).getLeft() + asset.getArea().getWidth() > rectangle.width) {
                rectangle.setSize(getSlotPosition().apply(i).getLeft() + asset.getArea().width, rectangle.height);
            }
            if (getSlotPosition().apply(i).getRight() + asset.getArea().getHeight() > rectangle.height) {
                rectangle.setSize(rectangle.width, getSlotPosition().apply(i).getRight() + asset.getArea().height);
            }
        }
        return new Rectangle(this.getXPos() - renderingOffset - 2 + rectangle.x, this.getYPos() - renderingOffset - 2 + rectangle.y, rectangle.width + renderingOffset * 2 + 3, rectangle.height + renderingOffset * 2 + 3);
    }

    @Override
    public int getFacingHandlerX() {
        return this.facingHandlerX;
    }

    @Override
    public int getFacingHandlerY() {
        return this.facingHandlerY;
    }

    @Override
    public boolean work(Level world, BlockPos pos, Direction blockFacing, int workAmount) {
        for (FacingUtil.Sideness sideness : facingModes.keySet()) {
            if (facingModes.get(sideness).equals(FaceMode.PUSH)) {
                Direction real = FacingUtil.getFacingFromSide(blockFacing, sideness);
                BlockEntity entity = world.getBlockEntity(pos.relative(real));
                if (entity != null) {
//                    boolean hasWorked = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, real.getOpposite()) TODO: PORT
//                            .map(iItemHandler -> transfer(sideness, this, iItemHandler, workAmount))
//                            .orElse(false);
//                    if (hasWorked) {
//                        return true;
//                    }
                }
            }
        }
        for (FacingUtil.Sideness sideness : facingModes.keySet()) {
            if (facingModes.get(sideness).equals(FaceMode.PULL)) {
                Direction real = FacingUtil.getFacingFromSide(blockFacing, sideness);
                BlockEntity entity = world.getBlockEntity(pos.relative(real));
                if (entity != null) {
//                    boolean hasWorked = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, real.getOpposite()) TODO: PORT
//                            .map(iItemHandler -> transfer(sideness, iItemHandler, this, workAmount))
//                            .orElse(false);
//                    if (hasWorked) {
//                        return true;
//                    }
                }
            }
        }
        return false;
    }

    @Override
    public SidedInventoryComponent<T> setFacingHandlerPos(int x, int y) {
        this.facingHandlerX = x;
        this.facingHandlerY = y;
        return this;
    }

    @Override
    public FaceMode[] getValidFacingModes() {
        return validFaceModes;
    }

    public SidedInventoryComponent<T> setValidFaceModes(FaceMode... validFaceModes){
        this.validFaceModes = validFaceModes;
        for (FacingUtil.Sideness value : FacingUtil.Sideness.values()) {
            this.facingModes.put(value, validFaceModes[0]);
        }
        return this;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        CompoundTag compound = new CompoundTag();
        for (FacingUtil.Sideness facing : facingModes.keySet()) {
            compound.putString(facing.name(), facingModes.get(facing).name());
        }
        nbt.put("FacingModes", compound);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        if (nbt.contains("FacingModes")) {
            CompoundTag compound = nbt.getCompound("FacingModes");
            for (String face : compound.getAllKeys()) {
                facingModes.put(FacingUtil.Sideness.valueOf(face), FaceMode.valueOf(compound.getString(face)));
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        List<IFactory<? extends IScreenAddon>> addons = super.getScreenAddons();
        if (hasFacingAddon)
            addons.add(() -> new FacingHandlerScreenAddon(SidedComponentManager.ofRight(getFacingHandlerX(), getFacingHandlerY(), position, AssetTypes.BUTTON_SIDENESS_MANAGER, 4), this, AssetTypes.SLOT, this.getComponentHarness() instanceof ActiveTile ? ((ActiveTile) this.getComponentHarness()).getFacingDirection() : Direction.NORTH));
        return addons;
    }

    private int getNextSlot(SlotExposedStorage handler, int currentSlot) {
        for (int i = currentSlot; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) return i;
        }
        return 0;
    }

    private boolean transfer(FacingUtil.Sideness sideness, SlotExposedStorage from, SlotExposedStorage to, int workAmount) {
        if (from.getSlots() <= 0) return false;
        int slot = slotCache.getOrDefault(sideness, getNextSlot(from, 0));
        if (slot >= from.getSlots()) slot = 0;
        try (Transaction t = TransferUtil.getTransaction()) {
            long extracted = from.simulateExtract(ItemVariant.of(from.getStackInSlot(slot)), workAmount, t);
            int outSlot = isValidForAnySlot(to, new ItemStack(from.getStackInSlot(slot).getItem(), (int) extracted));
            if (extracted >= 0 && outSlot != -1) {
                long returned = to.insert(ItemVariant.of(to.getStackInSlot(outSlot)), extracted, t);
                boolean result = from.extract(ItemVariant.of(from.getStackInSlot(slot)), extracted - returned, t) != 0;
                t.commit();
                return result;
            }
            slotCache.put(sideness, getNextSlot(from, slot + 1));
        }
        return false;
    }

    private int isValidForAnySlot(SlotExposedStorage dest, ItemStack stack) {
        for (int i = 0; i < dest.getSlots(); i++) {
            if (!dest.isItemValid(i, ItemVariant.of(stack), stack.getCount())) continue;
            if (dest.getStackInSlot(i).isEmpty()) return i;
            if (ItemHandlerHelper.canItemStacksStack(dest.getStackInSlot(i), stack) && dest.getStackInSlot(i).getCount() < dest.getSlotLimit(i) && dest.getStackInSlot(i).getCount() < dest.getStackInSlot(i).getMaxStackSize()) {
                return i;
            }
        }
        return -1;
    }
}
