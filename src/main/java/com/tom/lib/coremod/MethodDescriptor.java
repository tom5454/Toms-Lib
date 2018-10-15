package com.tom.lib.coremod;

public class MethodDescriptor {
	public final String ret;
	public final String[] args;
	public MethodDescriptor(String ret, String[] args) {
		this.ret = ret;
		this.args = args;
	}
	public MethodDescriptor(String toParse) {
		String in = toParse.substring(1);
		String[] sp = in.split(";");
		args = new String[sp.length-1];
		for (int i = 0;i < sp.length-1;i++) {
			String str = sp[i];
			args[i] = map(str.substring(1).replace('/', '.'));
		}
		ret = map(sp[sp.length-1].substring(1));
	}
	public static String map(String in){
		switch(in){
		case "V": return "void";
		default:
			return in;
		}
	}
}
