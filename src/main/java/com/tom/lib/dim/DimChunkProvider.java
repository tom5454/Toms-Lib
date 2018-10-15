package com.tom.lib.dim;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;

public class DimChunkProvider extends ChunkProviderServer {
	public DimChunkProvider(WorldServer worldObjIn, IChunkLoader chunkLoaderIn) {
		super(worldObjIn, chunkLoaderIn, new Generator());
		((Generator)chunkGenerator).ins = this;
	}

	public static class Generator implements IChunkGenerator {
		DimChunkProvider ins;
		@Override
		public Chunk generateChunk(int x, int z) {
			ChunkPrimer chunkprimer = new ChunkPrimer();
			IBlockState iblockstate = Blocks.BARRIER.getDefaultState();

			for (int i = 0; i < 256; ++i)
			{
				for (int j = 0; j < 16; ++j)
				{
					for (int k = 0; k < 16; ++k)
					{
						chunkprimer.setBlockState(j, i, k, iblockstate);
					}
				}
			}
			Chunk chunk = new Chunk(ins.world, chunkprimer, x, z);
			byte[] abyte = chunk.getBiomeArray();

			for (int l = 0; l < abyte.length; ++l)
			{
				abyte[l] = (byte)127;
			}

			chunk.generateSkylightMap();
			return chunk;
		}

		@Override
		public void populate(int x, int z) {

		}

		@Override
		public boolean generateStructures(Chunk chunkIn, int x, int z) {
			return false;
		}

		@Override
		public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
			return Collections.emptyList();
		}

		@Override
		public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
			return null;
		}

		@Override
		public void recreateStructures(Chunk chunkIn, int x, int z) {

		}

		@Override
		public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
			return false;
		}

	}
}
