package com.erp.service;

import com.erp.util.JSONUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Lightweight embedded HTTP server to receive API calls from the Supply Chain subsystem.
 */
public class InventoryApiServer {

    private static InventoryApiServer instance;
    private HttpServer server;

    private InventoryApiServer() {}

    public static InventoryApiServer getInstance() {
        if (instance == null) {
            instance = new InventoryApiServer();
        }
        return instance;
    }

    public void startServer() {
        try {
            // Start server on port 8081
            server = HttpServer.create(new InetSocketAddress(8081), 0);
            server.createContext("/api/inventory/materials", new MaterialsHandler());
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            System.out.println("Inventory API Server listening on port 8081...");
        } catch (IOException e) {
            System.err.println("Failed to start API Server: " + e.getMessage());
        }
    }

    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    static class MaterialsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            
            try {
                if ("GET".equalsIgnoreCase(method)) {
                    handleGet(exchange);
                } else if ("POST".equalsIgnoreCase(method)) {
                    handlePost(exchange);
                } else if ("PUT".equalsIgnoreCase(method)) {
                    handlePut(exchange, path);
                } else {
                    sendResponse(exchange, 405, "{\"error\": \"Method Not Allowed\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            try {
                List<Map<String, Object>> materials = BOMService.getInstance().getAllMaterials();
                
                // Extremely simple manual JSON array generation for the response
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < materials.size(); i++) {
                    Map<String, Object> mat = materials.get(i);
                    json.append("{")
                        .append("\"item_id\":").append(mat.get("item_id")).append(",")
                        .append("\"product_name\":\"").append(mat.get("item_name")).append("\",")
                        .append("\"current_stock\":").append(mat.get("stock_qty")).append(",")
                        .append("\"minimum_level\":").append(mat.get("reorder_level")).append(",")
                        .append("\"status\":\"").append("ACTIVE").append("\"")
                        .append("}");
                    if (i < materials.size() - 1) json.append(",");
                }
                json.append("]");
                
                sendResponse(exchange, 200, json.toString());
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\": \"Database Error\"}");
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String body = readBody(exchange);
            // Basic custom parsing since we don't have Jackson/Gson imported by default
            String productName = extractJsonString(body, "product_name");
            int stock = extractJsonInt(body, "current_stock", 0);
            int minLevel = extractJsonInt(body, "minimum_level", 0);
            
            if (productName == null || productName.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"product_name is required\"}");
                return;
            }

            try {
                // Save it to our database
                BOMService.getInstance().addMaterialFromAPI(productName, stock, minLevel);
                sendResponse(exchange, 201, "{\"message\": \"Material created successfully\"}");
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\": \"Failed to create material\"}");
            }
        }

        private void handlePut(HttpExchange exchange, String path) throws IOException {
            // Extract item_id from /api/inventory/materials/{item_id}
            String[] segments = path.split("/");
            if (segments.length < 5) {
                sendResponse(exchange, 400, "{\"error\": \"Missing item_id in URL\"}");
                return;
            }
            
            int itemId;
            try {
                itemId = Integer.parseInt(segments[4]);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\": \"Invalid item_id format\"}");
                return;
            }

            String body = readBody(exchange);
            String productName = extractJsonString(body, "product_name");
            int stock = extractJsonInt(body, "current_stock", -1);
            int minLevel = extractJsonInt(body, "minimum_level", -1);

            try {
                // Update our database
                BOMService.getInstance().updateMaterialFromAPI(itemId, productName, stock, minLevel);
                sendResponse(exchange, 200, "{\"message\": \"Material updated successfully\"}");
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\": \"Failed to update material\"}");
            }
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }

        private String readBody(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        private String extractJsonString(String json, String key) {
            String search = "\"" + key + "\":\"";
            int start = json.indexOf(search);
            if (start == -1) return null;
            start += search.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }

        private int extractJsonInt(String json, String key, int defaultVal) {
            String search = "\"" + key + "\":";
            int start = json.indexOf(search);
            if (start == -1) return defaultVal;
            start += search.length();
            int end = start;
            while (end < json.length() && Character.isDigit(json.charAt(end))) {
                end++;
            }
            try {
                return Integer.parseInt(json.substring(start, end).trim());
            } catch (Exception e) {
                return defaultVal;
            }
        }
    }
}
