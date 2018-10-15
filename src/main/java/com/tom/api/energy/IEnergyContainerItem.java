package com.tom.api.energy;

import net.minecraft.item.ItemStack;

public interface IEnergyContainerItem {
	double getEnergyStored(ItemStack container);
	long getMaxEnergyStored(ItemStack container);
	double receiveEnergy(ItemStack container, double maxReceive, boolean simulate);
	double extractEnergy(ItemStack container, double maxExtract, boolean simulate);
}
