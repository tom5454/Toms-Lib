package com.tom.lib.api.energy;

import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;

public interface IRFMachine {
	@SuppressWarnings("rawtypes")
	Map<Capability, Map<EnumFacing, Supplier<Object>>> initCapabilities();
}
