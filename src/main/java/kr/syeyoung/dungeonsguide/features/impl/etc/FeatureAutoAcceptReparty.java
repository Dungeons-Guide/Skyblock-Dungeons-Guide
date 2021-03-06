package kr.syeyoung.dungeonsguide.features.impl.etc;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.DungeonQuitListener;
import kr.syeyoung.dungeonsguide.features.listener.GuiOpenListener;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;

import java.awt.*;

public class FeatureAutoAcceptReparty extends SimpleFeature implements ChatListener {
    public FeatureAutoAcceptReparty() {
        super("ETC", "Auto accept reparty", "Automatically accept reparty", "qol.autoacceptreparty", true);
    }

    SkyblockStatus skyblockStatus = e.getDungeonsGuide().getSkyblockStatus();

    private String lastDisband;
    @Override
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        if (clientChatReceivedEvent.message.getFormattedText().endsWith("§ehas disbanded the party!§r")) {
            lastDisband = null;
            String[] texts = TextUtils.stripColor(clientChatReceivedEvent.message.getFormattedText()).split(" ");
            for (String s : texts) {
                if (s.isEmpty()) continue;
                if (s.startsWith("[")) continue;
                if (s.equalsIgnoreCase("has")) break;
                lastDisband = s;
                break;
            }
            System.out.println(lastDisband);
        } else if (clientChatReceivedEvent.message.getFormattedText().contains("§ehas invited you to join their party!")) {
            String[] texts = TextUtils.stripColor(clientChatReceivedEvent.message.getFormattedText()).split(" ");
            boolean equals = false;
            for (String s : texts) {
                System.out.println(s);
                if (s.isEmpty()) continue;
                if (s.startsWith("[")) continue;
                if (s.equalsIgnoreCase("has")) continue;
                if (s.equalsIgnoreCase(lastDisband)) {
                    equals = true;
                    break;
                }
            }

            if (equals && isEnabled()) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/p join "+lastDisband);
            }
        }
    }
}
