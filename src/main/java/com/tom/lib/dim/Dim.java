package com.tom.lib.dim;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

public class Dim {
	private final long id;
	public final WorldServer world;
	public final BlockPos startPos;
	public final AxisAlignedBB bb;
	public AxisAlignedBB actBounds;
	public NBTTagCompound tag = new NBTTagCompound();
	public Dim(long id, WorldServer world, BlockPos startPos, AxisAlignedBB bb) {
		this.id = id;
		this.world = world;
		this.startPos = startPos;
		this.bb = bb;
	}
	public long getId() {
		return id;
	}
}
