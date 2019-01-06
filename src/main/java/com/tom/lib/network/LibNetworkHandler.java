package com.tom.lib.network;

import java.util.EnumMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import com.tom.lib.LibInit;
import com.tom.lib.network.messages.MessageGuiSync;
import com.tom.lib.network.messages.MessagePacket;
import com.tom.lib.network.messages.MessageScroll;
import com.tom.lib.network.messages.MessageTileBuf;
import com.tom.lib.utils.ReflectionUtils;
import com.tom.lib.utils.TomsUtils.PacketNoSpamChat;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class LibNetworkHandler {
	private static final String CHANNEL = "tomslib:ch1";
	private static SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
	private static EnumMap<Side, FMLEmbeddedChannel> channels;
	@SuppressWarnings("unchecked")
	public static void libInit(){
		register(PacketNoSpamChat.Handler.class, PacketNoSpamChat.class, Side.CLIENT, 999);
		register(MessagePacket.class, MessagePacket.class, Side.CLIENT, 998);
		register(MessagePacket.class, MessagePacket.class, Side.SERVER, 997);
		register(MessageTileBuf.class, MessageTileBuf.class, Side.CLIENT, 996);
		register(MessageGuiSync.class, MessageGuiSync.class, Side.CLIENT, 995);
		register(MessageGuiSync.class, MessageGuiSync.class, Side.SERVER, 994);
		register(MessageScroll.class, MessageScroll.class, Side.SERVER, 993);
		try {
			channels = ReflectionUtils.getValue(SimpleNetworkWrapper.class, EnumMap.class, INSTANCE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void sendToServer(IMessage message) {
		INSTANCE.sendToServer(message);
	}

	public static void sendTo(IMessage message, EntityPlayerMP player) {
		INSTANCE.sendTo(message, player);
	}

	public static void sendToAllAround(IMessage message, TargetPoint point) {
		INSTANCE.sendToAllAround(message, point);
	}

	/**
	 * Will send the given packet to every player within 64 blocks of the XYZ of
	 * the XYZ packet.
	 *
	 * @param message
	 * @param world
	 */
	@SuppressWarnings("rawtypes")
	public static void sendToAllAround(MessageXYZ message, World world) {
		sendToAllAround(message, new TargetPoint(world.provider.getDimension(), message.x, message.y, message.z, 64D));
	}

	public static void sendToAll(IMessage message) {
		INSTANCE.sendToAll(message);
	}

	public static void sendToDimension(IMessage message, int dimensionId) {
		INSTANCE.sendToDimension(message, dimensionId);
	}

	protected static <REQ extends IMessage, REPLY extends IMessage> void register(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side, int id) {
		LibInit.log.info("Registering Message {} with id {}", messageHandler.getName(), id);
		INSTANCE.registerMessage(messageHandler, requestMessageType, id, side);
	}
	public static ChannelFuture sendToServerWF(IMessage message) {
		channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		ChannelFuture f = channels.get(Side.CLIENT).writeAndFlush(message);
		f.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
		return f;
	}

	public static ChannelFuture sendToWF(IMessage message, EntityPlayerMP player) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
		ChannelFuture f = channels.get(Side.SERVER).writeAndFlush(message);
		f.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
		return f;
	}
}
