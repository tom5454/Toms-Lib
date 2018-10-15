package com.tom.lib.api.tileentity;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.util.math.BlockPos;

public interface ICustomPacket {
	void writeToPacket(DataOutputStream buf) throws IOException;
	void readFromPacket(DataInputStream buf) throws IOException;
	default void readPacket(byte[] data){
		try {
			readFromPacket(new DataInputStream(new ByteArrayInputStream(data)));
		} catch (IOException e) {
		}
	}
	BlockPos getPos2();
}
