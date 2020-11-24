package kr.syeyoung.dungeonsguide.commands;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class CommandLoadData extends CommandBase {
    @Override
    public String getCommandName() {
        return "loadrooms";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "loadrooms";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        DungeonRoomInfoRegistry.loadAll(DungeonsGuide.getDungeonsGuide().getConfigDir());
    }
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
