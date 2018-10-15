package com.tom.lib.network.messages;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import com.tom.lib.network.MessageBase;
import com.tom.lib.network.packets.ControlPacket;
import com.tom.lib.network.patches.NetworkManagerPatchedServer;
import com.tom.lib.proxy.ClientProxy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

public class MessagePacket extends MessageBase<MessagePacket> {
	private Packet<?> packet;
	private long id;
	public MessagePacket(Packet<?> packetIn, long id) {
		packet = packetIn;
		this.id = id;
	}
	public MessagePacket() {
	}
	@SuppressWarnings("unchecked")
	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readLong();
		byte[] clazzName = new byte[buf.readShort()];
		buf.readBytes(clazzName);
		String clazz = new String(clazzName);
		try {
			Class<Packet<?>> packetClass = (Class<Packet<?>>) Class.forName(clazz);
			if(packetClass.isAssignableFrom(FMLProxyPacket.class)){
				String str = ByteBufUtils.readUTF8String(buf);
				int i = buf.readInt();
				ByteBuf b = UnpooledByteBufAllocator.DEFAULT.buffer(i);
				buf.readBytes(b);
				packet = new FMLProxyPacket(new PacketBuffer(b), str);
			}else{
				packet = packetClass.newInstance();
				packet.readPacketData(new PacketBuffer(buf));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(id);
		byte[] b = packet.getClass().getName().getBytes();
		buf.writeShort(b.length);
		buf.writeBytes(b);
		if(packet instanceof FMLProxyPacket){
			FMLProxyPacket p = (FMLProxyPacket) packet;
			String str = p.channel();
			ByteBufUtils.writeUTF8String(buf, str);
			ByteBuf pb = p.payload();
			buf.writeInt(pb.writerIndex());
			buf.writeBytes(pb);
		}else{
			try {
				packet.writePacketData(new PacketBuffer(buf));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleClientSide(MessagePacket message, EntityPlayer player) {
		if(message.packet instanceof ControlPacket){
			((ControlPacket)message.packet).processPacket();
		}else{
			try {
				//ClientProxy.addScheduledTask(() -> ((Packet<INetHandler>)message.packet).processPacket(ClientProxy.getHandler(message.id)));
				ClientProxy.addScheduledPacket((Packet<INetHandler>)message.packet, message.id);
			} catch(Exception e){
				//e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleServerSide(MessagePacket message, EntityPlayer player) {
		if(message.packet instanceof ControlPacket){
			((ControlPacket)message.packet).processPacket();
		}else{
			try {
				((Packet<INetHandler>)message.packet).processPacket(NetworkManagerPatchedServer.getHandler(message.id));
			} catch(Exception e){
				//e.printStackTrace();
			}
		}
	}

}
