package com.tom.lib.utils;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class RenderUtil {
	public static double zDepth = 0.0D;
	public static void setColourWithAlphaPercent(int colour, int alphaPercent) {
		setColour(((((alphaPercent * 0xff) / 100) & 0xff) << 24) | (colour & 0xffffff));
	}

	public static void setColour(int colour) {

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(((colour >> 16) & 0xff) / 255.0f, ((colour >> 8) & 0xff) / 255.0f, ((colour) & 0xff) / 255.0f, ((colour >> 24) & 0xff) / 255.0f);
		GlStateManager.disableBlend();
	}

	public static void resetColour() {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}
	public static void drawRect(double x, double y, double w, double h) {
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();
		vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		vertexbuffer.pos(x + w, y, zDepth).endVertex();
		vertexbuffer.pos(x, y, zDepth).endVertex();
		vertexbuffer.pos(x, y + h, zDepth).endVertex();
		vertexbuffer.pos(x + w, y + h, zDepth).endVertex();
		// renderer.finishDrawing();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
	public static void drawTexturedRect(double x, double y, double w, double h) {
		drawTexturedRect(x, y, w, h, 0.0D, 0.0D, 1.0D, 1.0D);
	}

	// draw rectangle with texture stretched to fill the shape with UV
	public static void drawTexturedRect(double x, double y, double w, double h, double u, double v) {
		drawTexturedRect(x, y, w, h, u, v, u + (w / 256D), v + (h / 256D));
	}

	// draw rectangle with texture UV coordinates specified (so only part of the
	// texture fills the rectangle).
	public static void drawTexturedRect(double x, double y, double w, double h, double u1, double v1, double u2, double v2) {
		try {
			GlStateManager.enableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexbuffer = tessellator.getBuffer();
			vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			;
			vertexbuffer.pos(x + w, y, zDepth).tex(u2, v1).endVertex();
			vertexbuffer.pos(x, y, zDepth).tex(u1, v1).endVertex();
			vertexbuffer.pos(x, y + h, zDepth).tex(u1, v2).endVertex();
			vertexbuffer.pos(x + w, y + h, zDepth).tex(u2, v2).endVertex();
			// renderer.finishDrawing();
			tessellator.draw();
			GlStateManager.disableBlend();
		} catch (NullPointerException e) {
		}
	}
}
