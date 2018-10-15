package com.tom.lib.dim;

import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.IChunkGenerator;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;

import com.google.common.base.Predicate;

import com.tom.lib.utils.ReflectionUtils;

public class DimChunk extends Chunk {
	private Chunk chunk;
	public DimChunk(Chunk old) {
		super(old.getWorld(), old.x, old.z);
		this.chunk = old;
		ReflectionUtils.nullify(Chunk.class, this, DimensionHandler.log);
	}
	@Override
	public int hashCode() {
		return chunk.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		return chunk.equals(obj);
	}
	@Override
	public boolean isAtLocation(int x, int z) {
		return chunk.isAtLocation(x, z);
	}
	@Override
	public int getHeight(BlockPos pos) {
		return chunk.getHeight(pos);
	}
	@Override
	public int getHeightValue(int x, int z) {
		return chunk.getHeightValue(x, z);
	}
	@Override
	public int getTopFilledSegment() {
		return chunk.getTopFilledSegment();
	}
	@Override
	public ExtendedBlockStorage[] getBlockStorageArray() {
		return chunk.getBlockStorageArray();
	}
	@Override
	public void generateSkylightMap() {
		chunk.generateSkylightMap();
	}
	@Override
	public String toString() {
		return chunk.toString();
	}
	@Override
	public int getBlockLightOpacity(BlockPos pos) {
		return chunk.getBlockLightOpacity(pos);
	}
	@Override
	public IBlockState getBlockState(BlockPos pos) {
		return chunk.getBlockState(pos);
	}
	@Override
	public IBlockState getBlockState(int x, int y, int z) {
		return chunk.getBlockState(x, y, z);
	}
	@Override
	public IBlockState setBlockState(BlockPos pos, IBlockState state) {
		return chunk.setBlockState(pos, state);
	}
	@Override
	public int getLightFor(EnumSkyBlock type, BlockPos pos) {
		return chunk.getLightFor(type, pos);
	}
	@Override
	public void setLightFor(EnumSkyBlock type, BlockPos pos, int value) {
		chunk.setLightFor(type, pos, value);
	}
	@Override
	public int getLightSubtracted(BlockPos pos, int amount) {
		return chunk.getLightSubtracted(pos, amount);
	}
	@Override
	public void addEntity(Entity entityIn) {
		chunk.addEntity(entityIn);
	}
	@Override
	public void removeEntity(Entity entityIn) {
		chunk.removeEntity(entityIn);
	}
	@Override
	public void removeEntityAtIndex(Entity entityIn, int index) {
		chunk.removeEntityAtIndex(entityIn, index);
	}
	@Override
	public boolean canSeeSky(BlockPos pos) {
		return chunk.canSeeSky(pos);
	}
	@Override
	public TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType creationMode) {
		return chunk.getTileEntity(pos, creationMode);
	}
	@Override
	public void addTileEntity(TileEntity tileEntityIn) {
		chunk.addTileEntity(tileEntityIn);
	}
	@Override
	public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
		chunk.addTileEntity(pos, tileEntityIn);
	}
	@Override
	public void removeTileEntity(BlockPos pos) {
		chunk.removeTileEntity(pos);
	}
	@Override
	public void onLoad() {
		chunk.onLoad();
	}
	@Override
	public void onUnload() {
		chunk.onUnload();
	}
	@Override
	public void markDirty() {
		chunk.markDirty();
	}
	@Override
	public void getEntitiesWithinAABBForEntity(Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill, Predicate<? super Entity> filter) {
		chunk.getEntitiesWithinAABBForEntity(entityIn, aabb, listToFill, filter);
	}
	@Override
	public <T extends Entity> void getEntitiesOfTypeWithinAABB(Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> filter) {
		chunk.getEntitiesOfTypeWithinAABB(entityClass, aabb, listToFill, filter);
	}
	@Override
	public boolean needsSaving(boolean p_76601_1_) {
		return chunk.needsSaving(p_76601_1_);
	}
	@Override
	public Random getRandomWithSeed(long seed) {
		return chunk.getRandomWithSeed(seed);
	}
	@Override
	public boolean isEmpty() {
		return chunk.isEmpty();
	}
	@Override
	public void populate(IChunkProvider chunkProvider, IChunkGenerator chunkGenrator) {
		chunk.populate(chunkProvider, chunkGenrator);
	}
	@Override
	public BlockPos getPrecipitationHeight(BlockPos pos) {
		return chunk.getPrecipitationHeight(pos);
	}
	@Override
	public void onTick(boolean skipRecheckGaps) {
		chunk.unloadQueued = unloadQueued;
		chunk.onTick(skipRecheckGaps);
	}
	@Override
	public boolean isPopulated() {
		return chunk.isPopulated();
	}
	@Override
	public boolean wasTicked() {
		return chunk.wasTicked();
	}
	@Override
	public ChunkPos getPos() {
		return chunk.getPos();
	}
	@Override
	public boolean isEmptyBetween(int startY, int endY) {
		return chunk.isEmptyBetween(startY, endY);
	}
	@Override
	public void setStorageArrays(ExtendedBlockStorage[] newStorageArrays) {
		chunk.setStorageArrays(newStorageArrays);
	}
	@Override
	public void read(PacketBuffer buf, int availableSections, boolean groundUpContinuous) {
		chunk.read(buf, availableSections, groundUpContinuous);
	}
	@Override
	public Biome getBiome(BlockPos pos, BiomeProvider provider) {
		return chunk.getBiome(pos, provider);
	}
	@Override
	public byte[] getBiomeArray() {
		return chunk.getBiomeArray();
	}
	@Override
	public void setBiomeArray(byte[] biomeArray) {
		chunk.setBiomeArray(biomeArray);
	}
	@Override
	public void resetRelightChecks() {
		chunk.resetRelightChecks();
	}
	@Override
	public void enqueueRelightChecks() {
		chunk.enqueueRelightChecks();
	}
	@Override
	public void checkLight() {
		chunk.checkLight();
	}
	@Override
	public boolean isLoaded() {
		return chunk.isLoaded();
	}
	@Override
	public void markLoaded(boolean loaded) {
		chunk.markLoaded(loaded);
	}
	@Override
	public World getWorld() {
		return chunk.getWorld();
	}
	@Override
	public int[] getHeightMap() {
		return chunk.getHeightMap();
	}
	@Override
	public void setHeightMap(int[] newHeightMap) {
		chunk.setHeightMap(newHeightMap);
	}
	@Override
	public Map<BlockPos, TileEntity> getTileEntityMap() {
		return chunk.getTileEntityMap();
	}
	@Override
	public ClassInheritanceMultiMap<Entity>[] getEntityLists() {
		return chunk.getEntityLists();
	}
	@Override
	public boolean isTerrainPopulated() {
		return chunk.isTerrainPopulated();
	}
	@Override
	public void setTerrainPopulated(boolean terrainPopulated) {
		chunk.setTerrainPopulated(terrainPopulated);
	}
	@Override
	public boolean isLightPopulated() {
		return chunk.isLightPopulated();
	}
	@Override
	public void setLightPopulated(boolean lightPopulated) {
		chunk.setLightPopulated(lightPopulated);
	}
	@Override
	public void setModified(boolean modified) {
		chunk.setModified(modified);
	}
	@Override
	public void setHasEntities(boolean hasEntitiesIn) {
		chunk.setHasEntities(hasEntitiesIn);
	}
	@Override
	public void setLastSaveTime(long saveTime) {
		chunk.setLastSaveTime(saveTime);
	}
	@Override
	public int getLowestHeight() {
		return chunk.getLowestHeight();
	}
	@Override
	public long getInhabitedTime() {
		return chunk.getInhabitedTime();
	}
	@Override
	public void setInhabitedTime(long newInhabitedTime) {
		chunk.setInhabitedTime(newInhabitedTime);
	}
	@Override
	public void removeInvalidTileEntity(BlockPos pos) {
		chunk.removeInvalidTileEntity(pos);
	}
	@Override
	public CapabilityDispatcher getCapabilities() {
		return chunk.getCapabilities();
	}
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return chunk.hasCapability(capability, facing);
	}
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return chunk.getCapability(capability, facing);
	}
}
