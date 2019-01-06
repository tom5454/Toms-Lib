package com.tom.lib.api.tileentity;

import com.tom.lib.handler.WorldHandler;

public interface IOwnable {
	String getOwnerName();
	void updatePlayerHandler();
	void setOwner(String owner);
	default void tileOnLoad(){
		WorldHandler.addTask(() -> {
			updatePlayerHandler();
		}, null);
	}
}
