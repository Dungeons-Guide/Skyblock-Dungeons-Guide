package kr.syeyoung.modapi.fakeserver;

import kr.syeyoung.modapi.world.IBlockAccessible;

public interface FakeServerUtils {
    void launchFakeServerAndJoin(IBlockAccessible accessible);

    boolean isRunning();
}
