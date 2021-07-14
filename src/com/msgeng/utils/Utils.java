package com.msgeng.utils;

public class Utils {

    public static float clamp(float var, float min, float max) {	
    	return var < min ? min : (var > max ? max : var);
    }
    
}