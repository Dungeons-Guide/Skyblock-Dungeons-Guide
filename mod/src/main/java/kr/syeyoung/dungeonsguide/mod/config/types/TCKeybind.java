/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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

package kr.syeyoung.dungeonsguide.mod.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.ParameterItem;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

public class TCKeybind implements FeatureTypeHandler<InputConstants.Key> {
    public static final TCKeybind INSTANCE = new TCKeybind();
    @Override
    public InputConstants.Key deserialize(JsonElement element) {
        // configs before 26.1 saved a bare lwjgl2 key code
        if (element.isJsonPrimitive()) return fromLegacy(element.getAsInt());

        JsonObject object = element.getAsJsonObject();
        int code = object.get("code").getAsInt();
        switch (object.get("type").getAsString()) {
            case "key": return InputConstants.Type.KEYSYM.getOrCreate(code);
            case "scancode": return InputConstants.Type.SCANCODE.getOrCreate(code);
            case "mouse": return InputConstants.Type.MOUSE.getOrCreate(code);
        }
        return InputConstants.UNKNOWN;
    }

    @Override
    public JsonElement serialize(InputConstants.Key element) {
        JsonObject object = new JsonObject();
        switch (element.getType()) {
            case KEYSYM: object.addProperty("type", "key"); break;
            case SCANCODE: object.addProperty("type", "scancode"); break;
            case MOUSE: object.addProperty("type", "mouse"); break;
        }
        object.addProperty("code", element.getValue());
        return object;
    }

    // lwjgl2: keyboard codes, -100 + button for mouse, typed char + 256 for keys lwjgl2 didn't know
    private static InputConstants.Key fromLegacy(int legacy) {
        if (legacy < 0) return InputConstants.Type.MOUSE.getOrCreate(legacy + 100);
        if (legacy > 256) {
            char c = Character.toUpperCase((char) (legacy - 256));
            if ((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')) return InputConstants.Type.KEYSYM.getOrCreate(c);
            return InputConstants.UNKNOWN;
        }
        int glfw = legacy < LEGACY_TO_GLFW.length ? LEGACY_TO_GLFW[legacy] : GLFW.GLFW_KEY_UNKNOWN;
        if (glfw == GLFW.GLFW_KEY_UNKNOWN) return InputConstants.UNKNOWN;
        return InputConstants.Type.KEYSYM.getOrCreate(glfw);
    }

    private static final int[] LEGACY_TO_GLFW = new int[256];
    static {
        java.util.Arrays.fill(LEGACY_TO_GLFW, GLFW.GLFW_KEY_UNKNOWN);
        int[][] pairs = {
                {1, GLFW.GLFW_KEY_ESCAPE},
                {2, GLFW.GLFW_KEY_1}, {3, GLFW.GLFW_KEY_2}, {4, GLFW.GLFW_KEY_3}, {5, GLFW.GLFW_KEY_4}, {6, GLFW.GLFW_KEY_5},
                {7, GLFW.GLFW_KEY_6}, {8, GLFW.GLFW_KEY_7}, {9, GLFW.GLFW_KEY_8}, {10, GLFW.GLFW_KEY_9}, {11, GLFW.GLFW_KEY_0},
                {12, GLFW.GLFW_KEY_MINUS}, {13, GLFW.GLFW_KEY_EQUAL}, {14, GLFW.GLFW_KEY_BACKSPACE}, {15, GLFW.GLFW_KEY_TAB},
                {16, GLFW.GLFW_KEY_Q}, {17, GLFW.GLFW_KEY_W}, {18, GLFW.GLFW_KEY_E}, {19, GLFW.GLFW_KEY_R}, {20, GLFW.GLFW_KEY_T},
                {21, GLFW.GLFW_KEY_Y}, {22, GLFW.GLFW_KEY_U}, {23, GLFW.GLFW_KEY_I}, {24, GLFW.GLFW_KEY_O}, {25, GLFW.GLFW_KEY_P},
                {26, GLFW.GLFW_KEY_LEFT_BRACKET}, {27, GLFW.GLFW_KEY_RIGHT_BRACKET}, {28, GLFW.GLFW_KEY_ENTER}, {29, GLFW.GLFW_KEY_LEFT_CONTROL},
                {30, GLFW.GLFW_KEY_A}, {31, GLFW.GLFW_KEY_S}, {32, GLFW.GLFW_KEY_D}, {33, GLFW.GLFW_KEY_F}, {34, GLFW.GLFW_KEY_G},
                {35, GLFW.GLFW_KEY_H}, {36, GLFW.GLFW_KEY_J}, {37, GLFW.GLFW_KEY_K}, {38, GLFW.GLFW_KEY_L},
                {39, GLFW.GLFW_KEY_SEMICOLON}, {40, GLFW.GLFW_KEY_APOSTROPHE}, {41, GLFW.GLFW_KEY_GRAVE_ACCENT}, {42, GLFW.GLFW_KEY_LEFT_SHIFT},
                {43, GLFW.GLFW_KEY_BACKSLASH},
                {44, GLFW.GLFW_KEY_Z}, {45, GLFW.GLFW_KEY_X}, {46, GLFW.GLFW_KEY_C}, {47, GLFW.GLFW_KEY_V}, {48, GLFW.GLFW_KEY_B},
                {49, GLFW.GLFW_KEY_N}, {50, GLFW.GLFW_KEY_M},
                {51, GLFW.GLFW_KEY_COMMA}, {52, GLFW.GLFW_KEY_PERIOD}, {53, GLFW.GLFW_KEY_SLASH}, {54, GLFW.GLFW_KEY_RIGHT_SHIFT},
                {55, GLFW.GLFW_KEY_KP_MULTIPLY}, {56, GLFW.GLFW_KEY_LEFT_ALT}, {57, GLFW.GLFW_KEY_SPACE}, {58, GLFW.GLFW_KEY_CAPS_LOCK},
                {59, GLFW.GLFW_KEY_F1}, {60, GLFW.GLFW_KEY_F2}, {61, GLFW.GLFW_KEY_F3}, {62, GLFW.GLFW_KEY_F4}, {63, GLFW.GLFW_KEY_F5},
                {64, GLFW.GLFW_KEY_F6}, {65, GLFW.GLFW_KEY_F7}, {66, GLFW.GLFW_KEY_F8}, {67, GLFW.GLFW_KEY_F9}, {68, GLFW.GLFW_KEY_F10},
                {69, GLFW.GLFW_KEY_NUM_LOCK}, {70, GLFW.GLFW_KEY_SCROLL_LOCK},
                {71, GLFW.GLFW_KEY_KP_7}, {72, GLFW.GLFW_KEY_KP_8}, {73, GLFW.GLFW_KEY_KP_9}, {74, GLFW.GLFW_KEY_KP_SUBTRACT},
                {75, GLFW.GLFW_KEY_KP_4}, {76, GLFW.GLFW_KEY_KP_5}, {77, GLFW.GLFW_KEY_KP_6}, {78, GLFW.GLFW_KEY_KP_ADD},
                {79, GLFW.GLFW_KEY_KP_1}, {80, GLFW.GLFW_KEY_KP_2}, {81, GLFW.GLFW_KEY_KP_3}, {82, GLFW.GLFW_KEY_KP_0}, {83, GLFW.GLFW_KEY_KP_DECIMAL},
                {87, GLFW.GLFW_KEY_F11}, {88, GLFW.GLFW_KEY_F12},
                {100, GLFW.GLFW_KEY_F13}, {101, GLFW.GLFW_KEY_F14}, {102, GLFW.GLFW_KEY_F15}, {103, GLFW.GLFW_KEY_F16},
                {104, GLFW.GLFW_KEY_F17}, {105, GLFW.GLFW_KEY_F18}, {113, GLFW.GLFW_KEY_F19},
                {141, GLFW.GLFW_KEY_KP_EQUAL}, {156, GLFW.GLFW_KEY_KP_ENTER}, {157, GLFW.GLFW_KEY_RIGHT_CONTROL},
                {181, GLFW.GLFW_KEY_KP_DIVIDE}, {183, GLFW.GLFW_KEY_PRINT_SCREEN}, {184, GLFW.GLFW_KEY_RIGHT_ALT},
                {197, GLFW.GLFW_KEY_PAUSE}, {199, GLFW.GLFW_KEY_HOME}, {200, GLFW.GLFW_KEY_UP}, {201, GLFW.GLFW_KEY_PAGE_UP},
                {203, GLFW.GLFW_KEY_LEFT}, {205, GLFW.GLFW_KEY_RIGHT}, {207, GLFW.GLFW_KEY_END}, {208, GLFW.GLFW_KEY_DOWN},
                {209, GLFW.GLFW_KEY_PAGE_DOWN}, {210, GLFW.GLFW_KEY_INSERT}, {211, GLFW.GLFW_KEY_DELETE},
                {219, GLFW.GLFW_KEY_LEFT_SUPER}, {220, GLFW.GLFW_KEY_RIGHT_SUPER}, {221, GLFW.GLFW_KEY_MENU},
        };
        for (int[] pair : pairs) LEGACY_TO_GLFW[pair[0]] = pair[1];
    }


    @Override
    public Widget createDefaultWidgetFor(FeatureParameter parameter) {
        ParameterItem parameterItem = new ParameterItem(parameter, new KeybindEditWidget(parameter));
        return parameterItem;
    }

    public static class KeybindEditWidget extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "key")
        public final BindableAttribute<String> value = new BindableAttribute<>(String.class);

        private InputConstants.Key currentKey = InputConstants.UNKNOWN;
        private boolean listen = false;
        private FeatureParameter<InputConstants.Key> parameter;
        public KeybindEditWidget(FeatureParameter<InputConstants.Key> featureParameter) {
            super(Identifier.parse("dungeonsguide:gui/config/parameter/keybind.gui"));
            this.parameter = featureParameter;
            currentKey = featureParameter.getValue();
            value.setValue(currentKey.getDisplayName().getString());
        }

        @On(functionName = "setKey")
        public void listen() {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            getDomElement().obtainFocus();
            value.setValue("> "+currentKey.getDisplayName().getString()+" <");
            listen =true;
        }

        @Override
        public boolean keyPressed(KeyEvent keyEvent) {
            if (!listen) return false;
            if (keyEvent.isEscape()) {
                currentKey = InputConstants.UNKNOWN;
            } else {
                // keys glfw has no key code for come back as scancodes
                currentKey = InputConstants.getKey(keyEvent);
            }
            listen = false;
            parameter.setValue(currentKey);
            value.setValue(currentKey.getDisplayName().getString());
            return true;
        }
    }
}
