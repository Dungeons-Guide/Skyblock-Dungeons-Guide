/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.events.annotations;

import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.IFeature;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EventHandlerRegistry {
    public static Logger logger = LogManager.getLogger("DG-EventPropagator");

    public static final Map<Class, List<InvocationTarget>> targets = new HashMap<>();

    @AllArgsConstructor
    @Getter
    private static class InvocationTarget<T extends Event> {
        private final Class<T> targetEvent;
        private final IFeature feature;
        private final String targetName;
        private final Supplier<Boolean> condition;
        private final MethodHandle invokeSite;
    }

    private static final int busID;
    static {
        try {
            Field f = EventBus.class.getDeclaredField("busID");
            f.setAccessible(true);
            busID = (int) f.get(MinecraftForge.EVENT_BUS);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerEvents(IFeature feature) {
        Class clazz = feature.getClass();
        while (clazz != null) {
            for (Method declaredMethod : clazz.getDeclaredMethods()) {
                declaredMethod.setAccessible(true);
                DGEventHandler dgEventHandler = declaredMethod.getAnnotation(DGEventHandler.class);
                if (dgEventHandler == null) continue;
                if (declaredMethod.getParameterTypes().length != 1)
                    throw new RuntimeException("Too much or too less parameers: "+declaredMethod.getName()+" on "+clazz.getName());
                Class eventType = declaredMethod.getParameterTypes()[0];
                if (!targets.containsKey(eventType)) targets.put(eventType, new LinkedList<>());

                boolean force = dgEventHandler.ignoreDisabled();
                boolean sb = dgEventHandler.triggerOutOfSkyblock();

                Supplier<Boolean> booleanSupplier =
                        force && sb ? null :
                                force ? SkyblockStatus::isOnSkyblock :
                                        sb ? feature::isEnabled : () -> SkyblockStatus.isOnSkyblock() && feature.isEnabled();

                try {
                    InvocationTarget target = new InvocationTarget(
                            eventType,
                            feature,
                            (feature.getClass().getSimpleName() + ":" + declaredMethod.getName()).replace(".", "/"),
                            booleanSupplier,
                            MethodHandles.publicLookup().unreflect(declaredMethod).bindTo(feature)
                    );
                    targets.get(eventType).add(target);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e); // this shouldn't happen.
                }

            }
            clazz = clazz.getSuperclass();
        }
        registerActualListeners();
    }

    private static Map<Event, IEventListener> registeredHandlers = new HashMap<>();

    public static void registerActualListeners() {
        for (Class<? extends Event> aClass : targets.keySet()) {
            if (registeredHandlers.containsKey(aClass)) continue;
            try {
                Event ev = aClass.getConstructor().newInstance();
                List<InvocationTarget> targetList = targets.get(aClass);
                Profiler profiler = Minecraft.getMinecraft().mcProfiler;
                IEventListener registered;
                ev.getListenerList().register(busID, EventPriority.NORMAL, registered = (event) -> {
                    profiler.startSection("Dungeons Guide Event Handling");
                    for (InvocationTarget target : targetList) {
                        profiler.startSection(target.getTargetName());
                        try {
                            if (target.condition == null || (target.condition.get() == Boolean.TRUE)) // it is safe to use this here.
                                target.invokeSite.invoke(event);
                        } catch (Throwable e) {
                            logger.error("An error occured while handling event: \nFeature = " + target.getFeature().getClass().getName(), e);
                        }
                        profiler.endSection();
                    }
                    profiler.endSection();
                });
                registeredHandlers.put(ev, registered);
            } catch (Exception e) {
                throw new RuntimeException("An error occured while registering listener for "+aClass.getName(), e);
            }
        }
    }

    public static void unregisterListeners() {
        for (Map.Entry<Event, IEventListener> eventIEventListenerEntry : registeredHandlers.entrySet()) {
            eventIEventListenerEntry.getKey().getListenerList().unregister(busID, eventIEventListenerEntry.getValue());
        }
        registeredHandlers.clear();
    }
}
