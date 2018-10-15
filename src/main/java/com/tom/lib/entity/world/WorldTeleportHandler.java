package com.tom.lib.entity.world;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

import com.tom.lib.LibConfig;
import com.tom.lib.dim.Dim;
import com.tom.lib.dim.DimensionHandler;
import com.tom.lib.dim.TomsLibTeleporter;
import com.tom.lib.network.LibNetworkHandler;
import com.tom.lib.network.messages.MessagePacket;
import com.tom.lib.network.packets.PacketSetPos;
import com.tom.lib.network.packets.PacketSwap;
import com.tom.lib.network.packets.PacketWorldStart;
import com.tom.lib.network.packets.PacketWorldStop;
import com.tom.lib.network.patches.EntityPlayerMapped;
import com.tom.lib.utils.TomsUtils;

public class WorldTeleportHandler {
	private static boolean ignoreDimChangedEvent;
	private WorldTeleportHandler() {
	}
	public static void init(){
		MinecraftForge.EVENT_BUS.register(new WorldTeleportHandler());
	}
	public static void teleportPlayerIn(EntityPlayer player, EntityWorld entity){
		Dim dim = entity.getDim();
		long id = dim.getId();
		BlockPos size = entity.getSize();
		double x = dim.startPos.getX() + size.getX()/2 + player.posX - entity.posX;
		double y = dim.startPos.getY() + size.getY()/2 + player.posY - entity.posY;
		double z = dim.startPos.getZ() + size.getZ()/2 + player.posZ - entity.posZ;
		EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
		dim.world.getPlayers(EntityPlayerMapped.class, v -> (v.getName().equals(player.getName()) && v.mapid == id)).forEach(EntityPlayerMapped::remove);
		LibNetworkHandler.sendTo(new MessagePacket(new PacketSwap(id + 1, true, new Vec3d(entity.posX, entity.posY, entity.posZ), entity.getUniqueID(), entity.getSize()), 0), (EntityPlayerMP) player);
		ignoreDimChangedEvent = true;
		tpx(entityPlayerMP, LibConfig.dimID, x, y, z);
		ignoreDimChangedEvent = false;
		WorldServer oldWorld = (WorldServer) player.getEntityWorld();
		MinecraftServer server = oldWorld.getMinecraftServer();
		//LibNetworkHandler.sendTo(new MessagePacket(new PacketWorldStart(oldWorld.provider.getDimension(), new WorldSettings(0L, oldWorld.getMinecraftServer().getGameType(), false, oldWorld.getMinecraftServer().isHardcore(), oldWorld.getWorldType()), 0), 0), (EntityPlayerMP) player);
		EntityPlayerMapped pl = new EntityPlayerMapped(server, oldWorld, entityPlayerMP, 0, () -> {
			double xv = dim.startPos.getX() + size.getX()/2 + player.posX - entity.posX;
			double yv = dim.startPos.getY() + size.getY()/2 + player.posY - entity.posY;
			double zv = dim.startPos.getZ() + size.getZ()/2 + player.posZ - entity.posZ;
			return new Vec3d(xv, yv, zv);
		}, () -> {
			float yaw = entity.rotationYaw + player.rotationYaw;
			float pitch = entity.rotationPitch + player.rotationPitch;
			return new Vec2f(yaw, pitch);
		});
		oldWorld.getPlayerChunkMap().addPlayer(pl);
		oldWorld.spawnEntity(pl);
	}
	public static void teleportPlayerOut(EntityPlayer player, Dim dim){
		EntityWorld entity = getEntityWorld(dim);
		long id = dim.getId();
		BlockPos size = entity == null ? BlockPos.ORIGIN : entity.getSize();
		double x = entity == null ? 0 : entity.posX + size.getX()/2 + player.posX - dim.startPos.getX();
		double y = entity == null ? 256 : entity.posY + size.getY()/2 + player.posY - dim.startPos.getY();
		double z = entity == null ? 0 : entity.posZ + size.getZ()/2 + player.posZ - dim.startPos.getZ();
		EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
		//LibNetworkHandler.sendTo(new MessagePacket(new PacketWorldStop(0), 0), entityPlayerMP);
		LibNetworkHandler.sendTo(new MessagePacket(new PacketSwap(id + 1, false, Vec3d.ZERO, TomsUtils.NULL_ID, entity.getSize()), 0), (EntityPlayerMP) player);
		World world = entity == null ? FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld() : entity.world;
		world.getPlayers(EntityPlayerMapped.class, v -> (v.getName().equals(player.getName()) && v.mapid == id)).forEach(EntityPlayerMapped::remove);
		ignoreDimChangedEvent = true;
		tpx(entityPlayerMP, world.provider.getDimension(), x, y, z);
		ignoreDimChangedEvent = false;
	}
	public static void tpx(EntityPlayerMP player, int dim, double x, double y, double z){
		int oldDimension = player.getEntityWorld().provider.getDimension();
		WorldServer oldWorld = (WorldServer) player.getEntityWorld();
		MinecraftServer server = oldWorld.getMinecraftServer();
		WorldServer worldServer = server.getWorld(LibConfig.dimID);
		player.addExperienceLevel(0);

		worldServer.getMinecraftServer().getPlayerList().transferPlayerToDimension(player, dim, new TomsLibTeleporter(worldServer, x, y, z));
		player.setPositionAndUpdate(x, y, z);
		if (oldDimension == 1) {
			// For some reason teleporting out of the end does weird things.
			player.setPositionAndUpdate(x, y, z);
			worldServer.spawnEntity(player);
			worldServer.updateEntityWithOptionalForce(player, false);
		}
	}
	public static void addTracking(EntityPlayerMP player, EntityWorld entity) {
		Dim dim = entity.getDim();
		if(dim == null){
			entity.queue(player);
			return;
		}
		if(player instanceof EntityPlayerMapped){
			EntityPlayerMapped m = (EntityPlayerMapped) player;
			if(m.mapid == 0)return;
		}
		long id = dim.getId();
		System.out.println("startTracking: " + id);
		LibNetworkHandler.sendTo(new MessagePacket(new PacketWorldStart(LibConfig.dimID, new WorldSettings(0L, player.world.getMinecraftServer().getGameType(), false, player.world.getMinecraftServer().isHardcore(), player.world.getWorldType()), id+1, entity.getSize()), id+1), player);
		BlockPos size = entity.getSize();
		EntityPlayerMapped pl = new EntityPlayerMapped(player.world.getMinecraftServer(), dim.world, player, id+1, () -> {
			double xv = dim.startPos.getX() + size.getX()/2 + player.posX - entity.posX;
			double yv = dim.startPos.getY() + size.getY()/2 + player.posY - entity.posY;
			double zv = dim.startPos.getZ() + size.getZ()/2 + player.posZ - entity.posZ;
			return new Vec3d(xv, yv, zv);
		}, () -> {
			float yaw = entity.rotationYaw + player.rotationYaw;
			float pitch = entity.rotationPitch + player.rotationPitch;
			return new Vec2f(yaw, pitch);
		});
		dim.world.getPlayerChunkMap().addPlayer(pl);
		dim.world.spawnEntity(pl);
	}
	public static void removeTracking(EntityPlayerMP player, EntityWorld entity) {
		Dim dim = entity.getDim();
		long id = dim.getId();
		System.out.println("stopTracking: " + id);
		dim.world.getPlayers(EntityPlayerMapped.class, v -> (v.getName().equals(player.getName()) && v.mapid == id)).forEach(EntityPlayerMapped::remove);
		LibNetworkHandler.sendTo(new MessagePacket(new PacketWorldStop(id + 1), id + 1), player);
	}
	public static EntityWorld getEntityWorld(Dim dim){
		WorldServer s = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim.tag.getInteger("dim"));
		Vec3d pos = new Vec3d(dim.tag.getDouble("x"), dim.tag.getDouble("y"), dim.tag.getDouble("z"));
		s.getBlockState(new BlockPos(pos));
		Entity e = s.getEntityFromUuid(dim.tag.getUniqueId("uuid"));
		return e instanceof EntityWorld ? (EntityWorld) e : null;
	}
	public static void writeEntityPos(Dim dim, EntityWorld world){
		if(dim != null){
			dim.tag.setDouble("ex", world.posX);
			dim.tag.setDouble("ey", world.posX);
			dim.tag.setDouble("ez", world.posX);
			dim.tag.setInteger("dim", world.world.provider.getDimension());
			dim.tag.setUniqueId("uuid", world.getUniqueID());
		}
	}
	private static void update(Pair<EntityPlayer, Dim> p){
		EntityPlayer player = p.getLeft();
		Dim dim = p.getRight();
		EntityWorld world = getEntityWorld(dim);
		if(world != null){
			World ww = world.world;
			EntityPlayer pl = ww.getPlayerEntityByName(player.getName());
			if(pl == null){
				BlockPos size = world.getSize();
				EntityPlayerMapped plm = new EntityPlayerMapped(ww.getMinecraftServer(), (WorldServer) ww, (EntityPlayerMP) player, 0, () -> {
					double xv = dim.startPos.getX() + size.getX()/2 + player.posX - world.posX;
					double yv = dim.startPos.getY() + size.getY()/2 + player.posY - world.posY;
					double zv = dim.startPos.getZ() + size.getZ()/2 + player.posZ - world.posZ;
					return new Vec3d(xv, yv, zv);
				}, () -> {
					float yaw = world.rotationYaw + player.rotationYaw;
					float pitch = world.rotationPitch + player.rotationPitch;
					return new Vec2f(yaw, pitch);
				});
				LibNetworkHandler.sendTo(new MessagePacket(new PacketWorldStart(ww.provider.getDimension(), new WorldSettings(0L, ww.getMinecraftServer().getGameType(), false, ww.getMinecraftServer().isHardcore(), ww.getWorldType()), 0, world.getSize()), 0), (EntityPlayerMP) player);
				LibNetworkHandler.sendTo(new MessagePacket(new PacketSetPos(new Vec3d(world.posX, world.posY, world.posZ), world.getUniqueID()), 0), (EntityPlayerMP) player);
				((WorldServer) ww).getPlayerChunkMap().addPlayer(plm);
				ww.spawnEntity(plm);
			}
		}
	}
	private static Pair<EntityPlayer, Dim> anyContains(EntityPlayer player, List<Dim> data){
		AxisAlignedBB playerbb = player.getEntityBoundingBox();
		for (Dim dim : data) {
			if(dim.bb.intersects(playerbb))
				return Pair.of(player, dim);
		}
		return null;
	}
	@SubscribeEvent
	public void tick(WorldTickEvent event) {
		if(event.world.provider.getDimension() == LibConfig.dimID){
			event.world.profiler.startSection("EntityWorldHandlerUpdate");

			List<Dim> l = DimensionHandler.list(d -> d.tag.hasKey("dim"));

			DimensionHandler.getWorld().playerEntities.stream().
			map(p -> anyContains(p, l)).filter(p -> p != null).forEach(WorldTeleportHandler::update);

			event.world.profiler.endSection();
		}
	}
	@SubscribeEvent
	public void onItemRc(RightClickItem evt){
		if(!evt.getWorld().isRemote && evt.getEntityPlayer().world.provider.getDimension() == LibConfig.dimID && evt.getItemStack().getItem() == Items.STICK){
			BlockPos pos = evt.getPos();
			Dim dim = DimensionHandler.getRegion(pos);
			WorldTeleportHandler.teleportPlayerOut(evt.getEntityPlayer(), dim);
		}
	}
	@SubscribeEvent
	public void dimChanged(PlayerChangedDimensionEvent e){
		if(!e.player.world.isRemote && e.fromDim == LibConfig.dimID && !ignoreDimChangedEvent){
			LibNetworkHandler.sendTo(new MessagePacket(new PacketWorldStop(-1), 0), (EntityPlayerMP) e.player);
		}
	}
}
