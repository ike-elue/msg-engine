package com.msgeng.render;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VALIDATE_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glValidateProgram;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public class ShaderProgram {
	private final String alias;
	private final int programId;
    private final Map<String, Integer> uniforms;
	private int vertexShaderId;
	private int fragmentShaderId;

	public ShaderProgram(String alias) throws Exception {
		this.alias = alias;
		programId = glCreateProgram();
		if (programId == 0) {
			throw new Exception("Could not create Shader");
		}
		uniforms = new HashMap<>();	
	}

	public void createVertexShader(String shaderCode) throws Exception {
		vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
	}

	public void createFragmentShader(String shaderCode) throws Exception {
		fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
	}

	protected int createShader(String shaderCode, int shaderType) throws Exception {
		int shaderId = glCreateShader(shaderType);
		if (shaderId == 0) {
			throw new Exception("Error creating shader. Type: " + shaderType);
		}

		glShaderSource(shaderId, shaderCode);
		glCompileShader(shaderId);

		if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
			throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
		}

		glAttachShader(programId, shaderId);

		return shaderId;
	}

	protected final void createUniform(String uniformName) throws Exception {
		int uniformLocation = glGetUniformLocation(programId, uniformName);
		if (uniformLocation < 0) {
			throw new Exception("Could not find uniform:" + uniformName);
		}
		uniforms.put(uniformName, uniformLocation);
	}

	public void setUniform(String uniformName, Matrix4f value) {
		Integer uniformData = uniforms.get(uniformName);
		if (uniformData == null) {
			throw new RuntimeException("Uniform [" + uniformName + "] has nor been created");
		}
		// Check if float buffer has been created
		FloatBuffer fb = BufferUtils.createFloatBuffer(16);

		// Dump the matrix into a float buffer
		fb = value.get(fb);
		glUniformMatrix4fv(uniformData, false, fb);
	}

	public void setUniform(String uniformName, int value) {
		Integer uniformData = uniforms.get(uniformName);
		if (uniformData == null) {
			throw new RuntimeException("Uniform [" + uniformName + "] has nor been created");
		}
		glUniform1i(uniformData, value);
	}

	public void setUniform(String uniformName, float value) {
		Integer uniformData = uniforms.get(uniformName);
		if (uniformData == null) {
			throw new RuntimeException("Uniform [" + uniformName + "] has nor been created");
		}
		glUniform1f(uniformData, value);
	}

	public void setUniform(String uniformName, boolean value) {
		Integer uniformData = uniforms.get(uniformName);
		if (uniformData == null) {
			throw new RuntimeException("Uniform [" + uniformName + "] has nor been created");
		}
		int load = 0;
		if (value)
			load = 1;
		glUniform1f(uniformData, load);
	}

	public void setUniform(String uniformName, Vector3f value) {
		Integer uniformData = uniforms.get(uniformName);
		if (uniformData == null) {
			throw new RuntimeException("Uniform [" + uniformName + "] has nor been created");
		}
		glUniform3f(uniformData, value.x, value.y, value.z);
	}

	public void setUniform(String uniformName, Vector2f value) {
		Integer uniformData = uniforms.get(uniformName);
		if (uniformData == null) {
			throw new RuntimeException("Uniform [" + uniformName + "] has nor been created");
		}
		glUniform3f(uniformData, value.x, value.y, 1);
	}

	public void link() throws Exception {
		glLinkProgram(programId);
		if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
			throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
		}

		if (vertexShaderId != 0) {
			glDetachShader(programId, vertexShaderId);
		}
		if (fragmentShaderId != 0) {
			glDetachShader(programId, fragmentShaderId);
		}

		glValidateProgram(programId);
		if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
			System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
		}

	}

	public String getAlias() {
		return alias;
	}

	public void bind() {
		glUseProgram(programId);
	}

	public void unbind() {
		glUseProgram(0);
	}

	public void cleanup() {
		unbind();
		if (programId != 0) {
			glDeleteProgram(programId);
		}
	}
}
