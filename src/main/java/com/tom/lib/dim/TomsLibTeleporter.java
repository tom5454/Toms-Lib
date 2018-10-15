package com.tom.lib.dim;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class TomsLibTeleporter extends Teleporter {
	private final WorldServer world;
	private double x;
	private double y;
	private double z;

	public TomsLibTeleporter(WorldServer world, double x, double y, double z) {
		super(world);
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;

	}

	@Override
	public void placeInPortal(Entity pEntity, float rotationYaw) {
		this.world.getBlockState(new BlockPos((int) this.x, (int) this.y, (int) this.z));
		pEntity.setPosition(this.x, this.y, this.z);
		pEntity.motionX = 0.0f;
		pEntity.motionY = 0.0f;
		pEntity.motionZ = 0.0f;
	}
}
