package com.tom.lib.api.module;

import java.lang.annotation.AnnotationFormatError;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraftforge.fml.common.discovery.ASMDataTable;

import com.tom.lib.LibInit;
import com.tom.lib.utils.TomsUtils;

public class ModuleManager {
	private static final Set<String> loadedModules = new HashSet<>();
	private static final Map<String, List<String>> modRequests = new HashMap<>();
	private static final Map<String, List<String>> moduleDeps = new HashMap<>();
	public static final String DIMENSION = "dims", ENTITYWORLD = "entityworld", DATA_STORAGE = "datastore",
			PLAYER_HANDLER = "player_handler", WORLD_HANDLER = "world_handler";
	private static void initDeps() {
		moduleDeps.put(ENTITYWORLD, Collections.singletonList(DIMENSION));
	}
	public static void initModuleManager(ASMDataTable d){
		LibInit.log.info("Loading module manager...");
		initDeps();
		List<Entry<Class<?>, TMLibAddon>> an = TomsUtils.getInstances(d, TMLibAddon.class);
		for (Entry<Class<?>, TMLibAddon> entry : an) {
			TMLibAddon tmLibAddon = entry.getValue();
			Class<?> cl = entry.getKey();
			String mid = tmLibAddon.modid();
			if(mid.equals("<|null|>"))throw new AnnotationFormatError("Missing modid " + entry.getKey());
			List<Method> methods = Arrays.stream(cl.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(TMLibAddon.class)).collect(Collectors.toList());
			if(methods.isEmpty() || methods.size() > 1)throw new AnnotationFormatError("Method error" + entry.getKey());
			Method m = methods.get(0);
			String[] modules;
			try {
				modules = (String[]) m.invoke(null);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassCastException e) {
				throw new AnnotationFormatError(e);
			}
			if(modules != null){
				for (int i = 0;i < modules.length;i++) {
					String module = modules[i];
					if(!modRequests.containsKey(module))modRequests.put(module, new ArrayList<>());
					modRequests.get(module).add(mid);
					loadedModules.add(module);
					checkDeps(mid, module);
				}
			}else throw new AnnotationFormatError("modules == null");
		}
		LibInit.log.info("Loaded module manager");
		loadedModules.forEach(LibInit.log::info);
	}
	private static void checkDeps(String mid, String module) {
		List<String> deps = moduleDeps.get(module);
		if(deps != null){
			for (String string : deps) {
				checkDeps(mid, string);
			}
		}
		if(!modRequests.containsKey(module))modRequests.put(module, new ArrayList<>());
		modRequests.get(module).add(mid);
		loadedModules.add(module);
	}
	public static boolean isModuleLoaded(String id){
		return loadedModules.contains(id);
	}
}
