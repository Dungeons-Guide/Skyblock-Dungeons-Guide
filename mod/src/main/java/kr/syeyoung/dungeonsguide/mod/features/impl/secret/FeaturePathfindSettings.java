/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.features.impl.secret;

import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.config.types.TCDouble;
import kr.syeyoung.dungeonsguide.mod.config.types.TCEnum;
import kr.syeyoung.dungeonsguide.mod.config.types.TCInteger;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

import java.util.LinkedHashMap;

public class FeaturePathfindSettings extends SimpleFeature {
    public FeaturePathfindSettings() {
        super("Pathfinding & Secrets", "Experimental Pathfind Settings", "Configure A* FineGrid Smart algorithm\n\nUpdate to the config after entering dungeon will not be applied until player enters next dungeon.", "secret.secretpathfind.algorithmconfig", true);
        parameters = new LinkedHashMap<>();
        addParameter("pickaxe", new FeatureParameter<Boolean>("pickaxe", "Use Pickaxe", "Hint to the algorithm to use pickaxe stonks", true, new TCBoolean()));
        addParameter("pickaxe_type", new FeatureParameter<Material>("pickaxe_type", "Pickaxe Type", "Type of your pickaxe", Material.GOLD, new TCEnum<>(Material.values())));
        addParameter("pickaxe_efficiency", new FeatureParameter<Integer>("pickaxe_efficiency", "Pickaxe Efficiency", "Efficiency Enchant Lv on your pickaxe", 6, new TCInteger()));
        addParameter("shovel", new FeatureParameter<Boolean>("shovel", "Use Shovel", "Hint to the algorithm to use shovel", true, new TCBoolean()));
        addParameter("shovel_type", new FeatureParameter<Material>("shovel_type", "Shovel Type", "Type of your shovel", Material.GOLD, new TCEnum<>(Material.values())));
        addParameter("shovel_efficiency", new FeatureParameter<Integer>("shovel_efficiency", "Shovel Efficiency", "Efficiency Enchant Lv on your shovel", 5, new TCInteger()));
        addParameter("axe", new FeatureParameter<Boolean>("axe", "Use Axe", "Hint to the algorithm to use axe", true, new TCBoolean()));
        addParameter("axe_type", new FeatureParameter<Material>("axe_type", "Axe Type", "Type of your axe", Material.GOLD, new TCEnum<>(Material.values())));
        addParameter("axe_efficiency", new FeatureParameter<Integer>("axe_efficiency", "Axe Efficiency", "Efficiency Enchant Lv on your axe",5 , new TCInteger()));

        addParameter("haste", new FeatureParameter<Integer>("haste", "Haste Lv", "Haste Lv on you when you stonk", 1, new TCInteger()));

        addParameter("stonk", new FeatureParameter<Boolean>("stonk", "Stonk Entrance: Use Stairs", "Whether to consider using stair to enter stonking mode", true, new TCBoolean()));
        addParameter("teleport_down", new FeatureParameter<Boolean>("teleport_down", "Stonk Entrance: Use Teleport down", "Whether to consider using teleport tools (hype or aote) on top of fence/stonewall without torch on top to enter stoning mode. If you use Stonk Enter: etherwarp, you can disable this", true, new TCBoolean()));
        addParameter("enderchest", new FeatureParameter<Boolean>("enderchest", "Stonk Entrance: Use Enderchest", "Whether to consider using enderchest on top of stair/fullblock combination to enter stonking mode.", false, new TCBoolean()));
        addParameter("enderpearl", new FeatureParameter<Boolean>("enderpearl", "Stonk Entrance: Use Enderpearl", "Whether to consider using enderpearl anywhere.", false, new TCBoolean()));
        addParameter("tntpearl", new FeatureParameter<Boolean>("tntpearl", "Stonk Entrance: Use Tntpearl", "Whether to consider using tntpearl to enter stonking mode.", false, new TCBoolean()));

        addParameter("max_stonk", new FeatureParameter<Integer>("max_stonk", "Maximum Length of Stonk path", "this is in dg-blocks, which means 1 mc block is 2 dg block", 12, new TCInteger()));

        addParameter("etherwarp", new FeatureParameter<Boolean>("etherwarp", "Routing/Stonk Entrance: Use Etherwarp", "Whether to use etherwarp in normal pathfinding.", true, new TCBoolean()));
        addParameter("max_etherwarp", new FeatureParameter<Integer>("max_etherwarp", "Maximum Length of Etherwarp path", "this is in real-blocks, which means 1 block is really 1 block. Default is 61 for maxed out etherwarp. You probably don't want to make this higher than 61. Range is [0, inf)", 61, new TCInteger()));
        addParameter("boffset_etherwarp", new FeatureParameter<Double>("boffset_etherwarp", "Etherwarp calculation block offset", "Leave it at 0.4 if you don't know what you're doing. Big number = wider cone from block, small number = smaller cone from block. Range: [0~0.5)", 0.4, new TCDouble()));
        addParameter("leeway_etherwarp", new FeatureParameter<Double>("leeway_etherwarp", "Etherwarp calculation block leeway", "Bigger number = less tight etherwarp (etherwarp target away from edge), smaller number = tighter etherwarp (right on the edge of block). Default is 0.0625 (1/16 of block). Range is [0, inf).", 0.0625, new TCDouble()));
    }

    public AlgorithmSettings getAlgorithmSettings() {
        return new AlgorithmSettings(
                getPickaxe(),
                isPickaxe() ? getPickaxeFactor() : -1,
                isShovel() ? getInstabreakShovel() : -1,
                isAxe() ? getInstabreakAxe() : -1,
                isStonkStair(),
                isStonkTeleport(),
                isStonkEChest(),
                isEtherwarp(),
                this.<Integer>getParameter("max_stonk").getValue(),
                isStonkEnderpearl(),
                isStonkTntpearl(),
                this.<Double>getParameter("boffset_etherwarp").getValue(),
                this.<Integer>getParameter("max_etherwarp").getValue(),
                this.<Double>getParameter("leeway_etherwarp").getValue()
        );
    }
    @AllArgsConstructor @Getter
    public static class AlgorithmSettings {
        private final Item pickaxe;
        private final double pickaxeSpeed;
        private final double shovelSpeed;
        private final double axeSpeed;

        private final boolean stonkDown;
        private final boolean stonkTeleport;
        private final boolean stonkEChest;

        private final boolean routeEtherwarp;

        private final int maxStonk;
        private final boolean enderpearl;
        private final boolean tntpearl;

        private final double etherwarpOffset;
        private final int etherwarpRadius;
        private final double etherwarpLeeway;
    }

    public Item getPickaxe() {
        return this.<Material>getParameter("pickaxe_type").getValue().getPickaxe();
    }
    public double getPickaxeFactor() {
        int val1 = this.<Integer>getParameter("haste").getValue();
        int val2 = this.<Integer>getParameter("pickaxe_efficiency").getValue();
        Item.ToolMaterial toolMaterial = this.<Material>getParameter("pickaxe_type").getValue().getToolMaterial();
        double efficiency2 = toolMaterial.getEfficiencyOnProperMaterial();
        efficiency2 += val2 * val2 + 1;
        efficiency2 *= val1 * 0.2 + 1;
//        efficiency2 /= 30;
        return efficiency2;
    }
    public double getInstabreakShovel() {
        int val1 = this.<Integer>getParameter("haste").getValue();
        int val2 = this.<Integer>getParameter("shovel_efficiency").getValue();
        Item.ToolMaterial toolMaterial = this.<Material>getParameter("shovel_type").getValue().getToolMaterial();
        double efficiency2 = toolMaterial.getEfficiencyOnProperMaterial();
        efficiency2 += val2 * val2 + 1;
        efficiency2 *= val1 * 0.2 + 1;
        efficiency2 /= 30;
        return efficiency2;
    }
    public double getInstabreakAxe() {
        int val1 = this.<Integer>getParameter("haste").getValue();
        int val2 = this.<Integer>getParameter("axe_efficiency").getValue();
        Item.ToolMaterial toolMaterial = this.<Material>getParameter("axe_type").getValue().getToolMaterial();
        double efficiency2 = toolMaterial.getEfficiencyOnProperMaterial();
        efficiency2 += val2 * val2 + 1;
        efficiency2 *= val1 * 0.2 + 1;
        efficiency2 /= 30;
        return efficiency2;
    }

    public boolean isPickaxe() {
        return this.<Boolean>getParameter("pickaxe").getValue();
    }
    public boolean isShovel() {
        return this.<Boolean>getParameter("shovel").getValue();
    }
    public boolean isAxe() {
        return this.<Boolean>getParameter("axe").getValue();
    }


    public boolean isStonkStair() {
        return this.<Boolean>getParameter("stonk").getValue();
    }
    public boolean isStonkEChest() {
        return this.<Boolean>getParameter("enderchest").getValue();
    }
    public boolean isStonkTeleport() {
        return this.<Boolean>getParameter("teleport_down").getValue();
    }
    public boolean isStonkEnderpearl() {
        return this.<Boolean>getParameter("enderpearl").getValue();
    }
    public boolean isStonkTntpearl() {
        return this.<Boolean>getParameter("tntpearl").getValue();
    }
    public boolean isEtherwarp() {
        return this.<Boolean>getParameter("etherwarp").getValue();
    }

    @Override
    public boolean isDisableable() {
        return false;
    }

    @Getter @RequiredArgsConstructor
    public enum PathfindStrategy {
        THETA_STAR("The default pathfinding algorithm. It will generate sub-optimal path quickly."),
        A_STAR_DIAGONAL("New pathfinding algorithm. It will generate path that looks like the one JPS generates"),
        A_STAR_FINE_GRID("New pathfinding algorithm. It will generate path that kind of looks like stair"),
        A_STAR_FINE_GRID_STONK("A Star Fine grid, but with STONKING SUPPORT!");
        private final String description;
    }

    public PathfindStrategy getPathfindStrat() {
        return FeaturePathfindSettings.this.<PathfindStrategy>getParameter("strategy").getValue();
    }

    @Getter @RequiredArgsConstructor
    public enum Material {
        WOOD(Item.ToolMaterial.WOOD, Items.wooden_pickaxe), STONE(Item.ToolMaterial.STONE, Items.stone_pickaxe), GOLD(Item.ToolMaterial.GOLD, Items.golden_pickaxe), IRON(Item.ToolMaterial.IRON, Items.iron_pickaxe), DIAMOND(Item.ToolMaterial.EMERALD, Items.diamond_pickaxe);
        private final Item.ToolMaterial toolMaterial;
        private final Item pickaxe;
    }

}
