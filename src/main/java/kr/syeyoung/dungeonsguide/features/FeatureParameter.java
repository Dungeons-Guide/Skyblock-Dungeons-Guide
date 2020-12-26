package kr.syeyoung.dungeonsguide.features;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FeatureParameter<T> {
    private String key;

    private String name;
    private String description;

    private T value;
    private String value_type;
}
