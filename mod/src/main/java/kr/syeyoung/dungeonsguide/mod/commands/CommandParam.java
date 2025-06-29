package kr.syeyoung.dungeonsguide.mod.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandParam {
    Class<?> type() default Void.class;
    Class<?> suggestionProvider() default Void.class;

    String[] suggestions() default {};
    String value();

    EnumStringType stringType() default EnumStringType.WORD;

    double min() default Double.NEGATIVE_INFINITY;
    double max() default Double.POSITIVE_INFINITY;

    public static enum EnumStringType {
        GREEDY, STRING, WORD
    }
}
