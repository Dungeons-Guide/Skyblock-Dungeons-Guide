package kr.syeyoung.dungeonsguide.commands;

import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoomInfoRegistry;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
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
import java.util.ArrayList;
import java.util.List;

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
        requestReparty();
    }

    public static enum Phase {
        NOT,
        REQUESTED,
        RECEIVE_PARTYMEMBERS,
        DISBAND,
        REPARTY
    }

    private List<String> players = new ArrayList<String>();
    private long nextTrigger = Long.MAX_VALUE;
    private Phase phase = Phase.NOT;
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent e) {
        if (e.type == 2) return;
        if (e.message.getFormattedText().startsWith("§6Party Members ") && phase == Phase.REQUESTED) {
            players.clear();
            phase = Phase.RECEIVE_PARTYMEMBERS;
        }
        if (e.message.getFormattedText().startsWith("§cYou are not currently in a party.§r") && phase == Phase.REQUESTED) {
            phase = Phase.NOT;
        }
        String txt = e.message.getFormattedText();
        if (txt.startsWith("§eParty ") && txt.contains(":")) {
            String playerNames = TextUtils.stripColor(txt.split(":")[1]);
            for (String s : playerNames.split(" ")) {
                if (s.isEmpty()) continue;
                if (s.equals("●")) continue;
                if (s.startsWith("[")) continue;
                players.add(s);
            }
        }
        if (e.message.getFormattedText().equals("§9§m-----------------------------§r") && phase == Phase.RECEIVE_PARTYMEMBERS) {
            phase = Phase.DISBAND;
            nextTrigger = System.currentTimeMillis() + 500;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (nextTrigger < System.currentTimeMillis() && (phase == Phase.DISBAND || phase == Phase.REPARTY)) {
            if (phase == Phase.DISBAND) {
                nextTrigger = System.currentTimeMillis() + 1000;
                phase = Phase.REPARTY;
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/p disband");
            } else {
                phase = Phase.NOT;
                nextTrigger = Long.MAX_VALUE;
                StringBuilder sb = new StringBuilder();
                sb.append("/p invite");
                for (String player : players) {
                    sb.append(" ").append(player);
                }
                Minecraft.getMinecraft().thePlayer.sendChatMessage(sb.toString());
            }
        }
    }

    public void requestReparty() {
        if (phase == Phase.NOT) {
            phase = Phase.REQUESTED;
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/pl");
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
