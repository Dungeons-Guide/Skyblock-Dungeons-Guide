package kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.cachedpathfind;

import com.sun.nio.file.ExtendedWatchEventModifier;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.dungeon.pathfinding.algorithmSetting.AlgorithmSettingRegistry;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class PathfindResultRegistry {
    @Getter
    private List<PathfindPrecalculation> loaded = new ArrayList<>();
    private Map<String, List<PathfindPrecalculation>> byId = new HashMap<>();
    private Map<String, PathfindPrecalculation> byFile = new HashMap<>();
    @Getter
    private Map<UUID, List<PathfindPrecalculation>> byRoom = new HashMap<>();
    private Map<String, List<PathfindPrecalculation>> byHash = new HashMap<>();
    private Map<String, List<PathfindPrecalculation>> byId2 = new HashMap<>();

    @Getter
    private static PathfindResultRegistry INSTANCE;

    public PathfindResultRegistry(File dir) throws IOException {
        if (INSTANCE != null) throw new IllegalStateException("Already initialized");
        PathfindResultRegistry.INSTANCE = this;

        loadAll(dir);

        new Thread(DungeonsGuide.THREAD_GROUP, () -> {
            try {
                FileSystem fs = FileSystems.getDefault();
                WatchService ws = fs.newWatchService();

                Path pTemp = dir.toPath();
                pTemp.register(ws, new WatchEvent.Kind[] {StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY}, ExtendedWatchEventModifier.FILE_TREE);

                while(!Thread.interrupted())
                {
                    try {
                        WatchKey k = ws.take();
                        for (WatchEvent<?> e : k.pollEvents()) {
                            Path c = (Path) e.context();
                            if (!c.getFileName().toString().endsWith(".pfres")) continue;

                            if (e.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                register(new PathfindPrecalculation(c.toFile()));
                            } else if (e.kind()== StandardWatchEventKinds.ENTRY_DELETE) {
                                PathfindPrecalculation precalculation = getByFile(c.toFile().getAbsolutePath());
                                unregister(precalculation);
                            } else if (e.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                PathfindPrecalculation precalculation = getByFile(c.toFile().getAbsolutePath());
                                unregister(precalculation);
                                register(new PathfindPrecalculation(c.toFile()));
                            }
                            System.out.printf("%s %d %s\n", e.kind(), e.count(), c);
                        }
                        k.reset();
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void register(PathfindPrecalculation precalculation) {
        if (!byId.containsKey(precalculation.getId()))
            byId.put(precalculation.getId(), new ArrayList<>());
        byId.get(precalculation.getId()).add(precalculation);
        byFile.put(precalculation.getFile(), precalculation);
        if (!byRoom.containsKey(precalculation.getRoomUID()))
            byRoom.put(precalculation.getRoomUID(), new ArrayList<>());
        byRoom.get(precalculation.getRoomUID()).add(precalculation);
        if (!byHash.containsKey(precalculation.getTargetHash()))
            byHash.put(precalculation.getTargetHash(), new ArrayList<>());
        byHash.get(precalculation.getTargetHash()).add(precalculation);
        if (!byId2.containsKey(precalculation.getTargetId()))
            byId2.put(precalculation.getTargetId(), new ArrayList<>());
        byId2.get(precalculation.getTargetId()).add(precalculation);
        loaded.add(precalculation);


        AlgorithmSettingRegistry.registerAlgorithmSetting(precalculation.getAlgorithmSetting());
    }

    public PathfindPrecalculation getById(String id) {
        List<PathfindPrecalculation> list  =getsById(id);
        return list.isEmpty() ? null : list.get(0);
    }
    public List<PathfindPrecalculation> getsById(String id) {
        List<PathfindPrecalculation> list = byId.get(id);
        if (list == null) return Collections.emptyList();
        return list;
    }
    public PathfindPrecalculation getByTargetId(String targetid) {
        List<PathfindPrecalculation> list  =getsByTargetId(targetid);
        return list.isEmpty() ? null : list.get(0);
    }
    public List<PathfindPrecalculation> getsByTargetId(String targetid) {
        List<PathfindPrecalculation> list = byId2.get(targetid);
        if (list == null) return Collections.emptyList();
        return list;
    }
    public List<PathfindPrecalculation> getsByHash(String hash) {
        List<PathfindPrecalculation> list = byHash.get(hash);
        if (list == null) return Collections.emptyList();
        return list;
    }

    public List<PathfindPrecalculation> getByRoom(UUID roomUID) {
        return byRoom.getOrDefault(roomUID, Collections.emptyList());
    }

    public PathfindPrecalculation getByFile(String file) {
        return byFile.get(file);
    }

    public void unregister(PathfindPrecalculation precalculation) {
        byId.get(precalculation.getId()).remove(precalculation);
        if (byId.get(precalculation.getId()).isEmpty()) byId.remove(precalculation.getId());

        byFile.remove(precalculation.getFile());
        byRoom.get(precalculation.getRoomUID()).remove(precalculation);
        byHash.get(precalculation.getTargetHash()).remove(precalculation);
        loaded.remove(precalculation);
    }


    public void loadAll(File dir) throws IOException {
        byId.clear();
        byHash.clear();
        byRoom.clear();
        byId2.clear();
        byFile.clear();

        try {
            Files.walk(dir.toPath(), FileVisitOption.FOLLOW_LINKS)
                    .forEach(path -> {
                        if (!path.getFileName().toString().endsWith(".pfres")) return;
                        try {
                            PathfindPrecalculation pathfindCache = new PathfindPrecalculation(path.toFile());
                            register(pathfindCache);
                        } catch (Exception e) {
                            System.out.println(path.getFileName());
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
