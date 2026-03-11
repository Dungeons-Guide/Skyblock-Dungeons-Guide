package kr.syeyoung.modapi.v1_21_11.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.command.UCommandContext;
import kr.syeyoung.modapi.command.UCommandManager;
import kr.syeyoung.modapi.event.events.RegisterCommandEvent;
import kr.syeyoung.modapi.v1_21_11.command.UCommandContextImpl;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CommandManagerImpl implements UCommandManager {
    private CommandDispatcher<UCommandContext> dispatcher = new CommandDispatcher<>();

    @Override
    public void registerCommand(LiteralArgumentBuilder<UCommandContext> command) {
        LiteralCommandNode<UCommandContext> incompatibleNode = command.build();
        dispatcher.register(command);
    }

    @Override
    public CommandNode<UCommandContext> getCommandNode(String command) {
        return dispatcher.findNode(Collections.singletonList(command));
    }

    private final ThreadLocal<CommandDispatcher<FabricClientCommandSource>> dispatcherEvent = new ThreadLocal<>();

    private CommandNode<FabricClientCommandSource> migrate(CommandNode<FabricClientCommandSource> fabricCommand, CommandNode<UCommandContext> command,
                                Map<CommandNode<UCommandContext>, CommandNode<FabricClientCommandSource>> mapping) {
        CommandNode<FabricClientCommandSource> redirect = null;
        if (command.getRedirect() != null) {
            redirect = migrate(null, command.getRedirect(), mapping); // yes this is orphan.
        }

        CommandNode<FabricClientCommandSource> migrated;
        if (!mapping.containsKey(command)) {
            if (command instanceof LiteralCommandNode<UCommandContext> literal) {
                LiteralArgumentBuilder<FabricClientCommandSource> builder = ClientCommandManager.literal(
                        literal.getLiteral()
                );
                if (redirect != null)
                    builder = builder.forward(redirect, context -> Collections.singleton(context.getSource()), command.isFork());
                if (literal.getRequirement() != null)
                    builder = builder.requires(ctx -> literal.getRequirement().test(new UCommandContextImpl(ctx)));
                if (literal.getCommand() != null) builder = builder.executes(ctx -> {
                    return this.dispatcher.execute(ctx.getInput(), new UCommandContextImpl(ctx.getSource()));
                });
                migrated = builder.build();
            } else if (command instanceof ArgumentCommandNode<UCommandContext, ?> argument) {
                ArgumentBuilder<FabricClientCommandSource, ?> builder = ClientCommandManager.argument(
                        argument.getName(),
                        argument.getType()
                );
                if (redirect != null)
                    builder = builder.forward(redirect, context -> Collections.singleton(context.getSource()), command.isFork());
                if (argument.getRequirement() != null)
                    builder = builder.requires(ctx -> argument.getRequirement().test(new UCommandContextImpl(ctx)));
                if (argument.getCommand() != null) builder = builder.executes(ctx -> {
                    return this.dispatcher.execute(ctx.getInput(), new UCommandContextImpl(ctx.getSource()));
                });
                migrated = builder.build();
            } else {
                throw new IllegalArgumentException("Invalid Command!");
            }
            mapping.put(command, migrated);
        } else {
            migrated = mapping.get(command);
        }
        if (fabricCommand != null) fabricCommand.addChild(migrated);
        mapping.put(command, migrated);

        if (command.getChildren().isEmpty() && !migrated.getChildren().isEmpty()) {
            for (CommandNode<UCommandContext> child : command.getChildren()) {
                migrate(migrated, child, mapping);
            }
        }


        return mapping.get(command);
    }


    public void init() {
        ClientCommandRegistrationCallback.EVENT.register((fabricDispatcher, access) -> {
            this.dispatcher = new CommandDispatcher<>();
            ModAPI.getAPI().getEventBus().fireEvent(new RegisterCommandEvent(this));

            // now we merge.
            Map<CommandNode<UCommandContext>, CommandNode<FabricClientCommandSource>> hashmap = new HashMap<>();
            for (CommandNode<UCommandContext> child : this.dispatcher.getRoot().getChildren()) {
                migrate(fabricDispatcher.getRoot(), child, hashmap);
            }
        });
    }
    @Override
    public void requestCommandReload() {
        // TODO: do nothing.... hmmm...
    }
}
