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

package kr.syeyoung.dungeonsguide.mod.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShaderManager {
    private static final Map<String, ShaderProgram> shaders = new HashMap<>();

    public static void onResourceReload() {
        for (ShaderProgram value : shaders.values()) {
            value.close();
        }
        Set<String> keySet = new HashSet<>(shaders.keySet());
        shaders.clear();
        for (String s : keySet) {
            loadShader(s);
        }
    }

    public static void unload() {
        for (ShaderProgram value : shaders.values()) {
            value.close();
        }
    }

    public static ShaderProgram getShader(String str) {
        if (shaders.containsKey(str)) return shaders.get(str);
        return loadShader(str);
    }

    private static ShaderProgram loadShader(String name) {
        int vertex = -1;
        String sourceVert = getShaderSource(name+".vert");
        String sourceFrag = getShaderSource(name + ".frag");
        if (sourceVert == null && sourceFrag == null) {
            shaders.put(name, null); // shortcut
            return null;
        }
        if (sourceVert != null) {
            vertex = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            System.out.println(sourceVert);
            GL20.glShaderSource(vertex, sourceVert);
            GL20.glCompileShader(vertex);

            if (GL20.glGetShaderi(vertex, 35713) == 0) {
                System.err.println(GL20.glGetShaderInfoLog(vertex, 100));
                GL20.glDeleteShader(vertex);
                return null;
            }
        }

        int fragment = -1;
        if (sourceFrag != null) {
            fragment = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            System.out.println(sourceFrag);
            GL20.glShaderSource(fragment, sourceFrag);
            GL20.glCompileShader(fragment);

            if (GL20.glGetShaderi(fragment, 35713) == 0) {
                System.err.println(GL20.glGetShaderInfoLog(fragment, 100));
                if (vertex != -1)
                    GL20.glDeleteShader(vertex);
                GL20.glDeleteShader(fragment);
                return null;
            }
        }


        int program = GL20.glCreateProgram();
        if (vertex != -1) GL20.glAttachShader(program, vertex);
        if (fragment != -1) GL20.glAttachShader(program, fragment);

        GL20.glLinkProgram(program);

        if (vertex != -1) GL20.glDeleteShader(vertex);
        if (fragment != -1) GL20.glDeleteShader(fragment);

        if (GL20.glGetProgrami(program, 35714) == 0) {
            System.err.println(GL20.glGetProgramInfoLog(program, 100));
            return null;
        }
        GL20.glValidateProgram(program);
        if (GL20.glGetProgrami(program, 35715) == 0) {
            System.err.println(GL20.glGetProgramInfoLog(program, 100));
            return null;
        }
        ShaderProgram shaderProgram = new ShaderProgram(name, program);
        shaders.put(name, shaderProgram);
        return shaderProgram;
    }

    private static String getShaderSource(String name) {
        ResourceLocation location = new ResourceLocation(
                "dungeonsguide:"+name
        );
        try (InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream()) {
            return IOUtils.toString(is);
        } catch (Exception e) {
        }
        return null;
    }
}
