package kr.syeyoung.dungeonsguide.mod.guiv2.stylesheet;

import java.lang.annotation.*;

@Repeatable(CssProperties.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CssProperty {
    int index();
    String attributeName();
}
