package com.tom.lib;

import static com.tom.lib.api.module.ModuleManager.*;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class LibConfig {
	public static Configuration config;
	public static int dimID;
	public static int worldEntityID;
	public static int maxTaskCount;
	public static void init(File configFile) {
		LibInit.log.info("Init Configuration");
		if (config == null) {
			config = new Configuration(configFile);
			config.load(); // get the actual data from the file.
		}
		if(isModuleLoaded(DIMENSION))dimID = config.getInt("id", "dimension", 10240, Integer.MIN_VALUE, Integer.MAX_VALUE, "Dimension id");
		if(isModuleLoaded(ENTITYWORLD))worldEntityID = config.getInt("eid", "dimension", 10240, Integer.MIN_VALUE, Integer.MAX_VALUE, "Entity ship id");
		if(isModuleLoaded(WORLD_HANDLER))maxTaskCount = config.getInt("Max tasks per tick", "server", 2000, -1, Integer.MAX_VALUE, "Max tasks to process on every server tick. The handler skips all the tasks above this limit to the next tick.");

		config.save();
	}
}
