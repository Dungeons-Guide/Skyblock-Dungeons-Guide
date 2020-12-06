package kr.syeyoung.dungeonsguide.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.syeyoung.dungeonsguide.utils.SkyblockUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CommandWhatYearIsIt extends CommandBase {
    @Override
    public String getCommandName() {
        return "tellmeyear";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "tellmeyear";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        try {



            sender.addChatMessage(new ChatComponentText("Current year is "+ SkyblockUtils.getSkyblockYear()));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
