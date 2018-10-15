package com.tom.lib.proxy;

import net.minecraft.entity.player.EntityPlayer;

public abstract class CommonProxy {
	public abstract EntityPlayer getClientPlayer();
	public abstract void preinit();
	public abstract void init();
}
