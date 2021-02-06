package kr.syeyoung.dungeonsguide.features;

import kr.syeyoung.dungeonsguide.features.impl.advanced.FeatureDebuggableMap;
import kr.syeyoung.dungeonsguide.features.impl.advanced.FeatureRoomCoordDisplay;
import kr.syeyoung.dungeonsguide.features.impl.advanced.FeatureRoomDebugInfo;
import kr.syeyoung.dungeonsguide.features.impl.boss.*;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.*;
import kr.syeyoung.dungeonsguide.features.impl.etc.FeatureCooldownCounter;
import kr.syeyoung.dungeonsguide.features.impl.etc.FeatureDisableMessage;
import kr.syeyoung.dungeonsguide.features.impl.etc.FeatureTooltipDungeonStat;
import kr.syeyoung.dungeonsguide.features.impl.etc.FeatureTooltipPrice;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureRegistry {
    @Getter
    private static List<AbstractFeature> featureList = new ArrayList<AbstractFeature>();
    private static Map<String, AbstractFeature> featureByKey = new HashMap<String, AbstractFeature>();
    @Getter
    private static Map<String, List<AbstractFeature>> featuresByCategory = new HashMap<String, List<AbstractFeature>>();

    public static AbstractFeature getFeatureByKey(String key) {
        return featureByKey.get(key);
    }

    public static <T extends AbstractFeature> T register(T abstractFeature) {
        featureList.add(abstractFeature);
        featureByKey.put(abstractFeature.getKey(), abstractFeature);
        List<AbstractFeature> features = featuresByCategory.get(abstractFeature.getCategory());
        if (features == null)
            features = new ArrayList<AbstractFeature>();
        features.add(abstractFeature);
        featuresByCategory.put(abstractFeature.getCategory(), features);

        return abstractFeature;
    }
    public static final SimpleFeature DEBUG = register(new SimpleFeature("hidden", "Debug", "Toggles debug mode", "debug", false));

    public static final SimpleFeature ADVANCED_ROOMEDIT = register(new SimpleFeature("advanced", "Room Edit", "Allow editing dungeon rooms\n\nWarning: using this feature can break or freeze your Minecraft\nThis is only for advanced users only", "advanced.roomedit", false));
    public static final FeatureRoomDebugInfo ADVANCED_DEBUG_ROOM = register(new FeatureRoomDebugInfo());
    public static final FeatureDebuggableMap ADVANCED_DEBUGGABLE_MAP = register(new FeatureDebuggableMap());
    public static final FeatureRoomCoordDisplay ADVANCED_COORDS = register(new FeatureRoomCoordDisplay());

    public static final SimpleFeature SOLVER_RIDDLE = register(new SimpleFeature("solver", "Riddle Puzzle (3 weirdo) Solver", "Highlights the correct box after clicking on all 3 weirdos",  "solver.riddle"));
    public static final SimpleFeature SOLVER_KAHOOT = register(new SimpleFeature("solver", "Trivia Puzzle (Omnicrescent) Solver", "Highlights the correct solution for trivia puzzle",  "solver.trivia"));
    public static final SimpleFeature SOLVER_BLAZE = register(new SimpleFeature("solver", "Blaze Puzzle Solver", "Highlights the blaze that needs to be killed in an blaze room", "solver.blaze"));
    public static final SimpleFeature SOLVER_TICTACTOE = register(new SimpleFeature("solver", "Tictactoe Solver", "Shows the best move that could be taken by player in the tictactoe room", "solver.tictactoe"));
    public static final SimpleFeature SOLVER_ICEPATH = register(new SimpleFeature("solver", "Icepath Puzzle Solver (Advanced)", "Calculates solution for icepath puzzle and displays it to user",  "solver.icepath"));
    public static final SimpleFeature SOLVER_SILVERFISH = register(new SimpleFeature("solver", "Silverfish Puzzle Solver (Advanced)", "Actively calculates solution for silverfish puzzle and displays it to user",  "solver.silverfish"));
    public static final SimpleFeature SOLVER_WATERPUZZLE = register(new SimpleFeature("solver", "Waterboard Puzzle Solver (Advanced)", "Calculates solution for waterboard puzzle and displays it to user",  "solver.waterboard"));
    public static final SimpleFeature SOLVER_BOX = register(new SimpleFeature("solver", "Box Puzzle Solver (Advanced)", "Calculates solution for box puzzle room, and displays it to user",  "solver.box"));
    public static final SimpleFeature SOLVER_CREEPER = register(new SimpleFeature("solver", "Creeper Puzzle Solver", "Draws line between prismarine lamps in creeper room",  "solver.creeper"));
    public static final SimpleFeature SOLVER_TELEPORT = register(new SimpleFeature("solver", "Teleport Puzzle Solver", "Shows teleport pads you've visited in a teleport maze room",  "solver.teleport"));
    public static final SimpleFeature SOLVER_BOMBDEFUSE = register(new SimpleFeature("solver", "Bomb Defuse Puzzle Solver", "Communicates with others dg using key 'F' for solutions and displays it",  "solver.bombdefuse"));

    public static final FeatureTooltipDungeonStat ETC_DUNGEONSTAT = register(new FeatureTooltipDungeonStat());
    public static final FeatureTooltipPrice ETC_PRICE = register(new FeatureTooltipPrice());
    public static final FeatureCooldownCounter ETC_COOLDOWN =  register(new FeatureCooldownCounter());
    public static final SimpleFeature ETC_REMOVE_REPARTY =  register(new SimpleFeature("ETC", "Remove Reparty Command From DG", "/dg reparty will still work, Auto reparty will still work\nRequires Restart to get applied", "qol.noreparty"));

    public static final SimpleFeature FIX_SPIRIT_BOOTS = register(new SimpleFeature("fixes", "Spirit Boots Fixer", "Fix Spirit boots messing up with inventory", "fixes.spirit", true));
    public static final FeatureDisableMessage FIX_MESSAGES = register(new FeatureDisableMessage());


    public static final SimpleFeature BOSSFIGHT_CHESTPRICE = register(new FeatureChestPrice());
    public static final FeatureAutoReparty BOSSFIGHT_AUTOREPARTY = register(new FeatureAutoReparty());
    public static final FeatureBoxRealLivid BOSSFIGHT_BOX_REALLIVID = register(new FeatureBoxRealLivid());
    public static final FeatureBossHealth BOSSFIGHT_HEALTH = register(new FeatureBossHealth());
    public static final FeatureThornBearPercentage BOSSFIGHT_BEAR_PERCENT = register(new FeatureThornBearPercentage());

    public static final FeatureInstaCloseChest DUNGEON_INSTACLOSE = register(new FeatureInstaCloseChest());
    public static final FeatureBoxSkelemaster DUNGEON_BOXSKELEMASTER = register(new FeatureBoxSkelemaster());
    public static final FeatureBoxStarMobs DUNGEON_BOXSTARMOBS = register(new FeatureBoxStarMobs());
    public static final FeatureDungeonDeaths DUNGEON_DEATHS = register(new FeatureDungeonDeaths());
    public static final FeatureDungeonMilestone DUNGEON_MILESTONE = register(new FeatureDungeonMilestone());
    public static final FeatureDungeonRealTime DUNGEON_REALTIME = register(new FeatureDungeonRealTime());
    public static final FeatureDungeonSBTime DUNGEON_SBTIME = register(new FeatureDungeonSBTime());
    public static final FeatureDungeonSecrets DUNGEON_SECRETS = register(new FeatureDungeonSecrets());
    public static final FeatureDungeonTombs DUNGEON_TOMBS = register(new FeatureDungeonTombs());
    public static final FeatureDungeonScore DUNGEON_SCORE = register(new FeatureDungeonScore());
    public static final FeatureWarnLowHealth DUNGEON_LOWHEALTH_WARN = register(new FeatureWarnLowHealth());
    public static final SimpleFeature DUNGEON_INTERMODCOMM = register(new SimpleFeature("Dungeon", "Communicate With Other's Dungeons Guide", "Sends total secret in the room to others\nSo that they can use the data to calculate total secret in dungeon run\n\nThis automates player chatting action, (chatting data) Thus it might be against hypixel's rules.\nBut mods like auto-gg which also automate player action and is kinda allowed mod exist so I'm leaving this feature.\nThis option is use-at-your-risk and you'll be responsible for ban if you somehow get banned because of this feature\n(Although it is not likely to happen)\nDefaults to off", "dungeon.intermodcomm", false));
    public static final FeatureDungeonMap DUNGEON_MAP = register(new FeatureDungeonMap());

}
