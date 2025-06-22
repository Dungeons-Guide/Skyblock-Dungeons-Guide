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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit;


import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.*;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.mechanicedit.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValueEditRegistry {
    private static final Map<String, ValueEditCreator> valueEditMap = new HashMap<String, ValueEditCreator>();

    public static ValueEditCreator getValueEditMap(String className) {
        return valueEditMap.get(className);
    }

    public static List<String> getClassesSupported() {
        return new ArrayList<String>(valueEditMap.keySet());
    }

    static {
        valueEditMap.put("null", new ValueEditNull());
        valueEditMap.put(String.class.getName(), new ValueEditString.Generator());
        valueEditMap.put(Boolean.class.getName(), new ValueEditBoolean.Generator());
        valueEditMap.put(Integer.class.getName(), new ValueEditInteger.Generator());
        valueEditMap.put(Float.class.getName(), new ValueEditFloat.Generator());
        valueEditMap.put(OffsetPoint.class.getName(), new ValueEditOffsetPoint.Generator());
        valueEditMap.put(OffsetPointSet.class.getName(), new ValueEditOffsetPointSet.Generator());
        valueEditMap.put(Color.class.getName(), new ValueEditColor.Generator());
        valueEditMap.put(AColor.class.getName(), new ValueEditAColor.Generator());


        valueEditMap.put(DungeonFairySoulState.DungeonFairySoulData.class.getName(), new ValueEditFairySoul.Generator());
        valueEditMap.put(DungeonNPCState.DungeonNPCData.class.getName(), new ValueEditNPC.Generator());
        valueEditMap.put(DungeonTombState.DungeonTombData.class.getName(), new ValueEditTomb.Generator());
        valueEditMap.put(DungeonBreakableWallState.DungeonBreakableWallData.class.getName(), new ValueEditBreakableWall.Generator());
        valueEditMap.put(DungeonJournalState.DungeonJournalData.class.getName(), new ValueEditJournal.Generator());
        valueEditMap.put(DungeonDummyState.DungeonDummyData.class.getName(), new ValueEditDummy.Generator());
        valueEditMap.put(DungeonMushroomState.DungeonMushroomData.class.getName(), new ValueEditMushroom.Generator());
        valueEditMap.put(DungeonSecretBatState.DungeonSecretBatData.class.getName(), new ValueEditSecretBat.Generator());
        valueEditMap.put(DungeonSecretItemDropState.DungeonSecretItemDropData.class.getName(), new ValueEditSecretItemdrop.Generator());
        valueEditMap.put(DungeonSecretEssenceState.DungeonSecretEssenceData.class.getName(), new ValueEditSecretEssence.Generator());
        valueEditMap.put(DungeonSecretChestState.DungeonSecretChestData.class.getName(), new ValueEditSecretChest.Generator());
        valueEditMap.put(DungeonSecretDoubleChestState.DungeonSecretDoubleChestData.class.getName(), new ValueEditSecretDoubleChest.Generator());

        valueEditMap.put(DungeonPressurePlateState.DungeonPressurePlateData.class.getName(), new ValueEditPressurePlate.Generator());
        valueEditMap.put(DungeonOnewayLeverState.DungeonOnewayLeverData.class.getName(), new ValueEditOnewayLever.Generator());
        valueEditMap.put(DungeonLeverState.DungeonLeverData.class.getName(), new ValueEditLever.Generator());
        valueEditMap.put(DungeonDoorState.DungeonDoorData.class.getName(), new ValueEditDoor.Generator());
        valueEditMap.put(DungeonOnewayDoorState.DungeonOnewayDoorData.class.getName(), new ValueEditOnewayDoor.Generator());
        valueEditMap.put(DungeonRedstoneKeyState.DungeonRedstoneKeyData.class.getName(), new ValueEditRedstoneKey.Generator());
        valueEditMap.put(DungeonRedstoneKeySlotState.DungeonRedstoneKeySlotData.class.getName(), new ValueEditRedstoneKeySlot.Generator());
        valueEditMap.put(DungeonWizardCrystalState.DungeonWizardCrystalData.class.getName(), new ValueEditWizardCrystal.Generator());
        valueEditMap.put(DungeonWizardState.DungeonWizardData.class.getName(), new ValueEditWizard.Generator());
        valueEditMap.put(DungeonFloorTrapState.DungeonFloorTrapData.class.getName(), new ValueEditFloorTrap.Generator());
        valueEditMap.put(DungeonTripwireTrapState.DungeonTripwireTrapData.class.getName(), new ValueEditTripwireTrap.Generator());
        valueEditMap.put(DungeonArrowTrapState.DungeonArrowTrapData.class.getName(), new ValueEditArrowTrap.Generator());
        valueEditMap.put(DungeonRoomDoor2State.DungeonRoomDoor2Data.class.getName(), new ValueEditRoomDoor.Generator());
        valueEditMap.put(DungeonFakeChestTrapState.DungeonFakeChestTrapData.class.getName(), new ValueEditFakeChestTrap.Generator());
        valueEditMap.put(DungeonFireTrapState.DungeonFireTrapData.class.getName(), new ValueEditFireTrap.Generator());
        valueEditMap.put(DungeonCrusherTrapState.DungeonCrusherTrapData.class.getName(), new ValueEditCrusherTrap.Generator());
    }
}
