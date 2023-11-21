package com.orderbook.clients;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// This class get current depth snapshot via binance api
public class DepthSnapshotApi {
    private static final String BASE_URL = "https://api.binance.com/api/v3/depth?symbol=%s&limit=%s";

    public static JsonNode getDepthSnapshot(String symbol, int limit) throws Exception {
        URL url = new URL(String.format(BASE_URL, symbol, limit));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readTree(content.toString());
        } else {
            throw new Exception("Failed to fetch depth snapshot. Response code: " + responseCode);
        }
    }
}
