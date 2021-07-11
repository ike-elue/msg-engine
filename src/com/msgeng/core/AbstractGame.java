package com.msgeng.core;

public abstract class AbstractGame {
	private final Core c;

	/**
	 * Constructor
	 * 
	 * @param title     title of game
	 * @param width     width of screen
	 * @param height    height of screen
	 * @param frameRate frame rate of screen
	 * @param isDebug
	 */
	public AbstractGame(String title, int width, int height, double frameRate, boolean isDebug) {
		c = new Core(title, width, height, frameRate, this, isDebug);
	}

	/**
	 * Initialize special features in game if any (None Yet)
	 */
	public abstract void init();

	/**
	 * Adds created Engines into game engine to be utilized in the actual game
	 */
	public abstract void initEngines();

	/**
	 * Starts the game functions
	 */
	public final void start() {
		c.start();
	}

	public void addEngine() {
		
	}

	/**
	 * @return returns Core
	 */
	public final Core getCore() {
		return c;
	}
}