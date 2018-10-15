package com.tom.lib.network.messages;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import com.tom.lib.api.tileentity.ICustomPacket;
import com.tom.lib.network.MessageBase;
import com.tom.lib.utils.TomsUtils;

import io.netty.buffer.ByteBuf;

public class MessageTileBuf extends MessageBase<MessageTileBuf> {
	private ICustomPacket tile;
	private byte[] data;
	private BlockPos pos;
	public MessageTileBuf(ICustomPacket tile) {
		this.tile = tile;
		this.pos = tile.getPos2();
	}
	public MessageTileBuf() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = TomsUtils.readBlockPosFromPacket(buf);
		short size = buf.readShort();
		if(size > -1){
			data = new byte[size];
			buf.readBytes(data);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		TomsUtils.writeBlockPosToPacket(buf, pos);
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			tile.writeToPacket(new DataOutputStream(bos));
			buf.writeShort(bos.size());
			buf.writeBytes(bos.toByteArray());
		} catch (IOException e) {
			buf.writeShort(-1);
		}
	}

	@Override
	public void handleClientSide(MessageTileBuf message, EntityPlayer player) {
		if(message.data == null)return;
		Minecraft mc = Minecraft.getMinecraft();
		mc.addScheduledTask(() -> {
			TileEntity tile = mc.world.getTileEntity(message.pos);
			if(tile instanceof ICustomPacket)
				((ICustomPacket)tile).readPacket(message.data);
		});
	}

	@Override
	public void handleServerSide(MessageTileBuf message, EntityPlayer player) {
	}

}
