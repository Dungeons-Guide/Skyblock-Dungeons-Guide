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

import net.minecraft.entity.boss.BossStatus;

import java.util.ArrayList;
import java.util.List;

public class BossfightProcessorNecron extends GeneralBossfightProcessor {
    // \A7 to §

    /*
    * §r§4[BOSS] Maxor§r§c: §r§cWELL! WELL! WELL! LOOK WHO'S HERE!§r
    * §r§4[BOSS] Maxor§r§c: §r§cI'VE BEEN TOLD I COULD HAVE A BIT OF FUN WITH YOU.§r
    * §r§4[BOSS] Maxor§r§c: §r§cDON'T DISAPPOINT ME, I HAVEN'T HAD A GOOD FIGHT IN A WHILE.§r
    *
    * §r§4[BOSS] Maxor§r§c: §r§cYOU TRICKED ME!§r
    * §r§4[BOSS] Maxor§r§c: §r§cTHAT BEAM! IT HURTS! IT HURTS!!§r <- never received yet tho
    *
    * §r§4[BOSS] Maxor§r§c: §r§cI'M TOO YOUNG TO DIE AGAIN!§r
    * §r§4[BOSS] Maxor§r§c: §r§cI’LL MAKE YOU REMEMBER MY DEATH!!§r <- never received yet tho
    *
    * R §r§4[BOSS] Maxor§r§c: §r§cMY MINIONS WILL HAVE TO WIPE THE FLOOR AFTER I'M DONE WITH YOU ALL!§r
    *
    *
    * §r§4[BOSS] Storm§r§c: §r§cPathetic Maxor, just like expected.§r
    * §r§4[BOSS] Storm§r§c: §r§cDon't boast about beating this simple minded Wither.§r
    * §r§4[BOSS] Storm§r§c: §r§cMy abilities are unparalleled, in many ways I am the last bastion.§r
    * §r§4[BOSS] Storm§r§c: §r§cThe memory of your death will be your fondest, focus up!§r
    * §r§4[BOSS] Storm§r§c: §r§cThe power of lightning is quite phenomenal. A single strike can vaporize a person whole.§r
    * §r§4[BOSS] Storm§r§c: §r§cI'd be happy to show you what that's like!§r
    *
    * §r§4[BOSS] Storm§r§c: §r§cTHUNDER LET ME BE YOUR CATALYST!§r
    *
    * §r§4[BOSS] Storm§r§c: §r§cOof§r
    * §r§4[BOSS] Storm§r§c: §r§cThe days are numbered until I am finally unleashed again on the world!§r
    * §r§4[BOSS] Storm§r§c: §r§cOuch, that hurt!§r
    * §r§4[BOSS] Storm§r§c: §r§cSlowing me down will be your greatest accomplishment!§r
    * §r§4[BOSS] Storm§r§c: §r§cI should have known that I stood no chance.§r
    * §r§4[BOSS] Storm§r§c: §r§cAt least my son died by your hands.§r
    *
    * §r§4[BOSS] Goldor§r§c: §r§cWho dares trespass into my domain?§r
    * §r§4[BOSS] Goldor§r§c: §r§cLittle ants, plotting and scheming, thinking they are invincible…§r
    * §r§4[BOSS] Goldor§r§c: §r§cI won't let you break the factory core, I gave my life to my Master.§r
    * §r§4[BOSS] Goldor§r§c: §r§cNo one matches me in close quarters.§r
    *
    * §r§4[BOSS] Goldor§r§c: §r§cCloser to me!§r
    * §r§4[BOSS] Goldor§r§c: §r§cThe little ants have a brain it seems.§r
    * §r§4[BOSS] Goldor§r§c: §r§cThere is no stopping me down there!§r
    * §r§4[BOSS] Goldor§r§c: §r§cSlowing me down only prolongs your pain!§r
    * §r§4[BOSS] Goldor§r§c: §r§cI will replace that gate with a stronger one!§r
    * §r§4[BOSS] Goldor§r§c: §r§cStop touching those terminals!§r
    *
    * §r§4[BOSS] Goldor§r§c: §r§cYou have done it, you destroyed the factory…§r
    * §r§4[BOSS] Goldor§r§c: §r§cBut you have nowhere to hide anymore!§r
    * §r§4[BOSS] Goldor§r§c: §r§cYOU ARE FACE TO FACE WITH GOLDOR!§r
    * §r§4[BOSS] Goldor§r§c: §r§c....§r
    * §r§4[BOSS] Goldor§r§c: §r§cNecron, forgive me.§r
    *
    * §r§4[BOSS] Necron§r§c: §r§cYou went further than any human before, congratulations.§r
    * §r§4[BOSS] Necron§r§c: §r§cI'm afraid, your journey ends now.§r
    * §r§4[BOSS] Necron§r§c: §r§cGoodbye.§r
    * §r§4[BOSS] Necron§r§c: §r§cThat's a very impressive trick. I guess I'll have to handle this myself.§r
    * §r§4[BOSS] Necron§r§c: §r§cSometimes when you have a problem, you just need to destroy it all and start again.§r
    * §r§4[BOSS] Necron§r§c: §r§cARGH!§r
    * §r§4[BOSS] Necron§r§c: §r§cLet's make some space!§r
    * §r§4[BOSS] Necron§r§c: §r§cWITNESS MY RAW NUCLEAR POWER!§r
    * §r§4[BOSS] Necron§r§c: §r§cARGH!§r
    * §r§4[BOSS] Necron§r§c: §r§cAll this, for nothing...§r
    *
    * */

    public BossfightProcessorNecron() {
        super("CATACOMBS_FLOOR_SEVEN");
        addPhase(PhaseData.builder()
                .phase("maxor-fight")
                .signatureMsg("§r§4[BOSS] Maxor§r§c: §r§cWELL! WELL! WELL! LOOK WHO'S HERE!§r")
                .nextPhase("storm-fight").nextPhase("lost").build()
        );
        addPhase(PhaseData.builder()
                .phase("storm-fight")
                .signatureMsg("§r§4[BOSS] Storm§r§c: §r§cPathetic Maxor, just like expected.§r")
                .nextPhase("goldor-terminals-1").nextPhase("lost").build()
        );
        addPhase(PhaseData.builder()
                .phase("goldor-terminals-1")
                .signatureMsg("§r§4[BOSS] Goldor§r§c: §r§cWho dares trespass into my domain?§r")
                .nextPhase("goldor-terminals-2").nextPhase("lost").build()
        );
        addPhase(PhaseData.builder()
                .phase("goldor-terminals-2")
                .signatureMsg("§r§aThe gate has been destroyed!§r")
                .nextPhase("goldor-terminals-3").nextPhase("lost").build()
        );
        addPhase(PhaseData.builder()
                .phase("goldor-terminals-3")
                .signatureMsg("§r§aThe gate has been destroyed!§r")
                .nextPhase("goldor-terminals-4").nextPhase("lost").build()
        );

        addPhase(PhaseData.builder()
                .phase("goldor-terminals-4")
                .signatureMsg("§r§aThe gate has been destroyed!§r")
                .nextPhase("goldor-fight").nextPhase("lost").build()
        );
        addPhase(PhaseData.builder()
                .phase("goldor-fight")
                .signatureMsg("§r§aThe Core entrance is opening!§r")
                .nextPhase("necron-intro").nextPhase("lost").build()
        );
        addPhase(PhaseData.builder()
                .phase("necron-intro")
                .signatureMsg("§r§4[BOSS] Necron§r§c: §r§cYou went further than any human before, congratulations.§r")
                .signatureMsg("§r§4[BOSS] Necron§r§c: §r§cFinally, I heard so much about you. The Eye likes you very much.§r")
                .nextPhase("necron-fight").nextPhase("lost").build()
        );
        addPhase(PhaseData.builder()
                .phase("necron-fight")
                .signatureMsg("§r§4[BOSS] Necron§r§c: §r§cThat's a very impressive trick. I guess I'll have to handle this myself.§r")
                .nextPhase("won").nextPhase("lost").build()
        );
        addPhase(PhaseData.builder()
                .phase("won")
                .signatureMsg("§r§4[BOSS] Necron§r§c: §r§cAll this, for nothing...§r").build()
        );
        addPhase(PhaseData.builder()
                .phase("lost")
                .signatureMsg("§r§4[BOSS] Necron§r§c: §r§cFINALLY! This took way too long.§r")
                .signatureMsg("§r§4[BOSS] Goldor§r§c: §r§cFINALLY! This took way too long.§r")
                .signatureMsg("§r§4[BOSS] Storm§r§c: §r§cFINALLY! This took way too long.§r")
                .signatureMsg("§r§4[BOSS] Maxor§r§c: §r§cFINALLY! This took way too long.§r")
                .build()
        );
    }


    @Override
    public List<HealthData> getHealths() {
        List<HealthData> healths = new ArrayList<HealthData>();
        int maxHealth = 1_000_000_000;
        healths.add(new HealthData("Necron", (int) (BossStatus.healthScale * maxHealth),maxHealth , this.getCurrentPhase().startsWith("fight-")));
        return healths;
    }

    @Override
    public String getBossName() {
        return "Necron";
    }
}
