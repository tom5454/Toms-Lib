package com.tom.lib.network.packets;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;

import com.tom.lib.proxy.ClientProxy;

public class PacketWorldStart extends ControlPacket {
	private int dim;
	private long dimRid;
	private WorldSettings set;
	private BlockPos size;
	public PacketWorldStart() {
	}

	public PacketWorldStart(int dim, WorldSettings set, long dimRid, BlockPos size) {
		this.dim = dim;
		this.set = set;
		this.dimRid = dimRid;
		this.size = size;
	}

	@Override
	public void readPacketData(PacketBuffer buf) throws IOException {
		dim = buf.readInt();
		dimRid = buf.readLong();
		long seed = buf.readLong();
		byte gameType = buf.readByte();
		byte bools = buf.readByte();
		int world = buf.readInt();
		set = new WorldSettings(seed, GameType.values()[gameType], (bools & 2) != 0, (bools & 4) != 0, WorldType.WORLD_TYPES[world]);
		set.setGeneratorOptions(buf.readString(Short.MAX_VALUE));
		if((bools & 1) != 0)set.enableBonusChest();
		size = buf.readBlockPos();
	}

	@Override
	public void writePacketData(PacketBuffer buf) throws IOException {
		buf.writeInt(dim);
		buf.writeLong(dimRid);
		buf.writeLong(set.getSeed());
		buf.writeByte(set.getGameType().ordinal());
		int bools = (set.isBonusChestEnabled() ? 1 : 0) | (set.isMapFeaturesEnabled() ? 2 : 0) | (set.getHardcoreEnabled() ? 4 : 0);
		buf.writeByte(bools);
		buf.writeInt(set.getTerrainType().getId());
		buf.writeString(set.getGeneratorOptions());
		buf.writeBlockPos(size);
	}

	@Override
	public void processPacket() {
		ClientProxy.startWorldRenderer(dimRid, dim, set, size);
	}

}
