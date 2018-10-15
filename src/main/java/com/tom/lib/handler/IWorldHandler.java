package com.tom.lib.handler;

import java.util.List;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;

public interface IWorldHandler {
	public void load(NBTTagCompound tag);
	public void save(NBTTagCompound tag);
	public String getID();
	public boolean onEntitySpawning(BlockPos pos, EnumCreatureType type, List<SpawnListEntry> list);
	public void loadChunk(Chunk chunk);
	public void unloadChunk(Chunk chunk);
	public void updatePre(World world);
	public void updatePost(World world);
}
