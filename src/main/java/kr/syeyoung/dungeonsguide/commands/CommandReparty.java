package kr.syeyoung.dungeonsguide.commands;

import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.e;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CommandReparty extends CommandBase {
    @Override
    public String getCommandName() {
        return "reparty";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "reparty";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {

    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent e) {

    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {

    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
