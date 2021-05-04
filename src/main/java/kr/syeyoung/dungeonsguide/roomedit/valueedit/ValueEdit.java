package kr.syeyoung.dungeonsguide.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.roomedit.Parameter;

public interface ValueEdit<T extends Object> {
    void setParameter(Parameter parameter);

    void renderWorld(float partialTicks);
}
