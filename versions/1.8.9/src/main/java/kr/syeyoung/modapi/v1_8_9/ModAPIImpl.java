package kr.syeyoung.modapi.v1_8_9;

import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.Platform;
import kr.syeyoung.modapi.audio.USoundHandler;
import kr.syeyoung.modapi.data.Vector3D;
import kr.syeyoung.modapi.data.VectorI3D;
import kr.syeyoung.modapi.entity.UEntity;
import kr.syeyoung.modapi.entity.UPlayerSelf;
import kr.syeyoung.modapi.entity.URenderManager;
import kr.syeyoung.modapi.event.EventBus;
import kr.syeyoung.modapi.event.listenerlist.BasicEventBus;
import kr.syeyoung.modapi.fakeserver.FakeServerUtils;
import kr.syeyoung.modapi.item.IItemStackRegistry;
import kr.syeyoung.modapi.resources.UResourceManager;
import kr.syeyoung.modapi.util.RaycastResult;
import kr.syeyoung.modapi.util.USession;
import kr.syeyoung.modapi.v1_8_9.audio.USoundHandlerImpl;
import kr.syeyoung.modapi.v1_8_9.client.renderer.entity.URenderManagerImpl;
import kr.syeyoung.modapi.v1_8_9.command.CommandManagerImpl;
import kr.syeyoung.modapi.v1_8_9.entity.UEntityDelegateFactory;
import kr.syeyoung.modapi.v1_8_9.entity.UEntityPlayerSP;
import kr.syeyoung.modapi.v1_8_9.fakeserver.BlockAccessibleServerLaunchUtils;
import kr.syeyoung.modapi.v1_8_9.item.IItemStackRegistryImpl;
import kr.syeyoung.modapi.v1_8_9.resources.DGTexturePack;
import kr.syeyoung.modapi.v1_8_9.resources.UResourceManagerImpl;
import kr.syeyoung.modapi.v1_8_9.util.USessionImpl;
import kr.syeyoung.modapi.v1_8_9.world.BlockStateRegistryImpl;
import kr.syeyoung.modapi.v1_8_9.world.UWorldImpl;
import kr.syeyoung.modapi.world.IBlockAccessible;
import kr.syeyoung.modapi.world.IBlockRegistry;
import kr.syeyoung.modapi.world.UWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.List;

public class ModAPIImpl implements ModAPI {
    Minecraft delegate; // dummy to trick. TODO

    public ModAPIImpl() {
    }

    public URenderManager getRenderManager() {
        return new URenderManagerImpl(Minecraft.getMinecraft().getRenderManager());
    }

    public UEntity getRenderViewEntity() {
        return Minecraft.getMinecraft().getRenderViewEntity() == null ? null : UEntityDelegateFactory.createEntityFor(Minecraft.getMinecraft().getRenderViewEntity());
    }

    public RaycastResult getObjectMouseOver() {
        MovingObjectPosition position = Minecraft.getMinecraft().objectMouseOver;

        if (position == null) return new RaycastResult(null, RaycastResult.HitType.MISS, null, null);
        return new RaycastResult(
                position.getBlockPos() == null ? null : new VectorI3D(position.getBlockPos().getX(), position.getBlockPos().getY(), position.getBlockPos().getZ()),
                position.typeOfHit == MovingObjectPosition.MovingObjectType.MISS ? RaycastResult.HitType.MISS :
                        position.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY ? RaycastResult.HitType.ENTITY :
                                RaycastResult.HitType.BLOCK,
                position.hitVec == null ? null : new Vector3D(position.hitVec.xCoord, position.hitVec.yCoord, position.hitVec.zCoord),
                position.entityHit == null ? null : UEntityDelegateFactory.createEntityFor(position.entityHit)
        );
    }

    public boolean isSinglePlayer() {
        return Minecraft.getMinecraft().isSingleplayer();
    }

    public UResourceManager getResourceManager() {
        return new UResourceManagerImpl(Minecraft.getMinecraft().getResourceManager());
    }


    public static class PlatformImpl implements Platform {

        @Override
        public String getName() {
            return "forge";
        }

        @Override
        public String getMinecraftVersion() {
            return ForgeVersion.mcVersion;
        }

        @Override
        public String getPlatformVersion() {
            return ForgeVersion.getVersion();
        }

        public static final PlatformImpl INST = new PlatformImpl();
    }

    private static final EventBus defaultEventBus = new BasicEventBus();

    @Override
    public Platform getPlatform() {
        return PlatformImpl.INST;
    }

    @Override
    public EventBus getEventBus() {
        return defaultEventBus;
    }

    @Override
    public USession getSession() {
        return new USessionImpl(Minecraft.getMinecraft().getSession());
    }

    public USoundHandler getSoundHandler() {
        return new USoundHandlerImpl(Minecraft.getMinecraft().getSoundHandler());
    }

    public int getDisplayWidth() {
        return Minecraft.getMinecraft().displayWidth;
    }

    public int getDisplayHeight() {
        return Minecraft.getMinecraft().displayHeight;
    }

    public UPlayerSelf getPlayer() {
        return Minecraft.getMinecraft().thePlayer == null ? null : new UEntityPlayerSP(Minecraft.getMinecraft().thePlayer);
    }

    @Override
    public UWorld getWorld() {
        return Minecraft.getMinecraft().theWorld == null ? null : new UWorldImpl(Minecraft.getMinecraft().theWorld, (BlockStateRegistryImpl) ModAPI.getAPI().getBlockRegistry());
    }

    @Override
    public CommandManagerImpl getCommandManager() {
        return commandManager;
    }

    private PacketInjector packetInjector = new PacketInjector();
    private EventListener eventListener = new EventListener();

    @Override
    public void init() {
        MinecraftForge.EVENT_BUS.register(packetInjector);
        eventListener.register();
        registry.init();

        try {
            List<IResourcePack> resourcePackList = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "aA", "field_110449_ao");
            resourcePackList.add(new DGTexturePack());
            Minecraft.getMinecraft().refreshResources();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Minecraft.getMinecraft().getNetHandler() != null)
            Minecraft.getMinecraft().getNetHandler().getNetworkManager().channel().pipeline().addBefore("packet_handler", "dg_packet_handler_2", packetInjector);
    }

    @Override
    public void unload() {
        MinecraftForge.EVENT_BUS.unregister(packetInjector);
        eventListener.unregister();

        commandManager.unregisterCommands();
        packetInjector.cleanup();

        try {
            List<IResourcePack> resourcePackList = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "aA", "field_110449_ao");
            resourcePackList.removeIf(a -> a instanceof DGTexturePack);
            Minecraft.getMinecraft().refreshResources();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BlockStateRegistryImpl registry = new BlockStateRegistryImpl();
    private CommandManagerImpl commandManager = new CommandManagerImpl();


    @Override
    public IBlockRegistry getBlockRegistry() {
        return registry;
    }

    @Override
    public FakeServerUtils getFakeServerUtils() {
        return new FakeServerUtils() {
            @Override
            public void launchFakeServerAndJoin(IBlockAccessible accessible) {
                BlockAccessibleServerLaunchUtils.launchDungeonServerAndJoin(accessible);
            }

            @Override
            public boolean isRunning() {
                return BlockAccessibleServerLaunchUtils.isDungeonIntegratedServerRunning();
            }
        };
    }

    @Override
    public IItemStackRegistry getItemStackRegistry() {
        return new IItemStackRegistryImpl();
    }
}
