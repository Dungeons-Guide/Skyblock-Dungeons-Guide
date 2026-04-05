package kr.syeyoung.modapi.event.events;

import kr.syeyoung.modapi.data.EnumFacing;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntityPlayer;
import kr.syeyoung.modapi.event.Cancelable;
import kr.syeyoung.modapi.event.UEvent;
import kr.syeyoung.modapi.world.UWorld;


public class PlayerInteractEvent extends UEvent implements Cancelable {
    public PlayerInteractEvent(UEntityPlayer player, Action action, UWorld world, VectorI3D pos, EnumFacing face, Vector3D localPos) {
        this.player = player;
        this.action = action;
        this.world = world;
        this.pos = pos;
        this.face = face;
        this.localPos = localPos;
    }

    public enum Action {
        RIGHT_CLICK_AIR,
        RIGHT_CLICK_BLOCK,
        LEFT_CLICK_BLOCK
    }

    public enum Result
    {
        DENY,
        DEFAULT,
        ALLOW
    }

    public final UEntityPlayer player;
    public final Action action;
    public final UWorld world;
    public final VectorI3D pos;
    public final EnumFacing face; // Can be null if unknown
    public final Vector3D localPos; // Can be null if unknown




    public Result useBlock = Result.DEFAULT;
    public Result useItem = Result.DEFAULT;


    private boolean canceled = false;
    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean cancel)
    {
        this.canceled = cancel;
        useBlock = (cancel ? Result.DENY : useBlock == Result.DENY ? Result.DEFAULT : useBlock);
        useItem = (cancel ? Result.DENY : useItem == Result.DENY ? Result.DEFAULT : useItem);
    }


}
