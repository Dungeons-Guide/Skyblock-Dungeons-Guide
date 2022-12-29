package kr.syeyoung.dungeonsguide.mod.guiv2;

import lombok.Getter;

import java.util.*;
import java.util.function.Consumer;

public class BindableAttribute<T> {
    public BindableAttribute(Class<T> type) {
        this.type = type;
    }
    public BindableAttribute(Class<T> type, T defaultValue) {
        this.type = type;
        value = defaultValue;
    }
    @Getter
    private final Class<T> type;
    private T value;
    private List<Consumer<T>> onUpdates = new ArrayList<>();

    private boolean updating = false;
    public void setValue(T t) {
        if (updating) return;
        updating = true;
        for (Consumer<T> onUpdate : onUpdates) {
            onUpdate.accept(t);
        }
        updating = false;
        this.value = t;
    }
    public T getValue() {
        return value;
    }

    public void addOnUpdate(Consumer<T> onUpdate) {
        onUpdates.add(onUpdate);
    }
    public void removeOnUpdate(Consumer<T> onUpdate) {
        onUpdates.remove(onUpdate);
    }

    private Set<BindableAttribute<T>> linkedWith = new HashSet<>();

    public void linkTo(BindableAttribute<T> bindableAttribute) { // This method has to be called by exporting bindable attribute
        if (bindableAttribute.type != type) throw new IllegalArgumentException("Different type!!");

        this.addOnUpdate(bindableAttribute::setValue);
        bindableAttribute.addOnUpdate(this::setValue);
        linkedWith.add(bindableAttribute);

        setValue(bindableAttribute.getValue());
    }

    public void unlink(BindableAttribute<T> bindableAttribute) {
        bindableAttribute.removeOnUpdate(this::setValue);
        removeOnUpdate(bindableAttribute::setValue);
        linkedWith.remove(bindableAttribute);
    }

    public void unlinkAll() {
        Set<BindableAttribute<T>> copy = new HashSet<>(linkedWith);
        for (BindableAttribute<T> tBindableAttribute : copy) {
            unlink(tBindableAttribute);
        }
    }
}
