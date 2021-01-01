package kr.syeyoung.dungeonsguide.features;

import kr.syeyoung.dungeonsguide.features.impl.FeatureCooldownCounter;
import kr.syeyoung.dungeonsguide.features.impl.FeatureInstaCloseChest;
import kr.syeyoung.dungeonsguide.features.impl.FeatureTooltipDungeonStat;
import kr.syeyoung.dungeonsguide.features.impl.FeatureTooltipPrice;
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

    public static final FeatureTooltipDungeonStat TOOLTIP_DUNGEONSTAT = register(new FeatureTooltipDungeonStat());
    public static final FeatureTooltipPrice TOOLTIP_PRICE = register(new FeatureTooltipPrice());

    public static final SimpleFeature ADVANCED_ROOMEDIT = register(new SimpleFeature("advanced", "Room Edit", "Allow editing dungeon rooms\n\nWarning: using this feature can break or freeze your Minecraft\nThis is only for advanced users only", "advanced.roomedit", false));


    public static final SimpleFeature DEBUG = register(new SimpleFeature("hidden", "Debug", "Toggles debug mode", "debug", false));

    public static final FeatureCooldownCounter QOL_COOLDOWN =  register(new FeatureCooldownCounter());
    public static final FeatureInstaCloseChest QOL_INSTACLOSE = register(new FeatureInstaCloseChest());
}
