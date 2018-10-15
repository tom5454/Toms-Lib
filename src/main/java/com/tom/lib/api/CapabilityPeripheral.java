package com.tom.lib.api;

import java.util.concurrent.Callable;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

import com.tom.lib.api.tileentity.ITMPeripheral.IComputer;
import com.tom.lib.api.tileentity.ITMPeripheral.ITMPeripheralCap;
import com.tom.lib.api.tileentity.ITMPeripheral.LuaException;

import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityPeripheral {
	@CapabilityInject(ITMPeripheralCap.class)
	public static Capability<ITMPeripheralCap> PERIPHERAL = null;

	public static class PeripheralTile implements ITMPeripheralCap {

		@Override
		public String getType() {
			return "dummy";
		}

		@Override
		public String[] getMethodNames() {
			return new String[0];
		}

		@Override
		public Object[] call(IComputer computer, String method, Object[] args) throws LuaException {
			return new Object[0];
		}

		@Override
		public void attach(IComputer computer) {
		}

		@Override
		public void detach(IComputer computer) {
		}

	}
	public static void init(){
		CapabilityManager.INSTANCE.register(ITMPeripheralCap.class, new IStorage<ITMPeripheralCap>() {

			@Override
			public NBTBase writeNBT(Capability<ITMPeripheralCap> capability, ITMPeripheralCap instance, EnumFacing side) {
				return null;
			}

			@Override
			public void readNBT(Capability<ITMPeripheralCap> capability, ITMPeripheralCap instance, EnumFacing side, NBTBase nbt) {

			}

		}, new Callable<ITMPeripheralCap>() {

			@Override
			public ITMPeripheralCap call() throws Exception {
				return new PeripheralTile();
			}
		});
	}
}
