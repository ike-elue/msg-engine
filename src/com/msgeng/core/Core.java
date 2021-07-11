package com.msgeng.core;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.msgeng.render.ShaderProgram;
import com.msgeng.utils.Utils;

/**
 *
 * @author 17737
 */
public class Core implements Runnable {

	public final double SECOND = 1000000000;

	// Thread Main
	private volatile Thread thread;
	public volatile boolean running;
	private final double frameRate;
	private String fpsString;
	private final boolean isDebug;
	private final int threadCount;

	private long window;
	private final String title;
	public final int width, height;

	// Classes
	private final AbstractGame game;

	// ShaderProgram
	private ShaderProgram shaderProgram;

    private int vboId;

    private int vaoId;

	/**
	 * Constructor
	 *
	 * @param title     title of game
	 * @param width     width of screen
	 * @param height    height of screen
	 * @param frameRate frame rate of screen
	 * @param game      reference to abstract game
	 * @param isDebug   sets mode of game engine
	 */
	public Core(String title, int width, int height, double frameRate, AbstractGame game, boolean isDebug) {
		this.frameRate = frameRate;
		threadCount = Runtime.getRuntime().availableProcessors();
		this.title = title;
		this.width = width;
		this.height = height;
		this.game = game;
		this.isDebug = isDebug;
	}

	/**
	 * Starts the main thread of the game
	 */
	public void start() {
		if (thread == null) {
			thread = new Thread(this, "Main Thread");
			running = true;
			thread.start();
		}
	}

	private void initWindow() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		// Configure GLFW
		glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will not be resizable

		// Create the window
		window = glfwCreateWindow(width, height, title, NULL, NULL);
		if (window == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}

		// Get the thread stack and push a new frame
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}

	/**
	 * Initializes the default engines in the game engine
	 * @throws Exception 
	 */
	private void initEngines() throws Exception {
		shaderProgram = new ShaderProgram();
		shaderProgram.createVertexShader(Utils.loadResource("/vertex.vs"));
		shaderProgram.createFragmentShader(Utils.loadResource("/fragment.fs"));
		shaderProgram.link();
		
		 float[] vertices = new float[]{
	                0.0f, 0.5f, 0.0f,
	                -0.5f, -0.5f, 0.0f,
	                0.5f, -0.5f, 0.0f
	        };

	        FloatBuffer verticesBuffer = null;
	        try {
	            verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
	            verticesBuffer.put(vertices).flip();

	            // Create the VAO and bind to it
	            vaoId = glGenVertexArrays();
	            glBindVertexArray(vaoId);

	            // Create the VBO and bint to it
	            vboId = glGenBuffers();
	            glBindBuffer(GL_ARRAY_BUFFER, vboId);
	            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
	            // Enable location 0
	            glEnableVertexAttribArray(0);
	            // Define structure of the data
	            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

	            // Unbind the VBO
	            glBindBuffer(GL_ARRAY_BUFFER, 0);

	            // Unbind the VAO
	            glBindVertexArray(0);
	        } finally {
	            if (verticesBuffer != null) {
	                MemoryUtil.memFree(verticesBuffer);
	            }
	        }
	}

	@Override
	public void run() {
		System.out.println("This program is using LWJGL " + Version.getVersion() + ", and it's working?");

		fpsString = "";

		initWindow();

		GL.createCapabilities();

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();

		glOrtho(0, width, 0, height, -1, 1);

		glMatrixMode(GL_MODELVIEW);

		glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		glEnable(GL_DEPTH_TEST);

		// Rest of Initialization
		try {
			initEngines();
		} catch (Exception e) {
			e.printStackTrace();
		}
		game.init();
		game.initEngines();

		long initialTime = System.nanoTime();
		final double timeU = SECOND / frameRate;
		final double timeF = SECOND / frameRate;
		double deltaU = 0, deltaF = 0;
		int frames = 0, ticks = 0;
		long timer = System.currentTimeMillis();

		while (running && !glfwWindowShouldClose(window)) {

			long currentTime = System.nanoTime();
			deltaU += (currentTime - initialTime) / timeU;
			deltaF += (currentTime - initialTime) / timeF;
			initialTime = currentTime;

			if (deltaU >= 1) {
				update(deltaU);
				ticks++;
				deltaU--;
			}

			if (deltaF >= 1) {
				render();
				frames++;
				deltaF--;
			}

			if (System.currentTimeMillis() - timer > 1000) {
				fpsString = String.format("UPS: %s FPS: %s", ticks, frames);
				if (isDebug) {
					System.out.println(fpsString);
				}
				frames = 0;
				ticks = 0;
				timer += 1000;
			}
		}

		if (!running) {
			glfwSetWindowShouldClose(window, true);
		}

		dispose();
	}

	/**
	 * Updates the engines
	 *
	 * @param delta
	 */
	private void update(double delta) {
		glfwPollEvents();
		// update code (can be multithreaded)
	}

	private void render() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		// render code (can't be multithreaded)
		exampleRender();
		glClearColor(0f, 0f, 0f, 0f); // Background colour
		glfwSwapBuffers(window);
	}

	private void exampleRender() {

	    shaderProgram.bind();

	    // Bind to the VAO
	    glBindVertexArray(vaoId);
	    glEnableVertexAttribArray(0);

	    // Draw the vertices
	    glDrawArrays(GL_TRIANGLES, 0, 3);

	    // Restore state
	    glDisableVertexAttribArray(0);
	    glBindVertexArray(0);

	    shaderProgram.unbind();
	}
	
	/**
	 * Dispose any required resources
	 */
	public void dispose() {

		if (shaderProgram != null) {
	        shaderProgram.cleanup();
	    }

	    glDisableVertexAttribArray(0);

	    // Delete the VBO
	    glBindBuffer(GL_ARRAY_BUFFER, 0);
	    glDeleteBuffers(vboId);

	    // Delete the VAO
	    glBindVertexArray(0);
	    glDeleteVertexArrays(vaoId);
		
		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();

		System.out.println("Cleaned Up!");
		System.exit(0);
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public final String getFPSString() {
		return fpsString;
	}

	public final long getWindow() {
		return window;
	}
}