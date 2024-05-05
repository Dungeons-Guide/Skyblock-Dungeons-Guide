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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomprocessor.bossfight;

import com.google.gson.Gson;
import kr.syeyoung.dungeonsguide.mod.events.impl.BlockUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.BossfightRenderSettings;
import lombok.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public abstract class GeneralBossfightProcessor implements BossfightProcessor {
    private final Map<String, PhaseData> phases = new HashMap<String, PhaseData>();
    private PhaseData currentPhase = null;

    @Getter
    private final String name;

    @Override
    public String getFloorName() {
        return name;
    }

    @Override
    public BossfightRenderSettings getMapRenderSettings() {
        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("dungeonsguide:map/bossfight/"+name+".json"));
            if (resource != null) {
                resource.getInputStream();
                try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
                    settings = new Gson()
                            .fromJson(inputStreamReader, BossfightRenderSettingSettings.class);
                    if (settings.getResources() != null)
                        for (BossfightRenderSettings value : settings.getResources().values()) {
                            value.setResourceLocation(new ResourceLocation(value.getLocation()));
                        }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (settings == null) return null;
        return settings.getResources().get(settings.phaseMap.getOrDefault(getCurrentPhase(), "default"));
    }

    private BossfightRenderSettingSettings settings;
    public GeneralBossfightProcessor(String name) {
        this.name = name;

        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("dungeonsguide:map/bossfight/"+name+".json"));
            if (resource != null) {
                resource.getInputStream();
                try (InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream())) {
                    settings = new Gson()
                            .fromJson(inputStreamReader, BossfightRenderSettingSettings.class);
                    if (settings.getResources() != null)
                        for (BossfightRenderSettings value : settings.getResources().values()) {
                            value.setResourceLocation(new ResourceLocation(value.getLocation()));
                        }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Data
    public static class BossfightRenderSettingSettings {
        private Map<String, BossfightRenderSettings> resources = new HashMap<>();
        private Map<String, String> phaseMap = new HashMap<>();
    }

    private World world;

    public void addPhase(PhaseData phaseData) {
        if (phaseData == null) return;
        if (currentPhase == null) currentPhase = phaseData;
        phases.put(phaseData.getPhase(), phaseData);
    }

    @Override
    public List<String> getPhases() {
        List<String> phases = new ArrayList<String>();
        for (PhaseData pd:this.phases.values())
            phases.add(pd.getPhase());
        return phases;
    }

    @Override
    public List<String> getNextPhases() {
        if (currentPhase == null) return Collections.emptyList();
        List<String> phases = new ArrayList<String>(this.currentPhase.getNextPhases());
        return phases;
    }


    @Override
    public String getCurrentPhase() {
        return currentPhase == null ? "unknown" : currentPhase.getPhase();
    }

    @Override
    public void chatReceived(IChatComponent chat) {
        if (currentPhase == null) return;

        for (String nextPhase : currentPhase.getNextPhases()) {
            PhaseData phaseData = phases.get(nextPhase);
            if (phaseData == null) continue;
            if (phaseData.signatureMsgs.contains(chat.getFormattedText().replace(" ", ""))) {
                    currentPhase = phaseData;
                    onPhaseChange();
                    return;
            }
        }
    }

    @Override
    public void actionbarReceived(IChatComponent chat) {}

    @Override
    public void tick() {}

    @Override
    public void drawScreen(float partialTicks) {}

    @Override
    public void drawWorld(float partialTicks) {}

    @Override
    public boolean readGlobalChat() {return true;}

    @Override
    public void onPostGuiRender(GuiScreenEvent.DrawScreenEvent.Post event) {

    }

    @Override
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent) {

    }

    @Override
    public void onInteract(PlayerInteractEntityEvent event) {

    }

    @Override
    public void onKeybindPress(KeyBindPressedEvent keyInputEvent) {

    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {

    }
    @Override
    public void onEntityDeath(LivingDeathEvent deathEvent) {

    }
    @Override
    public void onBlockUpdate(BlockUpdateEvent blockUpdateEvent) {

    }

    public void onPhaseChange() {}

    @Data
    @Builder
    public static class PhaseData {
        private PhaseData(String phase, Set<String> signatureMsgs, Set<String> nextPhase) {
            this.phase = phase;
            this.nextPhases = new HashSet<>(nextPhase);
            this.signatureMsgs = new HashSet<>();
            for (String signatureMsg : signatureMsgs) {
                this.signatureMsgs.add(signatureMsg.replace(" ", ""));
            }
        }

        private String phase;
        @Singular
        private Set<String> signatureMsgs;
        @Singular
        private Set<String> nextPhases;

    }
}
