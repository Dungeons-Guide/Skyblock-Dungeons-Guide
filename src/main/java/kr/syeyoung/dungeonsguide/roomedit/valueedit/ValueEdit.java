package kr.syeyoung.dungeonsguide.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.roomedit.Parameter;

public interface ValueEdit<T extends Object> {
    public void setParameter(Parameter parameter);
}
