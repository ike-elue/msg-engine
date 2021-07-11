package com.msgeng.core;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWVidMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 *
 * @author 17737
 */
public class Core implements Runnable {

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

    /**
     * Constructor
     *
     * @param title title of game
     * @param width width of screen
     * @param height height of screen
     * @param frameRate frame rate of screen
     * @param game reference to abstract game
     * @param isDebug sets mode of game engine
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
     * Initializes the default engines in the game engine
     */
    private void initEngines() {
        
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

    private void init() {
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
        try ( MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    @Override
    public void run() {
        System.out.println("This program is using LWJGL " + Version.getVersion() + ", and it's working?");

        fpsString = "";

        // First Initialization
        init();
        
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        glOrtho(0, width, 0, height, -1, 1);

        glMatrixMode(GL_MODELVIEW);

        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);

        
        // Rest of Initialization
        initEngines();
        game.init();
        game.initEngines();

        long initialTime = System.nanoTime();
        final double timeU = 1000000000 / frameRate;
        final double timeF = 1000000000 / frameRate;
        double deltaU = 0, deltaF = 0;
        int frames = 0, ticks = 0;
        long timer = System.currentTimeMillis();

        while (running && !glfwWindowShouldClose(window)) {

            long currentTime = System.nanoTime();
            deltaU += (currentTime - initialTime) / timeU;
            deltaF += (currentTime - initialTime) / timeF;
            initialTime = currentTime;

            if (deltaU >= 1) {
                // Poll for window events. The key callback above will only be
                // invoked during this call.
                glfwPollEvents();
                update(deltaU);
                ticks++;
                deltaU--;
            }

            if (deltaF >= 1) {
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
                render();
                glfwSwapBuffers(window); // swap the color buffers
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

        init();
        double TPS = 0;
        long lastTime = System.nanoTime();
        double ns = SECOND / (frameRate/2);
        double delta = 0;
        while (running && !glfwWindowShouldClose(window)) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while(delta >= 1) {
                threadLoop();
                TPS++;
                delta--;
            }
            if(System.currentTimeMillis() - timer > 1000) {
                finalTPS = TPS;
                timer += 1000;
                TPS = 0;
            }
        }
        stop();
    }

    /**
     * Updates the engines
     *
     * @param delta
     */
    private void update(double delta) {
        
    }
    
    private void render() {
        
    }
    
    private void stop() {
    	if (!running) {
            glfwSetWindowShouldClose(window, true);
        }

        dispose();
    }

    /**
     * Dispose any required resources
     */
    public void dispose() {
        
        
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