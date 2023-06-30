package com.mbaas.services;

import com.backendless.Backendless;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BackendlessStarter {
    @Value("${backendless.applicationId}")
    private String applicationId;

    @Value("${backendless.apiKey}")
    private String apiKey;

    public void initializeBackendless() {
        Backendless.initApp(applicationId, apiKey);
    }
}
