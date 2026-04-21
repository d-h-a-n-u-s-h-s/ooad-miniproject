package com.erp.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Client to send API requests to the Supply Chain subsystem over the network.
 */
public class InventoryApiClient {

    // Target URL of the Supply Chain subsystem (update when they provide their IP/port)
    private static final String SUPPLY_CHAIN_API_URL = "https://siesta-amniotic-defendant.ngrok-free.dev/api/inventory/materials";

    public static void pushMaterialToSupplyChain(String productName, int currentStock, int minimumLevel) {
        // Run network call on a background thread so it doesn't freeze the UI
        new Thread(() -> {
            try {
                URL url = new URL(SUPPLY_CHAIN_API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                String jsonInputString = String.format(
                    "{\"product_name\": \"%s\", \"current_stock\": %d, \"minimum_level\": %d}",
                    productName, currentStock, minimumLevel
                );

                try(OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);			
                }

                int code = conn.getResponseCode();
                System.out.println("API Sync to Supply Chain Response Code: " + code);
            } catch (Exception e) {
                System.err.println("Failed to sync to Supply Chain subsystem: " + e.getMessage());
            }
        }).start();
    }
}
