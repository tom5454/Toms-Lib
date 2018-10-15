package com.tom.lib.entity.world;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityWorldShip extends EntityWorld {

	public EntityWorldShip(World worldIn) {
		super(worldIn);
	}

	@Override
	public BlockPos getSize() {
		return new BlockPos(64, 64, 64);
	}

}
