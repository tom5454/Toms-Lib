package com.tom.lib.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.PotentialSpawns;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

import com.google.common.collect.Queues;

import com.tom.lib.LibConfig;
import com.tom.lib.api.tileentity.ICustomPacket;
import com.tom.lib.network.LibNetworkHandler;
import com.tom.lib.network.messages.MessageTileBuf;
import com.tom.lib.utils.Ticker;
import com.tom.lib.utils.TomsUtils;

public class WorldHandler {
	private static Map<Integer, WorldHandler> handlerMap = new HashMap<>();
	private static final Queue<Runnable> scheduledTasks = Queues.<Runnable>newArrayDeque();
	private static final Map<Integer, Stack<Runnable>> tasks = new HashMap<>();
	private static Map<String, Function<World, IWorldHandler>> subHandlers = new HashMap<>();
	private Map<String, IWorldHandler> handlers = new HashMap<>();
	private static Logger log = LogManager.getLogger("Tom's Lib World Handler");
	private static File mainFolder;
	private static boolean serverStarted = false;
	private final int dimID;
	public World worldObj;
	private List<Chunk> loadedChunks = new ArrayList<>();
	public Set<Ticker> tickers = new HashSet<>();
	public Set<ChunkPos> dirty = new HashSet<>();

	public static void init(){
		MinecraftForge.EVENT_BUS.register(new EventHandler());
	}

	public WorldHandler(World world) {
		this.dimID = world.provider.getDimension();
		this.worldObj = world;
		handlers = subHandlers.entrySet().stream().map(TomsUtils.valueMapper(e -> e.apply(world))).collect(TomsUtils.toMapCollector());
	}

	public static WorldHandler getWorldHandlerForDim(int dim) {
		return handlerMap.get(dim);
	}
	public IWorldHandler getHandler(String id){
		return handlers.get(id);
	}
	public static WorldHandler getOrLoadWorldHandlerForDim(World world) {
		WorldHandler h = getWorldHandlerForDim(world.provider.getDimension());
		if (h == null)
			loadWorld(world);
		return h == null ? getWorldHandlerForDim(world.provider.getDimension()) : h;
	}

	public static void loadWorld(final World world) {
		if (world.isRemote)
			return;
		final int dim = world.provider.getDimension();
		addTask(new Runnable() {

			@Override
			public void run() {
				if (handlerMap.containsKey(dim))
					return;
				log.info("Loading world: " + dim);
				handlerMap.put(dim, new WorldHandler(world));
				if (!tasks.containsKey(dim)) {
					tasks.put(dim, new Stack<>());
				}
				try {
					getWorldHandlerForDim(dim).load(CompressedStreamTools.read(new File(mainFolder, "dim_" + dim + ".dat")));
				} catch (Exception e) {
				}
			}
		}, "Load World " + dim);
	}

	private void load(NBTTagCompound read) {
		handlers.values().forEach(h -> h.load(read));
	}

	public static void saveWorld(int dim) {
		// log.info("Saving world: "+dim);
		try {
			NBTTagCompound tag = new NBTTagCompound();
			getWorldHandlerForDim(dim).save(tag);
			CompressedStreamTools.write(tag, new File(mainFolder, "dim_" + dim + ".dat"));
		} catch (Exception e) {
		}
	}

	private void save(NBTTagCompound tag) {
		handlers.values().forEach(h -> h.save(tag));
	}

	public static void unloadWorld(int dim) {
		log.info("Unloading world: " + dim);
		saveWorld(dim);
		handlerMap.remove(dim);
		tasks.remove(dim);
	}

	public static boolean onEntitySpawning(PotentialSpawns spawns) {
		return handlerMap.containsKey(spawns.getWorld().provider.getDimension()) ? handlerMap.get(spawns.getWorld().provider.getDimension()).onEntitySpawning(spawns.getPos(), spawns.getType(), spawns.getList()) : false;
	}

	private boolean onEntitySpawning(BlockPos pos, EnumCreatureType type, List<SpawnListEntry> list) {
		for (IWorldHandler e : handlers.values()) {
			if(e.onEntitySpawning(pos, type, list))return true;
		}
		return false;
	}
	public static void onServerStart(File file) {
		log.info("Loading World Handler...");
		handlerMap.clear();
		mainFolder = file;
		while (!scheduledTasks.isEmpty()) {
			scheduledTasks.poll().run();
		}
		serverStarted = true;
		log.info("World Handler Loaded.");
	}

	public static void unloadChunkS(Chunk chunk) {
		try {
			getWorldHandlerForDim(chunk.getWorld().provider.getDimension()).unloadChunk(chunk);
		} catch (Exception e) {
		}
	}

	private void unloadChunk(Chunk chunk) {
		loadedChunks.remove(chunk);
		handlers.values().forEach(h -> h.unloadChunk(chunk));
	}

	public static void loadChunkS(Chunk chunk) {
		try {
			getWorldHandlerForDim(chunk.getWorld().provider.getDimension()).loadChunk(chunk);
		} catch (Exception e) {
		}
	}

	private void loadChunk(Chunk chunk) {
		loadedChunks.add(chunk);
		handlers.values().forEach(h -> h.loadChunk(chunk));
	}
	public static void onTick(World world, Phase phase) {
		int dim = world.provider.getDimension();
		if (handlerMap.containsKey(dim)) {
			handlerMap.get(dim).tick(world, phase);
		}
	}

	private void tick(World world, Phase phase) {
		this.worldObj = world;
		switch (phase) {
		case START:
			world.profiler.startSection("[Tom's Lib] Process Tasks");
			Stack<Runnable> stack2 = new Stack<>();
			Stack<Runnable> stack = tasks.put(dimID, stack2);
			int i = 0;
			while (!stack.isEmpty() && i < LibConfig.maxTaskCount) {
				try {
					stack.pop().run();
				} catch (Throwable e) {
					log.error("Encountered an unexpected exception while processing tasks", e);
				}
				i++;
			}
			if (i == LibConfig.maxTaskCount)
				log.warn("Queued Task handler overloaded! Processed " + LibConfig.maxTaskCount + " tasks. Skipping " + stack.size() + " tasks!");
			tasks.put(dimID, stack);
			stack.addAll(stack2);
			world.profiler.endStartSection("[Tom's Lib] Tick Tickers");
			Iterator<Ticker> each = tickers.iterator();
			Set<Ticker> oldTickers = tickers;
			tickers = new HashSet<>();
			while (each.hasNext()) {
				Ticker t = each.next();
				if (t.isTickerValid())
					t.updateTicker();
				else
					each.remove();
			}
			oldTickers.addAll(tickers);
			tickers = oldTickers;
			world.profiler.endStartSection("[Tom's Lib] Updating sub handlers");
			handlers.values().forEach(h -> h.updatePre(world));
			world.profiler.endSection();
			break;
		case END:
			handlers.values().forEach(h -> h.updatePost(world));
			if(!dirty.isEmpty()){
				dirty.forEach(c -> {
					List<EntityPlayerMP> l = EventHandler.watch.get(c);
					if(l != null && !l.isEmpty())l.forEach(p -> sendTo(c, p));
				});
				dirty.clear();
			}
			break;
		default:
			break;
		}
	}
	private void sendTo(ChunkPos c, EntityPlayerMP p) {
		Chunk chunk = worldObj.getChunkFromChunkCoords(c.x, c.z);
		chunk.getTileEntityMap().values().stream().filter(t -> t instanceof ICustomPacket).map(t -> new MessageTileBuf(((ICustomPacket)t))).forEach(m -> LibNetworkHandler.sendTo(m, p));
	}
	public static void addTask(Runnable r, String type) {
		if (serverStarted) {
			r.run();
		} else {
			if(type != null)log.info("Task " + type + " has queued up.");
			scheduledTasks.add(r);
		}
	}

	public static void stopServer() {
		serverStarted = false;
		List<Integer> set = new ArrayList<>(handlerMap.keySet());
		for (Integer e : set) {
			unloadWorld(e);
		}
	}
	public static void queueTask(int dim, Runnable task) {
		if (!tasks.containsKey(dim)) {
			tasks.put(dim, new Stack<>());
		}
		tasks.get(dim).add(task);
	}

	public static void addTicker(int dim, Ticker ticker) {
		getWorldHandlerForDim(dim).tickers.add(ticker);
	}

	public static void removeTicker(int dim, Ticker ticker) {
		getWorldHandlerForDim(dim).tickers.remove(ticker);
	}
	public static void registerHandler(String id, Function<World, IWorldHandler> factory){
		subHandlers.put(id, factory);
	}
	public void markDirty(ChunkPos chunk) {
		dirty.add(chunk);
	}

	public static void markDirty(World world, ChunkPos chunk) {
		getWorldHandlerForDim(world.provider.getDimension()).markDirty(chunk);
	}
	public static void markDirty(World world, BlockPos pos) {
		markDirty(world, new ChunkPos(pos));
	}
	private static class EventHandler {
		private static Map<ChunkPos, List<EntityPlayerMP>> watch = new HashMap<>();
		@SubscribeEvent
		public void chunkLoad(ChunkEvent.Load event) {
			loadChunkS(event.getChunk());
		}
		@SubscribeEvent
		public void chunkUnload(ChunkEvent.Unload event) {
			unloadChunkS(event.getChunk());
		}
		@SubscribeEvent
		public void dimLoad(WorldEvent.Load event) {
			loadWorld(event.getWorld());
		}
		@SubscribeEvent
		public void dimUnload(WorldEvent.Unload event) {
			if (event.getWorld().isRemote)
				return;
			unloadWorld(event.getWorld().provider.getDimension());
		}
		@SubscribeEvent
		public void onSpawn(WorldEvent.PotentialSpawns event) {
			try {
				boolean cancel = onEntitySpawning(event);
				if (cancel)
					event.setCanceled(true);
			} catch (Exception e) {
			}
		}
		@SubscribeEvent
		public void onWorldSave(WorldEvent.Save event) {
			saveWorld(event.getWorld().provider.getDimension());
		}
		@SubscribeEvent
		public void tick(WorldTickEvent event) {
			onTick(event.world, event.phase);
		}
		@SubscribeEvent
		public void watch(ChunkWatchEvent.Watch evt){
			TomsUtils.getOrPut(watch, evt.getChunkInstance().getPos(), ArrayList::new).add(evt.getPlayer());
			markDirty(evt.getPlayer().world, evt.getChunkInstance().getPos());
		}
		@SubscribeEvent
		public void unwatch(ChunkWatchEvent.UnWatch evt){
			List<EntityPlayerMP> l = watch.get(evt.getChunkInstance().getPos());
			if(l != null){
				l.remove(evt.getPlayer());
				if(l.isEmpty()){
					watch.remove(evt.getChunkInstance().getPos());
				}
			}
		}
	}
}
