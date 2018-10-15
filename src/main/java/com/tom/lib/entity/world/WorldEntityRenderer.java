package com.tom.lib.entity.world;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import com.tom.lib.client.dim.DimWorldRenderer;
import com.tom.lib.proxy.ClientProxy;

public class WorldEntityRenderer extends Render<EntityWorld> {
	private Minecraft mc = Minecraft.getMinecraft();
	public WorldEntityRenderer(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityWorld entity) {
		return null;
	}
	@Override
	public boolean shouldRender(EntityWorld livingEntity, ICamera camera, double camX, double camY, double camZ) {
		return true;
	}
	@Override
	public void doRender(EntityWorld entity, double x, double y, double z, float entityYaw, float partialTicks) {
		long id = entity.getDimID();
		BlockPos size = entity.getSize();
		mc.mcProfiler.func_194340_a(() -> prof(entity));
		DimWorldRenderer r = ClientProxy.dimRenderers.get(id + 1);
		if(r != null){
			r.ox = -entity.posX + size.getX();
			r.oy = -entity.posY + size.getY() + 325;
			r.oz = -entity.posZ + size.getZ();
			r.ry = entity.rotationYaw;
			r.rp = entity.rotationPitch;
			r.render(2, false);
		}
		mc.mcProfiler.endSection();
	}
	private static String prof(EntityWorld ent){
		BlockPos p = ent.getPosition();
		return "WER " + p.getX() + ", " + p.getY() + ", " + p.getZ();
	}
}
