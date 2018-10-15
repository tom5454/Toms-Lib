package com.tom.api.energy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import com.tom.lib.utils.EmptyEntry;
import com.tom.lib.utils.Modids;

import cofh.redstoneflux.api.IEnergyReceiver;

public interface IRFReceiver extends IEnergyReceiver, IRFMachine {
	long receiveRF(EnumFacing side, long maxReceive, boolean simulate);

	@Override
	default int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
		return canConnectEnergy(from) ? (int) receiveRF(from, maxReceive, simulate) : 0;
	}

	@Override
	@SuppressWarnings("rawtypes")
	default Map<Capability, Map<EnumFacing, Supplier<Object>>> initCapabilities() {
		Map<Capability, Map<EnumFacing, Supplier<Object>>> caps = new HashMap<>();
		if (Loader.isModLoaded(Modids.TESLA)) {
			Entry<Capability, Map<EnumFacing, Supplier<Object>>> c = createTeslaCapability(this);
			caps.put(c.getKey(), c.getValue());
		}
		Map<EnumFacing, Supplier<Object>> forge = new HashMap<>();
		for (EnumFacing f : EnumFacing.VALUES) {
			Object o = new RFStorage(this, f);
			forge.put(f, () -> o);
		}
		caps.put(CapabilityEnergy.ENERGY, forge);
		return caps;
	}

	@SuppressWarnings("rawtypes")
	@Optional.Method(modid = Modids.TESLA)
	static Entry<Capability, Map<EnumFacing, Supplier<Object>>> createTeslaCapability(IRFReceiver receiver) {
		Map<EnumFacing, Supplier<Object>> forge = new HashMap<>();
		for (EnumFacing f : EnumFacing.VALUES) {
			Object o = new ITeslaConsumer() {

				@Override
				public long givePower(long power, boolean simulated) {
					return receiver.receiveRF(f, power, simulated);
				}
			};
			forge.put(f, () -> o);
		}
		return new EmptyEntry<>(TeslaCapabilities.CAPABILITY_CONSUMER, forge);
	}
}
