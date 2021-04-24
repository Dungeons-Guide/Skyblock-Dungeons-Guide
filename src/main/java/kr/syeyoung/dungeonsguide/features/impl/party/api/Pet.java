package kr.syeyoung.dungeonsguide.features.impl.party.api;

import lombok.Data;

@Data
public class Pet {
    private String uuid;
    private String type;
    private double exp;
    private boolean active;
    private String heldItem;
    private String skin;
}
