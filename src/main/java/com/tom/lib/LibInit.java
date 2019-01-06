package com.tom.lib;

import static com.tom.lib.api.module.ModuleManager.*;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import com.tom.lib.api.CapabilityGridDeviceHost;
import com.tom.lib.api.CapabilityPeripheral;
import com.tom.lib.api.energy.EnergyType;
import com.tom.lib.dim.DimensionHandler;
import com.tom.lib.entity.world.EntityWorldShip;
import com.tom.lib.entity.world.WorldTeleportHandler;
import com.tom.lib.handler.FileManager;
import com.tom.lib.handler.LibEventHandler;
import com.tom.lib.handler.PlayerHandler;
import com.tom.lib.handler.WorldHandler;
import com.tom.lib.network.LibNetworkHandler;
import com.tom.lib.proxy.CommonProxy;
import com.tom.lib.thirdparty.computercraft.ComputerCraft;
import com.tom.lib.thirdparty.oc.OpenComputers;
import com.tom.lib.utils.Modids;
import com.tom.lib.utils.ReflectionUtils;
import com.tom.lib.utils.TomsUtils;

@Mod(modid = LibInit.modid, name = LibInit.modName, version = LibInit.version)
public class LibInit {
	public static boolean isCCLoaded = false, isOCLoaded = false;
	public static final String modid = "tomslib";
	public static final String modName = "Tom's Lib";
	public static final String version = "1.1.0";
	public static final Logger log = LogManager.getLogger(modName);
	private static final String CLIENT_PROXY_CLASS = "com.tom.lib.proxy.ClientProxy";
	private static final String SERVER_PROXY_CLASS = "com.tom.lib.proxy.ServerProxy";

	@Instance(modid)
	public static LibInit modInstance;
	public static double forceMultipier = 2;
	@SidedProxy(clientSide = CLIENT_PROXY_CLASS, serverSide = SERVER_PROXY_CLASS)
	public static CommonProxy proxy;
	@EventHandler
	public static void PreLoad(FMLPreInitializationEvent PreEvent) {
		log.info("Start Pre Initialization");
		long tM = System.currentTimeMillis();
		initModuleManager(PreEvent.getAsmData());
		isCCLoaded = Loader.isModLoaded(Modids.COMPUTERCRAFT);
		isOCLoaded = Loader.isModLoaded(Modids.OPEN_COMPUTERS);
		LibConfig.init(PreEvent.getSuggestedConfigurationFile());
		LibNetworkHandler.libInit();
		proxy.preinit();
		TomsUtils.printFakePlayerInfo();
		DataSerializers.registerSerializer(TomsUtils.LONG_SERIALIZER);
		MinecraftForge.EVENT_BUS.register(new LibEventHandler());
		long time = System.currentTimeMillis() - tM;
		log.info("Pre Initialization took in " + time + " milliseconds");
	}

	@EventHandler
	public static void load(FMLInitializationEvent event) {
		log.info("Start Initialization");
		long tM = System.currentTimeMillis();
		forceMultipier = ReflectionUtils.getValueOrDef("com.tom.config.Config", "forceMultipier", null, 2d);
		CapabilityPeripheral.init();
		CapabilityGridDeviceHost.init();
		EnergyType.init();
		if(isModuleLoaded(ENTITYWORLD))
			EntityRegistry.registerModEntity(new ResourceLocation(modid, "world"), EntityWorldShip.class, "worldEntity", LibConfig.worldEntityID, modInstance, 256, 2, true);
		if(isCCLoaded)ComputerCraft.init();
		if(isOCLoaded)OpenComputers.init();
		proxy.init();
		if(isModuleLoaded(DIMENSION))DimensionHandler.init();
		if(isModuleLoaded(ENTITYWORLD))WorldTeleportHandler.init();
		if(isModuleLoaded(PLAYER_HANDLER))PlayerHandler.init();
		if(isModuleLoaded(WORLD_HANDLER))WorldHandler.init();
		long time = System.currentTimeMillis() - tM;
		log.info("Initialization took in " + time + " milliseconds");
	}
	@EventHandler
	public static void onServerStart(FMLServerStartingEvent event) {
		log.info("Server Start");
		if(isModuleLoaded(DATA_STORAGE)){
			FileManager.INSTANCE = new FileManager(new File(TomsUtils.getSavedFile(), "dataStore"));
			FileManager.INSTANCE.init();
		}
		if(isModuleLoaded(PLAYER_HANDLER))PlayerHandler.onServerStart(new File(TomsUtils.getSavedFile(), "playerData"));
		if(isModuleLoaded(WORLD_HANDLER))WorldHandler.onServerStart(new File(TomsUtils.getSavedFile(), "chunkData"));
		log.info("Loading Completed");
	}
	@EventHandler
	public static void onServerStop(FMLServerStoppingEvent event) {
		log.info("Stopping the Server");
		if(isModuleLoaded(DIMENSION))DimensionHandler.close();
		if(isModuleLoaded(PLAYER_HANDLER))PlayerHandler.cleanup();
		if(isModuleLoaded(WORLD_HANDLER))WorldHandler.stopServer();
	}
	@EventHandler
	public static void onServerStoped(FMLServerStoppedEvent event) {
		log.info("Stopped the Server");
		if(isModuleLoaded(DATA_STORAGE))FileManager.clean();
	}
}
