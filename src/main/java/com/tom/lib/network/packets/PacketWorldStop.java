package com.tom.lib.network.packets;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import com.tom.lib.proxy.ClientProxy;

public class PacketWorldStop extends ControlPacket {
	private long id;
	public PacketWorldStop(long id) {
		this.id = id;
	}
	public PacketWorldStop() {
	}
	@Override
	public void readPacketData(PacketBuffer buf) throws IOException {
		id = buf.readLong();
	}

	@Override
	public void writePacketData(PacketBuffer buf) throws IOException {
		buf.writeLong(id);
	}

	@Override
	public void processPacket() {
		ClientProxy.stopWorldRenderer(id);
	}

}
