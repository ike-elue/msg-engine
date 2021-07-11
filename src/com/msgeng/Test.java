package com.msgeng;

import com.msgeng.core.AbstractGame;

public class Test extends AbstractGame{
    public static void main(String[] args){
        Test t = new Test("Test", 640, 480, 60, true);
        t.start();
    }

    public Test(String title, int width, int height, double frameRate, boolean isDebug) {
		super(title, width, height, frameRate, isDebug);
    }

    
    @Override
    public void init() {
    	
    }

    @Override
    public void initEngines() {
    	
    }
}