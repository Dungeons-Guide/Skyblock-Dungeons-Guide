package kr.syeyoung.dungeonsguide.commands;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class CommandToggleDebug extends CommandBase {
    @Override
    public String getCommandName() {
        return "debugtoggle";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "debugtoggle";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        DungeonsGuide.DEBUG = !DungeonsGuide.DEBUG;
        sender.addChatMessage(new ChatComponentText("Toggled Debug mode to "+ DungeonsGuide.DEBUG));
    }
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
