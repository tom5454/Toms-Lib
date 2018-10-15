package com.tom.lib.network.patches;

import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class SimpleAttribute<T> implements Attribute<T> {
	private T value;
	private final AttributeKey<T> key;

	public SimpleAttribute(AttributeKey<T> key) {
		this.key = key;
	}

	@Override
	public AttributeKey<T> key() {
		return key;
	}

	@Override
	public T get() {
		return value;
	}

	@Override
	public void set(T value) {
		this.value = value;
	}

	@Override
	public T getAndSet(T value) {
		T old = value;
		this.value = value;
		return old;
	}

	@Override
	public T setIfAbsent(T value) {
		if(this.value == null)this.value = value;
		return value;
	}

	@Override
	public T getAndRemove() {
		return getAndSet(null);
	}

	@Override
	public boolean compareAndSet(T oldValue, T newValue) {
		if(oldValue == value)this.value = newValue;
		return true;
	}

	@Override
	public void remove() {
		value = null;
	}

}
