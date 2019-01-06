package com.tom.lib.api.grid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.tom.lib.api.CapabilityGridDeviceHost;

public abstract class GridBase<D, T extends IGrid<D, T>> implements IGrid<D, T> {
	protected List<IGridDevice<T>> parts = new ArrayList<>();
	protected IGridDevice<T> master;

	@Override
	public void reloadGrid(World world, IGridDevice<T> master) {
		this.forceUpdateGrid(world, master);
		this.updateGrid(world, master);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void forceUpdateGrid(IBlockAccess world, IGridDevice<T> thisT) {
		invalidateAll();
		parts.clear();
		Set<IGridDevice<T>> connectedStorages = new HashSet<>();
		Stack<IGridDevice<T>> traversingStorages = new Stack<>();
		IGridDevice<T> masterOld = master;
		master = thisT;
		traversingStorages.add(thisT);
		while (!traversingStorages.isEmpty()) {
			IGridDevice<T> storage = traversingStorages.pop();
			if (storage != null && storage.isValid()) {
				if (storage.isMaster()) {
					master = storage;
				}
				connectedStorages.add(storage);
				List<IGridDevice<T>> sub = storage.listSubDevices();
				if(sub != null && !sub.isEmpty()){
					connectedStorages.addAll(sub);
				}
				for (BlockAccess pos : storage.next()) {
					TileEntity hostTile = pos.getTileEntity();
					IGridDevice<T> dev = CapabilityGridDeviceHost.getDevice(hostTile, pos.getFacing(), (Class<T>) getClass(), getExtra());
					if (dev != null && !connectedStorages.contains(dev) && !traversingStorages.contains(dev)){
						traversingStorages.add(dev);
					}
				}
			}
		}
		if (masterOld != null && (!masterOld.isValid() || master.getPos2().equals(masterOld.getPos2()))) {
			NBTTagCompound tag = masterOld.getGridData();
			if (tag != null)
				setData(importFromNBT(tag).getData());
		}
		master.setGrid((T) this);
		master.getGrid().onForceUpdateDone();
		List<IGridUpdateListener> listeners = new ArrayList<>();
		for (IGridDevice<T> storage : connectedStorages) {
			storage.setMaster(master, connectedStorages.size());
			if (storage instanceof IGridUpdateListener) {
				IGridUpdateListener l = ((IGridUpdateListener) storage);
				l.onGridReload();
				listeners.add(l);
			}
		}
		for (IGridUpdateListener l : listeners) {
			l.onGridPostReload();
		}
		this.parts.addAll(connectedStorages);
	}

	@Override
	public List<IGridDevice<T>> getParts() {
		return this.parts;
	}

	@Override
	public IGridDevice<T> getMaster() {
		return master;
	}

	public abstract void writeToNBT(NBTTagCompound tag);

	@Override
	public NBTTagCompound exportToNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);
		return tag;
	}

	@Override
	public void onForceUpdateDone() {

	}

	@Override
	public void setMaster(IGridDevice<T> master) {
		this.master = master;
	}

	@Override
	public void invalidate() {
	}

	@Override
	public void invalidateAll() {
		for (int i = 0;i < parts.size();i++)
			parts.get(i).invalidateGrid();
		parts.clear();
	}
	public Object[] getExtra(){
		return null;
	}
}
