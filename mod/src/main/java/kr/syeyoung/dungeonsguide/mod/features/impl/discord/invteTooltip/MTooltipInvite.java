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

import kr.syeyoung.dungeonsguide.mod.discord.DiscordIntegrationManager;
import kr.syeyoung.dungeonsguide.mod.discord.JDiscordRelation;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.*;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class MTooltipInvite extends MModal {
    private MScrollablePanel mScrollablePanel;
    private MList list;
    private MTextField search;
    private MButton close;
    private Set<Long> invited = new HashSet<>();
    public MTooltipInvite() {
        setTitle("Invite Discord Friend");
        mScrollablePanel = new MScrollablePanel(1);
        mScrollablePanel.setHideScrollBarWhenNotNecessary(true);
        add(mScrollablePanel);

        list = new MList() {
            @Override
            public void resize(int parentWidth, int parentHeight) {
                setSize(new Dimension(parentWidth, 9999));
                realignChildren();
            }
        };
        list.setGap(1);
        list.setDrawLine(true);
        list.setGapLineColor(RenderUtils.blendAlpha(0x141414, 0.13f));
        mScrollablePanel.add(list);
        mScrollablePanel.getScrollBarY().setWidth(5);

        search = new MTextField() {
            @Override
            public void edit(String str) {
                super.edit(str); resetListContent();
            }
        };
        search.setPlaceHolder("Search...");
        add(search);

        close = new MButton();
        close.setText("X");
        close.setBackground( RenderUtils.blendAlpha(0x141414, 0.20f));
        close.setHover( RenderUtils.blendAlpha(0x141414, 0.25f));
        close.setClicked( RenderUtils.blendAlpha(0x141414, 0.24f));
        close.setOnActionPerformed(this::close);
        addSuper(close);
        resetListContent();
    }

    @Override
    public void onBoundsUpdate() {
        super.onBoundsUpdate();
        Dimension effDim = getModalContent().getSize();
        search.setBounds(new Rectangle(5,2,effDim.width-10, 15));
        mScrollablePanel.setBounds(new Rectangle(10,18,effDim.width-20, effDim.height-25));
        close.setBounds(new Rectangle(getModalContent().getBounds().x + effDim.width-15,getModalContent().getBounds().y - 16,15,15));
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        GlStateManager.pushMatrix();
        super.render(absMousex, absMousey, relMousex0, relMousey0, partialTicks, scissor);
        GlStateManager.popMatrix();

        Dimension modalSize = getModalSize();
        Dimension effDim = getEffectiveDimension();
        int x = (effDim.width-modalSize.width)/2;
        int y = (effDim.height - modalSize.height)/2;
        GlStateManager.translate(x,y, 0);
    }

    private void resetListContent() {
        for (MPanel childComponent : list.getChildComponents()) {
            list.remove(childComponent);
        }

        String searchTxt = search.getText().trim().toLowerCase();
        for (JDiscordRelation value : DiscordIntegrationManager.INSTANCE.getRelationMap().values()) {
//            if (value.getDiscordActivity().getApplicationId() != 816298079732498473L) continue;
            if (value.getRelationType() == JDiscordRelation.DiscordRelationType.Blocked) continue;
            if (!searchTxt.isEmpty() && !(value.getDiscordUser().getName().toLowerCase().contains(searchTxt))) continue;
            list.add(new MTooltipInviteElement(value, invited.contains(value.getDiscordUser().getId()), this::invite));
        }
        setBounds(getBounds());
    }

    public void invite(long id) {
        invited.add(id);
        DiscordIntegrationManager.INSTANCE.sendInvite(Long.toUnsignedString(id), "Pls join my game");
    }
}
