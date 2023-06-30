package com.mbaas.services.mappers;

import com.backendless.BackendlessUser;
import com.mbaas.models.UserDTO;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class UserDTOMapper implements Function<BackendlessUser, UserDTO> {
    @Override
    public UserDTO apply(BackendlessUser backendlessUser) {
        return new UserDTO(backendlessUser.getUserId(),
                backendlessUser.getProperty("name").toString(),
                (boolean) backendlessUser.getProperty("isMarked"),
                (double) backendlessUser.getProperty("latitude"),
                (double) backendlessUser.getProperty("longitude"));
    }
}
