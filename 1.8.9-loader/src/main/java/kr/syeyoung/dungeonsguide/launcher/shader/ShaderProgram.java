/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.launcher.shader;

import lombok.AllArgsConstructor;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix2f;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import java.nio.FloatBuffer;

@AllArgsConstructor
public class ShaderProgram {
    private String shaderName;
    private int shaderId;

    private void ensureInitialized() {
        if (shaderId == -1) throw new IllegalStateException("ShaderProgram not initialized");
    }

    public void useShader() {
        ensureInitialized();
        GL20.glUseProgram(shaderId);
    }

    public int getUniformLocation(String name) {
        ensureInitialized();
        return GL20.glGetUniformLocation(shaderId, name);
    }

    public void uploadUniform(String name, int val) {
        GL20.glUniform1i(getUniformLocation(name), val);
    }

    public void uploadUniform(String name, int v1, int v2) {
        GL20.glUniform2i(getUniformLocation(name), v1, v2);
    }

    public void uploadUniform(String name, int v1, int v2, int v3) {
        GL20.glUniform3i(getUniformLocation(name), v1, v2, v3);
    }

    public void uploadUniform(String name, int v1, int v2, int v3, int v4) {
        GL20.glUniform4i(getUniformLocation(name), v1, v2, v3, v4);
    }

    public void uploadUniform(String name, float val) {
        GL20.glUniform1f(getUniformLocation(name), val);
    }

    public void uploadUniform(String name, float v1, float v2) {
        GL20.glUniform2f(getUniformLocation(name), v1, v2);
    }

    public void uploadUniform(String name, float v1, float v2, float v3) {
        GL20.glUniform3f(getUniformLocation(name), v1, v2, v3);
    }

    public void uploadUniform(String name, float v1, float v2, float v3, float v4) {
        GL20.glUniform4f(getUniformLocation(name), v1, v2, v3, v4);
    }

    public void uploadUniform(String name, Matrix2f matrix2f) {
        FloatBuffer floatBuffer = FloatBuffer.allocate(4);
        matrix2f.store(floatBuffer);
        GL20.glUniformMatrix2(getUniformLocation(name), false, floatBuffer);
    }

    public void uploadUniform(String name, Matrix3f matrix3f) {
        FloatBuffer floatBuffer = FloatBuffer.allocate(9);
        floatBuffer.put(matrix3f.m00);
        floatBuffer.put(matrix3f.m01);
        floatBuffer.put(matrix3f.m02);
        floatBuffer.put(matrix3f.m10);
        floatBuffer.put(matrix3f.m11);
        floatBuffer.put(matrix3f.m12);
        floatBuffer.put(matrix3f.m20);
        floatBuffer.put(matrix3f.m21);
        floatBuffer.put(matrix3f.m22);
        GL20.glUniformMatrix3(getUniformLocation(name), false, floatBuffer);
    }

    public void uploadUniform(String name, Matrix4f matrix4f) {
        FloatBuffer floatBuffer = FloatBuffer.allocate(16);
        floatBuffer.put(matrix4f.m00);
        floatBuffer.put(matrix4f.m01);
        floatBuffer.put(matrix4f.m02);
        floatBuffer.put(matrix4f.m03);
        floatBuffer.put(matrix4f.m10);
        floatBuffer.put(matrix4f.m11);
        floatBuffer.put(matrix4f.m12);
        floatBuffer.put(matrix4f.m13);
        floatBuffer.put(matrix4f.m20);
        floatBuffer.put(matrix4f.m21);
        floatBuffer.put(matrix4f.m22);
        floatBuffer.put(matrix4f.m23);
        floatBuffer.put(matrix4f.m30);
        floatBuffer.put(matrix4f.m31);
        floatBuffer.put(matrix4f.m32);
        floatBuffer.put(matrix4f.m33);
        GL20.glUniformMatrix4(getUniformLocation(name), false, floatBuffer);
    }

    public void close() {
        if (shaderId == -1) return;
        GL20.glDeleteProgram(shaderId);
        shaderId = -1;
    }
}
