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

package kr.syeyoung.dungeonsguide.chat;

import kr.syeyoung.dungeonsguide.stomp.StompManager;
import kr.syeyoung.dungeonsguide.stomp.StompPayload;
import lombok.Data;
import net.minecraft.client.Minecraft;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


@Data
public class PartyContext {
    private String partyID;

    private String partyOwner;
    private Set<String> partyModerator; private boolean isModeratorComplete;
    private Set<String> partyMember; private boolean isMemberComplete;

    private Set<String> dgUsers;

    private Set<String> partyRawMembers = new HashSet<>(); private boolean isRawMemberComplete;

    private Boolean allInvite;

    private boolean partyExistHypixel = true;

    public void setPartyOwner(String partyOwner) {
            this.partyOwner = partyOwner;
            if (partyMember != null) partyMember.remove(partyOwner);
            if (partyModerator != null) partyModerator.remove(partyOwner);
            addRawMember(partyOwner);
    }
    public void addPartyModerator(String partyModerator) {
        if (partyModerator.equalsIgnoreCase(partyOwner)) partyOwner = null;
        if (partyMember != null) partyMember.remove(partyModerator);

        if (this.partyModerator == null) this.partyModerator = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.partyModerator.add(partyModerator);
        addRawMember(partyModerator);
    }
    public void addPartyMember(String partyMember) {
        if (partyMember.equalsIgnoreCase(partyOwner)) partyOwner = null;
        if (partyModerator != null) partyModerator.remove(partyMember);

        if (this.partyMember == null) this.partyMember = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.partyMember.add(partyMember);
        addRawMember(partyMember);
    }

    public void addDgUser(String partyMember) {
        if (this.dgUsers == null) this.dgUsers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.dgUsers.add(partyMember);
    }

    public void addRawMember(String partyMember){
        partyRawMembers.add(partyMember);

        String actualPayload = "C:"+Minecraft.getMinecraft().getSession().getUsername();
        StompManager.getInstance().send(new StompPayload().header("destination", "/app/party.broadcast").payload(
                new JSONObject().put("partyID", PartyManager.INSTANCE.getPartyContext().getPartyID())
                        .put("payload", actualPayload).toString()
        ));
    }
    public void removeFromParty(String username) {
        if (username.equalsIgnoreCase(partyOwner)) {
            partyOwner = null;
        }
        if (partyModerator != null) partyModerator.remove(username);
        if (partyMember != null) partyMember.remove(username);
        partyRawMembers.remove(username);
    }



    public boolean isDgUser(String username) {
        return dgUsers != null && dgUsers.contains(username);
    }
    public boolean hasModerator(String username) {
        return partyModerator != null && partyModerator.contains(username);
    }
    public boolean hasMember(String username) {
        return partyMember != null && partyMember.contains(username);
    }
    public boolean hasLeader(String username) {
        return username.equalsIgnoreCase(partyOwner);
    }
    public boolean isSelfSolo() {
        return hasLeader(Minecraft.getMinecraft().getSession().getUsername()) && getPartyRawMembers().size() == 1;
    }
}
