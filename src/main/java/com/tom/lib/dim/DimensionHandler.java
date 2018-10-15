package com.tom.lib.dim;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.storage.ISaveHandler;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.tom.lib.LibConfig;
import com.tom.lib.client.dim.DimChunkProviderClient;
import com.tom.lib.debug.CMEMap;
import com.tom.lib.utils.ReflectionUtils;
import com.tom.lib.utils.TomsUtils;

public class DimensionHandler {
	public static final Logger log = LogManager.getLogger("Tom's Lib Dimension Handler");
	private static DimensionHandler ins;
	public static DimensionType type;
	private static Map<Long, Dim> dimParts = new HashMap<>();
	private static Map<BlockPos, Dim> dimToBP = new HashMap<>();
	private static long nextid;
	private static File dimFile;
	public static void init(){
		ins = new DimensionHandler();
		MinecraftForge.EVENT_BUS.register(ins);
		type = DimensionType.register("tomslib_dim", "_tdim", LibConfig.dimID, DimProvider.class, true);
		DimensionManager.registerDimension(LibConfig.dimID, type);
	}
	private static void patchChunkProvider(WorldServer world){
		try {
			ISaveHandler saveHandler = ReflectionUtils.getValue(World.class, ISaveHandler.class, world);
			IChunkLoader ichunkloader = saveHandler.getChunkLoader(world.provider);
			IChunkProvider p = new DimChunkProvider(world, ichunkloader);
			ReflectionUtils.setField(World.class, IChunkProvider.class, world, p);
		} catch (Throwable e) {
			log.error("Failed to patch dimension world", e);
		}
	}
	@SideOnly(Side.CLIENT)
	public static void patchChunkProvider(WorldClient world) {
		try {
			IChunkProvider p = new DimChunkProviderClient(world);
			ReflectionUtils.setField(World.class, IChunkProvider.class, world, p);
			ReflectionUtils.setField(WorldClient.class, ChunkProviderClient.class, world, p);
		} catch (Throwable e) {
			log.error("Failed to patch dimension world", e);
		}
	}
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void worldLoad(WorldEvent.Load event){
		if(dimFile == null){
			onServerStart(TomsUtils.getSavedFile());
		}
		if(event.getWorld() instanceof WorldServer && event.getWorld().provider instanceof DimProvider){
			patchChunkProvider((WorldServer) event.getWorld());
			try {
				ReflectionUtils.setFinalField(EntityTracker.class, Set.class, ((WorldServer)event.getWorld()).getEntityTracker(), Collections.newSetFromMap(new CMEMap<>()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public static Dim createRegion(int xs, int ys, int zs, IBlockState boundary, boolean hasSky){
		WorldServer world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(LibConfig.dimID);
		if(xs > 0 && xs < 257 && ys > 0 && ys < 257 & zs > 0 && zs < 257){
			long id = nextid++;
			long xl1 = id % 1000;
			long xl2 = xl1 % 2 == 0 ? xl1/2 : -((xl1+1)/2);
			long zl1 = id / 1000;
			long zl2 = zl1 % 2 == 0 ? zl1/2 : -((zl1+1)/2);
			BlockPos posS = new BlockPos(xl2 * 512, hasSky ? 255-ys : 128 - (ys/2), zl2 * 512);
			BlockPos posE = posS.add(xs, ys, zs);
			log.info("Creating new Region: " + id + ", " + posS);
			AxisAlignedBB bb = new AxisAlignedBB(posS, posE);
			Iterator<MutableBlockPos> itr = BlockPos.getAllInBoxMutable(posS.add(-1, -1, -1), posE.add(1, hasSky ? 0 : 1, 1)).iterator();
			while(itr.hasNext()){
				MutableBlockPos pos = itr.next();
				if(contains(bb, pos)){
					world.setBlockToAir(pos);
				}else{
					world.setBlockState(pos, boundary);
				}
			}
			Dim dim = new Dim(id, world, posS, bb);
			dimParts.put(id, dim);
			dimToBP.put(new BlockPos(xl2, 0, zl2), dim);
			return dim;
		}
		return null;
	}
	public static Dim getRegion(long id){
		return dimParts.get(id);
	}
	public static Dim getRegion(BlockPos pos){
		return dimToBP.get(getRegionPos(pos));
	}
	public static BlockPos getRegionPos(BlockPos pos){
		return new BlockPos(pos.getX() / 512, 0, pos.getZ() / 512);
	}
	public static BlockPos getRegionCenter(BlockPos pos, BlockPos size, boolean hasSky){
		return new BlockPos(pos.getX() / 512 * 512 - size.getX() / 2, hasSky ? 256 - size.getY() : 128 - size.getY()/2, pos.getZ() / 512 * 512 - size.getZ() / 2);
	}
	/**
	 * Returns if the supplied Vec3I is completely inside the bounding box
	 */
	private static boolean contains(AxisAlignedBB bb, Vec3i vec)
	{
		if (vec.getX() > bb.minX && vec.getX() < bb.maxX)
		{
			if (vec.getY() > bb.minY && vec.getY() < bb.maxY)
			{
				return vec.getZ() > bb.minZ && vec.getZ() < bb.maxZ;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	public static void save(){
		if(dimFile != null){
			NBTTagCompound tag = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			tag.setTag("dim", list);
			for (Dim dim : dimParts.values()) {
				NBTTagCompound t = new NBTTagCompound();
				list.appendTag(t);
				t.setLong("id", dim.getId());
				t.setDouble("x1", dim.bb.minX);
				t.setDouble("y1", dim.bb.minY);
				t.setDouble("z1", dim.bb.minZ);
				t.setDouble("x2", dim.bb.maxX);
				t.setDouble("y2", dim.bb.maxY);
				t.setDouble("z2", dim.bb.maxZ);
				t.setTag("tag", dim.tag != null ? dim.tag : new NBTTagCompound());
			}
			try {
				CompressedStreamTools.write(tag, dimFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void load(){
		dimParts.clear();
		dimToBP.clear();
		WorldServer world = getWorld();
		if(dimFile.exists()){
			try {
				NBTTagCompound tag = CompressedStreamTools.read(dimFile);
				NBTTagList list = tag.getTagList("dim", 10);
				for(int i = 0;i<list.tagCount();i++){
					NBTTagCompound t = list.getCompoundTagAt(i);
					long id = t.getLong("id");
					double x1 = t.getDouble("x1");
					double y1 = t.getDouble("y1");
					double z1 = t.getDouble("z1");
					BlockPos s = new BlockPos(x1, y1, z1);
					double x2 = t.getDouble("x2");
					double y2 = t.getDouble("y2");
					double z2 = t.getDouble("z2");
					AxisAlignedBB bb = new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
					Dim dim = new Dim(id, world, s, bb);
					dim.tag = t.getCompoundTag("tag");
					long xl1 = id % 1000;
					long xl2 = xl1 % 2 == 0 ? xl1/2 : -((xl1+1)/2);
					long zl1 = id / 1000;
					long zl2 = zl1 % 2 == 0 ? zl1/2 : -((zl1+1)/2);
					dimParts.put(id, dim);
					dimToBP.put(new BlockPos(xl2, 0, zl2), dim);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void onServerStart(File file) {
		file.mkdirs();
		dimFile = new File(file, "dimData.dat");
		load();
	}
	@SubscribeEvent
	public void worldSave(WorldEvent.Save event){
		if (event.getWorld().provider.getDimension() == 0)save();
	}
	public static void close() {
		save();
		dimFile = null;
		dimParts.clear();
		dimToBP.clear();
	}
	public static WorldServer getWorld(){
		return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(LibConfig.dimID);
	}
	public static void teleportPlayer(EntityPlayer player, int dimension, double x, double y, double z){
		int oldDimension = player.getEntityWorld().provider.getDimension();
		EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
		WorldServer oldWorld = (WorldServer) player.getEntityWorld();
		MinecraftServer server = oldWorld.getMinecraftServer();
		WorldServer worldServer = server.getWorld(dimension);
		player.addExperienceLevel(0);

		worldServer.getMinecraftServer().getPlayerList().transferPlayerToDimension(entityPlayerMP, dimension, new TomsLibTeleporter(worldServer, x, y, z));
		player.setPositionAndUpdate(x, y, z);
		if (oldDimension == 1) {
			// For some reason teleporting out of the end does weird things.
			player.setPositionAndUpdate(x, y, z);
			worldServer.spawnEntity(player);
			worldServer.updateEntityWithOptionalForce(player, false);
		}
	}

	public static List<Dim> list(Predicate<Dim> p) {
		return dimParts.values().stream().filter(p).collect(Collectors.toList());
	}
}
