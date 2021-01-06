package kr.syeyoung.dungeonsguide.dungeon;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.e;
import kr.syeyoung.dungeonsguide.roomprocessor.RoomProcessor;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IntegerCache;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DungeonContext {
    @Getter
    private World world;
    @Getter
    private MapProcessor mapProcessor;

    @Getter
    @Setter
    private BlockPos dungeonMin;

    @Getter
    private Map<Point, DungeonRoom> roomMapper = new HashMap<Point, DungeonRoom>();
    @Getter
    private List<DungeonRoom> dungeonRoomList = new ArrayList<DungeonRoom>();

    @Getter
    private List<RoomProcessor> globalRoomProcessors = new ArrayList<RoomProcessor>();

    @Getter
    private Map<String, Integer> deaths = new HashMap<String, Integer>();
    @Getter
    private List<String[]> milestoneReached = new ArrayList<String[]>();
    @Getter
    @Setter
    private int BossRoomEnterSeconds;

    public DungeonContext(World world) {
        this.world = world;
        mapProcessor = new MapProcessor(this);
    }


    public void tick() {
        mapProcessor.tick();
    }

    public void onChat(ClientChatReceivedEvent event) {
        IChatComponent component = event.message;
        if (component.getFormattedText().contains("$DG-Comm")) {
            event.setCanceled(true);
            String data = component.getFormattedText().substring(component.getFormattedText().indexOf("$DG-Comm"));
            String actual = TextUtils.stripColor(data);
            String coords = actual.split(" ")[1];
            String secrets = actual.split(" ")[2];
            int x = Integer.parseInt(coords.split("/")[0]);
            int z = Integer.parseInt(coords.split("/")[1]);
            int secrets2 = Integer.parseInt(secrets);
            Point roomPt = mapProcessor.worldPointToRoomPoint(new BlockPos(x,70,z));
            e.sendDebugChat(new ChatComponentText("Message from Other dungeons guide :: "+roomPt.x+" / " + roomPt.y + " total secrets "+secrets2));
            DungeonRoom dr = roomMapper.get(roomPt);
            if (dr != null) {
                dr.setTotalSecrets(secrets2);
            }
        }
    }
}
