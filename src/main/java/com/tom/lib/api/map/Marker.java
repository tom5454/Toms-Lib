package com.tom.lib.api.map;

import net.minecraft.nbt.NBTTagCompound;

public class Marker {
	public final String name;
	public final String groupName;
	public String iconLocation, beamIconLocation;
	public int x;
	public int y;
	public int z;
	public int dimension;
	public final boolean reloadable;
	public int color;
	public final RenderType beamType;
	public final RenderType labelType;
	public boolean isServerSided = false;
	public Marker(String name, String groupName, int x, int y, int z, int dimension, String icon, int color, RenderType beamType, RenderType labelType, String beamLoc, boolean reloadable) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.dimension = dimension;
		this.iconLocation = icon != null ? icon : "normal";
		this.groupName = groupName;
		this.color = color;
		this.reloadable = reloadable;
		this.beamType = beamType;
		this.labelType = labelType;
		this.beamIconLocation = beamLoc != null ? beamLoc : "normal";
	}
	public static Marker fromNBT(NBTTagCompound tag) {
		return new Marker(tag.getString("name"), tag.getString("group"), tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"), tag.getInteger("dim"), tag.getString("icon"), tag.getInteger("color"), RenderType.VALUES[tag.getInteger("beam")], RenderType.VALUES[tag.getInteger("label")], tag.getString("beamLoc"), true);
	}

	public void writeToNBT(NBTTagCompound tag) {
		if (reloadable) {
			tag.setString("name", name);
			tag.setString("group", groupName);
			tag.setString("icon", iconLocation);
			tag.setInteger("x", x);
			tag.setInteger("y", y);
			tag.setInteger("z", z);
			tag.setInteger("dim", dimension);
			tag.setInteger("color", color);
			tag.setInteger("beam", beamType.ordinal());
			tag.setInteger("label", labelType.ordinal());
			tag.setString("beamLoc", beamIconLocation);
		} else {
			tag.setBoolean("null", true);
		}
	}
}