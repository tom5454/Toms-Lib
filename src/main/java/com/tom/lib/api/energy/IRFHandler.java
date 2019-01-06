package com.tom.lib.api.energy;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaProducer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import com.tom.lib.utils.EmptyEntry;
import com.tom.lib.utils.Modids;

public interface IRFHandler extends IRFProvider, IRFReceiver {
	@Override
	long extractRF(EnumFacing side, long maxExtract, boolean simulate);

	@Override
	long receiveRF(EnumFacing side, long maxReceive, boolean simulate);

	@Override
	default int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
		return canConnectEnergy(from) ? (int) receiveRF(from, maxReceive, simulate) : 0;
	}

	@Override
	default int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
		return canConnectEnergy(from) ? (int) extractRF(from, maxExtract, simulate) : 0;
	}

	@Override
	@SuppressWarnings("rawtypes")
	default Map<Capability, Map<EnumFacing, Supplier<Object>>> initCapabilities() {
		Map<Capability, Map<EnumFacing, Supplier<Object>>> caps = new HashMap<>();
		if (Loader.isModLoaded(Modids.TESLA)) {
			Entry<Capability, Map<EnumFacing, Supplier<Object>>>[] c = createTeslaCapability(this);
			for (Entry<Capability, Map<EnumFacing, Supplier<Object>>> e : c)
				caps.put(e.getKey(), e.getValue());
		}
		Map<EnumFacing, Supplier<Object>> forge = new HashMap<>();
		for (EnumFacing f : EnumFacing.VALUES) {
			Object o = new RFStorage(this, f, false);
			forge.put(f, () -> o);
		}
		caps.put(CapabilityEnergy.ENERGY, forge);
		return caps;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Optional.Method(modid = Modids.TESLA)
	static Entry<Capability, Map<EnumFacing, Supplier<Object>>>[] createTeslaCapability(IRFHandler handler) {
		Map<EnumFacing, Supplier<Object>> forge = new HashMap<>();
		Map<EnumFacing, Supplier<Object>> forge2 = new HashMap<>();
		for (EnumFacing f : EnumFacing.VALUES) {
			Object o1 = new ITeslaConsumer() {

				@Override
				public long givePower(long power, boolean simulated) {
					return handler.receiveRF(f, power, simulated);
				}
			}, o2 = new ITeslaProducer() {

				@Override
				public long takePower(long power, boolean simulated) {
					return handler.extractRF(f, power, simulated);
				}
			};
			forge.put(f, () -> o1);
			forge2.put(f, () -> o2);
		}
		return new EmptyEntry[]{new EmptyEntry<>(TeslaCapabilities.CAPABILITY_CONSUMER, forge), new EmptyEntry<>(TeslaCapabilities.CAPABILITY_PRODUCER, forge2)};
	}
}
