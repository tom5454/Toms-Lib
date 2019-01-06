package com.tom.lib.api;

import java.util.concurrent.Callable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import com.tom.lib.api.grid.IGrid;
import com.tom.lib.api.grid.IGridDevice;
import com.tom.lib.api.grid.IGridDeviceHost;

public class CapabilityGridDeviceHost {
	@CapabilityInject(IGridDeviceHost.class)
	public static Capability<IGridDeviceHost> GRID_DEVICE_HOST = null;

	public static void init(){
		CapabilityManager.INSTANCE.register(IGridDeviceHost.class, new IStorage<IGridDeviceHost>() {

			@Override
			public NBTBase writeNBT(Capability<IGridDeviceHost> capability, IGridDeviceHost instance, EnumFacing side) {
				return null;
			}

			@Override
			public void readNBT(Capability<IGridDeviceHost> capability, IGridDeviceHost instance, EnumFacing side, NBTBase nbtIn) {
			}

		}, new Callable<IGridDeviceHost>() {

			@Override
			public IGridDeviceHost call() throws Exception {
				return new IGridDeviceHost() {

					@Override
					public <G extends IGrid<?, G>> IGridDevice<G> getDevice(Class<G> gridClass, Object... objects) {
						return null;
					}
				};
			}
		});
	}
	public static class Wrapper implements IGridDeviceHost {
		private final IGridDevice<?> dev;
		private final EnumFacing side;
		public Wrapper(IGridDevice<?> dev, EnumFacing side) {
			this.dev = dev;
			this.side = side;
		}
		@SuppressWarnings("unchecked")
		@Override
		public <G extends IGrid<?, G>> IGridDevice<G> getDevice(Class<G> gridClass, Object... objects) {
			if(!dev.isValidConnection(side))return null;
			if(dev.getGrid().getClass() != gridClass)return null;
			try {
				return (IGridDevice<G>) dev;
			} catch (ClassCastException e) {
				return null;
			}
		}
	}
	public static <G extends IGrid<?, G>> IGridDevice<G> getDevice(ICapabilityProvider tile, EnumFacing facing, Class<G> clazz, Object... objects){
		if(tile == null)return null;
		if(tile.hasCapability(GRID_DEVICE_HOST, facing)){
			IGridDeviceHost host = tile.getCapability(GRID_DEVICE_HOST, facing);
			if(host != null){
				return host.getDevice(clazz, objects);
			}
		}
		return null;
	}
}
