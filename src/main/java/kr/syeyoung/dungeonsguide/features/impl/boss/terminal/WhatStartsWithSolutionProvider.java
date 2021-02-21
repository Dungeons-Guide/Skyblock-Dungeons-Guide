package kr.syeyoung.dungeonsguide.features.impl.boss.terminal;

import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import org.apache.logging.log4j.core.appender.db.jpa.converter.ThrowableAttributeConverter;

import java.util.ArrayList;
import java.util.List;

public class WhatStartsWithSolutionProvider implements TerminalSolutionProvider{
    @Override
    public TerminalSolution provideSolution(ContainerChest chest, List<Slot> clicked) {
        String that = chest.getLowerChestInventory().getName().replace("What starts with: '", "").replace("'?", "").trim().toLowerCase();

        TerminalSolution ts = new TerminalSolution();
        ts.setCurrSlots(new ArrayList<Slot>());
        for (Slot inventorySlot : chest.inventorySlots) {
            if (inventorySlot.inventory != chest.getLowerChestInventory()) continue;
            if (inventorySlot.getHasStack() && inventorySlot.getStack() != null && !inventorySlot.getStack().isItemEnchanted() ) {
                String name = TextUtils.stripColor(inventorySlot.getStack().getDisplayName()).toLowerCase();
                if (name.startsWith(that))
                    ts.getCurrSlots().add(inventorySlot);
            }
        }
        return ts;
    }

    @Override
    public boolean isApplicable(ContainerChest chest) {
        return chest.getLowerChestInventory().getName().startsWith("What starts with: '");
    }
}
