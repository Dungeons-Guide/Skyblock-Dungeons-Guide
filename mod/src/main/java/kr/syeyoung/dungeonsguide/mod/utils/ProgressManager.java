/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.utils;

// keeps track of loading steps like forge's ProgressManager did on 1.8.9. nothing displays it on 26.1
public class ProgressManager {
    public static ProgressBar push(String title, int steps) {
        return new ProgressBar(title, steps);
    }

    public static void pop(ProgressBar progressBar) {
    }

    public static class ProgressBar {
        private final String title;
        private final int steps;
        private int step;
        private String message;

        private ProgressBar(String title, int steps) {
            this.title = title;
            this.steps = steps;
        }

        public void step(String message) {
            this.step++;
            this.message = message;
        }

        public String getTitle() {
            return title;
        }

        public int getStep() {
            return step;
        }

        public int getSteps() {
            return steps;
        }

        public String getMessage() {
            return message;
        }
    }
}
