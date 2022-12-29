package kr.syeyoung.dungeonsguide.mod.guiv2.stylesheet;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Styles {
    @CssProperty(attributeName = "padding-left", index = 0)
    @CssProperty(attributeName = "padding", index = 0)
    private Integer paddingLeft;
    @CssProperty(attributeName = "padding-right", index = 0)
    @CssProperty(attributeName = "padding", index = 1)
    private int paddingRight;
    @CssProperty(attributeName = "padding-top", index = 0)
    @CssProperty(attributeName = "padding", index = 2)
    private int paddingTop;
    @CssProperty(attributeName = "padding-bottom", index = 0)
    @CssProperty(attributeName = "padding", index = 3)
    private int paddingBottom;

    private int marginLeft;
    private int marginRight;
    private int marginTop;
    private int marginBottom;

    private int backgroundColor;
    private int textColor;

    private int borderWidth;
    private int borderThickness;
    private int borderRadius;
    private int borderColor;


    private static final Map<String, StyleValue> map = new HashMap<>();
    static {
        for (Field declaredField : Styles.class.getDeclaredFields()) {
            if (declaredField.isAnnotationPresent(CssProperties.class)) {
                CssProperties properties = declaredField.getAnnotation(CssProperties.class);
                for (CssProperty cssProperty : properties.value()) {
                    map.put(cssProperty.attributeName(), new StyleValue(cssProperty, declaredField));
                }
            }
        }
    }

    private int verbosity = 0;
    private String originalStr = "";

    @AllArgsConstructor @Getter
    private static class StyleValue {
        CssProperty property;
        Field field;
    }
    public Styles(int verbosity, String originalStr) {
        this.verbosity = verbosity;
        this.originalStr = originalStr;
        loadFromString(originalStr);
    }

    private void loadFromString(String styles) {
        for (String property : styles.split(";")) {
            String value = property.trim();
            String name = value.split(":")[0];
            String targetValue = value.split(":")[1];
            String[] parameters = targetValue.split(" ");

            StyleValue property1 = map.get(name);
            String parameterValue = parameters[property1.getProperty().index()];

            try {
                if (property1.field.getType() == float.class) {
                    property1.field.set(this, Float.parseFloat(parameterValue));
                } else if (property1.field.getType() == double.class) {
                    property1.field.set(this, Double.parseDouble(parameterValue));
                } else if (property1.field.getType() == int.class) {
                    property1.field.set(this, Integer.parseInt(parameterValue));
                } else if (property1.field.getType() == String.class) {
                    property1.field.set(this, parameterValue);
                } else if (property1.field.getType().isEnum()) {
                    for (Object enumConstant : property1.field.getType().getEnumConstants()) {
                        if (parameterValue.equalsIgnoreCase(enumConstant.toString()))
                            property1.field.set(this, enumConstant);
                    }
                } else if (property1.field.getType() == boolean.class) {
                    property1.field.set(this, Boolean.parseBoolean(parameterValue));
                } else if (property1.field.getType() == LengthUnit.class) {
                    property1.field.set(this,
                            LengthUnit.parseLength(parameterValue));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Styles combineStyles(List<Styles> stylesList) {
        String styleStr = stylesList.stream().sorted(Comparator.comparingInt(a -> a.verbosity))
                .map(a -> a.originalStr).collect(Collectors.joining());
        return new Styles(stylesList.stream()
        .map(a-> a.verbosity).max(Integer::compare).orElse(0), styleStr);
    }
}
