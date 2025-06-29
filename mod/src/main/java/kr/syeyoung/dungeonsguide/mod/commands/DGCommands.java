package kr.syeyoung.dungeonsguide.mod.commands;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DGCommands {
    DGCommand[] value();
}
