package com.tom.lib.thirdparty.computercraft;

import com.tom.lib.api.tileentity.ITMPeripheral;
import com.tom.lib.api.tileentity.ITMPeripheral.IComputer;
import com.tom.lib.api.tileentity.ITMPeripheral.ITMLuaObject;
import com.tom.lib.api.tileentity.ITMPeripheral.LuaInteruptedException;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.ILuaObject;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class CCComputer implements IComputer {
	private IComputerAccess c;
	private ILuaContext context;
	public CCComputer(IComputerAccess c, ILuaContext context) {
		this.c = c;
		this.context = context;
	}
	@Override
	public void queueEvent(String event, Object[] arguments) {
		c.queueEvent(event, arguments);
	}

	@Override
	public String getAttachmentName() {
		return c.getAttachmentName();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((c == null) ? 0 : c.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CCComputer other = (CCComputer) obj;
		if (c == null) {
			if (other.c != null)
				return false;
		} else if (!c.equals(other.c))
			return false;
		return true;
	}
	@Override
	public Object[] map(Object[] in) {
		if(in == null){
			return new Object[0];
		}
		for (int i = 0;i < in.length;i++) {
			Object object = in[i];
			if(object instanceof ITMLuaObject){
				in[i] = new TMLuaObject((ITMLuaObject) object);
			}
		}
		return in;
	}
	public class TMLuaObject implements ILuaObject {
		private ITMLuaObject obj;
		public TMLuaObject(ITMLuaObject obj) {
			this.obj = obj;
		}
		@Override
		public String[] getMethodNames() {
			return obj.getMethodNames();
		}

		@Override
		public Object[] callMethod(ILuaContext context, int method, Object[] args) throws LuaException, InterruptedException {
			try {
				return map(obj.call(CCComputer.this, obj.getMethodNames()[method], args));
			} catch (com.tom.lib.api.tileentity.ITMPeripheral.LuaException e) {
				throw e.toLuaException();
			}
		}
	}
	@Override
	public Object[] pullEvent(String string) throws com.tom.lib.api.tileentity.ITMPeripheral.LuaException {
		try {
			return context != null ? context.pullEvent(string) : new Object[0];
		} catch (dan200.computercraft.api.lua.LuaException e) {
			throw new ITMPeripheral.LuaException(e.getMessage(), e.getLevel());
		} catch (InterruptedException e){
			throw new LuaInteruptedException();
		}
	}
}
