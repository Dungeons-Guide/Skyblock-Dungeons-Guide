/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features;

import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.ParameterItem;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.config.types.TCDouble;
import kr.syeyoung.dungeonsguide.mod.config.types.TCKeybind;
import kr.syeyoung.dungeonsguide.mod.events.annotations.EventHandlerRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.advanced.*;
import kr.syeyoung.dungeonsguide.mod.features.impl.boss.*;
import kr.syeyoung.dungeonsguide.mod.features.impl.boss.terminal.FeatureSimonSaysSolver;
import kr.syeyoung.dungeonsguide.mod.features.impl.boss.terminal.FeatureTerminalSolvers;
import kr.syeyoung.dungeonsguide.mod.features.impl.cosmetics.FeatureNicknameColor;
import kr.syeyoung.dungeonsguide.mod.features.impl.cosmetics.FeatureNicknamePrefix;
import kr.syeyoung.dungeonsguide.mod.features.impl.cosmetics.FeaturePlayerModel;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteViewer.PartyInviteViewer;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.onlinealarm.PlayingDGAlarm;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.*;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.*;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.ability.FeatureAbilityCooldown;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.FeaturePartyList;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.FeaturePartyReady;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.customgui.FeatureCustomPartyFinder;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.FeatureViewPlayerStatsOnJoin;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.*;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.mechanicbrowser.FeatureMechanicBrowse;
import kr.syeyoung.dungeonsguide.mod.features.impl.solvers.*;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultTextHUDFeatureStyleFeature;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayManager;
import lombok.Getter;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureRegistry {
    @Getter
    private static final List<AbstractFeature> featureList = new ArrayList<AbstractFeature>();
    private static final Map<String, AbstractFeature> featureByKey = new HashMap<String, AbstractFeature>();
    @Getter
    private static final Map<String, List<AbstractFeature>> featuresByCategory = new HashMap<String, List<AbstractFeature>>();
    @Getter
    private static final Map<String, String> categoryDescription = new HashMap<>();

    public static AbstractFeature getFeatureByKey(String key) {
        return featureByKey.get(key);
    }


    public static <T extends AbstractFeature> T register(T abstractFeature) {
        if (featureByKey.containsKey(abstractFeature.getKey())) throw new IllegalArgumentException("DUPLICATE FEATURE DEFINITION");
        featureList.add(abstractFeature);
        featureByKey.put(abstractFeature.getKey(), abstractFeature);
        List<AbstractFeature> features = featuresByCategory.get(abstractFeature.getCategory());
        if (features == null)
            features = new ArrayList<AbstractFeature>();
        features.add(abstractFeature);
        featuresByCategory.put(abstractFeature.getCategory(), features);

        EventHandlerRegistry.registerEvents(abstractFeature);

        return abstractFeature;
    }

    // Global HUD
    public static final DefaultTextHUDFeatureStyleFeature DEFAULT_STYLE = register(new DefaultTextHUDFeatureStyleFeature());

    public static final SimpleFeature GLOBAL_HUD_SCALE = register(new SimpleFeature("Global HUD", "Global HUD Scale", "Configure to use gui scale from Minecraft or specify custom one", "hud.globalscale", false) {
        private boolean init = false;
        {
            addParameter("mc", new FeatureParameter<Boolean>("mc", "Minecraft", "Enable to use minecraft default hud scale", true, TCBoolean.INSTANCE, a -> {
                if (init)
                    OverlayManager.getEventHandler().guiResize(null);
            }));
            addParameter("scale", new FeatureParameter<Double>("scale", "Scale", "Custom HUD Scale",1.0, TCDouble.INSTANCE, a -> {
                if (init)
                    OverlayManager.getEventHandler().guiResize(null);

            }).setWidgetGenerator((param) -> new ParameterItem(param, new TCDouble.DoubleEditWidget(param, 0.1, Double.POSITIVE_INFINITY))));
            init = true;
        }

        @Override
        public boolean isDisableable() {
            return false;
        }
    });


    // Pathfinding & Secrets

    // Display All
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT = register(new PathfindLineProperties("Pathfinding & Secrets.Display All", "Parent Line Settings", "Line Settings to be used by default", "secret.lineproperties.apf.parent", false, null));
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_ITEM_DROP = register(new PathfindLineProperties("Pathfinding & Secrets.Display All", "Item Drop Line Settings", "Line Settings when pathfind to Item Drop, when using above feature", "secret.lineproperties.apf.itemdrop", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_ESSENCE = register(new PathfindLineProperties("Pathfinding & Secrets.Display All", "Essence Line Settings", "Line Settings when pathfind to Essence, when using above feature", "secret.lineproperties.apf.essence", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_CHEST = register(new PathfindLineProperties("Pathfinding & Secrets.Display All", "Chest Line Settings", "Line Settings when pathfind to Chest, when using above feature", "secret.lineproperties.apf.chest", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_BAT = register(new PathfindLineProperties("Pathfinding & Secrets.Display All", "Bat Line Settings", "Line Settings when pathfind to Bat, when using above feature", "secret.lineproperties.apf.bat", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
    public static final FeaturePathfindToAll SECRET_PATHFIND_ALL = register(new FeaturePathfindToAll());

    // Display One
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_AUTOPATHFIND = register(new PathfindLineProperties("Pathfinding & Secrets.Display One", "Line Settings", "Line Settings when pathfinding using above features", "secret.lineproperties.autopathfind", true, null));
    public static final SimpleFeature SECRET_NEXT_KEY = register(new SimpleFeature("Pathfinding & Secrets.Display One", "Auto Pathfind to new secret upon pressing a key", "Auto browse the best next secret when you press key.\nPress settings to edit the key", "secret.keyfornext", false) {{
        addParameter("key", new FeatureParameter<Integer>("key", "Key", "Press to navigate to next best secret", Keyboard.KEY_NONE, TCKeybind.INSTANCE));
    }});
    public static final SimpleFeature SECRET_AUTO_START = register(new SimpleFeature("Pathfinding & Secrets.Display One", "Auto pathfind to new secret", "Auto browse best secret upon entering the room.", "secret.autouponenter", false));
    public static final SimpleFeature SECRET_AUTO_BROWSE_NEXT = register(new SimpleFeature("Pathfinding & Secrets.Display One", "Auto Pathfind to next secret", "Auto browse best next secret after current one completes.\nthe first pathfinding of first secret needs to be triggered first in order for this option to work", "secret.autobrowse", false));

    // Smart Route
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_SMART_ROUTE = register(new PathfindLineProperties("Pathfinding & Secrets.Smart Route", "Line Settings", "Line Settings when pathfinding using above features", "secret.lineproperties.smartroute", true, null));
    public static final SimpleFeature SECRET_SMART_AUTO_START = register(new SimpleFeature("Pathfinding & Secrets.Smart Route", "Auto pathfind to new secret", "Generate smart route going through all secrets upon entering the room.", "secret.smartroute", false));
    public static final SimpleFeature SECRET_SMART_KEYBIND = register(new SimpleFeature("Pathfinding & Secrets.Smart Route", "Auto generate smart route upon pressing a key", "Generate smart route going through all secrets upon entering the room.\nPress settings to edit the key", "secret.smartroutekeynext", false) {{
        addParameter("key", new FeatureParameter<Integer>("key", "Key", "Press to navigate to next best secret", Keyboard.KEY_NONE, TCKeybind.INSTANCE));
    }});

    // Secret Browser
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_SECRET_BROWSER = register(new PathfindLineProperties("Pathfinding & Secrets.Secret Browser", "Line Settings", "Line Settings when pathfinding using Secret Browser", "secret.lineproperties.secretbrowser", true, null));
    public static final FeatureMechanicBrowse SECRET_BROWSE = register(new FeatureMechanicBrowse());

    // Blood Rush
    public static final PathfindLineProperties SECRET_BLOOD_RUSH_LINE_PROPERTIES = register(new PathfindLineProperties("Pathfinding & Secrets.Blood Rush", "Blood Rush Line Settings", "Line Settings to be used", "secret.lineproperties.bloodrush", false, null));
    public static final FeatureBloodRush SECRET_BLOOD_RUSH = register(new FeatureBloodRush());


    public static final FeatureCreateRefreshLine SECRET_CREATE_REFRESH_LINE = register(new FeatureCreateRefreshLine());
    public static final FeatureFreezePathfind SECRET_FREEZE_LINES = register(new FeatureFreezePathfind());
    public static final FeatureTogglePathfind SECRET_TOGGLE_KEY = register(new FeatureTogglePathfind());
    public static final FeaturePathfindStrategy SECRET_PATHFIND_STRATEGY = register(new FeaturePathfindStrategy());
    public static final FeaturePathfindSettings SECRET_PATHFIND_SETTINGS = register(new FeaturePathfindSettings());
    public static final FeatureActions SECRET_ACTIONS = register(new FeatureActions());
    public static final FeatureDAGs SECRET_DAGS = register(new FeatureDAGs());



    static {
        categoryDescription.put("ROOT.Secrets.Keybinds", "Useful keybinds / Toggle Pathfind lines, Freeze Pathfind lines, Refresh pathfind line or Trigger pathfind (you would want to use it, if you're using Pathfind to All)");
    }



    // Puzzle Solvers
    public static final FeatureSolverBombdefuse SOLVER_BOMBDEFUSE = register(new FeatureSolverBombdefuse());
    public static final FeatureSolverKahoot SOLVER_KAHOOT = register(new FeatureSolverKahoot());
    public static final FeatureSolverBox SOLVER_BOX = register(new FeatureSolverBox());
    public static final FeatureSolverSilverfish SOLVER_SILVERFISH = register(new FeatureSolverSilverfish());
    public static final FeatureSolverIcefill SOLVER_ICEPATH = register(new FeatureSolverIcefill());
    public static final FeatureSolverBlaze SOLVER_BLAZE = register(new FeatureSolverBlaze());
    public static final FeatureSolverTeleport SOLVER_TELEPORT = register(new FeatureSolverTeleport());
    public static final SimpleFeature SOLVER_CREEPER = register(new SimpleFeature("Puzzle Solvers", "Creeper", "Draws line between prismarine lamps in creeper room", "solver.creeper"));
    public static final SimpleFeature SOLVER_WATERPUZZLE = register(new SimpleFeature("Puzzle Solvers", "Waterboard (Advanced)", "Calculates solution for waterboard puzzle and displays it to user", "solver.waterboard"));
    public static final FeatureSolverTictactoe SOLVER_TICTACTOE = register(new FeatureSolverTictactoe());
    public static final FeatureSolverRiddle SOLVER_RIDDLE = register(new FeatureSolverRiddle());

    // BossfightsBossfights
    public static final FeatureBossHealth BOSSFIGHT_HEALTH = register(new FeatureBossHealth());
    public static final FeatureHideAnimals BOSSFIGHT_HIDE_ANIMALS = register(new FeatureHideAnimals());
    public static final FeatureThornBearPercentage BOSSFIGHT_BEAR_PERCENT = register(new FeatureThornBearPercentage());
    public static final FeatureThornSpiritBowTimer BOSSFIGHT_BOW_TIMER = register(new FeatureThornSpiritBowTimer());
    public static final FeatureBoxRealLivid BOSSFIGHT_BOX_REALLIVID = register(new FeatureBoxRealLivid());
    public static final FeatureTerracotaTimer BOSSFIGHT_TERRACOTTA_TIMER = register(new FeatureTerracotaTimer());
    public static final FeatureCurrentPhase BOSSFIGHT_CURRENT_PHASE = register(new FeatureCurrentPhase());
    public static final FeatureTerminalSolvers BOSSFIGHT_TERMINAL_SOLVERS = register(new FeatureTerminalSolvers());
    public static final FeatureSimonSaysSolver BOSSFIGHT_SIMONSAYS_SOLVER = register(new FeatureSimonSaysSolver());

    // Discord RPC
    public static final SimpleFeature DISCORD_RICHPRESENCE = register(new SimpleFeature("Discord", "Discord RPC", "Enable Discord rich presence", "advanced.discordrichpresence", true) {
        {
            addParameter("disablenotskyblock", new FeatureParameter<Boolean>("disablenotskyblock", "Disable When not on Skyblock", "Disable When not on skyblock", false, TCBoolean.INSTANCE));
        }
    });
    public static final PartyInviteViewer DISCORD_ASKTOJOIN = register(new PartyInviteViewer());
    public static final PlayingDGAlarm DISCORD_ONLINEALARM = register(new PlayingDGAlarm());

    // Cosmetics
    public static final FeatureNicknameColor COSMETIC_NICKNAMECOLOR = register(new FeatureNicknameColor());
    public static final FeatureNicknamePrefix COSMETIC_PREFIX = register(new FeatureNicknamePrefix());
    public static final FeaturePlayerModel COSMETIC_PLAYER_MODEL = register(new FeaturePlayerModel());

    // Debug
    public static final FeatureDebug DEBUG = register(new FeatureDebug());
    public static final FeatureStonkDebug DEBUG_ST = register(new FeatureStonkDebug());
    public static final FeatureCompareRoom COMPARE_ROOM = register(new FeatureCompareRoom());
    public static final FeatureRoomEdit ADVANCED_ROOMEDIT = register(new FeatureRoomEdit());
    public static final FeatureRoomDebugInfo ADVANCED_DEBUG_ROOM = register(new FeatureRoomDebugInfo());
    public static final FeatureDebuggableMap ADVANCED_DEBUGGABLE_MAP = register(new FeatureDebuggableMap());
    public static final FeatureRoomCoordDisplay ADVANCED_COORDS = register(new FeatureRoomCoordDisplay());
    public static final FeatureDebugTrap ADVANCED_BAT = register(new FeatureDebugTrap());

    public static final SimpleFeature DEBUG_BLOCK_CACHING = register(new SimpleFeature("Debug","Enable block getBlockCaching", "Cache all world.getBlockState callls", "debug.blockcache"));

    // Dungeon Party
    public static final FeatureAutoReparty BOSSFIGHT_AUTOREPARTY = register(new FeatureAutoReparty());
    public static final FeatureViewPlayerStatsOnJoin PARTYKICKER_VIEWPLAYER = register(new FeatureViewPlayerStatsOnJoin());
    public static final FeatureCustomPartyFinder PARTYKICKER_CUSTOM = register(new FeatureCustomPartyFinder());
    public static final FeaturePartyList PARTY_LIST = register(new FeaturePartyList());
    public static final FeaturePartyReady PARTY_READY = register(new FeaturePartyReady());
    public static final FeatureAutoAcceptReparty ETC_AUTO_ACCEPT_REPARTY = register(new FeatureAutoAcceptReparty());
    public static final FeatureRepartyCommand ETC_REPARTY = register(new FeatureRepartyCommand());

    // Data Communication
    public static final FeatureCollectScore ETC_COLLECT_SCORE = register(new FeatureCollectScore());
    public static final SimpleFeature DUNGEON_INTERMODCOMM = register(new SimpleFeature("Communication", "Communicate With Other's Dungeons Guide", "Sends total secret in the room to others\nSo that they can use the data to calculate total secret in dungeon run\n\nThis automates player chatting action, (chatting data) Thus it might be against hypixel's rules.\nBut mods like auto-gg which also automate player action and is kinda allowed mod exist so I'm leaving this feature.\nThis option is use-at-your-risk and you'll be responsible for ban if you somehow get banned because of this feature\n(Although it is not likely to happen)\nDefaults to off", "dungeon.intermodcomm", false));


    // Dungeon HUD

    // Alerts
    public static final FeatureSoulRoomWarning DUNGEON_FAIRYSOUL = register(new FeatureSoulRoomWarning());
    public static final FeatureWatcherWarning DUNGEON_WATCHERWARNING = register(new FeatureWatcherWarning());
    public static final FeatureWarnLowHealth DUNGEON_LOWHEALTH_WARN = register(new FeatureWarnLowHealth());

    // In Dungeon HUD
    public static final FeatureDungeonScore DUNGEON_SCORE = register(new FeatureDungeonScore());
    public static final FeatureDungeonTombs DUNGEON_TOMBS = register(new FeatureDungeonTombs());
    public static final FeatureDungeonCurrentRoomSecrets DUNGEON_SECRETS_ROOM = register(new FeatureDungeonCurrentRoomSecrets());
    public static final FeatureDungeonSecrets DUNGEON_SECRETS = register(new FeatureDungeonSecrets());
    public static final FeatureDungeonSBTime DUNGEON_SBTIME = register(new FeatureDungeonSBTime());
    public static final FeatureDungeonRealTime DUNGEON_REALTIME = register(new FeatureDungeonRealTime());
    public static final FeatureDungeonMilestone DUNGEON_MILESTONE = register(new FeatureDungeonMilestone());
    public static final FeatureDungeonDeaths DUNGEON_DEATHS = register(new FeatureDungeonDeaths());
    public static final FeatureDungeonRoomName DUNGEON_ROOMNAME = register(new FeatureDungeonRoomName());


    public static final FeatureDungeonMap DUNGEON_MAP = register(new FeatureDungeonMap());
//    public static final FeatureChestPrice BOSSFIGHT_CHESTPRICE = register(new FeatureChestPrice());
    public static final FeatureWarningOnPortal BOSSFIGHT_WARNING_ON_PORTAL = register(new FeatureWarningOnPortal());
    public static final FeatureEpicCountdown EPIC_COUNTDOWN = register(new FeatureEpicCountdown());


    // Player & Mob Utils
    public static final FeatureBoxStarMobs DUNGEON_BOXSTARMOBS = register(new FeatureBoxStarMobs());
    public static final FeatureBoxBats DUNGEON_BOXBAT = register(new FeatureBoxBats());
    public static final FeatureBoxSkelemaster DUNGEON_BOXSKELEMASTER = register(new FeatureBoxSkelemaster());
    public static final FeaturePenguins ETC_PENGUIN = register(new FeaturePenguins());
    public static final FeatureHideNameTags DUNGEON_HIDENAMETAGS = register(new FeatureHideNameTags());
    public static final FeaturePlayerESP DUNGEON_PLAYERESP = register(new FeaturePlayerESP());

    // Misc

    public static final FeatureCollectDiagnostics COLLECT_ERRORS = register(new FeatureCollectDiagnostics());
//    public static final FeatureCollectDungeonRooms COLLECT_DUNGEON_ROOMS = register(new FeatureCollectDungeonRooms());

    // HUD
    public static final FeatureAbilityCooldown ETC_ABILITY_COOLDOWN = register(new FeatureAbilityCooldown());
    public static final FeatureCooldownCounter ETC_COOLDOWN = register(new FeatureCooldownCounter());

    // Chat Utils
    public static final FeatureDisableMessage FIX_MESSAGES = register(new FeatureDisableMessage());
    public static final FeatureCopyMessages ETC_COPY_MSG = register(new FeatureCopyMessages());

    // API


    public static final FeatureDetectFreeze FREEZE_DETECTOR = register(new FeatureDetectFreeze());

    public static final FeatureTooltipDungeonStat ETC_DUNGEONSTAT = register(new FeatureTooltipDungeonStat());
    public static final FeatureTooltipPrice ETC_PRICE = register(new FeatureTooltipPrice());
    public static final FeatureDecreaseExplosionSound ETC_EXPLOSION_SOUND = register(new FeatureDecreaseExplosionSound());

    public static final FeatureUpdateAlarm ETC_TEST = register(new FeatureUpdateAlarm());
    public static final SimpleFeature FIX_SPIRIT_BOOTS = register(new SimpleFeature("Misc", "Spirit Boots Fixer", "Fix Spirit boots messing up with inventory", "fixes.spirit", true));

    public static final FeaturePressAnyKeyToCloseChest DUNGEON_CLOSECHEST = register(new FeaturePressAnyKeyToCloseChest());

}
