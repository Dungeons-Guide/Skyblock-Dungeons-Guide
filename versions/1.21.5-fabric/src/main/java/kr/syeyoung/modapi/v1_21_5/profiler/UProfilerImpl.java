package kr.syeyoung.modapi.v1_21_5.profiler;

import kr.syeyoung.modapi.profiler.UProfiler;
import net.minecraft.profiler.Profiler;

public class UProfilerImpl implements UProfiler {
    private Profiler delegate;

    public UProfilerImpl(Profiler delegate) {
        this.delegate = delegate;
    }

    public void startSection(String name) {
        delegate.startSection(name);
    }

    public void endSection() {
        delegate.endSection();
    }

    public void endStartSection(String name) {
        delegate.endStartSection(name);
    }

    public void clearprofiling() {
        delegate.clearProfiling();
    }
}
