package com.mbaas.services.mappers;

import com.backendless.files.FileInfo;
import com.mbaas.models.OpenFile;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Function;

@Service
public class OpenFileMapper implements Function<FileInfo, OpenFile> {
    @Override
    public OpenFile apply(FileInfo fileInfo) {
        String name = fileInfo.getName();
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(fileInfo.getPublicUrl());
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpConn.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                inputStream.close();
                bufferedReader.close();
                inputStreamReader.close();
            }
            httpConn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new OpenFile(name, sb.toString());
    }
}
