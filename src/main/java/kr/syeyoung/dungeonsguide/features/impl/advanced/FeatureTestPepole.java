package kr.syeyoung.dungeonsguide.features.impl.advanced;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.chat.PartyManager;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.features.listener.DungeonStartListener;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FeatureTestPepole extends GuiFeature implements ChatListener, DungeonStartListener {
    public FeatureTestPepole() {
        super("Dungeon", "Feuture test", "NOU", "", false, 200, 100);
        this.parameters.put("scale", new FeatureParameter<Float>("scale", "Scale", "Scale", 2.0f, "float"));

    }

    double getScale(){
        return (double) this.<Float>getParameter("scale").getValue();
    }


    @Override
    public void drawScreen(float partialTicks) {
//        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.skull, 1), 100, 100);
        super.drawScreen(partialTicks);
    }


    HashMap<String, ItemStack> SkullCashe = new HashMap<>();
    public ItemStack getSkullByUserName(String username){
        if(SkullCashe.containsKey(username)) return SkullCashe.get(username);
        ItemStack stack = new ItemStack(Items.skull, 1, 3);
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setTag("SkullOwner", new NBTTagString(username));
        SkullCashe.put(username, stack);
        return stack;
    }

    private Set<String> ready = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    private static final List<String> readyPhrase = Arrays.asList("r", "rdy", "ready");
    private static final List<String> negator = Arrays.asList("not ", "not", "n", "n ");
    private static final Map<String, Boolean> readynessIndicator = new HashMap<>();
    static {
        readyPhrase.forEach(val -> readynessIndicator.put(val, true));
        for (String s : negator) {
            readyPhrase.forEach(val -> readynessIndicator.put(s+val, false));
        }
        readynessIndicator.put("dont start", false);
        readynessIndicator.put("don't start", false);
        readynessIndicator.put("dont go", false);
        readynessIndicator.put("don't go", false);
        readynessIndicator.put("start", true);
        readynessIndicator.put("go", true);
    }

    @Override
    public void onChat(ClientChatReceivedEvent clientChatReceivedEvent) {
        String txt = clientChatReceivedEvent.message.getFormattedText();
        if (!txt.startsWith("§r§9Party §8>")) return;

        String chat = TextUtils.stripColor(txt.substring(txt.indexOf(":")+1)).trim().toLowerCase();


        String usernamearea = TextUtils.stripColor(txt.substring(13, txt.indexOf(":")));
        String username = null;
        for (String s : usernamearea.split(" ")) {
            if (s.isEmpty()) continue;
            if (s.startsWith("[")) continue;
            username = s;
            break;
        }

        Boolean status = null;
        String longestMatch = "";
        for (Map.Entry<String, Boolean> stringBooleanEntry : readynessIndicator.entrySet()) {
            if (chat.startsWith(stringBooleanEntry.getKey()) || chat.endsWith(stringBooleanEntry.getKey()) || (stringBooleanEntry.getKey().length()>=3 && chat.contains(stringBooleanEntry.getKey()))) {
                if (stringBooleanEntry.getKey().length() > longestMatch.length()) {
                    longestMatch = stringBooleanEntry.getKey();
                    status = stringBooleanEntry.getValue();
                }
            }
        }
        if (status == null);
        else if (status) ready.add(username);
        else ready.remove(username);

    }

    @Override
    public void onDungeonStart() {
        ready.clear();
    }

    boolean isAloneInParty(){
        if(PartyManager.INSTANCE.getPartyContext() != null){
            return PartyManager.INSTANCE.getPartyContext().getPartyRawMembers().size() == 1;
        }
        return false;
    }

    final Pattern tabListRegex = Pattern.compile("\\*[a-zA-Z0-9_]{2,16}\\*", Pattern.MULTILINE);

    /**
     * We regex their name out
     * @param networkPlayerInfo the network player info of player
     * @return the username of player
     */
    private String getPlayerNameWithChecks(NetworkPlayerInfo networkPlayerInfo) {
        String name;
        if (networkPlayerInfo.getDisplayName() != null) {
            name = networkPlayerInfo.getDisplayName().getFormattedText();
        } else {
            name = ScorePlayerTeam.formatPlayerName(
                    networkPlayerInfo.getPlayerTeam(),
                    networkPlayerInfo.getGameProfile().getName()
            );
        }

        if (name.trim().equals("§r") || name.startsWith("§r ")) return null;

        name = TextUtils.stripColor(name);

        name = name.replace(" ", "*");

        Matcher matcher = tabListRegex.matcher(name);
        if (!matcher.find()) return null;

        name = matcher.group(0);
        name = name.substring(0, name.length() - 1);
        name = name.substring(1);
        return name;
    }

    boolean isPlayerInDungeon(String username){

        List<NetworkPlayerInfo> list = new ArrayList<>(Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap());

        // 19 iterations bc we only want to scan the player part of tab list
        for (int i = 1; i < 20; i++) {
            NetworkPlayerInfo networkPlayerInfo = list.get(i);

            String name = getPlayerNameWithChecks(networkPlayerInfo);
            if (name == null) continue;

            EntityPlayer entityplayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(name);


            if(entityplayer != null && (!entityplayer.isInvisible())){
                if(name == username) return true;
            }


        }
        return false;
    }




    @Override
    public void drawHUD(float partialTicks) {

        if(PartyManager.INSTANCE.getPartyContext() == null && !PartyManager.INSTANCE.getPartyContext().isPartyExistHypixel()) return;
        if(isAloneInParty()) return;


        //        System.out.println(stack.getTagCompound().getCompoundTag("Owner"));
        FontRenderer fr = getFontRenderer();

        RenderHelper.enableStandardItemLighting();


        int y = 0;
        for (String partyRawMember : PartyManager.INSTANCE.getPartyContext().getPartyRawMembers()) {

            GlStateManager.pushMatrix();

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            GlStateManager.scale(getScale(),getScale(),1F);

            Gui.drawRect(15, 5 + y, fr.getStringWidth(partyRawMember + genPlayerText(partyRawMember)) + 20, 15 + y, getColorTextColor(partyRawMember));

            Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(getSkullByUserName(partyRawMember), 0, y);


            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            fr.drawString(partyRawMember, 15, y + 5, 0xffffff);




            fr.drawString(genPlayerText(partyRawMember), 16 + fr.getStringWidth(partyRawMember), y + 5, 0xf9f9fa);


            GlStateManager.popMatrix();
            y += 12;
        }

        RenderHelper.disableStandardItemLighting();
    }

    private int getColorTextColor(String partyRawMember) {
        if(Objects.equals(genPlayerText(partyRawMember), ": Ready") || Objects.equals(genPlayerText(partyRawMember), ": Not Ready")){
            boolean isPlayerReady = ready.contains(partyRawMember);
            return isPlayerReady ? 0xFF12bc00 : 0xFFd70022;
        }


        return 0xFF38383d;
    }


    String genPlayerText(String username){

        if(DungeonsGuide.getDungeonsGuide().getSkyblockStatus().isOnDungeon()){
            if(Objects.equals(username, Minecraft.getMinecraft().getSession().getUsername())){
                return ": In Dungeon";
            }
            else if(isPlayerInDungeon(username)){
                return ": In Dungeon";
            }else {
                return ": Somewhere";
            }
        } else {
            if(ready.contains(username)){
                return ": Ready";
            }

            return ": Not Ready";
        }
    }


}
