package com.tom.lib.network.packets;

import java.io.IOException;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;

import com.tom.lib.proxy.ClientProxy;
import com.tom.lib.utils.TomsUtils;

public class PacketSetPos extends ControlPacket {
	private Vec3d pos;
	private UUID ship;
	public PacketSetPos() {
	}
	public PacketSetPos(Vec3d pos, UUID ship) {
		this.pos = pos;
		this.ship = ship;
	}

	@Override
	public void readPacketData(PacketBuffer buf) throws IOException {
		pos = TomsUtils.readVec3d(buf);
		ship = buf.readUniqueId();
	}

	@Override
	public void writePacketData(PacketBuffer buf) throws IOException {
		TomsUtils.writeVec3d(buf, pos);
		buf.writeUniqueId(ship);
	}

	@Override
	public void processPacket() {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			ClientProxy.pos = pos;
			ClientProxy.shipID = ship;
		});
	}

}
