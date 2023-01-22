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

import kr.syeyoung.dungeonsguide.mod.events.annotations.EventHandlerRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.advanced.*;
import kr.syeyoung.dungeonsguide.mod.features.impl.boss.*;
import kr.syeyoung.dungeonsguide.mod.features.impl.boss.terminal.FeatureSimonSaysSolver;
import kr.syeyoung.dungeonsguide.mod.features.impl.boss.terminal.FeatureTerminalSolvers;
import kr.syeyoung.dungeonsguide.mod.features.impl.cosmetics.FeatureNicknameColor;
import kr.syeyoung.dungeonsguide.mod.features.impl.cosmetics.FeatureNicknamePrefix;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteViewer.PartyInviteViewer;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.onlinealarm.PlayingDGAlarm;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.*;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.*;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.ability.FeatureAbilityCooldown;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.APIKey;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.FeaturePartyList;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.FeaturePartyReady;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.customgui.FeatureCustomPartyFinder;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.FeatureViewPlayerStatsOnJoin;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.*;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.mechanicbrowser.FeatureMechanicBrowse;
import kr.syeyoung.dungeonsguide.mod.features.impl.solvers.*;
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



    public static PathfindLineProperties SECRET_LINE_PROPERTIES_GLOBAL;

    public static FeatureMechanicBrowse SECRET_BROWSE;
    public static PathfindLineProperties SECRET_LINE_PROPERTIES_SECRET_BROWSER;
    public static FeatureActions SECRET_ACTIONS;

    public static FeaturePathfindStrategy SECRET_PATHFIND_STRATEGY;
    public static FeatureTogglePathfind SECRET_TOGGLE_KEY;
    public static FeatureFreezePathfind SECRET_FREEZE_LINES;
    public static FeatureCreateRefreshLine SECRET_CREATE_REFRESH_LINE;

    public static SimpleFeature SECRET_AUTO_BROWSE_NEXT;
    public static SimpleFeature SECRET_AUTO_START;
    public static SimpleFeature SECRET_NEXT_KEY;

    public static SimpleFeature SECRET_BLOOD_RUSH;
    public static PathfindLineProperties SECRET_BLOOD_RUSH_LINE_PROPERTIES;


    public static PathfindLineProperties SECRET_LINE_PROPERTIES_AUTOPATHFIND;

    public static FeaturePathfindToAll SECRET_PATHFIND_ALL;
    public static PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT;

    public static PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_BAT;
    public static PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_CHEST;
    public static PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_ESSENCE;
    public static PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_ITEM_DROP;

    public static FeatureSolverRiddle SOLVER_RIDDLE;
    public static FeatureSolverTictactoe SOLVER_TICTACTOE;
    public static SimpleFeature SOLVER_WATERPUZZLE;
    public static SimpleFeature SOLVER_CREEPER;
    public static FeatureSolverTeleport SOLVER_TELEPORT;
    public static FeatureSolverBlaze SOLVER_BLAZE;
    public static FeatureSolverIcefill SOLVER_ICEPATH;
    public static FeatureSolverSilverfish SOLVER_SILVERFISH;
    public static FeatureSolverBox SOLVER_BOX;
    public static FeatureSolverKahoot SOLVER_KAHOOT;
    public static FeatureSolverBombdefuse SOLVER_BOMBDEFUSE;


    public static FeatureDungeonMap DUNGEON_MAP;
    public static FeatureTestPepole TEST_PEPOLE;
    public static FeatureDungeonRoomName DUNGEON_ROOMNAME;
    public static FeaturePressAnyKeyToCloseChest DUNGEON_CLOSECHEST;
    public static FeatureBoxSkelemaster DUNGEON_BOXSKELEMASTER;
    public static FeatureBoxBats DUNGEON_BOXBAT;
    public static FeatureBoxStarMobs DUNGEON_BOXSTARMOBS;
    public static FeatureWatcherWarning DUNGEON_WATCHERWARNING;
    public static FeatureDungeonDeaths DUNGEON_DEATHS;
    public static FeatureDungeonMilestone DUNGEON_MILESTONE;
    public static FeatureDungeonRealTime DUNGEON_REALTIME;
    public static FeatureDungeonSBTime DUNGEON_SBTIME;
    public static FeatureDungeonSecrets DUNGEON_SECRETS;
    public static FeatureDungeonCurrentRoomSecrets DUNGEON_SECRETS_ROOM;
    public static FeatureDungeonTombs DUNGEON_TOMBS;
    public static FeatureDungeonScore DUNGEON_SCORE;
    public static FeatureWarnLowHealth DUNGEON_LOWHEALTH_WARN;
    public static SimpleFeature DUNGEON_INTERMODCOMM;
    public static FeaturePlayerESP DUNGEON_PLAYERESP;
    public static FeatureHideNameTags DUNGEON_HIDENAMETAGS;
    public static FeatureSoulRoomWarning DUNGEON_FAIRYSOUL;

    public static FeatureWarningOnPortal BOSSFIGHT_WARNING_ON_PORTAL;
    public static SimpleFeature BOSSFIGHT_CHESTPRICE;
    public static FeatureAutoReparty BOSSFIGHT_AUTOREPARTY;
    public static FeatureBossHealth BOSSFIGHT_HEALTH;
    public static FeatureHideAnimals BOSSFIGHT_HIDE_ANIMALS;
    public static FeatureThornBearPercentage BOSSFIGHT_BEAR_PERCENT;
    public static FeatureThornSpiritBowTimer BOSSFIGHT_BOW_TIMER;
    public static FeatureBoxRealLivid BOSSFIGHT_BOX_REALLIVID;
    public static FeatureTerracotaTimer BOSSFIGHT_TERRACOTTA_TIMER;
    public static FeatureCurrentPhase BOSSFIGHT_CURRENT_PHASE;
    public static FeatureTerminalSolvers BOSSFIGHT_TERMINAL_SOLVERS;
    public static FeatureSimonSaysSolver BOSSFIGHT_SIMONSAYS_SOLVER;

    public static APIKey PARTYKICKER_APIKEY;
    public static FeatureViewPlayerStatsOnJoin PARTYKICKER_VIEWPLAYER;
    public static FeatureCustomPartyFinder PARTYKICKER_CUSTOM;
    public static FeaturePartyList PARTY_LIST;
    public static FeaturePartyReady PARTY_READY;

    public static FeatureTooltipDungeonStat ETC_DUNGEONSTAT;
    public static FeatureTooltipPrice ETC_PRICE;
    public static FeatureAbilityCooldown ETC_ABILITY_COOLDOWN;
    public static FeatureCooldownCounter ETC_COOLDOWN;
    public static FeatureRepartyCommand ETC_REPARTY;
    public static FeatureDecreaseExplosionSound ETC_EXPLOSION_SOUND;
    public static FeatureAutoAcceptReparty ETC_AUTO_ACCEPT_REPARTY;
    public static FeatureUpdateAlarm ETC_TEST;

    public static SimpleFeature FIX_SPIRIT_BOOTS;
    public static FeatureDisableMessage FIX_MESSAGES;

    public static FeatureCopyMessages ETC_COPY_MSG;

    public static FeatureEpicCountdown EPIC_COUNTDOWN;

    public static FeaturePenguins ETC_PENGUIN;

    public static FeatureCollectScore ETC_COLLECT_SCORE;





    public static FeatureNicknamePrefix COSMETIC_PREFIX;
    public static FeatureNicknameColor COSMETIC_NICKNAMECOLOR;



    public static SimpleFeature DISCORD_RICHPRESENCE;
    public static PartyInviteViewer DISCORD_ASKTOJOIN;
    public static PlayingDGAlarm DISCORD_ONLINEALARM;


    public static SimpleFeature DEBUG;
    public static SimpleFeature ADVANCED_ROOMEDIT;

    public static FeatureRoomDebugInfo ADVANCED_DEBUG_ROOM;

    public static FeatureDebuggableMap ADVANCED_DEBUGGABLE_MAP;

    public static FeatureRoomCoordDisplay ADVANCED_COORDS;

    private static FeatureDebugTrap ADVANCED_BAT;

    public static SimpleFeature RENDER_BREACONS;

    public static SimpleFeature RENDER_DESTENATION_TEXT;

    public static SimpleFeature DEBUG_BLOCK_CACHING;

    public void init(){
        try {
            SECRET_LINE_PROPERTIES_GLOBAL = register(new PathfindLineProperties("Dungeon.Secrets.Preferences", "Global Line Settings", "Global Line Settings", "secret.lineproperties.global", true, null));
            SECRET_CREATE_REFRESH_LINE = register(new FeatureCreateRefreshLine());
            SECRET_FREEZE_LINES = register(new FeatureFreezePathfind());
            SECRET_TOGGLE_KEY = register(new FeatureTogglePathfind());
            SECRET_PATHFIND_STRATEGY = register(new FeaturePathfindStrategy());
            SECRET_ACTIONS = register(new FeatureActions());
            SECRET_LINE_PROPERTIES_SECRET_BROWSER = register(new PathfindLineProperties("Dungeon.Secrets.Secret Browser", "Line Settings", "Line Settings when pathfinding using Secret Browser", "secret.lineproperties.secretbrowser", true, SECRET_LINE_PROPERTIES_GLOBAL));
            SECRET_BROWSE = register(new FeatureMechanicBrowse());
            categoryDescription.put("ROOT.Secrets.Keybinds", "Useful keybinds / Toggle Pathfind lines, Freeze Pathfind lines, Refresh pathfind line or Trigger pathfind (you would want to use it, if you're using Pathfind to All)");
            SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT = register(new PathfindLineProperties("Dungeon.Secrets.Pathfind To All", "Parent Line Settings", "Line Settings to be used by default", "secret.lineproperties.apf.parent", false, SECRET_LINE_PROPERTIES_GLOBAL));
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
//            TEST_PEPOLE = register(new FeatureTestPepole());
            DUNGEON_MAP = register(new FeatureDungeonMap());
            SOLVER_BOMBDEFUSE = register(new FeatureSolverBombdefuse());
            SOLVER_KAHOOT = register(new FeatureSolverKahoot());
            SOLVER_BOX = register(new FeatureSolverBox());
            SOLVER_SILVERFISH = register(new FeatureSolverSilverfish());
            SOLVER_ICEPATH = register(new FeatureSolverIcefill());
            SOLVER_BLAZE = register(new FeatureSolverBlaze());
            SOLVER_TELEPORT = register(new FeatureSolverTeleport());
            SOLVER_CREEPER = register(new SimpleFeature("Dungeon.Solvers.Any Floor", "Creeper", "Draws line between prismarine lamps in creeper room", "solver.creeper"));
            SOLVER_WATERPUZZLE = register(new SimpleFeature("Dungeon.Solvers.Any Floor", "Waterboard (Advanced)", "Calculates solution for waterboard puzzle and displays it to user", "solver.waterboard"));
            SOLVER_TICTACTOE = register(new FeatureSolverTictactoe());
            SOLVER_RIDDLE = register(new FeatureSolverRiddle());
            SECRET_LINE_PROPERTIES_PATHFINDALL_ITEM_DROP = register(new PathfindLineProperties("Dungeon.Secrets.Pathfind To All", "Item Drop Line Settings", "Line Settings when pathfind to Item Drop, when using above feature", "secret.lineproperties.apf.itemdrop", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
            SECRET_LINE_PROPERTIES_PATHFINDALL_ESSENCE = register(new PathfindLineProperties("Dungeon.Secrets.Pathfind To All", "Essence Line Settings", "Line Settings when pathfind to Essence, when using above feature", "secret.lineproperties.apf.essence", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
            SECRET_LINE_PROPERTIES_PATHFINDALL_CHEST = register(new PathfindLineProperties("Dungeon.Secrets.Pathfind To All", "Chest Line Settings", "Line Settings when pathfind to Chest, when using above feature", "secret.lineproperties.apf.chest", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
            SECRET_LINE_PROPERTIES_PATHFINDALL_BAT = register(new PathfindLineProperties("Dungeon.Secrets.Pathfind To All", "Bat Line Settings", "Line Settings when pathfind to Bat, when using above feature", "secret.lineproperties.apf.bat", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
            SECRET_PATHFIND_ALL = register(new FeaturePathfindToAll());
            SECRET_LINE_PROPERTIES_AUTOPATHFIND = register(new PathfindLineProperties("Dungeon.Secrets.Legacy AutoPathfind", "Line Settings", "Line Settings when pathfinding using above features", "secret.lineproperties.autopathfind", true, SECRET_LINE_PROPERTIES_GLOBAL));
            SECRET_BLOOD_RUSH_LINE_PROPERTIES = register(new PathfindLineProperties("Dungeon.Secrets.Blood Rush", "Blood Rush Line Settings", "Line Settings to be used", "secret.lineproperties.bloodrush", false, SECRET_LINE_PROPERTIES_GLOBAL));
            SECRET_BLOOD_RUSH = register(new FeatureBloodRush());
            SECRET_NEXT_KEY = register(new SimpleFeature("Dungeon.Secrets.Legacy AutoPathfind", "Auto Pathfind to new secret upon pressing a key", "Auto browse the best next secret when you press key.\nPress settings to edit the key", "secret.keyfornext", false) {{
                addParameter("key", new FeatureParameter<Integer>("key", "Key", "Press to navigate to next best secret", Keyboard.KEY_NONE, "keybind"));
            }});

            EPIC_COUNTDOWN = register(new FeatureEpicCountdown());

            RENDER_BREACONS = register(new SimpleFeature("Dungeon.Secrets.Preferences", "Render beacons", "Should the mod not render beacons on secret", "secret.beacons", false));
            DEBUG_BLOCK_CACHING = register(new SimpleFeature("Debug","Enable block getBlockCaching", "Cache all world.getBlockState callls", "debug.blockcache"));
            RENDER_DESTENATION_TEXT = register(new SimpleFeature("Dungeon.Secrets.Preferences", "Render Destination text", "Should the mod not render \"destination\" on secrets", "secret.desttext", false));
            SECRET_AUTO_START = register(new SimpleFeature("Dungeon.Secrets.Legacy AutoPathfind", "Auto pathfind to new secret", "Auto browse best secret upon entering the room.", "secret.autouponenter", false));
            SECRET_AUTO_BROWSE_NEXT = register(new SimpleFeature("Dungeon.Secrets.Legacy AutoPathfind", "Auto Pathfind to next secret", "Auto browse best next secret after current one completes.\nthe first pathfinding of first secret needs to be triggered first in order for this option to work", "secret.autobrowse", false));
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
            DISCORD_RICHPRESENCE = register(new SimpleFeature("Discord", "Discord RPC", "Enable Discord rich presence", "advanced.discordrichpresence", true) {
                {
                    addParameter("disablenotskyblock", new FeatureParameter<Boolean>("disablenotskyblock", "Disable When not on Skyblock", "Disable When not on skyblock", false, "boolean"));
                }
            });
            DISCORD_ASKTOJOIN = register(new PartyInviteViewer());
            DISCORD_ONLINEALARM = register(new PlayingDGAlarm());
            DEBUG = register(new FeatureDebug());
            ADVANCED_ROOMEDIT = register(new SimpleFeature("Debug", "Room Edit", "Allow editing dungeon rooms\n\nWarning: using this feature can break or freeze your Minecraft\nThis is for advanced users only", "advanced.roomedit", false) {
                {
                    addParameter("key", new FeatureParameter<Integer>("key", "Key", "Press to edit room", Keyboard.KEY_R, "keybind"));
                }
            });
            ADVANCED_DEBUG_ROOM = register(new FeatureRoomDebugInfo());
            ADVANCED_DEBUGGABLE_MAP = register(new FeatureDebuggableMap());
            ADVANCED_COORDS = register(new FeatureRoomCoordDisplay());
            ADVANCED_BAT = register(new FeatureDebugTrap());
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }


        for (AbstractFeature abstractFeature : featureList) {
            if(abstractFeature == null){
                throw new IllegalStateException("Feature " + abstractFeature.getKey() + " is null, this cannot happen!!!");
            }
        }

    }
}
