package kr.syeyoung.dungeonsguide.roomedit;

import kr.syeyoung.dungeonsguide.roomedit.valueedit.ActuallyClonable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Parameter {
    private String name;
    private Object previousData;
    private Object newData;
}
