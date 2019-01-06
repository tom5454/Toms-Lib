package com.tom.lib.api.map;

import net.minecraft.util.math.MathHelper;

public enum RenderType {
	NORMAL("normal"), NONE("none"), ICON("icon");
	public static final RenderType[] VALUES = values();
	private final String name;

	public static RenderType fromString(String in) {
		if (in != null) {
			if (in.equalsIgnoreCase("normal")) {
				return NORMAL;
			} else if (in.equalsIgnoreCase("none")) {
				return NONE;
			} else if (in.equalsIgnoreCase("icon")) { return ICON; }
		}
		return NORMAL;
	}

	private RenderType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static RenderType get(int index) {
		return VALUES[MathHelper.abs(index % VALUES.length)];
	}

	public static String[] getStringList() {
		String[] list = new String[VALUES.length];
		for (int i = 0;i < VALUES.length;i++) {
			list[i] = VALUES[i].toString();
		}
		return list;
	}
}