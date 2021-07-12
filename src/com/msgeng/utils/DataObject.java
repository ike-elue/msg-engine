package com.msgeng.utils;

public abstract class DataObject {
	public abstract <T> void set(T var, String name);
	public abstract <T> T get(String name);
}
