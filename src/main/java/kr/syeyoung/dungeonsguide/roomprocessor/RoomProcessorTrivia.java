package kr.syeyoung.dungeonsguide.roomprocessor;

import kr.syeyoung.dungeonsguide.Config;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.SkyblockUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.math.NumberUtils;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoomProcessorTrivia extends GeneralRoomProcessor {

    public RoomProcessorTrivia(DungeonRoom dungeonRoom) {
        super(dungeonRoom);
//        for (Map.Entry<String, String[]> answer : answers.entrySet()) {
//            StringBuilder sb = new StringBuilder();
//            for (String s : answer.getValue()) {
//                sb.append(s).append(',');
//            }
//            dungeonRoom.getDungeonRoomInfo().getProperties().put(answer.getKey(), sb.toString());
//        }
    }


    private List<String> questionDialog = new ArrayList<String>();
    private boolean questionDialogStart = false;

//    private static final Map<String, String[]> answers = new HashMap<String,String[]>() {{
//        put("what is the status of the watcher?", new String[]{"stalker"});
//        put("what is the status of bonzo?", new String[]{"new necromancer"});
//        put("what is the status of scarf?", new String[]{"apprentice necromancer"});
//        put("what is the status of the professor?", new String[]{"professor"});
//        put("what is the status of thorn?", new String[]{"shaman necromancer"});
//        put("what is the status of livid?", new String[]{"master necromancer"});
//        put("what is the status of sadan?", new String[]{"necromancer lord"});
//        put("what is the status of maxor?", new String[]{"young wither"});
//        put("what is the status of goldor?", new String[]{"wither soldier"});
//        put("what is the status of storm?", new String[]{"elementalist"});
//        put("what is the status of necron?", new String[]{"wither lord"});
//        put("how many total fairy souls are there?", new String[]{"209 fairy souls"});
//        put("how many fairy souls are there in spider's den?", new String[]{"17"});
//        put("how many fairy souls are there in the end?", new String[]{"12"});
//        put("how many fairy souls are there in the barn?", new String[]{"7"});
//        put("how many fairy souls are there in mushroom desert?", new String[]{"8"});
//        put("how many fairy souls are there in blazing fortress?", new String[]{"19"});
//        put("how many fairy souls are there in the park?", new String[]{"11"});
//        put("how many fairy souls are there in jerry's workshop?", new String[]{"5"});
//        put("how many fairy souls are there in the hub?", new String[]{"79"});
//        put("how many fairy souls are there in deep caverns?", new String[]{"21"});
//        put("how many fairy souls are there in gold mine?", new String[]{"12"});
//        put("how many fairy souls are there in dungeon hub?", new String[]{"7"});
//        put("which brother is on the spider's den?", new String[]{"rick"});
//        put("what is the name of rick's brother?", new String[]{"pat"});
//        put("what is the name of the painter in the hub?", new String[]{"marco"});
//        put("what is the name of the person that upgrades pets?", new String[]{"kat"});
//        put("what is the name of the lady of the nether?", new String[]{"elle"});
//        put("which villager in the village gives you a rogue sword?", new String[]{"jamie"});
//        put("how many unique minions are there?", new String[]{"52"});
//        put("which of these enemies does not spawn in the spider's den?", new String[]{"zombie spider","cave spider","broodfather"});
//        put("which of these monsters only spawns at night?", new String[]{"zombie villager","ghast"});
//        put("which of these is not a dragon in the end?", new String[]{"zoomer dragon","weak dragon","stonk dragon","holy dragon","boomer dragon","stable dragon"});
//    }};
    @Override
    public void chatReceived(IChatComponent chat) {
        super.chatReceived(chat);
        if (!Config.solver_kahoot) return;
        String ch2 = chat.getUnformattedText();
        if (chat.getFormattedText().contains("§r§6§lQuestion ")) {
            questionDialogStart = true;
            questionDialog.clear();
        }
        if (questionDialogStart && (chat.getFormattedText().startsWith("§r  ") || chat.getFormattedText().trim().startsWith("§r§6 "))) {
            questionDialog.add(chat.getFormattedText());
        }

        if (chat.getFormattedText().contains("§r§6 ⓒ")) {
            questionDialogStart = false;
            parseDialog();
        }
    }
    public static final Pattern anwerPattern = Pattern.compile("§r§6 . §a(.+)§r");
    private void parseDialog() {
        String question = TextUtils.stripColor(questionDialog.get(1)).trim();
        String answerA = getAnswer(questionDialog.get(2));
        String answerB = getAnswer(questionDialog.get(3));
        String answerC = getAnswer(questionDialog.get(4));
        String theRealAnswer = match(question, answerA, answerB, answerC);

        if (theRealAnswer == null)
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §eTrivia §7:: §cCouldn't determine the answer! (no question found)"));
        else if (theRealAnswer.length() >1)
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §eTrivia §7:: §cCouldn't determine the answer! ("+theRealAnswer+")"));
        else
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §7:: §eTrivia §7:: "+theRealAnswer+"§f is the correct answer!"));
        correctAnswer = theRealAnswer;
    }
    String correctAnswer;


    private String getAnswer(String answerString) {
        Matcher matcher = anwerPattern.matcher(answerString.trim());
        if (!matcher.matches()) return "";
        return matcher.group(1);
    }
    private String match(String question, String a, String b, String c) {
        String semi_answers = (String) getDungeonRoom().getDungeonRoomInfo().getProperties().get(question.toLowerCase().trim());
        if (semi_answers == null) return null;
        semi_answers = takeCareOfPlaceHolders(semi_answers);
        String[] answers = semi_answers.split(",");
        if (match(answers, a)) return "A";
        if (match(answers, b)) return "B";
        if (match(answers, c)) return "C";
        return semi_answers;
    }

    private String takeCareOfPlaceHolders(String input) {
        String str = input;
        if (str.contains("$year")) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§eDungeons Guide §fuses §eInventiveTalent§7(https://github.com/InventivetalentDev)§e's Skyblock Api §fto fetch current skyblock year!"));
            try {
                str = str.replace("$year", SkyblockUtils.getSkyblockYear()+"");
            } catch (IOException e) {
                str = str.replace("$year", "Couldn't determine current skyblock year :: "+e.getMessage());
            }
        }
        return str;
    }
    private boolean match(String[] match, String match2) {
        for (String s : match) {
            if (NumberUtils.isNumber(s)) {
                if (match2.toLowerCase().contains(s)) return true;
            } else {
                if (match2.equalsIgnoreCase(s)) return true;
            }
        }
        return false;
    }

    @Override
    public void drawWorld(float partialTicks) {
        super.drawWorld(partialTicks);
        if (!Config.solver_kahoot) return;
        if (correctAnswer == null) return;

        OffsetPoint op = (OffsetPoint) getDungeonRoom().getDungeonRoomInfo().getProperties().get(correctAnswer);
        if (op != null) {
            RenderUtils.highlightBlock(op.getBlockPos(getDungeonRoom()), new Color(0,255,0,50), partialTicks);
        }
    }

    public static class Generator implements RoomProcessorGenerator<RoomProcessorTrivia> {
        @Override
        public RoomProcessorTrivia createNew(DungeonRoom dungeonRoom) {
            RoomProcessorTrivia defaultRoomProcessor = new RoomProcessorTrivia(dungeonRoom);
            return defaultRoomProcessor;
        }
    }

    @Override
    public boolean readGlobalChat() {
        return true;
    }
}
