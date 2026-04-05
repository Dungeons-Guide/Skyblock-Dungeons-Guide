package kr.syeyoung.modapi.profiler;

public interface UProfiler {
    void startSection(String name);

    void endSection();

    void endStartSection(String name);

    void clearprofiling();
}
