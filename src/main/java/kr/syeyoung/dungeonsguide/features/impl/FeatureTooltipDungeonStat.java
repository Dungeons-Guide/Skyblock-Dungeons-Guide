package kr.syeyoung.dungeonsguide.features.impl;

import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.features.listener.TooltipListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class FeatureTooltipDungeonStat extends SimpleFeature implements TooltipListener {
    public FeatureTooltipDungeonStat() {
        super("tooltip", "Dungeon Item Stats", "Shows quality of dungeon items (floor, percentage)", "tooltip.dungeonitem");
    }

    @Override
    public void onTooltip(ItemTooltipEvent event) {
        if (!isEnabled()) return;

        ItemStack hoveredItem = event.itemStack;
        NBTTagCompound compound = hoveredItem.getTagCompound();
        if (compound == null)
            return;
        if (!compound.hasKey("ExtraAttributes"))
            return;
        NBTTagCompound nbtTagCompound = compound.getCompoundTag("ExtraAttributes");

        int floor = nbtTagCompound.getInteger("item_tier");
        int percentage = nbtTagCompound.getInteger("baseStatBoostPercentage");

        if (nbtTagCompound.hasKey("item_tier"))
            event.toolTip.add("§7Obtained in: §c"+(floor == 0 ? "Entrance" : "Floor "+floor));
        if (nbtTagCompound.hasKey("baseStatBoostPercentage"))
            event.toolTip.add("§7Stat Percentage: §"+(percentage == 50 ? "6§l":"c")+(percentage * 2)+"%");
    }
}
