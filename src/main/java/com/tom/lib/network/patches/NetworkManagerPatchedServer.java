package com.tom.lib.network.patches;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.util.text.ITextComponent;

import com.tom.lib.network.LibNetworkHandler;
import com.tom.lib.network.messages.MessagePacket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class NetworkManagerPatchedServer extends NetworkManager {
	private static Map<Long, INetHandler> managers = new HashMap<>();
	private EntityPlayerMP player;
	private PatchedChannel patchedChannel;
	private long id;
	public NetworkManagerPatchedServer(EntityPlayerMP player, long id) {
		super(EnumPacketDirection.CLIENTBOUND);
		this.player = player;
		patchedChannel = new PatchedChannel(this, player.mcServer.getPlayerList());
		this.id = id;
	}
	@Override
	public void setNetHandler(INetHandler handler) {
		super.setNetHandler(handler);
		managers.put(id, handler);
	}
	@Override
	public void channelActive(ChannelHandlerContext p_channelActive_1_) throws Exception {
	}
	@Override
	public void channelInactive(ChannelHandlerContext p_channelInactive_1_) throws Exception {
	}
	@Override
	public void closeChannel(ITextComponent message) {
	}
	@Override
	public boolean isChannelOpen() {
		return true;
	}
	@Override
	public void setConnectionState(EnumConnectionState newState) {
	}
	@Override
	public void sendPacket(Packet<?> packetIn) {
		LibNetworkHandler.sendTo(new MessagePacket(packetIn, id), player);
	}
	@SuppressWarnings("unchecked")
	@Override
	public void sendPacket(Packet<?> packetIn, GenericFutureListener<? extends Future<? super Void>> listener, GenericFutureListener<? extends Future<? super Void>>... listeners) {
		sendPacket(packetIn, ArrayUtils.add(listeners, 0, listener));
	}
	private void sendPacket(Packet<?> packetIn, @Nullable final GenericFutureListener <? extends Future <? super Void >> [] futureListeners) {
		ChannelFuture channelfuture = LibNetworkHandler.sendToWF(new MessagePacket(packetIn, id), player);
		if (futureListeners != null)
		{
			channelfuture.addListeners(futureListeners);
		}
	}
	@Override
	public void processReceivedPackets() {
	}
	@Override
	public boolean isLocalChannel() {
		return true;
	}
	@Override
	public boolean hasNoChannel() {
		return false;
	}
	@Override
	public void disableAutoRead() {
	}
	@Override
	public void setCompressionThreshold(int threshold) {
	}
	@Override
	public void checkDisconnected() {
	}
	@Override
	public Channel channel() {
		return patchedChannel;
	}
	public void stop() {
		managers.remove(id);
	}
	public static INetHandler getHandler(long id){
		return managers.get(id);
	}
}