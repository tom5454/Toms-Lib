package com.tom.lib.thirdparty.computercraft;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.tom.lib.api.CapabilityPeripheral;
import com.tom.lib.api.tileentity.ITMPeripheral.IComputer;
import com.tom.lib.api.tileentity.ITMPeripheral.ITMPeripheralCap;
import com.tom.lib.api.tileentity.ITMPeripheral.LuaInteruptedException;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;

public class TomsLibProvider implements IPeripheralProvider {
	public static final TomsLibProvider INSTANCE = new TomsLibProvider();
	private TomsLibProvider() {
	}
	private static ITMPeripheralCap getCap(World world, BlockPos pos, EnumFacing side){
		TileEntity te = world.getTileEntity(pos);
		if(te != null && te.hasCapability(CapabilityPeripheral.PERIPHERAL, side)){
			return te.getCapability(CapabilityPeripheral.PERIPHERAL, side);
		}
		return null;
	}
	@Override
	public IPeripheral getPeripheral(World world, BlockPos pos, EnumFacing side) {
		ITMPeripheralCap p = getCap(world, pos, side);
		return p != null ? new PeripheralTomsMod(p, world, pos, side, world.getTotalWorldTime()) : null;
	}
	public static class PeripheralTomsMod implements IPeripheral {
		private ITMPeripheralCap p;
		private World world;
		private BlockPos pos;
		private EnumFacing side;
		private long lastUpdate;
		public PeripheralTomsMod(ITMPeripheralCap p, World world, BlockPos pos, EnumFacing side, long update) {
			this.p = p;
			this.world = world;
			this.pos = pos;
			this.side = side;
			lastUpdate = update;
		}
		@Override
		public String getType() {
			if(!check())return "~~ERROR~~";
			return p.getType();
		}

		private boolean check() {
			long l = world.getTotalWorldTime();
			if(l > lastUpdate + 20){
				lastUpdate = l;
				p = getCap(world, pos, side);
			}
			return p != null;
		}
		@Override
		public String[] getMethodNames() {
			if(!check())return new String[0];
			return p.getMethodNames();
		}

		@Override
		public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
			if(!check())throw new LuaException("Peripheral missing");
			try {
				IComputer c = new CCComputer(computer, context);
				return c.map(p.call(c, p.getMethodNames()[method], arguments));
			} catch (com.tom.lib.api.tileentity.ITMPeripheral.LuaException e) {
				if(e instanceof LuaInteruptedException){
					throw new InterruptedException();
				}
				throw e.toLuaException();
			}
		}

		@Override
		public boolean equals(IPeripheral other) {
			if(!check())return false;
			return this == other;
		}
		@Override
		public void attach(IComputerAccess computer) {
			if(!check())return;
			p.attach(new CCComputer(computer, null));
		}
		@Override
		public void detach(IComputerAccess computer) {
			if(!check())return;
			p.detach(new CCComputer(computer, null));
		}
	}
}
