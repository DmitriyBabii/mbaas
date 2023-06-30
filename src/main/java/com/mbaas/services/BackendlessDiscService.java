package com.mbaas.services;

import com.backendless.Backendless;

public class BackendlessDiscService {
    public static boolean createDirectory(String path) {
        try {
            Backendless.Files.saveFile(path, "file", new byte[0], true);
            Backendless.Files.remove(path + "/file");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
