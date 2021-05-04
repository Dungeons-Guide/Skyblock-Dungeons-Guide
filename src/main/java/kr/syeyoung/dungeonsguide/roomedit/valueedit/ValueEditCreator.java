package kr.syeyoung.dungeonsguide.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.roomedit.Parameter;

public interface ValueEditCreator<T extends ValueEdit> {
    T createValueEdit(Parameter parameter);

    Object createDefaultValue(Parameter parameter);

    Object cloneObj(Object object);
}
