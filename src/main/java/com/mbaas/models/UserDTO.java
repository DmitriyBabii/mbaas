package com.mbaas.models;

import com.mbaas.services.Geo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class UserDTO {
    private String id;
    private String name;
    private boolean isMarked;
    private double latitude;
    private double longitude;

    public String getLink() {
        return Geo.generateGoogleMapsLink(latitude, longitude);
    }
}
