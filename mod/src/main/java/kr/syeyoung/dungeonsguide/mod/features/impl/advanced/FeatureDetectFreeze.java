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

package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FeatureDetectFreeze extends SimpleFeature {
    public FeatureDetectFreeze() {
        super("Misc", "Freeze Detector", "Detect freezes, and when mc freezes for more than 3s, copy threadump and show you a popup", "misc.freezedetect", false);
        t.start();
    }

    @DGEventHandler(ignoreDisabled = true, triggerOutOfSkyblock = true)
    public void onTick(TickEvent.ClientTickEvent tickEvent) {
        lastTick = System.currentTimeMillis() + 3000;
    }

    private volatile boolean isEnabled = false;

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.isEnabled = enabled;
    }

    private long lastTick = Long.MAX_VALUE;
    private Thread t = new Thread(DungeonsGuide.THREAD_GROUP,this::run);

    public void run() {
        while(!t.isInterrupted()) {
            if (lastTick < System.currentTimeMillis() && isEnabled) {
                ThreadMXBean bean = ManagementFactory.getThreadMXBean();
                ThreadInfo[] infos = bean.dumpAllThreads(true, true);
                String stacktrace = Arrays.stream(infos).map(Object::toString)
                        .collect(Collectors.joining());
                System.out.println(stacktrace);

                StringSelection selection = new StringSelection(stacktrace);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);

                JOptionPane.showMessageDialog(null, "Your Minecraft Seems to be frozen!\nThreadump has been copied into your clipboard!", "DG Freeze Alert", JOptionPane.INFORMATION_MESSAGE);
            }
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
