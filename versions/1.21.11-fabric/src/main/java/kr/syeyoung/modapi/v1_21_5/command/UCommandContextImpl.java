package kr.syeyoung.modapi.v1_21_5.command;

import kr.syeyoung.modapi.command.UCommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class UCommandContextImpl implements UCommandContext {
    private FabricClientCommandSource delegate;

    public UCommandContextImpl(FabricClientCommandSource sender) {
        this.delegate = sender;
    }


}
