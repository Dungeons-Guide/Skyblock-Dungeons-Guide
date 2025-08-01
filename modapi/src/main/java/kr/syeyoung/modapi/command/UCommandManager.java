package kr.syeyoung.modapi.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

public interface UCommandManager {

    void registerCommand(LiteralArgumentBuilder<UCommandContext> command);

    void requestCommandReload();

    CommandNode<UCommandContext> getCommandNode(String command);
}
