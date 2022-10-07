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

package kr.syeyoung.dungeonsguide.features;

import kr.syeyoung.dungeonsguide.features.impl.advanced.*;
import kr.syeyoung.dungeonsguide.features.impl.boss.*;
import kr.syeyoung.dungeonsguide.features.impl.boss.terminal.FeatureSimonSaysSolver;
import kr.syeyoung.dungeonsguide.features.impl.boss.terminal.FeatureTerminalSolvers;
import kr.syeyoung.dungeonsguide.features.impl.cosmetics.FeatureNicknameColor;
import kr.syeyoung.dungeonsguide.features.impl.cosmetics.FeatureNicknamePrefix;
import kr.syeyoung.dungeonsguide.features.impl.discord.inviteViewer.PartyInviteViewer;
import kr.syeyoung.dungeonsguide.features.impl.discord.onlinealarm.PlayingDGAlarm;
import kr.syeyoung.dungeonsguide.features.impl.dungeon.*;
import kr.syeyoung.dungeonsguide.features.impl.etc.*;
import kr.syeyoung.dungeonsguide.features.impl.etc.ability.FeatureAbilityCooldown;
import kr.syeyoung.dungeonsguide.features.impl.party.APIKey;
import kr.syeyoung.dungeonsguide.features.impl.party.FeaturePartyList;
import kr.syeyoung.dungeonsguide.features.impl.party.FeaturePartyReady;
import kr.syeyoung.dungeonsguide.features.impl.party.customgui.FeatureCustomPartyFinder;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.FeatureViewPlayerStatsOnJoin;
import kr.syeyoung.dungeonsguide.features.impl.secret.*;
import kr.syeyoung.dungeonsguide.features.impl.secret.mechanicbrowser.FeatureMechanicBrowse;
import kr.syeyoung.dungeonsguide.features.impl.solvers.*;
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

        return abstractFeature;
    }



    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_GLOBAL;

    static {
        SECRET_LINE_PROPERTIES_GLOBAL = register(new PathfindLineProperties("Dungeon Secrets", "Global Line Settings", "Global Line Settings", "secret.lineproperties.global", true, null));
    }


    public static final FeatureMechanicBrowse SECRET_BROWSE;
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_SECRET_BROWSER;
    public static final FeatureActions SECRET_ACTIONS;

    public static final FeaturePathfindStrategy SECRET_PATHFIND_STRATEGY;
    public static final FeatureTogglePathfind SECRET_TOGGLE_KEY;
    public static final FeatureFreezePathfind SECRET_FREEZE_LINES;
    public static final FeatureCreateRefreshLine SECRET_CREATE_REFRESH_LINE;

    public static final SimpleFeature SECRET_AUTO_BROWSE_NEXT;
    public static final SimpleFeature SECRET_AUTO_START;
    public static final SimpleFeature SECRET_NEXT_KEY;

    public static final SimpleFeature SECRET_BLOOD_RUSH;
    public static final PathfindLineProperties SECRET_BLOOD_RUSH_LINE_PROPERTIES;


    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_AUTOPATHFIND;

    public static final FeaturePathfindToAll SECRET_PATHFIND_ALL;
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT;

    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_BAT;
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_CHEST;
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_ESSENCE;
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_ITEM_DROP;

    public static final FeatureSolverRiddle SOLVER_RIDDLE;
    public static final FeatureSolverTictactoe SOLVER_TICTACTOE;
    public static final SimpleFeature SOLVER_WATERPUZZLE;
    public static final SimpleFeature SOLVER_CREEPER;
    public static final FeatureSolverTeleport SOLVER_TELEPORT;
    public static final FeatureSolverBlaze SOLVER_BLAZE;
    public static final FeatureSolverIcefill SOLVER_ICEPATH;
    public static final FeatureSolverSilverfish SOLVER_SILVERFISH;
    public static final FeatureSolverBox SOLVER_BOX;
    public static final FeatureSolverKahoot SOLVER_KAHOOT;
    public static final FeatureSolverBombdefuse SOLVER_BOMBDEFUSE;


    public static final FeatureDungeonMap DUNGEON_MAP;
    public static final FeatureTestPepole TEST_PEPOLE;
    public static final FeatureDungeonRoomName DUNGEON_ROOMNAME;
    public static final FeaturePressAnyKeyToCloseChest DUNGEON_CLOSECHEST;
    public static final FeatureBoxSkelemaster DUNGEON_BOXSKELEMASTER;
    public static final FeatureBoxBats DUNGEON_BOXBAT;
    public static final FeatureBoxStarMobs DUNGEON_BOXSTARMOBS;
    public static final FeatureWatcherWarning DUNGEON_WATCHERWARNING;
    public static final FeatureDungeonDeaths DUNGEON_DEATHS;
    public static final FeatureDungeonMilestone DUNGEON_MILESTONE;
    public static final FeatureDungeonRealTime DUNGEON_REALTIME;
    public static final FeatureDungeonSBTime DUNGEON_SBTIME;
    public static final FeatureDungeonSecrets DUNGEON_SECRETS;
    public static final FeatureDungeonCurrentRoomSecrets DUNGEON_SECRETS_ROOM;
    public static final FeatureDungeonTombs DUNGEON_TOMBS;
    public static final FeatureDungeonScore DUNGEON_SCORE;
    public static final FeatureWarnLowHealth DUNGEON_LOWHEALTH_WARN;
    public static final SimpleFeature DUNGEON_INTERMODCOMM;
    public static final FeaturePlayerESP DUNGEON_PLAYERESP;
    public static final FeatureHideNameTags DUNGEON_HIDENAMETAGS;
    public static final FeatureSoulRoomWarning DUNGEON_FAIRYSOUL;

    public static final FeatureWarningOnPortal BOSSFIGHT_WARNING_ON_PORTAL;
    public static final SimpleFeature BOSSFIGHT_CHESTPRICE;
    public static final FeatureAutoReparty BOSSFIGHT_AUTOREPARTY;
    public static final FeatureBossHealth BOSSFIGHT_HEALTH;
    public static final FeatureHideAnimals BOSSFIGHT_HIDE_ANIMALS;
    public static final FeatureThornBearPercentage BOSSFIGHT_BEAR_PERCENT;
    public static final FeatureThornSpiritBowTimer BOSSFIGHT_BOW_TIMER;
    public static final FeatureBoxRealLivid BOSSFIGHT_BOX_REALLIVID;
    public static final FeatureTerracotaTimer BOSSFIGHT_TERRACOTTA_TIMER;
    public static final FeatureCurrentPhase BOSSFIGHT_CURRENT_PHASE;
    public static final FeatureTerminalSolvers BOSSFIGHT_TERMINAL_SOLVERS;
    public static final FeatureSimonSaysSolver BOSSFIGHT_SIMONSAYS_SOLVER;

    public static final APIKey PARTYKICKER_APIKEY;
    public static final FeatureViewPlayerStatsOnJoin PARTYKICKER_VIEWPLAYER;
    public static final FeatureCustomPartyFinder PARTYKICKER_CUSTOM;
    public static final FeaturePartyList PARTY_LIST;
    public static final FeaturePartyReady PARTY_READY;

    public static final FeatureTooltipDungeonStat ETC_DUNGEONSTAT;
    public static final FeatureTooltipPrice ETC_PRICE;
    public static final FeatureAbilityCooldown ETC_ABILITY_COOLDOWN;
    public static final FeatureCooldownCounter ETC_COOLDOWN;
    public static final FeatureRepartyCommand ETC_REPARTY;
    public static final FeatureDecreaseExplosionSound ETC_EXPLOSION_SOUND;
    public static final FeatureAutoAcceptReparty ETC_AUTO_ACCEPT_REPARTY;
    public static final FeatureUpdateAlarm ETC_TEST;

    public static final SimpleFeature FIX_SPIRIT_BOOTS;
    public static final FeatureDisableMessage FIX_MESSAGES;

    public static final FeatureCopyMessages ETC_COPY_MSG;

    public static final FeaturePenguins ETC_PENGUIN;

    public static final FeatureCollectScore ETC_COLLECT_SCORE;





    public static final FeatureNicknamePrefix COSMETIC_PREFIX;
    public static final FeatureNicknameColor COSMETIC_NICKNAMECOLOR;



    public static final SimpleFeature DISCORD_RICHPRESENCE;
    public static final PartyInviteViewer DISCORD_ASKTOJOIN;
    public static final PlayingDGAlarm DISCORD_ONLINEALARM;
    public static final SimpleFeature DISCORD_DONOTUSE;


    public static final SimpleFeature DEBUG;
    public static final SimpleFeature ADVANCED_ROOMEDIT;

    public static final FeatureRoomDebugInfo ADVANCED_DEBUG_ROOM;

    public static final FeatureDebuggableMap ADVANCED_DEBUGGABLE_MAP;

    public static final FeatureRoomCoordDisplay ADVANCED_COORDS;

    static {
        SECRET_CREATE_REFRESH_LINE = register(new FeatureCreateRefreshLine());
        SECRET_FREEZE_LINES = register(new FeatureFreezePathfind());
        SECRET_TOGGLE_KEY = register(new FeatureTogglePathfind());
        SECRET_PATHFIND_STRATEGY = register(new FeaturePathfindStrategy());
        SECRET_ACTIONS = register(new FeatureActions());
        SECRET_LINE_PROPERTIES_SECRET_BROWSER = register(new PathfindLineProperties("Dungeon Secrets.Secret Browser", "Line Settings", "Line Settings when pathfinding using Secret Browser", "secret.lineproperties.secretbrowser", true, SECRET_LINE_PROPERTIES_GLOBAL));
        SECRET_BROWSE = register(new FeatureMechanicBrowse());
        categoryDescription.put("ROOT.Dungeon Secrets.Keybinds", "Useful keybinds / Toggle Pathfind lines, Freeze Pathfind lines, Refresh pathfind line or Trigger pathfind (you would want to use it, if you're using Pathfind to All)");
        SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT = register(new PathfindLineProperties("Dungeon Secrets.Pathfind To All", "Parent Line Settings", "Line Settings to be used by default", "secret.lineproperties.apf.parent", false, SECRET_LINE_PROPERTIES_GLOBAL));
        DUNGEON_FAIRYSOUL = register(new FeatureSoulRoomWarning());
        DUNGEON_HIDENAMETAGS = register(new FeatureHideNameTags());
        DUNGEON_PLAYERESP = register(new FeaturePlayerESP());
        DUNGEON_INTERMODCOMM = register(new SimpleFeature("Dungeon.Teammates", "Communicate With Other's Dungeons Guide", "Sends total secret in the room to others\nSo that they can use the data to calculate total secret in dungeon run\n\nThis automates player chatting action, (chatting data) Thus it might be against hypixel's rules.\nBut mods like auto-gg which also automate player action and is kinda allowed mod exist so I'm leaving this feature.\nThis option is use-at-your-risk and you'll be responsible for ban if you somehow get banned because of this feature\n(Although it is not likely to happen)\nDefaults to off", "dungeon.intermodcomm", false));
        DUNGEON_LOWHEALTH_WARN = register(new FeatureWarnLowHealth());
        DUNGEON_SCORE = register(new FeatureDungeonScore());
        DUNGEON_TOMBS = register(new FeatureDungeonTombs());
        DUNGEON_SECRETS_ROOM = register(new FeatureDungeonCurrentRoomSecrets());
        DUNGEON_SECRETS = register(new FeatureDungeonSecrets());
        DUNGEON_SBTIME = register(new FeatureDungeonSBTime());
        DUNGEON_REALTIME = register(new FeatureDungeonRealTime());
        DUNGEON_MILESTONE = register(new FeatureDungeonMilestone());
        DUNGEON_DEATHS = register(new FeatureDungeonDeaths());
        DUNGEON_WATCHERWARNING = register(new FeatureWatcherWarning());
        DUNGEON_BOXSTARMOBS = register(new FeatureBoxStarMobs());
        DUNGEON_BOXBAT = register(new FeatureBoxBats());
        DUNGEON_BOXSKELEMASTER = register(new FeatureBoxSkelemaster());
        DUNGEON_CLOSECHEST = register(new FeaturePressAnyKeyToCloseChest());
        DUNGEON_ROOMNAME = register(new FeatureDungeonRoomName());
        TEST_PEPOLE = register(new FeatureTestPepole());
        DUNGEON_MAP = register(new FeatureDungeonMap());
        SOLVER_BOMBDEFUSE = register(new FeatureSolverBombdefuse());
        SOLVER_KAHOOT = register(new FeatureSolverKahoot());
        SOLVER_BOX = register(new FeatureSolverBox());
        SOLVER_SILVERFISH = register(new FeatureSolverSilverfish());
        SOLVER_ICEPATH = register(new FeatureSolverIcefill());
        SOLVER_BLAZE = register(new FeatureSolverBlaze());
        SOLVER_TELEPORT = register(new FeatureSolverTeleport());
        SOLVER_CREEPER = register(new SimpleFeature("Solver.Any Floor", "Creeper", "Draws line between prismarine lamps in creeper room", "solver.creeper"));
        SOLVER_WATERPUZZLE = register(new SimpleFeature("Solver.Any Floor", "Waterboard (Advanced)", "Calculates solution for waterboard puzzle and displays it to user", "solver.waterboard"));
        SOLVER_TICTACTOE = register(new FeatureSolverTictactoe());
        SOLVER_RIDDLE = register(new FeatureSolverRiddle());
        SECRET_LINE_PROPERTIES_PATHFINDALL_ITEM_DROP = register(new PathfindLineProperties("Dungeon Secrets.Pathfind To All", "Item Drop Line Settings", "Line Settings when pathfind to Item Drop, when using above feature", "secret.lineproperties.apf.itemdrop", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
        SECRET_LINE_PROPERTIES_PATHFINDALL_ESSENCE = register(new PathfindLineProperties("Dungeon Secrets.Pathfind To All", "Essence Line Settings", "Line Settings when pathfind to Essence, when using above feature", "secret.lineproperties.apf.essence", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
        SECRET_LINE_PROPERTIES_PATHFINDALL_CHEST = register(new PathfindLineProperties("Dungeon Secrets.Pathfind To All", "Chest Line Settings", "Line Settings when pathfind to Chest, when using above feature", "secret.lineproperties.apf.chest", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
        SECRET_LINE_PROPERTIES_PATHFINDALL_BAT = register(new PathfindLineProperties("Dungeon Secrets.Pathfind To All", "Bat Line Settings", "Line Settings when pathfind to Bat, when using above feature", "secret.lineproperties.apf.bat", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
        SECRET_PATHFIND_ALL = register(new FeaturePathfindToAll());
        SECRET_LINE_PROPERTIES_AUTOPATHFIND = register(new PathfindLineProperties("Dungeon Secrets.Legacy AutoPathfind", "Line Settings", "Line Settings when pathfinding using above features", "secret.lineproperties.autopathfind", true, SECRET_LINE_PROPERTIES_GLOBAL));
        SECRET_BLOOD_RUSH_LINE_PROPERTIES = register(new PathfindLineProperties("Dungeon Secrets.Blood Rush", "Blood Rush Line Settings", "Line Settings to be used", "secret.lineproperties.bloodrush", false, SECRET_LINE_PROPERTIES_GLOBAL));
        SECRET_BLOOD_RUSH = register(new FeatureBloodRush());
        SECRET_NEXT_KEY = register(new SimpleFeature("Dungeon Secrets.Legacy AutoPathfind", "Auto Pathfind to new secret upon pressing a key", "Auto browse the best next secret when you press key.\nPress settings to edit the key", "secret.keyfornext", false) {{
            addParameter("key", new FeatureParameter<Integer>("key", "Key", "Press to navigate to next best secret", Keyboard.KEY_NONE, "keybind"));
        }});
        SECRET_AUTO_START = register(new SimpleFeature("Dungeon Secrets.Legacy AutoPathfind", "Auto pathfind to new secret", "Auto browse best secret upon entering the room.", "secret.autouponenter", false));
        SECRET_AUTO_BROWSE_NEXT = register(new SimpleFeature("Dungeon Secrets.Legacy AutoPathfind", "Auto Pathfind to next secret", "Auto browse best next secret after current one completes.\nthe first pathfinding of first secret needs to be triggered first in order for this option to work", "secret.autobrowse", false));
        BOSSFIGHT_WARNING_ON_PORTAL = register(new FeatureWarningOnPortal());
        BOSSFIGHT_CHESTPRICE = register(new FeatureChestPrice());
        BOSSFIGHT_AUTOREPARTY = register(new FeatureAutoReparty());
        BOSSFIGHT_HEALTH = register(new FeatureBossHealth());
        BOSSFIGHT_HIDE_ANIMALS = register(new FeatureHideAnimals());
        BOSSFIGHT_BEAR_PERCENT = register(new FeatureThornBearPercentage());
        BOSSFIGHT_BOW_TIMER = register(new FeatureThornSpiritBowTimer());
        BOSSFIGHT_BOX_REALLIVID = register(new FeatureBoxRealLivid());
        BOSSFIGHT_TERRACOTTA_TIMER = register(new FeatureTerracotaTimer());
        BOSSFIGHT_CURRENT_PHASE = register(new FeatureCurrentPhase());
        BOSSFIGHT_TERMINAL_SOLVERS = register(new FeatureTerminalSolvers());
        BOSSFIGHT_SIMONSAYS_SOLVER = register(new FeatureSimonSaysSolver());
        PARTYKICKER_APIKEY = register(new APIKey());
        PARTYKICKER_VIEWPLAYER = register(new FeatureViewPlayerStatsOnJoin());
        PARTYKICKER_CUSTOM = register(new FeatureCustomPartyFinder());
        PARTY_LIST = register(new FeaturePartyList());
        PARTY_READY = register(new FeaturePartyReady());
        ETC_DUNGEONSTAT = register(new FeatureTooltipDungeonStat());
        ETC_PRICE = register(new FeatureTooltipPrice());
        ETC_ABILITY_COOLDOWN = register(new FeatureAbilityCooldown());
        ETC_COOLDOWN = register(new FeatureCooldownCounter());
        ETC_REPARTY = register(new FeatureRepartyCommand());
        ETC_EXPLOSION_SOUND = register(new FeatureDecreaseExplosionSound());
        ETC_AUTO_ACCEPT_REPARTY = register(new FeatureAutoAcceptReparty());
        ETC_TEST = register(new FeatureUpdateAlarm());
        FIX_SPIRIT_BOOTS = register(new SimpleFeature("Misc", "Spirit Boots Fixer", "Fix Spirit boots messing up with inventory", "fixes.spirit", true));
        FIX_MESSAGES = register(new FeatureDisableMessage());
        ETC_COPY_MSG = register(new FeatureCopyMessages());
        ETC_PENGUIN = register(new FeaturePenguins());
        ETC_COLLECT_SCORE = register(new FeatureCollectScore());
        COSMETIC_NICKNAMECOLOR = register(new FeatureNicknameColor());
        COSMETIC_PREFIX = register(new FeatureNicknamePrefix());
        DISCORD_RICHPRESENCE = register(new SimpleFeature("Discord", "Discord RPC", "Enable Discord rich presence", "advanced.richpresence", true) {
            {
                addParameter("disablenotskyblock", new FeatureParameter<Boolean>("disablenotskyblock", "Disable When not on Skyblock", "Disable When not on skyblock", false, "boolean"));
            }
        });
        DISCORD_ASKTOJOIN = register(new PartyInviteViewer());
        DISCORD_ONLINEALARM = register(new PlayingDGAlarm());
        DISCORD_DONOTUSE = register(new SimpleFeature("Discord", "Disable Native Library", "Disables usage of jna for discord rpc support.\nBreaks any discord related feature in the mod.\nRequires mod restart to get affected.\n\nThis feature is only for those whose minecraft crashes due to discord gamesdk crash.", "discord.rpc", false));
        DEBUG = register(new FeatureDebug());
        ADVANCED_ROOMEDIT = register(new SimpleFeature("Advanced", "Room Edit", "Allow editing dungeon rooms\n\nWarning: using this feature can break or freeze your Minecraft\nThis is only for advanced users only", "advanced.roomedit", false) {
            {
                addParameter("key", new FeatureParameter<Integer>("key", "Key", "Press to edit room", Keyboard.KEY_R, "keybind"));
            }
        });
        ADVANCED_DEBUG_ROOM = register(new FeatureRoomDebugInfo());
        ADVANCED_DEBUGGABLE_MAP = register(new FeatureDebuggableMap());
        ADVANCED_COORDS = register(new FeatureRoomCoordDisplay());
    }
}
