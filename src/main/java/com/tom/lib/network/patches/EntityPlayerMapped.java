package com.tom.lib.network.patches;

import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public class EntityPlayerMapped extends EntityPlayerMP {
	public EntityPlayerMP player;
	public long mapid;
	private NetworkManagerPatchedServer manager;
	private Supplier<Vec3d> off;
	private Supplier<Vec2f> roff;
	public EntityPlayerMapped(MinecraftServer server, WorldServer worldIn, EntityPlayerMP player, long id, Supplier<Vec3d> off, Supplier<Vec2f> roff) {
		super(server, worldIn, player.getGameProfile(), new PlayerInteractionManager(worldIn));
		manager = new NetworkManagerPatchedServer(player, id);
		connection = new NetHandlerPlayServer(server, manager, this);
		this.player = player;
		this.mapid = id;
		this.off = off;
		this.roff = roff;
	}

	@Override
	public void onUpdate() {
		Vec3d off = this.off.get();
		Vec2f roff = this.roff.get();
		setPositionAndRotation(off.x, off.y, off.z, roff.x, roff.y);
		this.mcServer.getPlayerList().serverUpdateMovingPlayer(this);
		super.onUpdate();
	}
	public void remove(){
		world.removeEntityDangerously(this);
		manager.stop();
	}
	public Vec3d getPos(){
		return off.get();
	}
}
