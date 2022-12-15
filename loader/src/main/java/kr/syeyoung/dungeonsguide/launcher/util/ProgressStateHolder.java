package kr.syeyoung.dungeonsguide.launcher.util;

import net.minecraftforge.fml.common.ProgressManager;

import java.util.Stack;

public class ProgressStateHolder {
    private static Stack<ProgressManager.ProgressBar> progressBarStack = new Stack<>();

    public static void pushProgress(String name, int steps) {
        progressBarStack.push(ProgressManager.push(name, steps));
    }

    public static void step(String title) {
        progressBarStack.peek().step(title);
    }

    public static void pop() {
        ProgressManager.ProgressBar progressBar = progressBarStack.peek();
        while (progressBar.getStep() < progressBar.getSteps())
            progressBar.step("");
        ProgressManager.pop(progressBarStack.pop());
    }
}
