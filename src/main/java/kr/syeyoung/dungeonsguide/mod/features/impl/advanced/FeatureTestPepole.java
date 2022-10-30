package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.GuiFeature;
import kr.syeyoung.dungeonsguide.mod.features.listener.ChatListener;
import kr.syeyoung.dungeonsguide.mod.features.listener.DungeonStartListener;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompManager;
import kr.syeyoung.dungeonsguide.mod.stomp.StompPayload;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static kr.syeyoung.dungeonsguide.mod.utils.TabListUtil.getString;

public class FeatureTestPepole extends GuiFeature implements ChatListener, DungeonStartListener {

    Logger logger = LogManager.getLogger("FeatureTestPepole");
    private Float scale;
    private Set<String> lastmebersRaw;
    private boolean broadcastLock;

    public FeatureTestPepole() {
        super("Dungeon", "Feuture test", "NOU", "", false, 200, 100);


        addParameter("scale", new FeatureParameter<>("scale", "Scale", "Scale", 2.0f, "float", nval -> this.scale = nval));
        MinecraftForge.EVENT_BUS.register(this);

//        (new Thread(() -> {
//            while (true){
//                handleSelfBroadcasts();
//            }
//        }) ).start();
    }

    public static void handlePartyBroadCast(String playload) {
        String[] messagge = playload.substring(2).split(":");

//                String random = messagge[1];
        String username = messagge[0];
        System.out.println("Broadcast was a self broadcast with: " + username);
        PartyManager.INSTANCE.getPartyContext().addDgUser(username);

//                String actualPayload = "ACK" + random + ":" + username + ":" + Minecraft.getMinecraft().getSession().getUsername();
//                StompManager.getInstance().send(new StompPayload().header("destination", "/app/party.broadcast").payload(
//                        new JSONObject().put("partyID", PartyManager.INSTANCE.getPartyContext().getPartyID())
//                                .put("payload", actualPayload).toString()
//                ));

//            } else if (playload.startsWith("ACK")){
//                String ACKnick = playload.substring(3);
//                String[] nicks = ACKnick.split(":");
//                if(Objects.equals(nicks[0], Minecraft.getMinecraft().getSession().getUsername())) {
//                    FeatureTestPepole.addACK(new Tuple<String, String>(nicks[1], nicks[2]));
//                }
    }

//    static List<Tuple<String, String>> acknicks = new ArrayList<>();
//
//    static List<Tuple<Integer, Long>> sentContros = new ArrayList<>();

//    public static void addACK(Tuple<String,String> acKnick) {
//        acknicks.add(acKnick);
//    }

    public void broadcastYourself() {
        if (PartyManager.INSTANCE.getPartyContext().getPartyID() == null) {
            return;
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        logger.info("Sending self broadcast");
//        int control = (new Random()).nextInt(1000);
        String actualPayload = "C:" + Minecraft.getMinecraft().getSession().getUsername() + ":" + 222;
        StompManager.getInstance().send(new StompPayload().header("destination", "/app/party.broadcast").payload(
                new JSONObject().put("partyID", PartyManager.INSTANCE.getPartyContext().getPartyID())
                        .put("payload", actualPayload).toString()
        ));
//        sentContros.add(new Tuple<>(control, System.currentTimeMillis()));
        broadcastLock = false;
    }

    public void handleSelfBroadcasts(){
        if(broadcastLock) {
            broadcastYourself();
            return;
        }


//        for (Tuple<String, String> acknick : acknicks) {
//            int a = Integer.parseInt(acknick.getFirst());
//            for (Tuple<Integer, Long> sentContro : sentContros) {
//                if(sentContro.getFirst() == a){
//                    acknicks.remove(acknick);
//                    sentContros.remove(a);
//                    if(sentContro.getSecond() + 500 > System.currentTimeMillis()){
//                        broadcastLock = true;
//                        broadcastYourself();
//                        break;
//                    }
//                }
//            }
//
//        }


        if(PartyManager.INSTANCE.getPartyContext() == null) return;

        if(lastmebersRaw == null ) {
            lastmebersRaw = PartyManager.INSTANCE.getPartyContext().getPartyRawMembers();
            return;
        }

        Set<String> membersRaw = PartyManager.INSTANCE.getPartyContext().getPartyRawMembers();

        if(!membersRaw.equals(lastmebersRaw)){
            logger.info("members changed unlocking locking broadcast");
            broadcastLock = true;
            broadcastYourself();
            lastmebersRaw = membersRaw;
        }
    }


    HashMap<String, ItemStack> SkullCashe = new HashMap<>();



    ExecutorService executor = Executors.newFixedThreadPool(5);


    public ItemStack getSkullByUserName(String username) {
        if (SkullCashe.containsKey(username)) return SkullCashe.get(username);
        ItemStack stack = new ItemStack(Items.skull, 1, 3);

        executor.submit(() -> {
            EntityPlayer playerEntityByName = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(username);
            if(playerEntityByName == null || playerEntityByName.getGameProfile() == null) {
                stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setTag("SkullOwner", new NBTTagString(username));
                return;
            }

            // this line should trick mineshaft to caching the player skin
            // im doing this bc just setting SkullOwner downloads the skin on main thread
            // thus causing a lag spike
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Minecraft.getMinecraft().getSkinManager().loadSkinFromCache(playerEntityByName.getGameProfile());

            if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                Minecraft.getMinecraft().getSkinManager().loadSkin((MinecraftProfileTexture)map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
            }



            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setTag("SkullOwner", new NBTTagString(username));

        });

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
            readyPhrase.forEach(val -> readynessIndicator.put(s + val, false));
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

        String chat = TextUtils.stripColor(txt.substring(txt.indexOf(":") + 1)).trim().toLowerCase();


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
            if (chat.startsWith(stringBooleanEntry.getKey()) || chat.endsWith(stringBooleanEntry.getKey()) || (stringBooleanEntry.getKey().length() >= 3 && chat.contains(stringBooleanEntry.getKey()))) {
                if (stringBooleanEntry.getKey().length() > longestMatch.length()) {
                    longestMatch = stringBooleanEntry.getKey();
                    status = stringBooleanEntry.getValue();
                }
            }
        }
        if (status == null) ;
        else if (status) ready.add(username);
        else ready.remove(username);

    }

    @Override
    public void onDungeonStart() {
        ready.clear();
    }

    boolean isAloneInParty() {
        if (PartyManager.INSTANCE.getPartyContext() != null) {
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

        return getString(name, tabListRegex);
    }

    boolean isPlayerInDungeon(String username) {

        List<NetworkPlayerInfo> list = new ArrayList<>(Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap());

        // 19 iterations bc we only want to scan the player part of tab list
        for (int i = 1; i < 20; i++) {
            if(list.size() < i) break;
            NetworkPlayerInfo networkPlayerInfo = list.get(i);

            String name = getPlayerNameWithChecks(networkPlayerInfo);
            if (name == null) continue;

            EntityPlayer entityplayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByName(name);


            if (entityplayer != null && (!entityplayer.isInvisible())) {
                if (name == username) return true;
            }


        }
        return false;
    }


    public final Map<String, Boolean> cachedProfiles = new HashMap<>();


    void renderItem(GameProfile stack, int x, int y){


//        GameProfile a = new GameProfile();

        GameProfile toDraw = cachedProfiles.get(stack.getName()) ? stack : null;

        TileEntitySkullRenderer.instance.renderSkull(x, y, -0.5F, EnumFacing.UP, 180.0F, 1, stack, -1);

        if(toDraw == null && !cachedProfiles.containsKey(stack.getName())){
            cachedProfiles.put(stack.getName(), false);
            new Thread(() -> {
                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Minecraft.getMinecraft().getSkinManager().loadSkinFromCache(stack);

                if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {

                    MinecraftProfileTexture profileTexture =  map.get(MinecraftProfileTexture.Type.SKIN);

                    final ResourceLocation resourceLocation = new ResourceLocation("skins/" + profileTexture.getHash());
                    ITextureObject iTextureObject = Minecraft.getMinecraft().getTextureManager().getTexture(resourceLocation);
                    if (iTextureObject == null) {

                        String skinCacheDir = ReflectionHelper.getPrivateValue(SkinManager.class, Minecraft.getMinecraft().getSkinManager(), "field_152796_d", "skinCacheDir");

                        File file = new File(skinCacheDir, profileTexture.getHash().length() > 2 ? profileTexture.getHash().substring(0, 2) : "xx");
                        File file2 = new File(file, profileTexture.getHash());
                        final IImageBuffer iImageBuffer =  new ImageBufferDownload();
                        ThreadDownloadImageData threadDownloadImageData = new ThreadDownloadImageData(file2, profileTexture.getUrl(), DefaultPlayerSkin.getDefaultSkinLegacy(), new IImageBuffer() {

                            public BufferedImage parseUserSkin(BufferedImage image) {
                                if (iImageBuffer != null) {
                                    image = iImageBuffer.parseUserSkin(image);
                                }

                                return image;
                            }

                            public void skinAvailable() {
                                if (iImageBuffer != null) {
                                    iImageBuffer.skinAvailable();
                                }
                            }
                        });
                        Minecraft.getMinecraft().getTextureManager().loadTexture(resourceLocation, threadDownloadImageData);
                    }
                }

                cachedProfiles.replace(stack.getName(), true);
            }).start();
        }



    }

    @Override
    public void drawHUD(float partialTicks) {

        if (PartyManager.INSTANCE.getPartyContext() == null) return;
        if (!PartyManager.INSTANCE.getPartyContext().isPartyExistHypixel()) return;
//        if(isAloneInParty()) return;

        ResourceLocation logoLoc = new ResourceLocation("dungeonsguide:textures/dglogox32.png");

        FontRenderer fr = getFontRenderer();


        int y = 0;
        for (String partyRawMember : PartyManager.INSTANCE.getPartyContext().getPartyRawMembers()) {

            boolean isDgUser = isDgUser(partyRawMember);

            int xOffset = isDgUser ? 9 : -2;

            GlStateManager.pushMatrix();


            GlStateManager.scale(scale, scale, 1F);

            Gui.drawRect(15 + xOffset, 5 + y, fr.getStringWidth(partyRawMember + genPlayerText(partyRawMember)) + 20 + xOffset, 15 + y, getColorTextColor(partyRawMember));

            RenderHelper.enableStandardItemLighting();
            renderItem(Minecraft.getMinecraft().theWorld.getPlayerEntityByName(partyRawMember).getGameProfile(), 0, y + 1);
//            Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(getSkullByUserName(partyRawMember), 0, y + 1);
            RenderHelper.disableStandardItemLighting();

            fr.drawString(partyRawMember, 15 + xOffset, y + 5, 0xffffff);


            fr.drawString(genPlayerText(partyRawMember), 16 + fr.getStringWidth(partyRawMember) + xOffset, y + 5, 0xf9f9fa);

            if (isDgUser) {

                GlStateManager.translate(xOffset + 5, y + 3.5F, 200F);
                GlStateManager.scale(0.32F, 0.32F, 1F);
                Minecraft.getMinecraft().getTextureManager().bindTexture(logoLoc);
                Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 32, 32, 32, 32);
            }


            GlStateManager.popMatrix();

            y += 12;
        }

    }

    private boolean isDgUser(String partyRawMember) {
        if (Objects.equals(partyRawMember, Minecraft.getMinecraft().getSession().getUsername())) return true;
        return PartyManager.INSTANCE.getPartyContext().isDgUser(partyRawMember);
    }


    private int getColorTextColor(String partyRawMember) {
        if (Objects.equals(genPlayerText(partyRawMember), ": Ready") || Objects.equals(genPlayerText(partyRawMember), ": Not Ready")) {
            boolean isPlayerReady = ready.contains(partyRawMember);
            return isPlayerReady ? 0xFF12bc00 : 0xFFd70022;
        }


        return 0xFF38383d;
    }


    String genPlayerText(String username) {

        if (DungeonsGuide.getDungeonsGuide().getSkyblockStatus().isOnDungeon()) {
            if (Objects.equals(username, Minecraft.getMinecraft().getSession().getUsername())) {
                return ": In Dungeon";
            } else if (isPlayerInDungeon(username)) {
                return ": In Dungeon";
            } else {
                return ": Somewhere";
            }
        } else {
            if (ready.contains(username)) {
                return ": Ready";
            }

            return ": Not Ready";
        }
    }


}
