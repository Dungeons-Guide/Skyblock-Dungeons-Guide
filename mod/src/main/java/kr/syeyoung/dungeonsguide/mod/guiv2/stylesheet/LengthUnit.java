package kr.syeyoung.dungeonsguide.mod.guiv2.stylesheet;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor @Getter
public class LengthUnit {
    private final float value;
    private final Unit units;

    public static LengthUnit parseLength(String parameterValue) {
        parameterValue = parameterValue.trim();
        String value = parameterValue.substring(0, parameterValue.length() - 2);
        String units = parameterValue.substring(parameterValue.length() - 2);

        return new LengthUnit(Float.parseFloat(value), Unit.valueOf(units));
    }

    public static enum Unit {
        EM, PX
    }
}
