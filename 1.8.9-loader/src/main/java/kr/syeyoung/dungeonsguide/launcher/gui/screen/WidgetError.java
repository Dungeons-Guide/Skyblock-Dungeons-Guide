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

package kr.syeyoung.dungeonsguide.launcher.gui.screen;

import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.Notification;
import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.NotificationManager;
import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.WidgetNotificationAutoClose;
import kr.syeyoung.dungeonsguide.launcher.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.UUID;

public class WidgetError extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "stacktrace")
    public final BindableAttribute<String> stacktrace = new BindableAttribute<>(String.class);

    public WidgetError(Throwable cause) {
        super(new ResourceLocation("dungeons_guide_loader:gui/error.gui"));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        cause.printStackTrace(printStream);
        this.stacktrace.setValue(byteArrayOutputStream.toString().replace("\t", "    "));
    }

    @On(functionName = "continue")
    public void exit() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        Minecraft.getMinecraft().displayGuiScreen(null);
    }
    @On(functionName = "copy")
    public void copy() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(
                        new StringSelection(stacktrace.getValue()),
                        null
                );

        UUID uuid = UUID.randomUUID();
        NotificationManager.getInstance().updateNotification(uuid, new WidgetNotificationAutoClose(uuid, Notification.builder()
                .title("Successfully Copied!")
                .description("")
                .titleColor(0xFF00FF00)
                .build(), 5000L));
    }
}
