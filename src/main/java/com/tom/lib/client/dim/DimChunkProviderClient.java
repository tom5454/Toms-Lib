package com.tom.lib.client.dim;

import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DimChunkProviderClient extends ChunkProviderClient {

	public DimChunkProviderClient(World worldIn) {
		super(worldIn);
	}

}
