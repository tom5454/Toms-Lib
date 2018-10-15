package com.tom.lib.dim;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DimProvider extends WorldProvider {

	@Override
	public DimensionType getDimensionType() {
		return DimensionHandler.type;
	}
	/**
	 * the y level at which clouds are rendered.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public float getCloudHeight() {
		return 256;
	}
}
