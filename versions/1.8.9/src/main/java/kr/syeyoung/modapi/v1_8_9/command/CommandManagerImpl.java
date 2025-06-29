package kr.syeyoung.modapi.v1_8_9.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.command.UCommandContext;
import kr.syeyoung.modapi.command.UCommandManager;
import kr.syeyoung.modapi.event.events.RegisterCommandEvent;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommandManagerImpl implements UCommandManager {

    public static class BrigadierCommand implements ICommand {
        private final String name;
        private CommandDispatcher<UCommandContext> dispatcher;
        private List<String> alias = new ArrayList<>();

        public BrigadierCommand(String name) {
            dispatcher = new CommandDispatcher<>();
            this.name = name;
        }


        @Override
        public String getCommandName() {
            return name;
        }

        @Override
        public String getCommandUsage(ICommandSender sender) {
            return String.join("\n", dispatcher.getAllUsage(dispatcher.getRoot(), new UCommandContextImpl(sender), false));
        }

        @Override
        public List<String> getCommandAliases() {
            return alias;
        }

        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            String command = args.length == 0 ? name : name+" "+String.join(" ", args);
            try {
                dispatcher.execute(command, new UCommandContextImpl(sender));
            } catch (CommandSyntaxException e) {
                sender.addChatMessage(new ChatComponentText("§c"+e.getMessage()));
            }
        }

        @Override
        public boolean canCommandSenderUseCommand(ICommandSender sender) {
            return true;
        }

        @Override
        public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
            String command = name+" "+String.join(" ", args);
            ParseResults<UCommandContext> results = dispatcher.parse(command, new UCommandContextImpl(sender));
            try {
                Suggestions suggestions = dispatcher.getCompletionSuggestions(results).get(1000, TimeUnit.MILLISECONDS);
                List<String> suggestionList = new ArrayList<>();
                for (Suggestion suggestion : suggestions.getList()) {
                    suggestionList.add(suggestion.getText());
                }
                return suggestionList;
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        }

        @Override
        public boolean isUsernameIndex(String[] args, int index) {
            return false;
        }

        @Override
        public int compareTo(@NotNull ICommand o) {
            return name.compareTo(o.getCommandName());
        }
    }

    private Map<String, BrigadierCommand> mapping = new HashMap<>();


    @Override
    public void registerCommand(LiteralArgumentBuilder<UCommandContext> command) {
        if (!mapping.containsKey(command.getLiteral())) {
            mapping.put(command.getLiteral(), new BrigadierCommand(command.getLiteral()));
            ClientCommandHandler.instance.registerCommand(mapping.get(command.getLiteral()));
        }
        mapping.get(command.getLiteral()).dispatcher.register(command);
    }

    @Override
    public void addAlias(String root, String... alias) {
        BrigadierCommand command = mapping.get(root);
        if (command == null) registerCommand(LiteralArgumentBuilder.literal(root));
        command = mapping.get(root);

        boolean modified = false;
        for (String s : alias) {
            if (!command.alias.contains(s)) {
                command.alias.add(s);
                modified = true;
            }
        }

        if (modified)
            ClientCommandHandler.instance.registerCommand(command);
    }

    public void unregisterCommands() {
        Set<ICommand> commands = ReflectionHelper.getPrivateValue(CommandHandler.class, ClientCommandHandler.instance, "commandSet","field_71561_b","field_6467","c");

        for (BrigadierCommand registeredCommand : mapping.values()) {
            ClientCommandHandler.instance.getCommands().remove(registeredCommand.getCommandName());
            for (String commandAlias : registeredCommand.getCommandAliases()) {
                ClientCommandHandler.instance.getCommands().remove(commandAlias);
            }
            commands.remove(registeredCommand);
        }
        mapping.clear();
    }

    public void requestCommandReload() {
        unregisterCommands();
        ModAPI.getAPI().getEventBus().fireEvent(new RegisterCommandEvent(this));
    }
}
