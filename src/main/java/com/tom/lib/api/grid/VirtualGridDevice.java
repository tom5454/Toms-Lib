package com.tom.lib.api.grid;

import net.minecraft.nbt.NBTTagCompound;

import com.tom.lib.handler.WorldHandler;

public abstract class VirtualGridDevice<G extends IGrid<?, G>> implements IGridDevice<G> {
	protected G grid;
	protected IGridDevice<G> master;
	protected static final String GRID_TAG_NAME = "grid";
	private static final String MASTER_NBT_NAME = "isMaster";
	private boolean secondTick = false;
	protected boolean isMaster = false;
	protected int suction = -1;
	private NBTTagCompound last;
	public VirtualGridDevice() {
		grid = constructGrid();
	}
	@Override
	public boolean isMaster() {
		return isMaster;
	}

	@Override
	public void setMaster(IGridDevice<G> master, int size) {
		this.master = master;
		isMaster = master == this;
		grid.invalidate();
		this.grid = master.getGrid();
	}

	@Override
	public G getGrid() {
		return grid;
	}

	@Override
	public IGridDevice<G> getMaster() {
		grid.forceUpdateGrid(getWorld2(), this);
		return master;
	}

	@Override
	public void invalidateGrid() {
		this.master = null;
		this.isMaster = false;
		WorldHandler.queueTask(getWorld2().provider.getDimension(), () -> {
			if (this.master == null && !secondTick)
				WorldHandler.queueTask(getWorld2().provider.getDimension(), () -> {
					if (this.master == null && !secondTick)
						this.constructGrid().forceUpdateGrid(getWorld2(), this);
				});
		});
		last = grid.exportToNBT();
		grid.invalidate();
		this.grid = this.constructGrid();
	}

	@Override
	public void setSuctionValue(int suction) {
		this.suction = suction;
	}

	@Override
	public int getSuctionValue() {
		return this.suction;
	}

	@Override
	public void updateState() {
		updateGrid();
	}

	@Override
	public void setGrid(G newGrid) {
		grid.invalidate();
		this.grid = newGrid;
	}

	@Override
	public NBTTagCompound getGridData() {
		return last;
	}
	public abstract G constructGrid();

	private void updateGrid() {
		if (master != null && master != this && master.isValid())
			master.updateState();
		else {
			if (master == null) {
				grid.invalidateAll();
				G grid = this.constructGrid();
				grid.setMaster(master);
				grid.forceUpdateGrid(this.getWorld2(), this);
			} else {
				grid.forceUpdateGrid(this.getWorld2(), this);
			}
		}
	}
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag(GRID_TAG_NAME, grid.exportToNBT());
		compound.setBoolean(MASTER_NBT_NAME, isMaster);
		return compound;
	}

	public void readFromNBT(NBTTagCompound compound) {
		grid.importFromNBT(compound.getCompoundTag(GRID_TAG_NAME));
		this.isMaster = compound.getBoolean(MASTER_NBT_NAME);
	}
	public void update(){
		if (this.isMaster) {
			grid.updateGrid(getWorld2(), this);
		}
	}
	public void neighborUpdateGrid(boolean force) {
		if (force) {
			WorldHandler.queueTask(getWorld2().provider.getDimension(), grid::invalidateAll);
		} else
			updateGrid();
	}
}
