package com.tom.lib.network.packets;

import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

public abstract class ControlPacket implements Packet<INetHandler>{
	@Override
	public final void processPacket(INetHandler handler) {
		processPacket();
	}
	public abstract void processPacket();
}
