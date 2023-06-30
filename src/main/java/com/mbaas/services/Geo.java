package com.mbaas.services;

import com.mbaas.models.Position;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class Geo {
    public static Position getCurrentPosition() {
        try {
            URL url = new URL("https://ipapi.co/json/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            double latitude = json.getDouble("latitude");
            double longitude = json.getDouble("longitude");
            return new Position(latitude, longitude);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Position();
        }
    }

    public static String generateGoogleMapsLink(double latitude, double longitude) {
        return "https://www.google.com/maps?q=" + latitude + "," + longitude;
    }
}
