package v1_21_11.profiler;

import kr.syeyoung.modapi.profiler.UProfiler;
import net.minecraft.util.profiler.Profiler;

public class UProfilerImpl implements UProfiler {
    private Profiler delegate;

    public UProfilerImpl(Profiler delegate) {
        this.delegate = delegate;
    }

    public void startSection(String name) {
        delegate.push(name);
    }

    public void endSection() {
        delegate.pop();
    }

    public void endStartSection(String name) {
        delegate.swap(name);
    }

    public void clearprofiling() {
//        delegate.();
    }
}
