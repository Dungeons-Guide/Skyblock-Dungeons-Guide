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

import kr.syeyoung.dungeonsguide.features.impl.advanced.FeatureDebug;
import kr.syeyoung.dungeonsguide.features.impl.advanced.FeatureDebuggableMap;
import kr.syeyoung.dungeonsguide.features.impl.advanced.FeatureRoomCoordDisplay;
import kr.syeyoung.dungeonsguide.features.impl.advanced.FeatureRoomDebugInfo;
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
import kr.syeyoung.dungeonsguide.features.impl.party.customgui.FeatureCustomPartyFinder;
import kr.syeyoung.dungeonsguide.features.impl.party.playerpreview.FeatureViewPlayerOnJoin;
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



    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_GLOBAL = register(new PathfindLineProperties("Dungeon Secrets", "Global Line Settings", "Global Line Settings", "secret.lineproperties.global", true, null));


    public static final FeatureMechanicBrowse SECRET_BROWSE = register(new FeatureMechanicBrowse());
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_SECRET_BROWSER = register(new PathfindLineProperties("Dungeon Secrets.Secret Browser", "Line Settings", "Line Settings when pathfinding using Secret Browser", "secret.lineproperties.secretbrowser", true, SECRET_LINE_PROPERTIES_GLOBAL));
    public static final FeatureActions SECRET_ACTIONS = register(new FeatureActions());

    public static final SimpleFeature SECRET_PATHFIND_STRATEGY = register(new SimpleFeature("Dungeon Secrets", "Use NEW JPS Optimization instead of standard A Star", "Faster, and accurate routes.", "secret.secretpathfind.algorithm", true));
    public static final FeatureTogglePathfind SECRET_TOGGLE_KEY = register(new FeatureTogglePathfind());
    public static final FeatureFreezePathfind SECRET_FREEZE_LINES = register(new FeatureFreezePathfind());
    public static final FeatureCreateRefreshLine SECRET_CREATE_REFRESH_LINE = register(new FeatureCreateRefreshLine());
    static {
        categoryDescription.put("ROOT.Dungeon Secrets.Keybinds", "Useful keybinds / Toggle Pathfind lines, Freeze Pathfind lines, Refresh pathfind line or Trigger pathfind (you would want to use it, if you're using Pathfind to All)");
    }


    public static final SimpleFeature SECRET_AUTO_BROWSE_NEXT = register(new SimpleFeature("Dungeon Secrets.Legacy AutoPathfind", "Auto Pathfind to next secret", "Auto browse best next secret after current one completes.\nthe first pathfinding of first secret needs to be triggered first in order for this option to work", "secret.autobrowse", false));
    public static final SimpleFeature SECRET_AUTO_START = register(new SimpleFeature("Dungeon Secrets.Legacy AutoPathfind", "Auto pathfind to new secret", "Auto browse best secret upon entering the room.", "secret.autouponenter", false));
    public static final SimpleFeature SECRET_NEXT_KEY = register(new SimpleFeature("Dungeon Secrets.Legacy AutoPathfind", "Auto Pathfind to new secret upon pressing a key", "Auto browse the best next secret when you press key.\nPress settings to edit the key", "secret.keyfornext", false) {
        {
            parameters.put("key", new FeatureParameter<Integer>("key", "Key","Press to navigate to next best secret", Keyboard.KEY_NONE, "keybind"));
        }
    });

    public static final SimpleFeature SECRET_BLOOD_RUSH = register(new SimpleFeature("Dungeon Secrets.Blood Rush", "Blood Rush Mode", "Auto pathfind to witherdoors. \nCan be toggled with key set in settings", "secret.bloodrush", false));
    public static final PathfindLineProperties SECRET_BLOOD_RUSH_LINE_PROPERTIES = register(new PathfindLineProperties("Dungeon Secrets.Blood Rush", "Bloodrush Line Settings", "Line Settings to be used", "secret.lineproperties.bloodrush", false, SECRET_LINE_PROPERTIES_GLOBAL));


    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_AUTOPATHFIND = register(new PathfindLineProperties("Dungeon Secrets.Legacy AutoPathfind", "Line Settings", "Line Settings when pathfinding using above features", "secret.lineproperties.autopathfind", true, SECRET_LINE_PROPERTIES_GLOBAL));

    public static final FeaturePathfindToAll SECRET_PATHFIND_ALL = register(new FeaturePathfindToAll());
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT = register(new PathfindLineProperties("Dungeon Secrets.Pathfind To All", "Parent Line Settings", "Line Settings to be used by default", "secret.lineproperties.apf.parent", false, SECRET_LINE_PROPERTIES_GLOBAL));
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_BAT = register(new PathfindLineProperties("Dungeon Secrets.Pathfind To All", "Bat Line Settings", "Line Settings when pathfind to Bat, when using above feature", "secret.lineproperties.apf.bat", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_CHEST = register(new PathfindLineProperties("Dungeon Secrets.Pathfind To All", "Chest Line Settings", "Line Settings when pathfind to Chest, when using above feature", "secret.lineproperties.apf.chest", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_ESSENCE = register(new PathfindLineProperties("Dungeon Secrets.Pathfind To All", "Essence Line Settings", "Line Settings when pathfind to Essence, when using above feature", "secret.lineproperties.apf.essence", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));
    public static final PathfindLineProperties SECRET_LINE_PROPERTIES_PATHFINDALL_ITEM_DROP = register(new PathfindLineProperties("Dungeon Secrets.Pathfind To All", "Item Drop Line Settings", "Line Settings when pathfind to Item Drop, when using above feature", "secret.lineproperties.apf.itemdrop", true, SECRET_LINE_PROPERTIES_PATHFINDALL_PARENT));

    public static final FeatureSolverRiddle SOLVER_RIDDLE = register(new FeatureSolverRiddle());
    public static final FeatureSolverTictactoe SOLVER_TICTACTOE = register(new FeatureSolverTictactoe());
    public static final SimpleFeature SOLVER_WATERPUZZLE = register(new SimpleFeature("Solver.Any Floor", "Waterboard (Advanced)", "Calculates solution for waterboard puzzle and displays it to user",  "solver.waterboard"));
    public static final SimpleFeature SOLVER_CREEPER = register(new SimpleFeature("Solver.Any Floor", "Creeper", "Draws line between prismarine lamps in creeper room",  "solver.creeper"));
    public static final FeatureSolverTeleport SOLVER_TELEPORT = register(new FeatureSolverTeleport());
    public static final FeatureSolverBlaze SOLVER_BLAZE = register(new FeatureSolverBlaze());
    public static final FeatureSolverIcefill SOLVER_ICEPATH = register(new FeatureSolverIcefill());
    public static final FeatureSolverSilverfish SOLVER_SILVERFISH = register(new FeatureSolverSilverfish());
    public static final FeatureSolverBox SOLVER_BOX = register(new FeatureSolverBox());
    public static final FeatureSolverKahoot SOLVER_KAHOOT = register(new FeatureSolverKahoot());
    public static final FeatureSolverBombdefuse SOLVER_BOMBDEFUSE = register(new FeatureSolverBombdefuse());


    public static final FeatureDungeonMap DUNGEON_MAP = register(new FeatureDungeonMap());
    public static final FeatureDungeonRoomName DUNGEON_ROOMNAME = register(new FeatureDungeonRoomName());
    public static final FeaturePressAnyKeyToCloseChest DUNGEON_CLOSECHEST = register(new FeaturePressAnyKeyToCloseChest());
    public static final FeatureBoxSkelemaster DUNGEON_BOXSKELEMASTER = register(new FeatureBoxSkelemaster());
    public static final FeatureBoxBats DUNGEON_BOXBAT = register(new FeatureBoxBats());
    public static final FeatureBoxStarMobs DUNGEON_BOXSTARMOBS = register(new FeatureBoxStarMobs());
    public static final FeatureWatcherWarning DUNGEON_WATCHERWARNING = register(new FeatureWatcherWarning());
    public static final FeatureDungeonDeaths DUNGEON_DEATHS = register(new FeatureDungeonDeaths());
    public static final FeatureDungeonMilestone DUNGEON_MILESTONE = register(new FeatureDungeonMilestone());
    public static final FeatureDungeonRealTime DUNGEON_REALTIME = register(new FeatureDungeonRealTime());
    public static final FeatureDungeonSBTime DUNGEON_SBTIME = register(new FeatureDungeonSBTime());
    public static final FeatureDungeonSecrets DUNGEON_SECRETS = register(new FeatureDungeonSecrets());
    public static final FeatureDungeonCurrentRoomSecrets DUNGEON_SECRETS_ROOM = register(new FeatureDungeonCurrentRoomSecrets());
    public static final FeatureDungeonTombs DUNGEON_TOMBS = register(new FeatureDungeonTombs());
    public static final FeatureDungeonScore DUNGEON_SCORE = register(new FeatureDungeonScore());
    public static final FeatureWarnLowHealth DUNGEON_LOWHEALTH_WARN = register(new FeatureWarnLowHealth());
    public static final SimpleFeature DUNGEON_INTERMODCOMM = register(new SimpleFeature("Dungeon.Teammates", "Communicate With Other's Dungeons Guide", "Sends total secret in the room to others\nSo that they can use the data to calculate total secret in dungeon run\n\nThis automates player chatting action, (chatting data) Thus it might be against hypixel's rules.\nBut mods like auto-gg which also automate player action and is kinda allowed mod exist so I'm leaving this feature.\nThis option is use-at-your-risk and you'll be responsible for ban if you somehow get banned because of this feature\n(Although it is not likely to happen)\nDefaults to off", "dungeon.intermodcomm", false));
    public static final FeaturePlayerESP DUNGEON_PLAYERESP = register(new FeaturePlayerESP());
    public static final FeatureHideNameTags DUNGEON_HIDENAMETAGS = register(new FeatureHideNameTags());
    public static final FeatureSoulRoomWarning DUNGEON_FAIRYSOUL = register(new FeatureSoulRoomWarning());

    public static final FeatureWarningOnPortal BOSSFIGHT_WARNING_ON_PORTAL = register(new FeatureWarningOnPortal());
    public static final SimpleFeature BOSSFIGHT_CHESTPRICE = register(new FeatureChestPrice());
    public static final FeatureAutoReparty BOSSFIGHT_AUTOREPARTY = register(new FeatureAutoReparty());
    public static final FeatureBossHealth BOSSFIGHT_HEALTH = register(new FeatureBossHealth());
    public static final FeatureHideAnimals BOSSFIGHT_HIDE_ANIMALS = register(new FeatureHideAnimals());
    public static final FeatureThornBearPercentage BOSSFIGHT_BEAR_PERCENT = register(new FeatureThornBearPercentage());
    public static final FeatureThornSpiritBowTimer BOSSFIGHT_BOW_TIMER = register(new FeatureThornSpiritBowTimer());
    public static final FeatureBoxRealLivid BOSSFIGHT_BOX_REALLIVID = register(new FeatureBoxRealLivid());
    public static final FeatureTerracotaTimer BOSSFIGHT_TERRACOTTA_TIMER = register(new FeatureTerracotaTimer());
    public static final FeatureCurrentPhase BOSSFIGHT_CURRENT_PHASE = register(new FeatureCurrentPhase());
    public static final FeatureTerminalSolvers BOSSFIGHT_TERMINAL_SOLVERS = register(new FeatureTerminalSolvers());
    public static final FeatureSimonSaysSolver BOSSFIGHT_SIMONSAYS_SOLVER = register(new FeatureSimonSaysSolver());

    public static final APIKey PARTYKICKER_APIKEY = register(new APIKey());
    public static final FeatureViewPlayerOnJoin PARTYKICKER_VIEWPLAYER = register(new FeatureViewPlayerOnJoin());
    public static final FeatureCustomPartyFinder PARTYKICKER_CUSTOM = register(new FeatureCustomPartyFinder());
    public static final FeaturePartyList PARTY_LIST = register(new FeaturePartyList());

    public static final FeatureTooltipDungeonStat ETC_DUNGEONSTAT = register(new FeatureTooltipDungeonStat());
    public static final FeatureTooltipPrice ETC_PRICE = register(new FeatureTooltipPrice());
    public static final FeatureAbilityCooldown ETC_ABILITY_COOLDOWN = register(new FeatureAbilityCooldown());
    public static final FeatureCooldownCounter ETC_COOLDOWN =  register(new FeatureCooldownCounter());
    public static final FeatureRepartyCommand ETC_REPARTY =  register(new FeatureRepartyCommand());
    public static final FeatureDecreaseExplosionSound ETC_EXPLOSION_SOUND =  register(new FeatureDecreaseExplosionSound());
    public static final FeatureAutoAcceptReparty ETC_AUTO_ACCEPT_REPARTY =  register(new FeatureAutoAcceptReparty());
    public static final FeatureUpdateAlarm ETC_TEST = register(new FeatureUpdateAlarm());

    public static final SimpleFeature FIX_SPIRIT_BOOTS = register(new SimpleFeature("Misc", "Spirit Boots Fixer", "Fix Spirit boots messing up with inventory", "fixes.spirit", true));
    public static final FeatureDisableMessage FIX_MESSAGES = register(new FeatureDisableMessage());

    public static final FeatureCopyMessages ETC_COPY_MSG = register(new FeatureCopyMessages());

    public static final FeaturePenguins ETC_PENGUIN = register(new FeaturePenguins());





    public static final FeatureNicknamePrefix COSMETIC_PREFIX = register(new FeatureNicknamePrefix());
    public static final FeatureNicknameColor COSMETIC_NICKNAMECOLOR = register(new FeatureNicknameColor());



    public static final SimpleFeature DISCORD_RICHPRESENCE = register(new SimpleFeature("Discord", "Discord RPC", "Enable Discord rich presence", "advanced.richpresence", true) {
        {
            parameters.put("disablenotskyblock", new FeatureParameter<Boolean>("disablenotskyblock", "Disable When not on Skyblock", "Disable When not on skyblock", false, "boolean"));
        }
    });
    public static final PartyInviteViewer DISCORD_ASKTOJOIN = register(new PartyInviteViewer());
    public static final PlayingDGAlarm DISCORD_ONLINEALARM = register(new PlayingDGAlarm());
    public static final SimpleFeature DISCORD_DONOTUSE = register(new SimpleFeature("Discord", "Disable Native Library", "Disables usage of jna for discord rpc support.\nBreaks any discord related feature in the mod.\nRequires mod restart to get affected.\n\nThis feature is only for those whose minecraft crashes due to discord gamesdk crash.", "discord.rpc", false));


    public static final SimpleFeature DEBUG = register(new FeatureDebug());
    public static final SimpleFeature ADVANCED_ROOMEDIT = register(new SimpleFeature("Advanced", "Room Edit", "Allow editing dungeon rooms\n\nWarning: using this feature can break or freeze your Minecraft\nThis is only for advanced users only", "advanced.roomedit", false){
        {
            parameters.put("key", new FeatureParameter<Integer>("key", "Key","Press to edit room", Keyboard.KEY_R, "keybind"));
        }
    });

    public static final FeatureRoomDebugInfo ADVANCED_DEBUG_ROOM = register(new FeatureRoomDebugInfo());
    public static final FeatureDebuggableMap ADVANCED_DEBUGGABLE_MAP = register(new FeatureDebuggableMap());
    public static final FeatureRoomCoordDisplay ADVANCED_COORDS = register(new FeatureRoomCoordDisplay());

}
