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

package kr.syeyoung.dungeonsguide.mod.features.impl.discord.invteTooltip;

<<<<<<<< HEAD:mod/src/main/java/kr/syeyoung/dungeonsguide/features/impl/discord/invteTooltip/MTooltipInviteElement.java
import kr.syeyoung.dungeonsguide.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.impl.discord.inviteViewer.ImageTexture;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.rpc.JDiscordRelation;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
========
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteViewer.ImageTexture;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.mod.discord.rpc.JDiscordRelation;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
>>>>>>>> origin/breaking-changes-just-working-im-not-putting-all-of-these-into-3.0-but-for-the-sake-of-beta-release-this-thing-exists:mod/src/main/java/kr/syeyoung/dungeonsguide/mod/features/impl/discord/invteTooltip/MTooltipInviteElement.java
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class MTooltipInviteElement extends MPanel {
    private JDiscordRelation relation;
    private MButton invite;
    public MTooltipInviteElement(JDiscordRelation jDiscordRelation, boolean invited, Consumer<Long> inviteCallback) {
        this.relation = jDiscordRelation;
        this.invite = new MButton();
        if (!invited) {
            invite.setText("Invite");
            invite.setRoundness(2);
            invite.setBorder(0xFF02EE67);
            invite.setHover(RenderUtils.blendAlpha(0x141414, 0.2f));
            invite.setBackground(RenderUtils.blendAlpha(0x141414, 0.17f));
            invite.setForeground(new Color(0xFF02EE67));

            invite.setOnActionPerformed(() -> {
                if (invite.isEnabled())
                    inviteCallback.accept(jDiscordRelation.getDiscordUser().getId());

                invite.setText("Sent");
                invite.setRoundness(2);
                invite.setHover(0); invite.setBorder(0);
                invite.setBackground(0); invite.setDisabled(0); invite.setEnabled(false);
                invite.setForeground(new Color(0xFF02EE67));
            });
        } else {
            invite.setText("Sent");
            invite.setRoundness(2);
            invite.setHover(0);
            invite.setBackground(0); invite.setDisabled(0); invite.setEnabled(false);
            invite.setForeground(new Color(0xFF02EE67));
        }

        add(invite);
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        if (lastAbsClip.contains(absMousex, absMousey) && getTooltipsOpen() == 0) {
            RenderUtils.drawRoundedRectangle(0,0,bounds.width, bounds.height, 3, Math.PI/8, RenderUtils.blendAlpha(0x141414, 0.17f));
            GlStateManager.enableTexture2D();
        }

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        if (!relation.getDiscordUser().getAvatar().isEmpty()){
            String avatar = "https://cdn.discordapp.com/avatars/"+Long.toUnsignedString(relation.getDiscordUser().getId())+"/"+relation.getDiscordUser().getAvatar()+"."+(relation.getDiscordUser().getAvatar().startsWith("a_") ? "gif":"png");
            Future<ImageTexture> loadedImageFuture = FeatureRegistry.DISCORD_ASKTOJOIN.loadImage(avatar);
            ImageTexture loadedImage = null;
            if (loadedImageFuture.isDone()) {
                try {
                    loadedImage = loadedImageFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            if (loadedImage != null) {
                loadedImage.drawFrameAndIncrement( 3,3,bounds.height-6,bounds.height-6);
            } else {
                Gui.drawRect(3, 3, bounds.height - 6, bounds.height-6, 0xFF4E4E4E);
            }
        }
        fr.drawString(relation.getDiscordUser().getUsername()+"#"+relation.getDiscordUser().getDiscriminator(), bounds.height,(bounds.height-fr.FONT_HEIGHT)/2, -1);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, 20);
    }

    @Override
    public void onBoundsUpdate() {
        invite.setBounds(new Rectangle(bounds.width-53,2,50,bounds.height-4));
    }
}
