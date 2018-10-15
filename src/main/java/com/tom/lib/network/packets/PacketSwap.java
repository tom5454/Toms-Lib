package com.tom.lib.network.packets;

import java.io.IOException;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import com.tom.lib.proxy.ClientProxy;
import com.tom.lib.utils.TomsUtils;

public class PacketSwap extends ControlPacket {
	private long rid;
	private boolean dir;
	private Vec3d pos;
	private UUID ship;
	private BlockPos size;
	public PacketSwap(long rid, boolean dir, Vec3d pos, UUID uuid, BlockPos size) {
		this.rid = rid;
		this.dir = dir;
		this.pos = pos;
		ship = uuid;
		this.size = size;
	}
	public PacketSwap() {
	}

	@Override
	public void readPacketData(PacketBuffer buf) throws IOException {
		rid = buf.readLong();
		dir = buf.readBoolean();
		pos = TomsUtils.readVec3d(buf);
		ship = buf.readUniqueId();
		size = buf.readBlockPos();
	}

	@Override
	public void writePacketData(PacketBuffer buf) throws IOException {
		buf.writeLong(rid);
		buf.writeBoolean(dir);
		TomsUtils.writeVec3d(buf, pos);
		buf.writeUniqueId(ship);
		buf.writeBlockPos(size);
	}

	@Override
	public void processPacket() {
		System.out.println("Queued: SwapPacketProcess");
		Minecraft.getMinecraft().addScheduledTask(() -> {//Initiate quick load hack
			System.out.println("SwapPacketProcess");
			ClientProxy.rid = rid;
			ClientProxy.dir = dir;
			ClientProxy.pos = pos;
			ClientProxy.shipID = ship;
			ClientProxy.size = size;
			ClientProxy.patchNextWorldLoad = true;
		});
	}

}
