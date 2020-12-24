package kr.syeyoung.dungeonsguide;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class Config {
    public static Configuration configuration;

    public static final String CATEGORY_ADVANCED = "advanced";
    public static final String CATEGORY_PUZZLE_SOLVER = "solver";

    public static boolean DEBUG = true;

    public static boolean solver_riddle;
    public static boolean solver_kahoot;
    public static boolean solver_blaze;
    public static boolean solver_tictactoe;
    public static boolean solver_icepath;
    public static boolean solver_icesilverfish;
    public static boolean solver_waterpuzzle;
    public static boolean solver_box;
    public static boolean solver_creeper;
    public static boolean solver_teleport;

    public static void syncConfig(boolean load) { // Gets called from preInit
        try {
            // Load config
            if (load)
                configuration.load();

            // Read props from config
            solver_riddle = configuration.get(Config.CATEGORY_PUZZLE_SOLVER, "riddle", "true", "Riddle puzzle solver", Property.Type.BOOLEAN).getBoolean();
            solver_kahoot = configuration.get(Config.CATEGORY_PUZZLE_SOLVER, "kahoot", "true", "Omnicrescent puzzle solver", Property.Type.BOOLEAN).getBoolean();
            solver_teleport = configuration.get(Config.CATEGORY_PUZZLE_SOLVER, "teleport", "true", "Teleport puzzle solver", Property.Type.BOOLEAN).getBoolean();
            solver_creeper = configuration.get(Config.CATEGORY_PUZZLE_SOLVER, "creeper", "true", "Creeper puzzle solver", Property.Type.BOOLEAN).getBoolean();
            solver_blaze = configuration.get(Config.CATEGORY_PUZZLE_SOLVER, "blaze", "true", "Blaze puzzle solver", Property.Type.BOOLEAN).getBoolean();
            solver_tictactoe = configuration.get(Config.CATEGORY_PUZZLE_SOLVER, "tictactoe", "true", "Tictactoe puzzle solver", Property.Type.BOOLEAN).getBoolean();
            solver_icepath = configuration.get(Config.CATEGORY_PUZZLE_SOLVER, "icepath", "true", "Icepath puzzle solver", Property.Type.BOOLEAN).getBoolean();
            solver_icesilverfish = configuration.get(Config.CATEGORY_PUZZLE_SOLVER, "silverfish", "true", "Silverfish puzzle solver", Property.Type.BOOLEAN).getBoolean();
            solver_waterpuzzle = configuration.get(Config.CATEGORY_PUZZLE_SOLVER, "water", "true", "Water puzzle solver", Property.Type.BOOLEAN).getBoolean();
            solver_box = configuration.get(Config.CATEGORY_PUZZLE_SOLVER, "box", "true", "Box puzzle solver", Property.Type.BOOLEAN).getBoolean();



            DEBUG = configuration.get(Config.CATEGORY_ADVANCED, "debug","false", "Enable debug mode", Property.Type.BOOLEAN).getBoolean(); // Comment
        } catch (Exception e) {
            // Failed reading/writing, just continue
        } finally {
            // Save props to config IF config changed
            if (configuration.hasChanged()) configuration.save();
        }
    }
}
