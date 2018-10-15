package com.tom.api.energy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import net.darkhax.tesla.api.ITeslaProducer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import com.tom.lib.utils.EmptyEntry;
import com.tom.lib.utils.Modids;

import cofh.redstoneflux.api.IEnergyProvider;

public interface IRFProvider extends IEnergyProvider, IRFMachine {
	long extractRF(EnumFacing side, long maxExtract, boolean simulate);

	@Override
	default int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
		return canConnectEnergy(from) ? (int) extractRF(from, maxExtract, simulate) : 0;
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
	static Entry<Capability, Map<EnumFacing, Supplier<Object>>> createTeslaCapability(IRFProvider provider) {
		Map<EnumFacing, Supplier<Object>> forge = new HashMap<>();
		for (EnumFacing f : EnumFacing.VALUES) {
			Object o = new ITeslaProducer() {

				@Override
				public long takePower(long power, boolean simulated) {
					return provider.extractRF(f, power, simulated);
				}
			};
			forge.put(f, () -> o);
		}
		return new EmptyEntry<>(TeslaCapabilities.CAPABILITY_PRODUCER, forge);
	}
}
