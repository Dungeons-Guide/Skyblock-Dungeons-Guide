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
    private static final List<AbstractFeature> featureList = new ArrayList<>();
    private static final Map<String, AbstractFeature> featureByKey = new HashMap<>();
    @Getter
    private static final Map<String, List<AbstractFeature>> featuresByCategory = new HashMap<>();
    @Getter
    private static final Map<String, String> categoryDescription = new HashMap<>();



    private static FeatureRegistry instance;
    public static FeatureRegistry getInstance() {
        return instance == null ? instance = new FeatureRegistry() : instance;
    }

    public PathfindLineProperties SECRET_LINE_PROPERTIES_GLOBAL;
    public FeatureMechanicBrowse SECRET_BROWSE;
    public PathfindLineProperties SECRET_LINE_PROPERTIES_SECRET_BROWSER;
    public FeatureActions SECRET_ACTIONS;
    public FeaturePathfindStrategy SECRET_PATHFIND_STRATEGY;
    public FeatureTogglePathfind SECRET_TOGGLE_KEY;
    public FeatureFreezePathfind SECRET_FREEZE_LINES;
    public FeatureCreateRefreshLine SECRET_CREATE_REFRESH_LINE;
    public SimpleFeature SECRET_AUTO_BROWSE_NEXT;
    public SimpleFeature SECRET_AUTO_START;
    public SimpleFeature SECRET_NEXT_KEY;
    public SimpleFeature SECRET_BLOOD_RUSH;
    public PathfindLineProperties SECRET_BLOOD_RUSH_LINE_PROPERTIES;
    public PathfindLineProperties SECRET_LINE_PROPERTIES_AUTOPATHFIND;
    public FeaturePathfindToAll SECRET_PATHFIND_ALL;
    public PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT;
    public PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_BAT;
    public PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_CHEST;
    public PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_ESSENCE;
    public PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_ITEM_DROP;
    public FeatureSolverRiddle SOLVER_RIDDLE;
    public FeatureSolverTictactoe SOLVER_TICTACTOE;
    public SimpleFeature SOLVER_WATERPUZZLE;
    public SimpleFeature SOLVER_CREEPER;
    public FeatureSolverTeleport SOLVER_TELEPORT;
    public FeatureSolverBlaze SOLVER_BLAZE;
    public FeatureSolverIcefill SOLVER_ICEPATH;
    public FeatureSolverSilverfish SOLVER_SILVERFISH;
    public FeatureSolverBox SOLVER_BOX;
    public FeatureSolverKahoot SOLVER_KAHOOT;
    public FeatureSolverBombdefuse SOLVER_BOMBDEFUSE;
    public FeatureDungeonMap DUNGEON_MAP;
    public FeatureTestPepole TEST_PEPOLE;
    public FeatureDungeonRoomName DUNGEON_ROOMNAME;
    public FeaturePressAnyKeyToCloseChest DUNGEON_CLOSECHEST;
    public FeatureBoxSkelemaster DUNGEON_BOXSKELEMASTER;
    public FeatureBoxBats DUNGEON_BOXBAT;
    public FeatureBoxStarMobs DUNGEON_BOXSTARMOBS;
    public FeatureWatcherWarning DUNGEON_WATCHERWARNING;
    public FeatureDungeonDeaths DUNGEON_DEATHS;
    public FeatureDungeonMilestone DUNGEON_MILESTONE;
    public FeatureDungeonRealTime DUNGEON_REALTIME;
    public FeatureDungeonSBTime DUNGEON_SBTIME;
    public FeatureDungeonSecrets DUNGEON_SECRETS;
    public FeatureDungeonCurrentRoomSecrets DUNGEON_SECRETS_ROOM;
    public FeatureDungeonTombs DUNGEON_TOMBS;
    public FeatureDungeonScore DUNGEON_SCORE;
    public FeatureWarnLowHealth DUNGEON_LOWHEALTH_WARN;
    public SimpleFeature DUNGEON_INTERMODCOMM;
    public FeaturePlayerESP DUNGEON_PLAYERESP;
    public FeatureHideNameTags DUNGEON_HIDENAMETAGS;
    public FeatureSoulRoomWarning DUNGEON_FAIRYSOUL;
    public FeatureWarningOnPortal BOSSFIGHT_WARNING_ON_PORTAL;
    public SimpleFeature BOSSFIGHT_CHESTPRICE;
    public FeatureAutoReparty BOSSFIGHT_AUTOREPARTY;
    public FeatureBossHealth BOSSFIGHT_HEALTH;
    public FeatureHideAnimals BOSSFIGHT_HIDE_ANIMALS;
    public FeatureThornBearPercentage BOSSFIGHT_BEAR_PERCENT;
    public FeatureThornSpiritBowTimer BOSSFIGHT_BOW_TIMER;
    public FeatureBoxRealLivid BOSSFIGHT_BOX_REALLIVID;
    public FeatureTerracotaTimer BOSSFIGHT_TERRACOTTA_TIMER;
    public FeatureCurrentPhase BOSSFIGHT_CURRENT_PHASE;
    public FeatureTerminalSolvers BOSSFIGHT_TERMINAL_SOLVERS;
    public FeatureSimonSaysSolver BOSSFIGHT_SIMONSAYS_SOLVER;
    public APIKey PARTYKICKER_APIKEY;
    public FeatureViewPlayerStatsOnJoin PARTYKICKER_VIEWPLAYER;
    public FeatureCustomPartyFinder PARTYKICKER_CUSTOM;
    public FeaturePartyList PARTY_LIST;
    public FeaturePartyReady PARTY_READY;
    public FeatureTooltipDungeonStat ETC_DUNGEONSTAT;
    public FeatureTooltipPrice ETC_PRICE;
    public FeatureAbilityCooldown ETC_ABILITY_COOLDOWN;
    public FeatureCooldownCounter ETC_COOLDOWN;
    public FeatureRepartyCommand ETC_REPARTY;
    public FeatureDecreaseExplosionSound ETC_EXPLOSION_SOUND;
    public FeatureAutoAcceptReparty ETC_AUTO_ACCEPT_REPARTY;
    public FeatureUpdateAlarm ETC_TEST;
    public SimpleFeature FIX_SPIRIT_BOOTS;
    public FeatureDisableMessage FIX_MESSAGES;
    public FeatureCopyMessages ETC_COPY_MSG;
    public FeatureEpicCountdown EPIC_COUNTDOWN;
    public FeaturePenguins ETC_PENGUIN;
    public FeatureCollectScore ETC_COLLECT_SCORE;
    public FeatureNicknamePrefix COSMETIC_PREFIX;
    public FeatureNicknameColor COSMETIC_NICKNAMECOLOR;
    public SimpleFeature DISCORD_RICHPRESENCE;
    public PartyInviteViewer DISCORD_ASKTOJOIN;
    public PlayingDGAlarm DISCORD_ONLINEALARM;
    public SimpleFeature DISCORD_DONOTUSE;
    public SimpleFeature DEBUG;
    public SimpleFeature ADVANCED_ROOMEDIT;
    public FeatureRoomDebugInfo ADVANCED_DEBUG_ROOM;
    public FeatureDebuggableMap ADVANCED_DEBUGGABLE_MAP;
    public FeatureRoomCoordDisplay ADVANCED_COORDS;
    public SimpleFeature RENDER_BREACONS;
    public SimpleFeature RENDER_DESTENATION_TEXT;
    public SimpleFeature DEBUG_BLOCK_CACHING;
    private FeatureDebugTrap ADVANCED_BAT;

    public static AbstractFeature getFeatureByKey(String key) {
        return featureByKey.get(key);
    }

    public static <T extends AbstractFeature> T register(T abstractFeature) {
        if (featureByKey.containsKey(abstractFeature.getKey()))
            throw new IllegalArgumentException("DUPLICATE FEATURE DEFINITION");
        featureList.add(abstractFeature);
        featureByKey.put(abstractFeature.getKey(), abstractFeature);
        List<AbstractFeature> features = featuresByCategory.get(abstractFeature.getCategory());
        if (features == null)
            features = new ArrayList<AbstractFeature>();
        features.add(abstractFeature);
        featuresByCategory.put(abstractFeature.getCategory(), features);

        return abstractFeature;
    }

    static boolean fuse = false;

    public void init() {
        if(fuse){
            return;
        }
        fuse = true;

        categoryDescription.put("ROOT.Secrets.Keybinds", "Useful keybinds / Toggle Pathfind lines, Freeze Pathfind lines, Refresh pathfind line or Trigger pathfind (you would want to use it, if you're using Pathfind to All)");

        SECRET_LINE_PROPERTIES_PATHFINDALL_ITEM_DROP = register(new PathfindLineProperties("Dungeon.Secrets.Pathfind To All", "Item Drop Line Settings", "Line Settings when pathfind to Item Drop, when using above feature", "secret.lineproperties.apf.itemdrop", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
        SECRET_LINE_PROPERTIES_PATHFINDALL_ESSENCE = register(new PathfindLineProperties("Dungeon.Secrets.Pathfind To All", "Essence Line Settings", "Line Settings when pathfind to Essence, when using above feature", "secret.lineproperties.apf.essence", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
        SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT = register(new PathfindLineProperties("Dungeon.Secrets.Pathfind To All", "Parent Line Settings", "Line Settings to be used by default", "secret.lineproperties.apf.parent", false, SECRET_LINE_PROPERTIES_GLOBAL));
        SECRET_LINE_PROPERTIES_PATHFINDALL_CHEST = register(new PathfindLineProperties("Dungeon.Secrets.Pathfind To All", "Chest Line Settings", "Line Settings when pathfind to Chest, when using above feature", "secret.lineproperties.apf.chest", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
        SECRET_LINE_PROPERTIES_PATHFINDALL_BAT = register(new PathfindLineProperties("Dungeon.Secrets.Pathfind To All", "Bat Line Settings", "Line Settings when pathfind to Bat, when using above feature", "secret.lineproperties.apf.bat", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
        SECRET_LINE_PROPERTIES_SECRET_BROWSER = register(new PathfindLineProperties("Dungeon.Secrets.Secret Browser", "Line Settings", "Line Settings when pathfinding using Secret Browser", "secret.lineproperties.secretbrowser", true, SECRET_LINE_PROPERTIES_GLOBAL));
        SECRET_LINE_PROPERTIES_AUTOPATHFIND = register(new PathfindLineProperties("Dungeon.Secrets.Legacy AutoPathfind", "Line Settings", "Line Settings when pathfinding using above features", "secret.lineproperties.autopathfind", true, SECRET_LINE_PROPERTIES_GLOBAL));
        SECRET_BLOOD_RUSH_LINE_PROPERTIES = register(new PathfindLineProperties("Dungeon.Secrets.Blood Rush", "Blood Rush Line Settings", "Line Settings to be used", "secret.lineproperties.bloodrush", false, SECRET_LINE_PROPERTIES_GLOBAL));
        SECRET_LINE_PROPERTIES_GLOBAL = register(new PathfindLineProperties("Dungeon.Secrets.Preferences", "Global Line Settings", "Global Line Settings", "secret.lineproperties.global", true, null));
        SECRET_NEXT_KEY = register(new SimpleFeature("Dungeon.Secrets.Legacy AutoPathfind", "Auto Pathfind to new secret upon pressing a key", "Auto browse the best next secret when you press key.\nPress settings to edit the key", "secret.keyfornext", false) {{
            addParameter("key", new FeatureParameter<Integer>("key", "Key", "Press to navigate to next best secret", Keyboard.KEY_NONE, "keybind"));
        }});
        RENDER_BREACONS = register(new SimpleFeature("Dungeon.Secrets.Preferences", "Render beacons", "Should the mod not render beacons on secret", "secret.beacons", false));
        SOLVER_CREEPER = register(new SimpleFeature("Dungeon.Solvers.Any Floor", "Creeper", "Draws line between prismarine lamps in creeper room", "solver.creeper"));
        SOLVER_WATERPUZZLE = register(new SimpleFeature("Dungeon.Solvers.Any Floor", "Waterboard (Advanced)", "Calculates solution for waterboard puzzle and displays it to user", "solver.waterboard"));
        DEBUG_BLOCK_CACHING = register(new SimpleFeature("Debug", "Enable block getBlockCaching", "Cache all world.getBlockState callls", "debug.blockcache"));
        RENDER_DESTENATION_TEXT = register(new SimpleFeature("Dungeon.Secrets.Preferences", "Render Destination text", "Should the mod not render \"destination\" on secrets", "secret.desttext", false));
        SECRET_AUTO_START = register(new SimpleFeature("Dungeon.Secrets.Legacy AutoPathfind", "Auto pathfind to new secret", "Auto browse best secret upon entering the room.", "secret.autouponenter", false));
        SECRET_AUTO_BROWSE_NEXT = register(new SimpleFeature("Dungeon.Secrets.Legacy AutoPathfind", "Auto Pathfind to next secret", "Auto browse best next secret after current one completes.\nthe first pathfinding of first secret needs to be triggered first in order for this option to work", "secret.autobrowse", false));
        FIX_SPIRIT_BOOTS = register(new SimpleFeature("Misc", "Spirit Boots Fixer", "Fix Spirit boots messing up with inventory", "fixes.spirit", true));
        DISCORD_RICHPRESENCE = register(new SimpleFeature("Discord", "Discord RPC", "Enable Discord rich presence", "advanced.richpresence", true) {
            {
                addParameter("disablenotskyblock", new FeatureParameter<Boolean>("disablenotskyblock", "Disable When not on Skyblock", "Disable When not on skyblock", false, "boolean"));
            }
        });
        ADVANCED_ROOMEDIT = register(new SimpleFeature("Debug", "Room Edit", "Allow editing dungeon rooms\n\nWarning: using this feature can break or freeze your Minecraft\nThis is only for advanced users only", "advanced.roomedit", false) {
            {
                addParameter("key", new FeatureParameter<Integer>("key", "Key", "Press to edit room", Keyboard.KEY_R, "keybind"));
            }
        });
        DISCORD_DONOTUSE = register(new SimpleFeature("Discord", "Disable Native Library", "Disables usage of jna for discord rpc support.\nBreaks any discord related feature in the mod.\nRequires mod restart to get affected.\n\nThis feature is only for those whose minecraft crashes due to discord gamesdk crash.", "discord.rpc", false));
        DUNGEON_INTERMODCOMM = register(new SimpleFeature("Dungeon.Teammates", "Communicate With Other's Dungeons Guide", "Sends total secret in the room to others\nSo that they can use the data to calculate total secret in dungeon run\n\nThis automates player chatting action, (chatting data) Thus it might be against hypixel's rules.\nBut mods like auto-gg which also automate player action and is kinda allowed mod exist so I'm leaving this feature.\nThis option is use-at-your-risk and you'll be responsible for ban if you somehow get banned because of this feature\n(Although it is not likely to happen)\nDefaults to off", "dungeon.intermodcomm", false));


        SECRET_PATHFIND_ALL = register(new FeaturePathfindToAll());
        SECRET_BLOOD_RUSH = register(new FeatureBloodRush());
        EPIC_COUNTDOWN = register(new FeatureEpicCountdown());
        SECRET_CREATE_REFRESH_LINE = register(new FeatureCreateRefreshLine());
        SECRET_FREEZE_LINES = register(new FeatureFreezePathfind());
        SECRET_TOGGLE_KEY = register(new FeatureTogglePathfind());
        SECRET_PATHFIND_STRATEGY = register(new FeaturePathfindStrategy());
        SECRET_ACTIONS = register(new FeatureActions());
        SECRET_BROWSE = register(new FeatureMechanicBrowse());
        DUNGEON_FAIRYSOUL = register(new FeatureSoulRoomWarning());
        DUNGEON_HIDENAMETAGS = register(new FeatureHideNameTags());
        DUNGEON_PLAYERESP = register(new FeaturePlayerESP());
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
//            TEST_PEPOLE = register(new FeatureTestPepole());
        DUNGEON_MAP = register(new FeatureDungeonMap());
        SOLVER_BOMBDEFUSE = register(new FeatureSolverBombdefuse());
        SOLVER_KAHOOT = register(new FeatureSolverKahoot());
        SOLVER_BOX = register(new FeatureSolverBox());
        SOLVER_SILVERFISH = register(new FeatureSolverSilverfish());
        SOLVER_ICEPATH = register(new FeatureSolverIcefill());
        SOLVER_BLAZE = register(new FeatureSolverBlaze());
        SOLVER_TELEPORT = register(new FeatureSolverTeleport());
        SOLVER_TICTACTOE = register(new FeatureSolverTictactoe());
        SOLVER_RIDDLE = register(new FeatureSolverRiddle());
        DISCORD_ASKTOJOIN = register(new PartyInviteViewer());
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
        FIX_MESSAGES = register(new FeatureDisableMessage());
        ETC_COPY_MSG = register(new FeatureCopyMessages());
        ETC_PENGUIN = register(new FeaturePenguins());
        ETC_COLLECT_SCORE = register(new FeatureCollectScore());
        COSMETIC_NICKNAMECOLOR = register(new FeatureNicknameColor());
        COSMETIC_PREFIX = register(new FeatureNicknamePrefix());
        DEBUG = register(new FeatureDebug());
        DISCORD_ONLINEALARM = register(new PlayingDGAlarm());
        ADVANCED_DEBUG_ROOM = register(new FeatureRoomDebugInfo());
        ADVANCED_DEBUGGABLE_MAP = register(new FeatureDebuggableMap());
        ADVANCED_COORDS = register(new FeatureRoomCoordDisplay());
        ADVANCED_BAT = register(new FeatureDebugTrap());


        for (AbstractFeature abstractFeature : featureList) {
            if (abstractFeature == null) {
                throw new IllegalStateException("Feature " + abstractFeature.getKey() + " is null, this cannot happen!!!");
            }
        }

    }
}
