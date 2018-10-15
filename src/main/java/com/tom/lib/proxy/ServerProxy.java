package com.tom.lib.proxy;

import net.minecraft.entity.player.EntityPlayer;

public class ServerProxy extends CommonProxy {

	@Override
	public EntityPlayer getClientPlayer() {
		return null;
	}

	@Override
	public void init() {
	}

	@Override
	public void preinit() {
	}

}
