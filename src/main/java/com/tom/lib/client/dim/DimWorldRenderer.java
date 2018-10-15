package com.tom.lib.client.dim;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;

import com.mojang.authlib.GameProfile;

import com.tom.lib.dim.DimensionHandler;
import com.tom.lib.network.patches.NetworkManagerPatchedClient;
import com.tom.lib.utils.ReflectionUtils;

public class DimWorldRenderer {
	private Minecraft mc;
	public EntityPlayerSP entity;
	private WorldClient world;
	private int frameCount;
	public double ox, oy, oz;
	public float ry, rp;
	private ResourceLocation locationLightMap;
	public NetHandler network;
	public NetworkManagerPatchedClient networkManager;
	private RenderGlobal renderer;
	private boolean cleanupRenderer;
	private final BlockPos size;
	public DimWorldRenderer(long rid, WorldClient world, RenderGlobal renderer, BlockPos size) {
		mc = Minecraft.getMinecraft();
		try {
			locationLightMap = ReflectionUtils.getValue(EntityRenderer.class, ResourceLocation.class, mc.entityRenderer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.size = size;
		networkManager = new NetworkManagerPatchedClient(rid);
		network = new NetHandler(mc, null, networkManager, mc.player.getGameProfile());
		this.renderer = renderer;
		entity = new EntityPlayerSP(mc, world, network, new StatisticsManager(), new RecipeBook());
		setWorld(world);
	}
	public DimWorldRenderer(WorldSettings worldSettings, int dim, long rid, BlockPos size) {
		mc = Minecraft.getMinecraft();
		try {
			locationLightMap = ReflectionUtils.getValue(EntityRenderer.class, ResourceLocation.class, mc.entityRenderer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.size = size;
		networkManager = new NetworkManagerPatchedClient(rid);
		network = new NetHandler(mc, null, networkManager, mc.player.getGameProfile());
		world = new WorldClient(network, worldSettings, dim, mc.gameSettings.difficulty, mc.mcProfiler);
		renderer = new RenderGlobal(mc);
		cleanupRenderer = true;
		renderer.setWorldAndLoadRenderers(world);
		entity = new EntityPlayerSP(mc, world, network, new StatisticsManager(), new RecipeBook());
		setWorld(world);
	}
	/*public void renderPrep(float tickTime){
		GameSettings settings = mc.gameSettings;
		RenderGlobal renderBackup = mc.renderGlobal;
		Entity entityBackup = mc.getRenderViewEntity();
		entity.lastTickPosX = entityBackup.lastTickPosX;
		entity.lastTickPosY = entityBackup.lastTickPosY;
		entity.lastTickPosZ = entityBackup.lastTickPosZ;
		entity.prevRotationYaw = entityBackup.prevRotationYaw % 360.0F;
		entity.prevRotationPitch = 360-entityBackup.prevRotationPitch % 360.0F;
		entity.setPositionAndRotation(entityBackup.posX, entityBackup.posY, entityBackup.posZ, entityBackup.rotationYaw% 360.0F, entityBackup.rotationPitch% 360.0F);
		boolean hideGuiBackup = settings.hideGUI;
		WorldClient worldBackup = mc.world;

		mc.renderGlobal = this;
		mc.setRenderViewEntity(entity);
		mc.world = world;
		settings.hideGUI = true;
		ScaledResolution res = new ScaledResolution(mc);
		if(id == null){
			id = GL11.glGenTextures();
			GlStateManager.bindTexture(id);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, res.getScaledWidth(), res.getScaledHeight(), 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, BufferUtils.createByteBuffer(3 * res.getScaledWidth() * res.getScaledHeight()));
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		}
		try {
			int fps = Math.max(30, settings.limitFramerate);
			EntityRenderer entityRenderer = mc.entityRenderer;
			entityRenderer.renderWorld(tickTime, renderEndNanoTime + (1000000000 / fps));
		} catch(Exception e){
			e.printStackTrace();
		}

		GlStateManager.bindTexture(id);
		GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 0, 0, res.getScaledWidth(), res.getScaledHeight(), 0);

		renderEndNanoTime = System.nanoTime();

		mc.renderGlobal = renderBackup;
		mc.setRenderViewEntity(entityBackup);
		mc.world = worldBackup;
		settings.hideGUI = hideGuiBackup;
	}
	public void render(){
		if(id != null){
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
			Tessellator t = Tessellator.getInstance();
			BufferBuilder b = t.getBuffer();
			b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			b.pos(0, 0, 0).tex(0, 0).endVertex();
			b.pos(0, 1, 0).tex(0, 1).endVertex();
			b.pos(1, 1, 0).tex(1, 1).endVertex();
			b.pos(1, 0, 0).tex(1, 0).endVertex();
			t.draw();
		}
	}*/
	private void setWorld(WorldClient world){
		this.world = world;
		try {
			ReflectionUtils.setField(NetHandlerPlayClient.class, WorldClient.class, network, world);
		} catch (Exception e) {
			e.printStackTrace();
		}
		entity.setWorld(world);
	}
	public void cleanup(){
		if(cleanupRenderer)renderer.setWorldAndLoadRenderers(null);
		renderer = null;
		entity = null;
		network = null;
		networkManager = null;
		//if(id != null)GL11.glDeleteTextures(id);
	}
	public void render(int pass, boolean debugView){
		mc.mcProfiler.startSection("dimWorldRenderer");
		mc.mcProfiler.startSection("setup");
		GameSettings settings = mc.gameSettings;
		RenderGlobal renderBackup = mc.renderGlobal;
		Entity entityBackup = mc.getRenderViewEntity();
		BlockPos off = DimensionHandler.getRegionCenter(entityBackup.getPosition(), size, true);
		entity.lastTickPosX = entityBackup.lastTickPosX + ox + off.getX();
		entity.lastTickPosY = entityBackup.lastTickPosY+2 - off.getY() + oy;
		entity.lastTickPosZ = entityBackup.lastTickPosZ + oz + off.getZ();
		entity.prevRotationYaw = (entityBackup.prevRotationYaw + ry) % 360.0F;
		entity.prevRotationPitch = (360-entityBackup.prevRotationPitch + rp) % 360.0F;
		entity.setPositionAndRotation(entityBackup.posX + ox + off.getX(), entityBackup.posY+2 - off.getY() + oy, entityBackup.posZ + oz + off.getZ(), (entityBackup.rotationYaw + ry) % 360.0F, (entityBackup.rotationPitch + rp) % 360.0F);
		boolean hideGuiBackup = settings.hideGUI;
		WorldClient worldBackup = mc.world;

		mc.renderGlobal = renderer;
		mc.setRenderViewEntity(entity);
		mc.world = world;
		settings.hideGUI = true;

		int i2 = this.mc.gameSettings.limitFramerate;
		int j = Math.min(Minecraft.getDebugFPS(), i2);
		j = Math.max(j, 60);
		long k = 100000;
		long l = Math.max(1000000000 / j / 4 - k, 0L) / 2;

		mc.mcProfiler.endStartSection("render");
		GL11.glPushMatrix();
		renderWorld(pass, System.nanoTime() + l, debugView);
		GL11.glPopMatrix();

		mc.mcProfiler.endStartSection("restore");
		mc.renderGlobal = renderBackup;
		mc.setRenderViewEntity(entityBackup);
		mc.world = worldBackup;
		settings.hideGUI = hideGuiBackup;
		float partialTicks = mc.getRenderPartialTicks();
		Entity entity = this.mc.getRenderViewEntity();
		double d3 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
		double d4 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
		double d5 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
		TileEntityRendererDispatcher.staticPlayerX = d3;
		TileEntityRendererDispatcher.staticPlayerY = d4;
		TileEntityRendererDispatcher.staticPlayerZ = d5;
		mc.getRenderManager().setRenderPosition(d3, d4, d5);
		TileEntityRendererDispatcher.instance.prepare(worldBackup, mc.renderEngine, mc.fontRenderer, entityBackup, mc.objectMouseOver, mc.getRenderPartialTicks());
		mc.mcProfiler.endSection();
		mc.mcProfiler.endSection();
	}
	public void renderWorld(int pass, long finishTimeNano, boolean debugView){
		float partialTicks = mc.getRenderPartialTicks();
		RenderGlobal renderglobal = this.mc.renderGlobal;
		ParticleManager particlemanager = this.mc.effectRenderer;
		boolean flag = this.isDrawBlockOutline();
		ICamera icamera = new Frustum();
		Entity entity = this.mc.getRenderViewEntity();
		double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
		double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
		double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
		icamera.setPosition(d0, d1, d2);
		mc.getRenderManager().setRenderPosition(d0, d1, d2);
		GlStateManager.shadeModel(7425);
		this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.disableStandardItemLighting();
		this.mc.mcProfiler.startSection("terrain_setup");
		renderglobal.setupTerrain(entity, partialTicks, icamera, this.frameCount++, this.mc.player.isSpectator());

		if (pass == 0 || pass == 2)
		{
			this.mc.mcProfiler.endStartSection("updatechunks");
			this.mc.renderGlobal.updateChunks(finishTimeNano);
		}

		this.mc.mcProfiler.endStartSection("terrain");
		GlStateManager.matrixMode(5888);
		GlStateManager.pushMatrix();
		GlStateManager.disableAlpha();
		renderglobal.renderBlockLayer(BlockRenderLayer.SOLID, partialTicks, pass, entity);
		GlStateManager.enableAlpha();
		renderglobal.renderBlockLayer(BlockRenderLayer.CUTOUT_MIPPED, partialTicks, pass, entity);
		this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		renderglobal.renderBlockLayer(BlockRenderLayer.CUTOUT, partialTicks, pass, entity);
		this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		GlStateManager.shadeModel(7424);
		GlStateManager.alphaFunc(516, 0.1F);

		if (!debugView)
		{
			GlStateManager.matrixMode(5888);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			RenderHelper.enableStandardItemLighting();
			this.mc.mcProfiler.endStartSection("entities");
			net.minecraftforge.client.ForgeHooksClient.setRenderPass(0);
			renderglobal.renderEntities(entity, icamera, partialTicks);
			net.minecraftforge.client.ForgeHooksClient.setRenderPass(0);
			RenderHelper.disableStandardItemLighting();
			this.disableLightmap();
		}

		GlStateManager.matrixMode(5888);
		GlStateManager.popMatrix();

		if (flag && this.mc.objectMouseOver != null && !entity.isInsideOfMaterial(Material.WATER))
		{
			EntityPlayer entityplayer = (EntityPlayer)entity;
			GlStateManager.disableAlpha();
			this.mc.mcProfiler.endStartSection("outline");
			if (!net.minecraftforge.client.ForgeHooksClient.onDrawBlockHighlight(renderglobal, entityplayer, mc.objectMouseOver, 0, partialTicks))
				renderglobal.drawSelectionBox(entityplayer, this.mc.objectMouseOver, 0, partialTicks);
			GlStateManager.enableAlpha();
		}

		if (this.mc.debugRenderer.shouldRender())
		{
			this.mc.debugRenderer.renderDebug(partialTicks, finishTimeNano);
		}

		this.mc.mcProfiler.endStartSection("destroyProgress");
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
		renderglobal.drawBlockDamageTexture(Tessellator.getInstance(), Tessellator.getInstance().getBuffer(), entity, partialTicks);
		this.mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		GlStateManager.disableBlend();

		if (!debugView)
		{
			this.enableLightmap();
			this.mc.mcProfiler.endStartSection("litParticles");
			particlemanager.renderLitParticles(entity, partialTicks);
			RenderHelper.disableStandardItemLighting();
			//this.setupFog(0, partialTicks);
			this.mc.mcProfiler.endStartSection("particles");
			particlemanager.renderParticles(entity, partialTicks);
			this.disableLightmap();
		}

		GlStateManager.depthMask(false);
		GlStateManager.enableCull();
		this.mc.mcProfiler.endStartSection("weather");
		//this.renderRainSnow(partialTicks);
		GlStateManager.depthMask(true);
		renderglobal.renderWorldBorder(entity, partialTicks);
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.alphaFunc(516, 0.1F);
		//this.setupFog(0, partialTicks);
		GlStateManager.enableBlend();
		GlStateManager.depthMask(false);
		this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.shadeModel(7425);
		this.mc.mcProfiler.endStartSection("translucent");
		renderglobal.renderBlockLayer(BlockRenderLayer.TRANSLUCENT, partialTicks, pass, entity);
		if (!debugView) //Only render if render pass 0 happens as well.
		{
			RenderHelper.enableStandardItemLighting();
			this.mc.mcProfiler.endStartSection("entities");
			net.minecraftforge.client.ForgeHooksClient.setRenderPass(1);
			renderglobal.renderEntities(entity, icamera, partialTicks);
			// restore blending function changed by RenderGlobal.preRenderDamagedBlocks
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			net.minecraftforge.client.ForgeHooksClient.setRenderPass(-1);
			RenderHelper.disableStandardItemLighting();
		}
		GlStateManager.shadeModel(7424);
		GlStateManager.depthMask(true);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.disableFog();

		if (entity.posY + entity.getEyeHeight() >= 128.0D)
		{
			this.mc.mcProfiler.endStartSection("aboveClouds");
			//this.renderCloudsCheck(renderglobal, partialTicks, pass, d0, d1, d2);
		}

		this.mc.mcProfiler.endStartSection("forge_render_last");
		net.minecraftforge.client.ForgeHooksClient.dispatchRenderLast(renderglobal, partialTicks);
		this.mc.mcProfiler.endSection();
	}
	public void disableLightmap()
	{
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	public void enableLightmap()
	{
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.matrixMode(5890);
		GlStateManager.loadIdentity();
		//float f = 0.00390625F;
		GlStateManager.scale(0.00390625F, 0.00390625F, 0.00390625F);
		GlStateManager.translate(8.0F, 8.0F, 8.0F);
		GlStateManager.matrixMode(5888);
		this.mc.getTextureManager().bindTexture(this.locationLightMap);
		GlStateManager.glTexParameteri(3553, 10241, 9729);
		GlStateManager.glTexParameteri(3553, 10240, 9729);
		GlStateManager.glTexParameteri(3553, 10242, 10496);
		GlStateManager.glTexParameteri(3553, 10243, 10496);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}
	private boolean isDrawBlockOutline()
	{
		Entity entity = this.mc.getRenderViewEntity();
		boolean flag = entity instanceof EntityPlayer && !this.mc.gameSettings.hideGUI;

		if (flag && !((EntityPlayer)entity).capabilities.allowEdit)
		{
			ItemStack itemstack = ((EntityPlayer)entity).getHeldItemMainhand();

			if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK)
			{
				BlockPos blockpos = this.mc.objectMouseOver.getBlockPos();
				Block block = this.mc.world.getBlockState(blockpos).getBlock();

				if (this.mc.playerController.getCurrentGameType() == GameType.SPECTATOR)
				{
					flag = block.hasTileEntity(this.mc.world.getBlockState(blockpos)) && this.mc.world.getTileEntity(blockpos) instanceof IInventory;
				}
				else
				{
					flag = !itemstack.isEmpty() && (itemstack.canDestroy(block) || itemstack.canPlaceOn(block));
				}
			}
		}

		return flag;
	}
	public WorldClient getWorld() {
		return world;
	}
	public RenderGlobal getRenderer() {
		return renderer;
	}
	public class NetHandler extends NetHandlerPlayClient {
		public NetHandler(Minecraft mcIn, GuiScreen p_i46300_2_, NetworkManager networkManagerIn, GameProfile profileIn) {
			super(mcIn, p_i46300_2_, networkManagerIn, profileIn);
		}
		@Override
		public void handleUpdateTileEntity(SPacketUpdateTileEntity packetIn) {
			PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, mc);

			if (world.isBlockLoaded(packetIn.getPos()))
			{
				TileEntity tileentity = world.getTileEntity(packetIn.getPos());
				int i = packetIn.getTileEntityType();
				boolean flag = i == 2 && tileentity instanceof TileEntityCommandBlock;

				if (i == 1 && tileentity instanceof TileEntityMobSpawner || flag || i == 3 && tileentity instanceof TileEntityBeacon || i == 4 && tileentity instanceof TileEntitySkull || i == 5 && tileentity instanceof TileEntityFlowerPot || i == 6 && tileentity instanceof TileEntityBanner || i == 7 && tileentity instanceof TileEntityStructure || i == 8 && tileentity instanceof TileEntityEndGateway || i == 9 && tileentity instanceof TileEntitySign || i == 10 && tileentity instanceof TileEntityShulkerBox || i == 11 && tileentity instanceof TileEntityBed)
				{
					tileentity.readFromNBT(packetIn.getNbtCompound());
				}
				else
				{
					if(tileentity == null)
					{
						DimensionHandler.log.error("[NetHandlerPlayClient.handleUpdateTileEntity]: Received invalid update packet for null tile entity at {} with data: {}", packetIn.getPos(), packetIn.getNbtCompound());
						return;
					}
					tileentity.onDataPacket(networkManager, packetIn);
				}
			}
		}
		@Override
		public void handleEffect(SPacketEffect packetIn) {
			PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, mc);

			if (packetIn.isSoundServerwide())
			{
				world.playBroadcastSound(packetIn.getSoundType(), packetIn.getSoundPos(), packetIn.getSoundData());
			}
			else
			{
				world.playEvent(packetIn.getSoundType(), packetIn.getSoundPos(), packetIn.getSoundData());
			}
		}
		/**
		 * Triggers Block.onBlockEventReceived, which is implemented in BlockPistonBase for extension/retraction, BlockNote
		 * for setting the instrument (including audiovisual feedback) and in BlockContainer to set the number of players
		 * accessing a (Ender)Chest
		 */
		@Override
		public void handleBlockAction(SPacketBlockAction packetIn)
		{
			PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, mc);
			world.addBlockEvent(packetIn.getBlockPosition(), packetIn.getBlockType(), packetIn.getData1(), packetIn.getData2());
		}

		/**
		 * Updates all registered IWorldAccess instances with destroyBlockInWorldPartially
		 */
		@Override
		public void handleBlockBreakAnim(SPacketBlockBreakAnim packetIn)
		{
			PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, mc);
			world.sendBlockBreakProgress(packetIn.getBreakerId(), packetIn.getPosition(), packetIn.getProgress());
		}
		/**
		 * Initiates a new explosion (sound, particles, drop spawn) for the affected blocks indicated by the packet.
		 */
		@Override
		public void handleExplosion(SPacketExplosion packetIn)
		{
			PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, mc);
			Explosion explosion = new Explosion(world, (Entity)null, packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getStrength(), packetIn.getAffectedBlockPositions());
			explosion.doExplosionB(true);
			mc.player.motionX += packetIn.getMotionX();
			mc.player.motionY += packetIn.getMotionY();
			mc.player.motionZ += packetIn.getMotionZ();
		}
		@Override
		public void handleTimeUpdate(SPacketTimeUpdate packetIn)
		{
			PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, mc);
			world.setTotalWorldTime(packetIn.getTotalWorldTime());
			world.setWorldTime(packetIn.getWorldTime());
		}

		@Override
		public void handleSpawnPosition(SPacketSpawnPosition packetIn)
		{
			PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, mc);
			//mc.player.setSpawnPoint(packetIn.getSpawnPos(), true);
			world.getWorldInfo().setSpawn(packetIn.getSpawnPos());
		}
		/**
		 * Handles the creation of a nearby player entity, sets the position and held item
		 */
		@Override
		public void handleSpawnPlayer(SPacketSpawnPlayer packetIn)
		{
			PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, mc);
			double d0 = packetIn.getX();
			double d1 = packetIn.getY();
			double d2 = packetIn.getZ();
			float f = packetIn.getYaw() * 360 / 256.0F;
			float f1 = packetIn.getPitch() * 360 / 256.0F;
			NetworkPlayerInfo info = this.getPlayerInfo(packetIn.getUniqueId());
			if(info != null){
				EntityOtherPlayerMP entityotherplayermp = new EntityOtherPlayerMP(world, info.getGameProfile());
				entityotherplayermp.prevPosX = d0;
				entityotherplayermp.lastTickPosX = d0;
				entityotherplayermp.prevPosY = d1;
				entityotherplayermp.lastTickPosY = d1;
				entityotherplayermp.prevPosZ = d2;
				entityotherplayermp.lastTickPosZ = d2;
				EntityTracker.updateServerPosition(entityotherplayermp, d0, d1, d2);
				entityotherplayermp.setPositionAndRotation(d0, d1, d2, f, f1);
				world.addEntityToWorld(packetIn.getEntityID(), entityotherplayermp);
				List < EntityDataManager.DataEntry<? >> list = packetIn.getDataManagerEntries();

				if (list != null)
				{
					entityotherplayermp.getDataManager().setEntryValues(list);
				}
			}
		}
		/**
		 * Spawns the mob entity at the specified location, with the specified rotation, momentum and type. Updates the
		 * entities Datawatchers with the entity metadata specified in the packet
		 */
		@Override
		public void handleSpawnMob(SPacketSpawnMob packetIn)
		{
			PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, mc);
			double d0 = packetIn.getX();
			double d1 = packetIn.getY();
			double d2 = packetIn.getZ();
			float f = packetIn.getYaw() * 360 / 256.0F;
			float f1 = packetIn.getPitch() * 360 / 256.0F;
			EntityLivingBase entitylivingbase = (EntityLivingBase)EntityList.createEntityByID(packetIn.getEntityType(), world);

			if (entitylivingbase != null)
			{
				EntityTracker.updateServerPosition(entitylivingbase, d0, d1, d2);
				entitylivingbase.renderYawOffset = packetIn.getHeadPitch() * 360 / 256.0F;
				entitylivingbase.rotationYawHead = packetIn.getHeadPitch() * 360 / 256.0F;
				Entity[] aentity = entitylivingbase.getParts();

				if (aentity != null)
				{
					int i = packetIn.getEntityID() - entitylivingbase.getEntityId();

					for (Entity entity : aentity)
					{
						entity.setEntityId(entity.getEntityId() + i);
					}
				}

				entitylivingbase.setEntityId(packetIn.getEntityID());
				entitylivingbase.setUniqueId(packetIn.getUniqueId());
				entitylivingbase.setPositionAndRotation(d0, d1, d2, f, f1);
				entitylivingbase.motionX = packetIn.getVelocityX() / 8000.0F;
				entitylivingbase.motionY = packetIn.getVelocityY() / 8000.0F;
				entitylivingbase.motionZ = packetIn.getVelocityZ() / 8000.0F;
				world.addEntityToWorld(packetIn.getEntityID(), entitylivingbase);
				List < EntityDataManager.DataEntry<? >> list = packetIn.getDataManagerEntries();

				if (list != null)
				{
					entitylivingbase.getDataManager().setEntryValues(list);
				}
			}
			else
			{
				DimensionHandler.log.warn("[NetHandlerPlayClient.handleSpawnMob]: Skipping Entity with id {}", packetIn.getEntityType());
			}
		}
		@Override
		public void handleServerDifficulty(SPacketServerDifficulty packetIn)
		{
			PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, mc);
			world.getWorldInfo().setDifficulty(packetIn.getDifficulty());
			world.getWorldInfo().setDifficultyLocked(packetIn.isDifficultyLocked());
		}
		@Override
		public void handleSoundEffect(SPacketSoundEffect packetIn)
		{
			PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, mc);
			world.playSound(mc.player, packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getSound(), packetIn.getCategory(), packetIn.getVolume(), packetIn.getPitch());
		}
	}
}
