package com.tom.lib.debug;

import java.util.ConcurrentModificationException;

public class CMEDetector {
	private String thread;
	private StackTraceElement[] stacktrace;
	public void saveTrace(){
		thread = Thread.currentThread().getName();
		stacktrace = new Throwable().getStackTrace();
	}
	@Override
	public String toString() {
		if(thread != null && stacktrace != null){
			StringBuilder b = new StringBuilder();
			b.append('[');
			b.append(thread);
			b.append("] ");
			for (int i = 0;i < stacktrace.length;i++) {
				StackTraceElement stackTraceElement = stacktrace[i];
				b.append(stackTraceElement.toString());
				b.append('\n');
			}
			return b.toString();
		}
		return super.toString();
	}
	public ConcurrentModificationException toExeption() {
		return new ConcurrentModificationException(toString());
	}
}
