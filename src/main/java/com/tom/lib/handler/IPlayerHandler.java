package com.tom.lib.handler;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public interface IPlayerHandler {
	public void load(NBTTagCompound tag);
	public void save(NBTTagCompound tag);
	public String getID();
	public void updatePre(EntityPlayerMP player);
	public void updatePost(EntityPlayerMP player);

}
