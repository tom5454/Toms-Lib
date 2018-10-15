package com.tom.lib.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import com.tom.lib.utils.TomsUtils;

public class PlayerHandler {
	private static final String CATRGORY = "main", PLAYER_LIST = "playerList";
	private static Map<String, PlayerHandler> handlerMap = new HashMap<>();
	private static Map<String, Function<String, IPlayerHandler>> subHandlers = new HashMap<>();
	private static File mainFolder;
	private String name;
	private static List<PlayerHandler> online;
	private static File mainCfg;
	public static Logger log = LogManager.getLogger("Tom's Lib Player Handler");
	private static boolean allowSave = false;
	private Map<String, IPlayerHandler> handlers = new HashMap<>();
	public static void init(){
		MinecraftForge.EVENT_BUS.register(new EventHandler());
	}
	public PlayerHandler(String name) {
		this.name = name;
		handlers = subHandlers.entrySet().stream().map(TomsUtils.valueMapper(e -> e.apply(name))).collect(TomsUtils.toMapCollector());
	}
	public static void onServerStart(File file) {
		log.info("Loading Player Handler...");
		mainFolder = file;
		online = new ArrayList<>();
		mainCfg = new File(mainFolder, "main.tmcfg");
		Configuration mainCfg = new Configuration(PlayerHandler.mainCfg);
		String[] playerList = mainCfg.get(CATRGORY, PLAYER_LIST, new String[]{}).getStringList();
		Arrays.sort(playerList);
		for (String s : playerList) {
			loadPlayer(s);
		}
		mainCfg.save();
		allowSave = true;
	}

	public static void loadPlayer(String name) {
		log.info("Loading player: " + name);
		if (handlerMap.containsKey(name))
			handlerMap.remove(name);
		handlerMap.put(name, new PlayerHandler(name));
		File cfg = new File(mainFolder, "player_" + name + ".dat");
		try {
			NBTTagCompound tag = CompressedStreamTools.read(cfg);
			getPlayerHandlerForName(name).readFromFile(tag, name);
		} catch (Exception e) {
		}
	}
	private void readFromFile(NBTTagCompound tag, String name) {
		this.name = name;
		handlers.values().forEach(h -> h.load(tag));
	}

	private static void save(boolean log) {
		if (log)
			PlayerHandler.log.info("Saving Player Handler...");
		List<String> nameList = new ArrayList<>();
		for (Entry<String, PlayerHandler> e : handlerMap.entrySet()) {
			File cfg = new File(mainFolder, "player_" + e.getKey() + ".dat");
			NBTTagCompound tag = new NBTTagCompound();
			try {
				getPlayerHandlerForName(e.getKey()).writeToFile(tag);
				CompressedStreamTools.write(tag, cfg);
			} catch (Exception ex) {
				PlayerHandler.log.catching(ex);
			}
			nameList.add(e.getKey());
		}
		String[] out = nameList.toArray(new String[]{});
		Configuration mainCfg = new Configuration(PlayerHandler.mainCfg);
		mainCfg.get(CATRGORY, PLAYER_LIST, new String[]{}).set(out);
		mainCfg.save();
	}

	private void writeToFile(NBTTagCompound tag) {
		handlers.values().forEach(h -> h.save(tag));
	}

	public static PlayerHandler getPlayerHandlerForName(String name) {
		return handlerMap.get(name);
	}

	public static PlayerHandler addOrGetPlayer(EntityPlayer player) {
		String name = player.getName();
		if (!handlerMap.containsKey(name)) {
			log.info("Adding new Player to handler...");
			log.info("Name: " + name);
			PlayerHandler h = new PlayerHandler(name);
			handlerMap.put(name, h);
		}
		return handlerMap.get(name);
	}
	public static void cleanup() {
		log.info("Cleaning up Player Handler");
		save(true);
		handlerMap.clear();
		allowSave = false;
	}

	public static void save() {
		if (allowSave)
			save(false);
		else
			log.info("Player Handler already saved, ignore event.");
	}
	public static void update(Phase phase) {
		switch (phase) {
		case END:
			online.forEach(p -> p.update1());
			break;
		case START:
			online.forEach(p -> p.update0());
			break;
		default:
			break;
		}
	}

	private void update0() {
		FMLCommonHandler.instance().getMinecraftServerInstance().profiler.startSection("[Tom's Mod] Update Player Handler");
		EntityPlayerMP player = getPlayer();
		handlers.values().forEach(h -> h.updatePre(player));
		FMLCommonHandler.instance().getMinecraftServerInstance().profiler.endSection();
	}

	private void update1() {
		FMLCommonHandler.instance().getMinecraftServerInstance().profiler.startSection("[Tom's Mod] Post Update Player Handler");
		EntityPlayerMP player = getPlayer();
		handlers.values().forEach(h -> h.updatePost(player));
		FMLCommonHandler.instance().getMinecraftServerInstance().profiler.endSection();
	}

	public EntityPlayerMP getPlayer() {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(name);
	}

	public static void playerLogIn(EntityPlayer player) {
		online.add(addOrGetPlayer(player));
		log.info("'" + player.getName() + "' logged in");
	}

	public static void playerLogOut(EntityPlayer player) {
		online.remove(getPlayerHandler(player));
		log.info("'" + player.getName() + "' logged out");
	}

	public static PlayerHandler getPlayerHandler(EntityPlayer player) {
		return getPlayerHandlerForName(player.getName());
	}
	public IPlayerHandler getHandler(String id){
		return handlers.get(id);
	}
	public static Collection<PlayerHandler> handlers() {
		return handlerMap.values();
	}
	public static void registerHandler(String id, Function<String, IPlayerHandler> factory){
		subHandlers.put(id, factory);
	}
	private static class EventHandler {
		@SubscribeEvent
		public void tickServer(ServerTickEvent event) {
			if (event.phase == Phase.START && event.type == net.minecraftforge.fml.common.gameevent.TickEvent.Type.SERVER) {
				update(Phase.START);
			} else if (event.phase == Phase.END && event.type == net.minecraftforge.fml.common.gameevent.TickEvent.Type.SERVER) {
				update(Phase.END);
			}
		}
		@SubscribeEvent
		public void onPlayerLoggedOut(PlayerLoggedOutEvent event) {
			if (!event.player.world.isRemote) {
				playerLogOut(event.player);
			}
		}
		@SubscribeEvent
		public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
			if (!event.player.world.isRemote) {
				playerLogIn(event.player);
			}
		}
		@SubscribeEvent
		public void onWorldSave(WorldEvent.Save event) {
			if (event.getWorld().provider.getDimension() == 0)save();
		}
	}
}
