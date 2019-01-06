package com.tom.lib.network.messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.server.MinecraftServer;

import net.minecraftforge.fml.common.FMLCommonHandler;

import com.tom.lib.network.GuiSyncHandler;
import com.tom.lib.network.GuiSyncHandler.ISyncContainer;
import com.tom.lib.network.LibNetworkHandler;
import com.tom.lib.network.MessageBase;

import io.netty.buffer.ByteBuf;

public class MessageGuiSync extends MessageBase<MessageGuiSync> {
	private EntityPlayerMP player;
	private Map<Integer, Object> data = new HashMap<>();
	private Map<Integer, DataSerializer<?>> serializers = new HashMap<>();
	private byte id;
	public MessageGuiSync(IContainerListener player) {
		this.player = (EntityPlayerMP) player;
	}

	public MessageGuiSync() {
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void fromBytes(ByteBuf buf) {
		id = buf.readByte();
		PacketBuffer pb = new PacketBuffer(buf);
		byte j = buf.readByte();
		for(int i = 0;i<j;i++){
			int id = buf.readByte();
			int serID = buf.readInt();
			DataSerializer ser = DataSerializers.getSerializer(serID);
			try {
				data.put(id, ser.read(pb));
			} catch (IOException e) {
				return;
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(id);
		PacketBuffer pb = new PacketBuffer(buf);
		buf.writeByte(data.size());
		for (Entry<Integer, Object> e : data.entrySet()) {
			buf.writeByte(e.getKey());
			DataSerializer ser = serializers.get(e.getKey());
			buf.writeInt(DataSerializers.getSerializerId(ser));
			ser.write(pb, e.getValue());
		}
	}

	@Override
	public void handleClientSide(MessageGuiSync message, EntityPlayer player) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.addScheduledTask(() -> {
			if(mc.currentScreen instanceof ISyncContainer){
				ISyncContainer c = (ISyncContainer) mc.currentScreen;
				GuiSyncHandler h = c.getSyncHandler();
				h.update(player, message.data, message.id);
			}else if(mc.player.openContainer instanceof ISyncContainer){
				ISyncContainer c = (ISyncContainer) mc.player.openContainer;
				GuiSyncHandler h = c.getSyncHandler();
				h.update(player, message.data, message.id);
			}
		});
	}

	@Override
	public void handleServerSide(MessageGuiSync message, EntityPlayer player) {
		MinecraftServer mc = FMLCommonHandler.instance().getMinecraftServerInstance();
		mc.addScheduledTask(() -> {
			if(player.openContainer instanceof ISyncContainer){
				ISyncContainer c = (ISyncContainer) player.openContainer;
				GuiSyncHandler h = c.getSyncHandler();
				h.receiveFromClient(player, message.data);
			}
		});
	}
	public void send(){
		if(player != null)LibNetworkHandler.sendTo(this, player);
		else LibNetworkHandler.sendToServer(this);
	}
	public void add(int id, DataSerializer<?> serializer, Object ins){
		data.put(id, ins);
		serializers.put(id, serializer);
	}
	public void setID(int id) {
		this.id = (byte) id;
	}
}
