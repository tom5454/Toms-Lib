package com.tom.lib.thirdparty.oc;

import static com.tom.lib.utils.TomsUtils.get;

import java.lang.ref.WeakReference;

import com.tom.lib.api.tileentity.ITMPeripheral.IComputer;
import com.tom.lib.api.tileentity.ITMPeripheral.ITMLuaObject;

import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Node;

public class OCComputer implements IComputer {
	private WeakReference<Context> context;
	private WeakReference<Node> node;
	public OCComputer(Context context, Node node) {
		this.context = new WeakReference<>(context);
		this.node = new WeakReference<>(node);
	}

	@Override
	public void queueEvent(String event, Object[] arguments) {
		get(context).ifPresent(c -> c.signal(event, arguments));
	}

	@Override
	public String getAttachmentName() {
		return get(node).map(Node::address).orElse("missingno");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context.get() == null) ? 0 : context.get().hashCode());
		result = prime * result + ((node.get() == null) ? 0 : node.get().hashCode());
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
		OCComputer other = (OCComputer) obj;
		if (context.get() == null) {
			if (other.context.get() != null)
				return false;
		} else if (!context.get().equals(other.context.get()))
			return false;
		if (node.get() == null) {
			if (other.node.get() != null)
				return false;
		} else if (!node.get().equals(other.node.get()))
			return false;
		return true;
	}

	@Override
	public Object[] map(Object[] in) {
		for (int i = 0;i < in.length;i++) {
			Object object = in[i];
			if(object instanceof ITMLuaObject){
				long id = ((ITMLuaObject) object).getID();
				in[i] = id == -1 ? null : id;
			}
		}
		return in;
	}

	@Override
	public Object[] pullEvent(String string) {
		return new Object[0];
	}
}
