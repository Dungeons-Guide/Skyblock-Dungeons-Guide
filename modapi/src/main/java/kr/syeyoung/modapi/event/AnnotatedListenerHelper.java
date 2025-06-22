package kr.syeyoung.modapi.event;

import lombok.AllArgsConstructor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class AnnotatedListenerHelper {


    @AllArgsConstructor
    private static class AnnotatedListener<T extends UEvent> implements EventListener<T> {
        private MethodHandle methodHandle;
        public String name;
        @Override
        public EventProcessResult onEvent(UEvent event) {
            try {
                methodHandle.invoke(event);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return EventProcessResult.COMPLETE;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static List<ListenerRegistration> registerListeners(EventBus eventBus, Object o) throws IllegalAccessException {
        List<ListenerRegistration> registrations = new ArrayList<>();
        for (Method declaredMethod : o.getClass().getDeclaredMethods()) {
            SubscribeEvent event = declaredMethod.getAnnotation(SubscribeEvent.class);
            if (event == null) continue;

            if (declaredMethod.getParameterCount() != 1) throw new IllegalArgumentException("Method "+declaredMethod.getName()+" should only take in 1 parameter, that extends event");
            Parameter parameter = declaredMethod.getParameters()[0];
            Class type = parameter.getType();
            if (!UEvent.class.isAssignableFrom(type)) throw new IllegalArgumentException("Method "+declaredMethod.getName()+" should only take in 1 parameter, that extends event");
            if ((declaredMethod.getModifiers() & Modifier.PUBLIC)  == 0 ) throw new IllegalArgumentException("Method is not public");
            MethodHandle handle = MethodHandles.publicLookup().unreflect(declaredMethod).bindTo(o);

            registrations.add(eventBus.registerListener(type, event.priority(), new AnnotatedListener(handle, o.getClass().getName()+"."+declaredMethod.getName()+"("+type.getName()+")")));
        }
        return registrations;
    }
}
