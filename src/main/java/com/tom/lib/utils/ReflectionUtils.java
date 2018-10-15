package com.tom.lib.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

import org.apache.logging.log4j.Logger;

import com.tom.lib.LibInit;

public class ReflectionUtils {
	protected static Field modifiersField;
	static {
		try {
			modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
		} catch (Throwable e) {
			throw new RuntimeException("?!?!?", e);
		}
	}
	public static void setFinalField(Field field, Object ins, Object value) throws Exception {
		field.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		field.set(ins, value);
	}

	public static void trySetFinalField(Field field, Object ins, Object value, Logger log, String errorMsg) {
		try {
			setFinalField(field, ins, value);
		} catch (Throwable e) {
			log.error(errorMsg, e);
		}
	}

	@SuppressWarnings("rawtypes")
	public static void trySetFinalField(Class clazz, Predicate<Object> insChecker, Object ins, Object value, Logger log, String errorMsg) {
		try {
			for (Field f : clazz.getDeclaredFields()) {
				f.setAccessible(true);
				if (insChecker.test(f.get(ins))) {
					setFinalField(f, ins, value);
				}
			}
		} catch (Throwable e) {
			log.error(errorMsg, e);
		}
	}

	@SuppressWarnings("rawtypes")
	public static void setFinalField(Class clazz, Predicate<Object> insChecker, Object ins, Object value) throws Exception {
		for (Field f : clazz.getDeclaredFields()) {
			f.setAccessible(true);
			if (insChecker.test(f.get(ins))) {
				setFinalField(f, ins, value);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static void trySetFinalField(Class clazz, Class type, Object ins, Object value, Logger log, String errorMsg) {
		trySetFinalField(clazz, type::isInstance, ins, value, log, errorMsg);
	}

	@SuppressWarnings("rawtypes")
	public static void setFinalField(Class clazz, Class type, Object ins, Object value) throws Exception {
		setFinalField(clazz, type::isInstance, ins, value);
	}
	@SuppressWarnings("unchecked")
	public static <I, T> T getValueOrDef(String clazz, String field, I i, T def){
		try {
			Class<?> c = Class.forName(clazz);
			Field f = c.getDeclaredField(field);
			return (T) f.get(i);
		} catch (Exception e) {
			LibInit.log.error("Failed to get value: " + clazz + "." + field + ", using default value", e);
			return def;
		}
	}

	@SuppressWarnings("rawtypes")
	public static Object getValue(Class clazz, Predicate<Object> insChecker, Object ins) throws Exception {
		for (Field f : clazz.getDeclaredFields()) {
			f.setAccessible(true);
			if(ins == null || !Modifier.isStatic(f.getModifiers())){
				Object i = f.get(ins);
				if (insChecker.test(i)) {
					return i;
				}
			}
		}
		return null;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> T getValue(Class clazz, Class<T> type, Object ins) throws Exception {
		return (T) getValue(clazz, type::isInstance, ins);
	}

	@SuppressWarnings("rawtypes")
	public static void setField(Class clazz, Predicate<Object> insChecker, Object ins, Object value) throws Exception {
		for (Field f : clazz.getDeclaredFields()) {
			f.setAccessible(true);
			Object i = f.get(ins);
			if (insChecker.test(i)) {
				f.set(ins, value);
			}
		}
	}
	@SuppressWarnings({"rawtypes"})
	public static void setField(Class clazz, Class type, Object ins, Object value) throws Exception {
		for (Field f : clazz.getDeclaredFields()) {
			f.setAccessible(true);
			if (f.getType().isAssignableFrom(type)) {
				f.set(ins, value);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static <T1, T2 extends T1> void nullify(Class<T1> clazz, T2 ins, Logger log) {
		try {
			for (Field f : clazz.getDeclaredFields()) {
				if(!Modifier.isPublic(f.getModifiers()) && !Modifier.isStatic(f.getModifiers())){
					Class c = f.getType();
					if(c.isPrimitive())continue;
					setFinalField(f, ins, null);
				}
			}
		} catch (Throwable e) {
			log.error("Failed to set field value", e);
		}
	}
}
