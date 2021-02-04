package kr.syeyoung.dungeonsguide.roomedit.panes;

import kr.syeyoung.dungeonsguide.gui.elements.MParameter;

import java.util.List;

public interface DynamicEditor {
    void delete(MParameter parameter);

    List<String> allowedClass();
}
