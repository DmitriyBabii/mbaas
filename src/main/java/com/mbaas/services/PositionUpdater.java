package com.mbaas.services;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.logging.Logger;
import com.mbaas.models.Position;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PositionUpdater {
    private static final Logger log = Backendless.Logging.getLogger(PositionUpdater.class);

    @Scheduled(fixedDelay = 60000)
    public void updatePositions() {
        log.info("Update positions");
        List<BackendlessUser> users = Backendless.Data.of(BackendlessUser.class).find();
        users = users.stream().peek(u -> {
            Position position = Geo.getCurrentPosition();
            u.setProperty("latitude", position.getLatitude());
            u.setProperty("longitude", position.getLongitude());
        }).collect(Collectors.toList());
        users.forEach(Backendless.UserService::update);
    }
}
