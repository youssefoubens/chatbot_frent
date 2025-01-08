package com.example.chatbot.services;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FileUploader {

    private static final String BASE_URL = "http://localhost:8080/documents/upload";

    public static void uploadFile(File file, Map<String, String> metadata) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(BASE_URL);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                builder.addTextBody(entry.getKey(), entry.getValue());
            }

            HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                    throw new IOException("Upload failed: " + EntityUtils.toString(response.getEntity()));
                }
            }
        }
    }
}
