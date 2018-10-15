package com.tom.lib.thirdparty.oc;

import com.tom.lib.LibInit;

import li.cil.oc.api.Driver;

public class OpenComputers {
	public static DriverTomsLib DRIVER;
	public static void init(){
		LibInit.log.info("Init Open Computers Handler");
		Driver.add(DRIVER = new DriverTomsLib());
		//ITMPeripheral.Handler.addCapabilityInit(Capabilities.EnvironmentCapability, (p, s) -> DRIVER.createEnvironment(p.getWorld2(), p.getPos2(), s));
	}
}
