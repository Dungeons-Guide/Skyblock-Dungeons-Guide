package kr.syeyoung.modapi.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

public interface UCommandManager {

    void registerCommand(LiteralArgumentBuilder<UCommandContext> command);
    void addAlias(String root, String... alias);

    void requestCommandReload();
}
