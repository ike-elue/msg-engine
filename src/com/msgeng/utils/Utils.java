package com.msgeng.utils;

import java.io.InputStream;
import java.util.Scanner;

public class Utils {

	private volatile static int idCounter = 0;
	
    public static String loadResource(String fileName) throws Exception {
        String result;
        try (InputStream in = Utils.class.getResourceAsStream(fileName);
                Scanner scanner = new Scanner(in, java.nio.charset.StandardCharsets.UTF_8.name())) {
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }
    
    public static synchronized int getId() {
    	idCounter++;
    	return idCounter;
    }

}