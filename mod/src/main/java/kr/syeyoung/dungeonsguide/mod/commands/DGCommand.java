package kr.syeyoung.dungeonsguide.mod.commands;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Repeatable(DGCommands.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface DGCommand {
    String value();
}
