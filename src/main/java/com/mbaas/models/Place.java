package com.mbaas.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Setter
@Getter
public class Place {
    private String name;
    private String description;
    private String pictureUrl;
    private double latitude;
    private double longitude;
    private String meta;
    private LocalDateTime dateTime = LocalDateTime.now();

    public Place(String name, String description, String pictureUrl, double latitude, double longitude, String meta) {
        this.name = name;
        this.description = description;
        this.pictureUrl = pictureUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.meta = meta;
    }
}
