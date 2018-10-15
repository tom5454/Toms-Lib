package com.tom.lib.api.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;

import com.tom.lib.api.CapabilityPeripheral;
import com.tom.lib.utils.TomsUtils;

public interface ITMPeripheral {
	public class LuaInteruptedException extends LuaException {
		private static final long serialVersionUID = -7396879728472447101L;

	}
	public interface IComputer {
		void queueEvent(@Nonnull String event, @Nullable Object[] arguments);
		String getAttachmentName();
		Object[] map(Object[] in);
		Object[] pullEvent(String string) throws LuaException;
	}
	String getType();
	String[] getMethodNames();
	Object[] call(IComputer computer, String method, Object[] args) throws LuaException;
	default void attach(IComputer computer){}
	default void detach(IComputer computer){}
	public static class LuaException extends Exception {
		private static final long serialVersionUID = -6487582951282118489L;
		private final int level;

		public LuaException() {
			this("error", 1);
		}

		public LuaException( @Nullable String message ) {
			this(message, 1);
		}

		public LuaException( @Nullable String message, int level ) {
			super(message);
			this.level = level;
		}
		public dan200.computercraft.api.lua.LuaException toLuaException(){
			return new dan200.computercraft.api.lua.LuaException(getMessage(), level);
		}
	}
	public static interface ITMCompatPeripheral extends ITMPeripheral {
		Object[] callMethod(IComputer computer, int method, Object[] args) throws LuaException;
		@Override
		default Object[] call(IComputer computer, String method, Object[] args) throws LuaException {
			return callMethod(computer, TomsUtils.find(getMethodNames(), method), args);
		}

	}
	public interface ITMLuaObject {
		String[] getMethodNames();
		Object[] call(IComputer computer, String method, Object[] args) throws LuaException;
		long getID();
	}
	public static class Handler {
		@SuppressWarnings("rawtypes")
		private static final List<Function<Function<EnumFacing, ITMPeripheral>, Map<Capability, Map<EnumFacing, Supplier<Object>>>>> INIT = new ArrayList<>();
		@SuppressWarnings("rawtypes")
		public static void addCapabilityInit(Capability c, BiFunction<ITMPeripheral, EnumFacing, Object> supplier){
			INIT.add(p -> create(c, f -> supplier.apply(p.apply(f), f)));
		}
		@SuppressWarnings("rawtypes")
		public static void initCapabilities(Map<Capability, Map<EnumFacing, Supplier<Object>>> capMap, ITMPeripheral te){
			INIT.stream().map(f -> f.apply(a -> te)).forEach(capMap::putAll);
		}
		@SuppressWarnings("rawtypes")
		private static Map<Capability, Map<EnumFacing, Supplier<Object>>> create(Capability c, Function<EnumFacing, Object> supplier){
			Map<Capability, Map<EnumFacing, Supplier<Object>>> ret = new HashMap<>();
			Map<EnumFacing, Supplier<Object>> map = new HashMap<>();
			ret.put(c, map);
			map.put(null, () -> supplier.apply(null));
			for (int i = 0;i < EnumFacing.VALUES.length;i++) {
				EnumFacing f = EnumFacing.VALUES[i];
				map.put(f, () -> supplier.apply(f));
			}
			return ret;
		}
		static {
			addCapabilityInit(CapabilityPeripheral.PERIPHERAL, (p, side) -> new TMPeripheralCap(p));
		}
		@SuppressWarnings("rawtypes")
		public static void initCapabilitiesSided(Map<Capability, Map<EnumFacing, Supplier<Object>>> capMap, ISidedTMPeripheral te) {
			INIT.stream().map(f -> f.apply(facing -> te.getPeripheral(facing))).forEach(capMap::putAll);
		}
	}
	public static interface ITMPeripheralCap {
		String getType();
		String[] getMethodNames();
		Object[] call(IComputer computer, String method, Object[] args) throws LuaException;
		void attach(IComputer computer);
		void detach(IComputer computer);
	}
	public static class TMPeripheralCap implements ITMPeripheralCap {
		private ITMPeripheral p;
		public TMPeripheralCap(ITMPeripheral p) {
			this.p = p;
		}

		@Override
		public String getType() {
			return p.getType();
		}

		@Override
		public String[] getMethodNames() {
			return p.getMethodNames();
		}

		@Override
		public Object[] call(IComputer computer, String method, Object[] args) throws LuaException {
			return p.call(computer, method, args);
		}

		@Override
		public void attach(IComputer computer) {
			p.attach(computer);
		}

		@Override
		public void detach(IComputer computer) {
			p.detach(computer);
		}
	}
	public static class SidedTMPeripheralCap implements ITMPeripheralCap {
		private ITMPeripheral p;
		public SidedTMPeripheralCap(ISidedTMPeripheral p, EnumFacing side) {
			this.p = p.getPeripheral(side);
		}

		@Override
		public String getType() {
			return p.getType();
		}

		@Override
		public String[] getMethodNames() {
			return p.getMethodNames();
		}

		@Override
		public Object[] call(IComputer computer, String method, Object[] args) throws LuaException {
			return p.call(computer, method, args);
		}

		@Override
		public void attach(IComputer computer) {
			p.attach(computer);
		}

		@Override
		public void detach(IComputer computer) {
			p.detach(computer);
		}
	}
	public static interface ISidedTMPeripheral {
		ITMPeripheral getPeripheral(EnumFacing side);
	}
}
