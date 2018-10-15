package com.tom.lib.utils;

public class Pair<K, V> {
	private K key;
	private V value;
	public K getKey() {
		return key;
	}
	public V getValue() {
		return value;
	}
	public void setKey(K key) {
		this.key = key;
	}
	public void setValue(V value) {
		this.value = value;
	}
	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}
	public static <K, V> Pair<K, V> of(K key, V value){
		return new Pair<>(key, value);
	}
	public static <K, V> UnmodifiablePair<K, V> ofUnmodifiable(K key, V value){
		return new UnmodifiablePair<>(key, value);
	}
	public UnmodifiablePair<K, V> toUnmodifiable(){
		return new UnmodifiablePair<>(key, value);
	}
	public static class UnmodifiablePair<K, V> extends Pair<K, V>{

		public UnmodifiablePair(K key, V value) {
			super(key, value);
		}
		@Override
		public void setKey(K key) {
			throw new UnsupportedOperationException("Cannot set the key of an UnmodifiablePair");
		}
		@Override
		public void setValue(V value) {
			throw new UnsupportedOperationException("Cannot set the value of an UnmodifiablePair");
		}
		@Override
		public UnmodifiablePair<K, V> toUnmodifiable() {
			return this;
		}
	}
	@Override
	public String toString() {
		return key + "=" + value;
	}
}
