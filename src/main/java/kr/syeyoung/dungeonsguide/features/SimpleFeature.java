package kr.syeyoung.dungeonsguide.features;

public class SimpleFeature extends AbstractFeature {
    protected SimpleFeature(String category, String name, String key) {
        super(category, name, name, key);
    }
    protected SimpleFeature(String category, String name, String description, String key) {
        super(category, name, description, key);
    }
}
