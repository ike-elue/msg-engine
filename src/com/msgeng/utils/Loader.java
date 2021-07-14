package com.msgeng.utils;

import static org.lwjgl.opengl.ARBVertexArrayBGRA.GL_BGRA;
import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glDeleteVertexArrays;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12C.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL15C.glGenBuffers;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import org.lwjgl.BufferUtils;

import com.msgeng.render.ShaderProgram;

public class Loader {

	private static final int BYTES_PER_PIXEL = 4;

	private final static ArrayList<Integer> VAOS = new ArrayList<>();
	private final static ArrayList<Integer> VBOS = new ArrayList<>();
	private final static ArrayList<Integer> TEXTURES = new ArrayList<>();
	private final static HashMap<String, ShaderProgram> SHADERS = new HashMap<>();

	private static boolean finishedVAOLoading = true;
	private static int attribArrayCount = 0;

	public static String loadShaderResource(String fileName) throws Exception {
		String result;
		try (InputStream in = Utils.class.getResourceAsStream(fileName);
				Scanner scanner = new Scanner(in, java.nio.charset.StandardCharsets.UTF_8.name())) {
			result = scanner.useDelimiter("\\A").next();
		}
		return result;
	}

	public synchronized static int startVAOLoad(int[] indices) {
		if (!finishedVAOLoading)
			return -1;
		attribArrayCount = 0;
		int vaoId = createVAO();
		bindIndicesBuffer(indices);
		finishedVAOLoading = false;
		return vaoId;
	}

	public synchronized static RawModel finishVAOLoad(int vaoId, int[] indices, ShaderProgram sp) {
		if(finishedVAOLoading) return null;
		
		unbindVAO();
		SHADERS.put(sp.getAlias(), sp);
		finishedVAOLoading = true;
		return new RawModel(vaoId, indices.length, attribArrayCount, sp.getAlias());
	}

	public static void cleanUp() {
		VAOS.stream().forEach((vao) -> {
			glDeleteVertexArrays(vao);
		});
		VBOS.stream().forEach((vbo) -> {
			glDeleteBuffers(vbo);
		});
		TEXTURES.stream().forEach((texture) -> {
			glDeleteTextures(texture);
		});
		SHADERS.keySet().forEach((alias) -> {
			SHADERS.get(alias).cleanup();
		});
	}

	private synchronized static int createVAO() {
		int vaoID = glGenVertexArrays();
		VAOS.add(vaoID);
		glBindVertexArray(vaoID);
		return vaoID;
	}

	public synchronized static void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data) {
		if(finishedVAOLoading) return;
		attribArrayCount++;
		int vboID = glGenBuffers();
		VBOS.add(vboID);
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		glVertexAttribPointer(attributeNumber, coordinateSize, GL_FLOAT, false, 0, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	private synchronized static void unbindVAO() {
		glBindVertexArray(0);
	}

	private synchronized static void bindIndicesBuffer(int[] indices) {
		int vboID = glGenBuffers();
		VBOS.add(vboID);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer = storeDataInIntBuffer(indices);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
	}

	private synchronized static IntBuffer storeDataInIntBuffer(int[] data) {
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

	private synchronized static FloatBuffer storeDataInFloatBuffer(float[] data) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

	public synchronized static int loadTexture(int width, int height, int[] pixels) {

		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * BYTES_PER_PIXEL); // 4 for RGBA, 3 for RGB

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = pixels[y * width + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red component
				buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
				buffer.put((byte) (pixel & 0xFF)); // Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component.
															// Only for RGBA
			}
		}

		buffer.flip(); // NEVER FORGET THIS

		int textureID = glGenTextures(); // Generate texture ID
		glBindTexture(GL_TEXTURE_2D, textureID); // Bind texture ID

		// Setup wrap mode
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		// Setup texture scaling filtering
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		// Send pixel data to OpenGL
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

		// Return the texture ID so we can bind it later again
		return textureID;
	}

	/**
	 * Not sure if this works
	 * @param width
	 * @param height
	 * @param pixels
	 * @return
	 */
	public synchronized static int loadTextureFont(int width, int height, int[] pixels) {

		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * BYTES_PER_PIXEL); // 4 for RGBA, 3 for RGB

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixel = pixels[y * width + x];
				buffer.put((byte) (pixel & 0xFF)); // Red component
				buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green component
				buffer.put((byte) ((pixel >> 16) & 0xFF)); // Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component.
															// Only for RGBA
			}
		}

		buffer.flip(); // NEVER FORGET THIS

		int textureID = glGenTextures(); // Generate texture ID
		glBindTexture(GL_TEXTURE_2D, textureID); // Bind texture ID

		// Setup wrap mode
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

		// Setup texture scaling filtering
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		// Send pixel data to OpenGL
		glTexImage2D(GL_TEXTURE_2D, 0, GL_BGRA, width, height, 0, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, buffer);

		// Return the texture ID so we can bind it later again
		return textureID;
	}
	
	public static ShaderProgram getShaderProgram(String alias) {
		return SHADERS.get(alias);
	} 
}
