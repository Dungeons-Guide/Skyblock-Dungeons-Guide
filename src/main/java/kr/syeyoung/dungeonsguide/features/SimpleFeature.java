package kr.syeyoung.dungeonsguide.features;

public class SimpleFeature extends AbstractFeature {
    protected SimpleFeature(String category, String name, String key) {
        this(category, name, name, key);
    }
    protected SimpleFeature(String category, String name, String description, String key) {
        this(category, name, description, key, true);
    }

    protected SimpleFeature(String category, String name, String description, String key, boolean enabled) {
        super(category, name, description, key);
        this.setEnabled(enabled);
    }
}
