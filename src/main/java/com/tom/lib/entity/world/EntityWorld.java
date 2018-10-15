package com.tom.lib.entity.world;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.tom.lib.dim.Dim;
import com.tom.lib.dim.DimensionHandler;
import com.tom.lib.utils.TomsUtils;

public abstract class EntityWorld extends Entity {
	private Dim dim;
	private List<EntityPlayerMP> queuedTracking = new ArrayList<>();
	public static final DataParameter<Long> ID_PARAM = new DataParameter<>(16, TomsUtils.LONG_SERIALIZER);
	public EntityWorld(World worldIn) {
		super(worldIn);
		dataManager.register(ID_PARAM, -1L);
	}

	@Override
	protected void entityInit() {
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		long dimid = compound.getLong("worldid")-1;
		dataManager.set(ID_PARAM, dimid);
		if(dimid != -1){
			dim = DimensionHandler.getRegion(dimid);
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setLong("worldid", dim != null ? dim.getId()+1 : dataManager.get(ID_PARAM));
		WorldTeleportHandler.writeEntityPos(dim, this);
	}
	public abstract BlockPos getSize();

	@Override
	public void onEntityUpdate() {
		world.profiler.startSection("EntityWorld");
		super.onEntityUpdate();
		if(dim == null && !world.isRemote){
			BlockPos p = getSize();
			dim = DimensionHandler.createRegion(p.getX(), p.getY(), p.getZ(), Blocks.BARRIER.getDefaultState(), true);
			queuedTracking.forEach(this::addTrackingPlayer);
			queuedTracking = null;
		}
		world.profiler.endSection();
	}
	@Override
	public boolean canBeCollidedWith() {
		return true;
	}
	@Override
	public AxisAlignedBB getEntityBoundingBox() {
		return new AxisAlignedBB(posX-5, posY-5, posZ-5, posX+5, posY+5, posZ+5);// new AxisAlignedBB(posX-64, posY-64, posZ-64, posX+64, posY+64, posZ+64);
	}
	@Override
	public AxisAlignedBB getCollisionBox(Entity entityIn) {
		return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
	}
	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
		if(!world.isRemote){
			WorldTeleportHandler.teleportPlayerIn(player, this);
		}
		return true;
	}
	public Dim getDim() {
		return dim;
	}
	@Override
	public void addTrackingPlayer(EntityPlayerMP player) {
		WorldTeleportHandler.addTracking(player, this);
		System.out.println("EntityWorld.addTrackingPlayer(" + player + ")");
	}
	@Override
	public void removeTrackingPlayer(EntityPlayerMP player) {
		WorldTeleportHandler.removeTracking(player, this);
		System.out.println("EntityWorld.removeTrackingPlayer(" + player + ")");
	}

	public long getDimID() {
		return dataManager.get(ID_PARAM);
	}

	public void queue(EntityPlayerMP player) {
		queuedTracking.add(player);
	}
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
}
