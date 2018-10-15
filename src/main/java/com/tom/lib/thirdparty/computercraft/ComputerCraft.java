package com.tom.lib.thirdparty.computercraft;

import com.tom.lib.LibInit;

import dan200.computercraft.api.ComputerCraftAPI;

public class ComputerCraft {
	public static void init(){
		LibInit.log.info("Init ComputerCraft Handler");
		ComputerCraftAPI.registerPeripheralProvider(TomsLibProvider.INSTANCE);
	}
}
