package com.tom.lib.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.tom.lib.api.item.IScroller;
import com.tom.lib.api.item.IScroller.ScrollDirection;
import com.tom.lib.network.LibNetworkHandler;
import com.tom.lib.network.messages.MessageScroll;

public class LibEventHandler {
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void handleMouseInputEvent(MouseEvent event) {
		if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isSneaking()) {
			if (event.getDwheel() != 0) {
				int w = event.getDwheel();
				ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
				if (stack.getItem() instanceof IScroller) {
					if (((IScroller) stack.getItem()).canScroll(stack)) {
						event.setCanceled(true);
						LibNetworkHandler.sendToServer(new MessageScroll(w > 0 ? ScrollDirection.UP : ScrollDirection.DOWN));
					}
				}
			}
		}
	}
}
