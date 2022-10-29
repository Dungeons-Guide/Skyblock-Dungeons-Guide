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

package kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bombdefuse.chambers;

import kr.syeyoung.dungeonsguide.events.impl.BlockUpdateEvent;
import kr.syeyoung.dungeonsguide.events.impl.KeyBindPressedEvent;
import kr.syeyoung.dungeonsguide.events.impl.PlayerInteractEntityEvent;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.dungeon.roomprocessor.bombdefuse.RoomProcessorBombDefuseSolver;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

@Getter
public abstract class GeneralDefuseChamberProcessor  implements ChamberProcessor{
    private final RoomProcessorBombDefuseSolver solver;
    private final BDChamber chamber;

    public GeneralDefuseChamberProcessor(RoomProcessorBombDefuseSolver solver, BDChamber chamber) {
        this.solver = solver;
        this.chamber = chamber;
    }


    @Override
    public void onDataRecieve(NBTTagCompound compound) {

    }

    @Override
    public void tick() {

    }

    @Override
    public void drawScreen(float partialTicks) {

    }

    @Override
    public void drawWorld(float partialTicks) {

    }

    @Override
    public void chatReceived(IChatComponent chat) {

    }

    @Override
    public void onEntityDeath(LivingDeathEvent deathEvent) {

    }

    @Override
    public void actionbarReceived(IChatComponent chat) {

    }
    @Override
    public void onBlockUpdate(BlockUpdateEvent blockUpdateEvent) {

    }

    @Override
    public boolean readGlobalChat() {
        return false;
    }

    @Override
    public void onPostGuiRender(GuiScreenEvent.DrawScreenEvent.Post event) {

    }

    @Override
    public void onEntityUpdate(LivingEvent.LivingUpdateEvent updateEvent) {

    }

    protected void drawPressKey() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        String str = "Press "+ GameSettings.getKeyDisplayString(FeatureRegistry.getInstance().SOLVER_BOMBDEFUSE.<Integer>getParameter("key").getValue()) + " to save and send solution";
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString(str, (sr.getScaledWidth() - fr.getStringWidth(str)) / 2, (sr.getScaledHeight() - fr.FONT_HEIGHT) / 2, 0xFFFFFFFF);
    }

    @Override
    public void onKeybindPress(KeyBindPressedEvent keyInputEvent) {
        if (keyInputEvent.getKey() == FeatureRegistry.getInstance().SOLVER_BOMBDEFUSE.<Integer>getParameter("key").getValue()) {
            if (!getChamber().isWithinAbsolute(Minecraft.getMinecraft().thePlayer.getPosition())) {
                return;
            }
            onSendData();
        }
    }

    @Override
    public void onInteract(PlayerInteractEntityEvent event) {

    }

    @Override
    public void onInteractBlock(PlayerInteractEvent event) {

    }

    public void onSendData() {}
}
