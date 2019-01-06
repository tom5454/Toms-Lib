package com.tom.lib.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collector;

public interface IValidationChecker {
	boolean isValid();
	public static <T extends IValidationChecker> void removeAllInvalid(Collection<T> list){
		list.removeIf(e -> !e.isValid());
	}
	@SuppressWarnings("unchecked")
	public static <T extends IValidationChecker, R, A> R removeAllInvalidCollectValid(Collection<T> list, Collector<T, A, R> collector){
		final Iterator<T> each = list.iterator();
		A obj = collector.supplier().get();
		while (each.hasNext()) {
			IValidationChecker ch = each.next();
			if (!ch.isValid()) {
				each.remove();
			}else{
				collector.accumulator().accept(obj, (T) ch);
			}
		}
		return collector.finisher().apply(obj);
	}
}
