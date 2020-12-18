package kr.syeyoung.dungeonsguide.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.roomedit.Parameter;

public class ValueEditNull implements ValueEditCreator {
    @Override
    public ValueEdit createValueEdit(Parameter parameter) {
        return null;
    }

    @Override
    public Cloneable createDefaultValue(Parameter parameter) {
        return null;
    }

    @Override
    public Object cloneObj(Object object) {
        return null;
    }
}
