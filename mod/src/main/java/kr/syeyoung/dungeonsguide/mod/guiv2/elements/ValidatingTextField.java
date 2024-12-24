package kr.syeyoung.dungeonsguide.mod.guiv2.elements;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import net.minecraft.client.renderer.texture.Stitcher;
import scala.actors.threadpool.Arrays;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ValidatingTextField extends AnnotatedExportOnlyWidget {
    @Export(
            attributeName = "value"
    )
    public final BindableAttribute<String> value = new BindableAttribute<>(String.class, "");

    @Export(
            attributeName = "placeholder"
    )
    public final BindableAttribute<String> placeholder = new BindableAttribute<>(String.class, "");

    @Export(
            attributeName = "color"
    )
    public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class, 0xFFFFFFFF);
    @Export(
            attributeName = "placeholderColor"
    )
    public final BindableAttribute<Integer> placeholderColor = new BindableAttribute<>(Integer.class, 0xFFAAAAAA);

    @Export(
            attributeName = "isValid"
    )
    public final BindableAttribute<Boolean> isValid = new BindableAttribute<>(Boolean.class, true);

    @Export(
            attributeName = "validatedValue"
    )
    public final BindableAttribute<String> validatedValue = new BindableAttribute<>(String.class, "");

    @Export(
            attributeName = "regex"
    )
    public final BindableAttribute<String> regex = new BindableAttribute<>(String.class, ".+");

    @Export(
            attributeName = "extraValidator"
    )
    public final BindableAttribute<Predicate<String>> validator = new BindableAttribute(Predicate.class, (Predicate<String>) o -> true);

    public final BindableAttribute<Integer> focusedBorderColor = new BindableAttribute<>(Integer.class, 0xFFFFFFFF);
    public final BindableAttribute<Integer> borderColor = new BindableAttribute<>(Integer.class, 0xFF808080);


    private Pattern pattern = Pattern.compile(".+");

    public ValidatingTextField() {
        regex.addOnUpdate((old, neu) -> {
            pattern = Pattern.compile(neu);
        });
        value.addOnUpdate((old, neu) -> {
            boolean fine = pattern.matcher(neu).matches() && validator.getValue().test(neu);
            if (fine)
                validatedValue.setValue(neu);
            isValid.setValue(fine);

            focusedBorderColor.setValue(fine ? 0xFFFFFFFF : 0xFFFF0000);
            borderColor.setValue(fine ? 0xFF808080 : 0xFF800000);
        });

        validatedValue.addOnUpdate((old, neu) -> {
            value.setValue(neu);
        });
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        TextField textField = new TextField();

        textField.focusedBorderColor.exportTo(focusedBorderColor);
        textField.borderColor.exportTo(borderColor);
        textField.value.exportTo(value);
        textField.color.exportTo(color);
        textField.placeholderColor.exportTo(placeholderColor);
        textField.placeholder.exportTo(placeholder);

        return Collections.singletonList(textField);
    }
}
