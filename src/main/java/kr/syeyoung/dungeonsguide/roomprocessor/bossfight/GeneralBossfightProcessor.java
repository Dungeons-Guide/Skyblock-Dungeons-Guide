package kr.syeyoung.dungeonsguide.roomprocessor.bossfight;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import net.minecraft.util.IChatComponent;

import java.util.*;

public class GeneralBossfightProcessor implements BossfightProcessor {
    private Map<String, PhaseData> phases = new HashMap<String, PhaseData>();
    private PhaseData currentPhase = null;

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
            if (phaseData.signatureMsgs.contains(chat.getFormattedText())) {
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

    public void onPhaseChange() {}

    @Data
    @Builder
    public static class PhaseData {
        private String phase;
        @Singular
        private Set<String> signatureMsgs;
        @Singular
        private Set<String> nextPhases;
    }
}
