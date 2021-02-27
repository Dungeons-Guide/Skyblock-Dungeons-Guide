package kr.syeyoung.dungeonsguide.features.impl.etc;

import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.SimpleFeature;

public class FeatureRepartyCommand extends SimpleFeature {
    public FeatureRepartyCommand() {
       super("ETC", "Enable Reparty Command From DG", "if you disable, /dg reparty will still work, Auto reparty will still work\nRequires Restart to get applied", "qol.reparty");
       parameters.put("command", new FeatureParameter<String>("command", "The Command", "Command that the reparty will be bound to", "reparty", "string"));
    }
}
