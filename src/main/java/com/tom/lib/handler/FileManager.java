package com.tom.lib.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.storage.RegionFile;
import net.minecraft.world.storage.IThreadedFileIO;
import net.minecraft.world.storage.ThreadedFileIOBase;

import com.google.common.collect.Maps;

import com.tom.lib.LibInit;

public class FileManager implements IThreadedFileIO {
	public static FileManager INSTANCE;
	private long id = 1;
	private final Map<Long, NBTTagCompound> filesToSave = Maps.<Long, NBTTagCompound>newConcurrentMap();
	private final Set<Long> filesBeingSaved = Collections.<Long>newSetFromMap(Maps.newConcurrentMap());
	public final File saveLocation;
	private boolean flushing;
	public FileManager(File f) {
		saveLocation = f;
	}
	public void init(){
		try {
			boolean saveReq = false;
			NBTTagCompound tag = loadFile__Async(0);
			if(tag != null){
				if(tag.hasKey("id")){
					long i = tag.getLong("id");
					if(i > 0)id = i;
					else {
						id = 1;
						saveReq = true;
					}
				}
			}else{
				saveReq = true;
			}
			if(saveReq)storeID();
		} catch (IOException e) {
		}
	}
	public NBTTagCompound getTagCompound(long id){
		if(id > 0){
			try {
				NBTTagCompound tag = loadFile__Async(id);
				if(tag != null)return tag;
				else return new NBTTagCompound();
			} catch (IOException e) {
			}
			return new NBTTagCompound();
		} else throw new IllegalArgumentException();
	}
	public void saveTagCompound(NBTTagCompound tag, long id){
		if(id > 0)
			addFileToPending(id, tag);
		else throw new IllegalArgumentException();
	}
	public long newTag(){
		long i = id++;
		storeID();
		return i;
	}
	private void storeID() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setLong("id", id);
		addFileToPending(0, tag);
	}
	protected void addFileToPending(long pos, NBTTagCompound compound)
	{
		if (!this.filesBeingSaved.contains(pos))
		{
			this.filesToSave.put(pos, compound);
		}

		ThreadedFileIOBase.getThreadedIOInstance().queueIO(this);
	}

	/**
	 * Writes one queued IO action.
	 *
	 * @return true if there are more IO actions to perform afterwards, or false if there are none (and this instance of
	 * IThreadedFileIO should be removed from the queued list)
	 */
	@Override
	public boolean writeNextIO()
	{
		if (this.filesToSave.isEmpty())
		{
			if (this.flushing)
			{
				LibInit.log.info("ThreadedDataStorage ({}): All files are saved", this.saveLocation.getName());
			}

			return false;
		}
		else
		{
			Long chunkpos = this.filesToSave.keySet().iterator().next();
			boolean lvt_3_1_;

			try
			{
				this.filesBeingSaved.add(chunkpos);
				NBTTagCompound nbttagcompound = this.filesToSave.remove(chunkpos);

				if (nbttagcompound != null)
				{
					try
					{
						this.writeFileData(chunkpos, nbttagcompound);
					}
					catch (Exception exception)
					{
						LibInit.log.error("Failed to save data", exception);
					}
				}

				lvt_3_1_ = true;
			}
			finally
			{
				this.filesBeingSaved.remove(chunkpos);
			}

			return lvt_3_1_;
		}
	}

	private void writeFileData(long id, NBTTagCompound compound) throws IOException
	{
		ChunkPos chunkpos = getPos(id);
		DataOutputStream dataoutputstream = FileCache.getChunkOutputStream(this.saveLocation, chunkpos.x, chunkpos.z);
		CompressedStreamTools.write(compound, dataoutputstream);
		dataoutputstream.close();
	}
	@Nullable
	private NBTTagCompound loadFile__Async(long id) throws IOException
	{
		ChunkPos chunkpos = getPos(id);
		NBTTagCompound nbttagcompound = this.filesToSave.get(chunkpos);

		if (nbttagcompound == null)
		{
			DataInputStream datainputstream = FileCache.getChunkInputStream(this.saveLocation, chunkpos.x, chunkpos.z);

			if (datainputstream == null)
			{
				return null;
			}
			nbttagcompound = CompressedStreamTools.read(datainputstream);
		}

		return nbttagcompound;
	}
	private ChunkPos getPos(long id) {
		int v7 = (byte)(id >>> 56);
		int v6 = (byte)(id >>> 48);
		int v5 = (byte)(id >>> 40);
		int v4 = (byte)(id >>> 32);
		int v3 = (byte)(id >>> 24);
		int v2 = (byte)(id >>> 16);
		int v1 = (byte)(id >>>  8);
		int v0 = (byte)(id >>>  0);
		return new ChunkPos(v0 | (v2 << 8) | (v4 << 16) | (v6 << 24), v1 | (v3 << 8) | (v5 << 16) | (v7 << 24));
	}
	public void flush()
	{
		try
		{
			this.flushing = true;

			while (this.writeNextIO());
		}
		finally
		{
			this.flushing = false;
		}
	}
	public static class FileCache
	{
		/** A map containing Files as keys and RegionFiles as values */
		private static final Map<File, RegionFile> REGIONS_BY_FILE = Maps.<File, RegionFile>newHashMap();

		public static synchronized RegionFile createOrLoadRegionFile(File file1, int chunkX, int chunkZ)
		{
			File file2 = new File(file1, "d." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".dat");
			RegionFile regionfile = REGIONS_BY_FILE.get(file2);

			if (regionfile != null)
			{
				return regionfile;
			}
			else
			{
				if (!file1.exists())
				{
					file1.mkdirs();
				}

				if (REGIONS_BY_FILE.size() >= 256)
				{
					clearRegionFileReferences();
				}

				RegionFile regionfile1 = new RegionFile(file2);
				REGIONS_BY_FILE.put(file2, regionfile1);
				return regionfile1;
			}
		}

		public static synchronized RegionFile getRegionFileIfExists(File file1, int chunkX, int chunkZ)
		{
			File file2 = new File(file1, "d." + (chunkX >> 5) + "." + (chunkZ >> 5) + ".dat");
			RegionFile regionfile = REGIONS_BY_FILE.get(file2);

			if (regionfile != null)
			{
				return regionfile;
			}
			else if (file1.exists() && file2.exists())
			{
				if (REGIONS_BY_FILE.size() >= 256)
				{
					clearRegionFileReferences();
				}

				RegionFile regionfile1 = new RegionFile(file2);
				REGIONS_BY_FILE.put(file2, regionfile1);
				return regionfile1;
			}
			else
			{
				return null;
			}
		}

		/**
		 * clears region file references
		 */
		public static synchronized void clearRegionFileReferences()
		{
			for (RegionFile regionfile : REGIONS_BY_FILE.values())
			{
				try
				{
					if (regionfile != null)
					{
						regionfile.close();
					}
				}
				catch (IOException ioexception)
				{
					ioexception.printStackTrace();
				}
			}

			REGIONS_BY_FILE.clear();
		}

		/**
		 * Gets an input stream for the chunk at the specified location.
		 */
		public static DataInputStream getChunkInputStream(File worldDir, int chunkX, int chunkZ)
		{
			RegionFile regionfile = createOrLoadRegionFile(worldDir, chunkX, chunkZ);
			return regionfile.getChunkDataInputStream(chunkX & 31, chunkZ & 31);
		}

		/**
		 * Gets an output stream for the specified chunk.
		 */
		public static DataOutputStream getChunkOutputStream(File worldDir, int chunkX, int chunkZ)
		{
			RegionFile regionfile = createOrLoadRegionFile(worldDir, chunkX, chunkZ);
			return regionfile.getChunkDataOutputStream(chunkX & 31, chunkZ & 31);
		}

		public static boolean chunkExists(File worldDir, int chunkX, int chunkZ)
		{
			RegionFile regionfile = getRegionFileIfExists(worldDir, chunkX, chunkZ);
			return regionfile != null ? regionfile.isChunkSaved(chunkX & 31, chunkZ & 31) : false;
		}
	}
	public static void clean() {
		INSTANCE = null;
	}
}
