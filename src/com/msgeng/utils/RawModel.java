package com.msgeng.utils;

public class RawModel {
	private final int vaoID;
	private final int vertexCount;
	private final int attribArrayCount;
	private final String shaderAlias;

	public RawModel(int vaoID, int vertexCount, int attribArrayCount, String shaderAlias) {
		this.vaoID = vaoID;
		this.vertexCount = vertexCount;
		this.attribArrayCount = attribArrayCount;
		this.shaderAlias = shaderAlias;
	}

	public int getVaoID() {
		return vaoID;
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public int getAttribArrayCount() {
		return attribArrayCount;
	}

	public String getShaderAlias() {
		return shaderAlias;
	}

	@Override
	public String toString() {
		return "RawModel's ShaderProgram = " + shaderAlias;
	}
}
