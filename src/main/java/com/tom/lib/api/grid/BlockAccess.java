package com.tom.lib.api.grid;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAccess {
	private final World world;
	private final BlockPos pos;
	private final EnumFacing facing;

	public BlockAccess(World world, BlockPos pos, EnumFacing facing) {
		this.world = world;
		this.pos = pos;
		this.facing = facing;
	}

	public BlockPos getPos() {
		return pos;
	}

	public EnumFacing getFacing() {
		return facing;
	}

	public World getWorld() {
		return world;
	}
	public TileEntity getTileEntity(){
		return world.getTileEntity(pos);
	}
}