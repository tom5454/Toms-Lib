package com.tom.lib.proxy;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang3.Validate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldSettings;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;

import com.tom.lib.LibInit;
import com.tom.lib.api.module.ModuleManager;
import com.tom.lib.client.dim.DimWorldRenderer;
import com.tom.lib.dim.DimProvider;
import com.tom.lib.dim.DimensionHandler;
import com.tom.lib.entity.world.EntityWorld;
import com.tom.lib.entity.world.EntityWorldShip;
import com.tom.lib.entity.world.WorldEntityRenderer;
import com.tom.lib.utils.ReflectionUtils;

public class ClientProxy extends CommonProxy {
	//private static final int MAX_PACKETS = 100;
	public static Map<Long, DimWorldRenderer> dimRenderers = Collections.synchronizedMap(new HashMap<>());
	private static final Queue < FutureTask<? >> scheduledTasks = Queues. < FutureTask<? >> newArrayDeque();
	public static long rid;
	public static boolean dir;
	public static boolean patchNextWorldLoad;
	private boolean rendering;
	private static Minecraft mc;
	private static boolean ignoreUnloadEvent;
	public static Vec3d pos;
	public static UUID shipID;
	public static BlockPos size;
	@Override
	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().player;
	}

	@Override
	public void init() {
		new NetHandlerPlayClient(null, null, null, null);
		MinecraftForge.EVENT_BUS.register(this);
		mc = Minecraft.getMinecraft();
	}
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void worldLoad(WorldEvent.Load event){
		if(ModuleManager.isModuleLoaded(ModuleManager.ENTITYWORLD)){
			if(event.getWorld() instanceof WorldClient && event.getWorld().provider instanceof DimProvider){
				DimensionHandler.patchChunkProvider((WorldClient) event.getWorld());
			}
		}
	}
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void worldUnLoad(WorldEvent.Unload event){
		if(ModuleManager.isModuleLoaded(ModuleManager.ENTITYWORLD)){
			if(event.getWorld() instanceof WorldClient && !ignoreUnloadEvent){
				System.out.println("Unload");
				DimWorldRenderer dimRenderer = dimRenderers.get(0L);
				if(event.getWorld().provider instanceof DimProvider){
					if(dimRenderer != null)dimRenderer.cleanup();
					dimRenderer = null;
					dimRenderers.remove(0L);
				}
			}
		}
	}
	@SubscribeEvent
	public void tick(ClientTickEvent ev){
		if(!mc.isGamePaused()){
			if(ev.phase == Phase.START){
				if(ModuleManager.isModuleLoaded(ModuleManager.ENTITYWORLD)){
					mc.mcProfiler.startSection("remote_scheduledTasks");
					long timeS = System.currentTimeMillis();
					synchronized (scheduledTasks)
					{
						int pr = 0;
						while (!scheduledTasks.isEmpty())
						{
							long time = System.currentTimeMillis();
							if(time - timeS > 20){
								LibInit.log.info("Queued Task handler overloaded! Processed " + pr + " tasks. Skipping " + scheduledTasks.size() + " tasks!");
								break;
							}
							Util.runTask(scheduledTasks.poll(), LibInit.log);
							pr++;
						}
					}
					mc.mcProfiler.endSection();
				}
			}else if(ev.phase == Phase.END){
				if(ModuleManager.isModuleLoaded(ModuleManager.ENTITYWORLD)){
					mc.mcProfiler.startSection("dimWorldTick");
					EntityPlayerSP backup = mc.player;
					if(backup != null){
						for(Entry<Long, DimWorldRenderer> e : dimRenderers.entrySet()){
							long id = e.getKey();
							DimWorldRenderer dimRenderer = e.getValue();
							if(dimRenderer != null){
								mc.mcProfiler.startSection("dim:" + e.getKey());
								if(id == 0){
									List<EntityWorld> el = dimRenderer.getWorld().getEntities(EntityWorld.class, a -> a.getUniqueID().equals(shipID));
									if(!el.isEmpty()){
										EntityWorld p = el.get(0);
										BlockPos size = p.getSize();
										dimRenderer.ox = p.posX;
										dimRenderer.oy = p.posY - 7;
										dimRenderer.oz = p.posZ;
										dimRenderer.ry = p.rotationYaw;
										dimRenderer.rp = p.rotationPitch;
									}else if(pos != null){
										dimRenderer.ox = pos.x;
										dimRenderer.oy = pos.y;
										dimRenderer.oz = pos.z;
									}
								}
								mc.player = dimRenderer.entity;
								dimRenderer.entity.world.updateEntities();
								dimRenderer.entity.world.tick();
								mc.mcProfiler.endSection();
							}
						}
						mc.player = backup;
					}else if(!dimRenderers.isEmpty()){
						DimensionHandler.log.info("Cleaning up dim renderers");
						cleanAll();
					}
					mc.mcProfiler.endSection();
				}
			}
		}
	}
	private static void cleanAll() {
		dimRenderers.values().forEach(DimWorldRenderer::cleanup);
		dimRenderers.clear();
	}

	@SubscribeEvent
	public void openGui(GuiOpenEvent e){
		if(ModuleManager.isModuleLoaded(ModuleManager.ENTITYWORLD)){
			if(e.getGui() instanceof GuiDownloadTerrain){
				e.setCanceled(true);
			}
		}
	}
	@SubscribeEvent
	public void drawWorld(RenderWorldLastEvent ev){
		if(ModuleManager.isModuleLoaded(ModuleManager.ENTITYWORLD)){
			try {
				DimWorldRenderer dimRenderer = dimRenderers.get(0L);
				if(dimRenderer != null && !rendering){
					rendering = true;
					mc.mcProfiler.startSection("renderWorld");
					dimRenderer.render(2, false);
					mc.mcProfiler.endSection();
					rendering = false;
				}
			} catch(Exception e){

			}
		}
	}

	@Override
	public void preinit() {
		if(ModuleManager.isModuleLoaded(ModuleManager.ENTITYWORLD))
			RenderingRegistry.registerEntityRenderingHandler(EntityWorldShip.class, m -> new WorldEntityRenderer(m));
	}
	public static void startWorldRenderer(long rid, int dim, WorldSettings s, BlockPos size) {
		mc.addScheduledTask(() -> {
			System.out.println("ClientProxy.startWorldRenderer(" + rid + ", " + dim + ")");
			DimWorldRenderer dimRenderer = dimRenderers.remove(rid);
			if(dimRenderer != null)dimRenderer.cleanup();
			dimRenderers.put(rid, new DimWorldRenderer(s, dim, rid, size));
			System.out.println(dimRenderers);
		});
	}
	public static void stopWorldRenderer(long rid) {
		mc.addScheduledTask(() -> {
			System.out.println("ClientProxy.stopWorldRenderer(" + rid + ")");
			if(rid != -1){
				DimWorldRenderer dimRenderer = dimRenderers.remove(rid);
				if(dimRenderer != null)dimRenderer.cleanup();
			}else{
				cleanAll();
			}
		});
	}

	public static INetHandler getHandler(long id) {
		synchronized (dimRenderers) {
			return dimRenderers.get(id).network;
		}
	}
	public static WorldClient patchedLoadWorld(WorldClient world){
		if(ModuleManager.isModuleLoaded(ModuleManager.ENTITYWORLD)){
			System.out.println("Intercepted load world");
			if(!doSwap())mc.loadWorld(world);
			return mc.world;
		}else{
			mc.loadWorld(world);
			return world;
		}
	}
	private static boolean doSwap(){
		if(patchNextWorldLoad){
			System.out.println("Patched load world");
			System.out.println(dimRenderers);
			ignoreUnloadEvent = true;
			if (mc.world != null) net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.WorldEvent.Unload(mc.world));
			ignoreUnloadEvent = false;
			patchNextWorldLoad = false;
			if(dir){
				DimWorldRenderer r = dimRenderers.remove(rid);
				DimWorldRenderer bg = dimRenderers.remove(0L);
				if(bg != null)bg.cleanup();
				bg = new DimWorldRenderer(0, mc.world, mc.renderGlobal, size);
				dimRenderers.put(0L, bg);
				bg.ox = pos.x;
				bg.oy = pos.y;
				bg.oz = pos.z;
				pos = null;
				if(r != null){
					try {
						if(ReflectionUtils.getValue(RenderGlobal.class, WorldClient.class, r.getRenderer()) != null){
							mc.world = r.getWorld();
							mc.renderGlobal = r.getRenderer();
							mc.entityRenderer.updateRenderer();
							updateMCWorld(mc.world);
							System.out.println("Patched load world successful");
							return true;
						}else{
							System.out.println("Null World");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return false;
				}else{
					System.out.println(rid + " renderer == null");
					System.out.println(dimRenderers);
				}
				return false;
			}else{
				DimWorldRenderer bg = dimRenderers.remove(0L);
				if(bg == null)return false;
				DimWorldRenderer n = dimRenderers.remove(rid);
				if(n != null)n.cleanup();
				n = new DimWorldRenderer(rid, mc.world, mc.renderGlobal, size);
				dimRenderers.put(rid, n);
				pos = null;
				mc.world = bg.getWorld();
				mc.renderGlobal = bg.getRenderer();
				mc.entityRenderer.updateRenderer();
				updateMCWorld(mc.world);
				System.out.println("Patched load world successful");
				return true;
			}
		}
		return false;
	}
	private static void updateMCWorld(WorldClient world){
		if (mc.effectRenderer != null)
		{
			mc.effectRenderer.clearEffects(world);
		}

		TileEntityRendererDispatcher.instance.setWorld(world);

		if (world != null)
		{
			if (!mc.isIntegratedServerRunning())
			{
				AuthenticationService authenticationservice = new YggdrasilAuthenticationService(mc.getProxy(), UUID.randomUUID().toString());
				MinecraftSessionService minecraftsessionservice = authenticationservice.createMinecraftSessionService();
				GameProfileRepository gameprofilerepository = authenticationservice.createProfileRepository();
				PlayerProfileCache playerprofilecache = new PlayerProfileCache(gameprofilerepository, new File(mc.mcDataDir, MinecraftServer.USER_CACHE_FILE.getName()));
				TileEntitySkull.setProfileCache(playerprofilecache);
				TileEntitySkull.setSessionService(minecraftsessionservice);
				PlayerProfileCache.setOnlineMode(false);
			}

			if (mc.player == null)
			{
				mc.player = mc.playerController.createPlayer(world, new StatisticsManager(), new RecipeBookClient());
				mc.playerController.flipPlayer(mc.player);
			}

			mc.player.preparePlayerToSpawn();
			world.spawnEntity(mc.player);
			mc.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
			mc.playerController.setPlayerCapabilities(mc.player);
			mc.setRenderViewEntity(mc.player);
		}
	}
	public static <V> ListenableFuture<V> addScheduledTask(Callable<V> callableToSchedule)
	{
		Validate.notNull(callableToSchedule);

		if (mc.isCallingFromMinecraftThread())
		{
			try
			{
				return Futures.<V>immediateFuture(callableToSchedule.call());
			}
			catch (Exception exception)
			{
				return Futures.immediateFailedCheckedFuture(exception);
			}
		}
		else
		{
			ListenableFutureTask<V> listenablefuturetask = ListenableFutureTask.<V>create(callableToSchedule);

			synchronized (scheduledTasks)
			{
				scheduledTasks.add(listenablefuturetask);
				return listenablefuturetask;
			}
		}
	}

	public static ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule)
	{
		Validate.notNull(runnableToSchedule);
		return ClientProxy.<Object>addScheduledTask(Executors.callable(runnableToSchedule));
	}
	public static void addScheduledPacket(Packet<INetHandler> packet, long id){
		addScheduledTask(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				try {
					mc.mcProfiler.startSection(packet.getClass().toString());
					packet.processPacket(getHandler(id));
				} catch(Exception e){
				} finally { mc.mcProfiler.endSection(); }
				return null;
			}
		});
	}
}
