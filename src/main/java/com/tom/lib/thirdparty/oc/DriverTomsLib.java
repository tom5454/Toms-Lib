package com.tom.lib.thirdparty.oc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.tom.lib.api.CapabilityPeripheral;
import com.tom.lib.api.tileentity.ITMPeripheral.IComputer;
import com.tom.lib.api.tileentity.ITMPeripheral.ITMPeripheralCap;

import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.ManagedPeripheral;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;

public class DriverTomsLib extends DriverSidedTileEntity {

	public static class InternalManagedEnvironment extends AbstractManagedEnvironment implements ManagedPeripheral, NamedBlock {
		private final ITMPeripheralCap tile;
		private final String[] methods;

		public InternalManagedEnvironment(ITMPeripheralCap tile){
			this.tile = tile;
			setNode(Network.newNode(this, Visibility.Network).withComponent(this.tile.getType(), Visibility.Network).create());
			String[] m = tile.getMethodNames();
			List<String> methods = new ArrayList<>(Arrays.asList(m));
			methods.add("connectListener");
			methods.add("disconnectListener");
			this.methods = methods.toArray(new String[0]);
		}

		@Override
		public String preferredName(){
			return tile.getType();
		}

		@Override
		public int priority(){
			return 20;
		}

		@Override
		public String[] methods(){
			return methods;
		}

		@Override
		public Object[] invoke(String method, Context context, Arguments args) throws Exception {
			IComputer c = new OCComputer(context, node());
			if("connectListener".equals(method)) {
				tile.attach(c);
				return new Object[]{true};
			}else if("disconnectListener".equals(method)) {
				tile.detach(c);
				return new Object[]{true};
			}else
				return c.map(tile.call(c, method, args.toArray()));
		}
	}

	@Override
	public Class<?> getTileEntityClass(){
		return TileEntity.class;
	}

	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
		TileEntity te = world.getTileEntity(pos);
		if(te != null && te.hasCapability(CapabilityPeripheral.PERIPHERAL, side)){
			ITMPeripheralCap p = te.getCapability(CapabilityPeripheral.PERIPHERAL, side);
			return p != null ? new InternalManagedEnvironment(p) : null;
		}
		return null;
	}
}
