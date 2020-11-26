package kr.syeyoung.dungeonsguide.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.roomedit.Parameter;

public interface ValueEditCreator<T extends ValueEdit> {
    public T createValueEdit(Parameter parameter);

    public Object createDefaultValue(Parameter parameter);

    public Object cloneObj(Object object);
}
