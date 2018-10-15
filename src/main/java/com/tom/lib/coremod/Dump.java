package com.tom.lib.coremod;

import net.minecraft.client.multiplayer.WorldClient;

import com.tom.lib.proxy.ClientProxy;

public class Dump {
	private WorldClient world;
	public void dump1(){
		world = ClientProxy.patchedLoadWorld(world);
	}
}
