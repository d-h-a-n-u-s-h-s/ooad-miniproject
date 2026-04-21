package com.erp.service;

import com.erp.sdk.config.DatabaseConfig;
import com.erp.sdk.factory.SubsystemFactory;
import com.erp.sdk.subsystem.Manufacturing;
import com.erp.sdk.subsystem.SubsystemName;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * BOMService acts as a GRASP Controller and Information Expert for Manufacturing logic.
 * It separates the UI from the database access layer (Low Coupling).
 * 
 * Principles & Patterns Used:
 * 1. Singleton Pattern: Ensures only one instance of BOMService manages the Subsystem connection.
 * 2. Dependency Inversion Principle (SOLID): Depends on the abstract Manufacturing interface, not concrete DB drivers.
 * 3. Single Responsibility Principle (SOLID): Only responsible for orchestrating BOM and Material logic.
 */
public class BOMService {

    private static BOMService instance;
    private Manufacturing mfg;
    private static final String USERNAME = "mfg_lead";

    private BOMService() {
        try {
            DatabaseConfig config = DatabaseConfig.fromProperties(
                    Path.of("src", "main", "resources", "application-rds.properties")
            );
            // Factory Pattern: We delegate creation to the SDK's SubsystemFactory
            mfg = (Manufacturing) SubsystemFactory.create(SubsystemName.MANUFACTURING, config);
        } catch (Exception e) {
            System.err.println("Failed to initialize BOMService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Singleton Pattern Implementation.
     */
    public static BOMService getInstance() {
        if (instance == null) {
            instance = new BOMService();
        }
        return instance;
    }

    public List<Map<String, Object>> getAllMaterials() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        return mfg.readAll("materials", Map.of(), USERNAME);
    }

    public List<Map<String, Object>> getAllComponents() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        return mfg.readAll("components", Map.of(), USERNAME);
    }

    public List<Map<String, Object>> getAllBOMs() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        return mfg.readAll("bills_of_materials", Map.of(), USERNAME);
    }

    // --- Work Centers ---

    public List<Map<String, Object>> getAllWorkCenters() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        return mfg.readAll("work_centers", Map.of(), USERNAME);
    }

    public void addWorkCenter(String id, String name, String type, double capacityHours, String location) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "work_center_id", id,
            "work_center_name", name,
            "work_center_type", type,
            "capacity_hours", capacityHours,
            "utilization_pct", 0.0,
            "location", location
        );
        mfg.create("work_centers", payload, USERNAME);
    }

    // --- Routing ---

    public List<Map<String, Object>> getRoutingSteps() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        return mfg.readAll("routing_steps", Map.of(), USERNAME);
    }

    public void addRoutingStep(int bomId, String operationId, int sequenceNumber, String operationName, String workCenterId, double setupTime, double runTime) throws Exception {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        
        // Sequence gap check
        List<Map<String, Object>> steps = getRoutingSteps();
        int maxSeq = 0;
        if (steps != null) {
            for (Map<String, Object> s : steps) {
                if (((Number)s.get("routing_id")).intValue() == bomId) {
                    int seq = ((Number)s.get("sequence_number")).intValue();
                    if (seq > maxSeq) maxSeq = seq;
                }
            }
        }
        if (sequenceNumber > maxSeq + 1) {
            throw new com.erp.exceptions.RoutingStepSequenceGapException("Routing for BOM #" + bomId + " has a gap in step sequence (e.g., steps 1, 2, 4; step 3 missing).");
        }
        
        Map<String, Object> payload = Map.of(
            "routing_id", bomId, // Mapping bomId directly as routing_id for simplicity, since BOM to routing is usually 1:1 or 1:N
            "operation_id", operationId,
            "sequence_number", sequenceNumber,
            "operation_name", operationName,
            "work_center_id", workCenterId,
            "setup_time", setupTime,
            "run_time", runTime
        );
        mfg.create("routing_steps", payload, USERNAME);
    }

    public void updateWorkCenterUtilization(String workCenterId, double consumedHours) throws Exception {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        
        List<Map<String, Object>> wcs = getAllWorkCenters();
        if (wcs != null) {
            for (Map<String, Object> wc : wcs) {
                if (workCenterId.equals(wc.get("work_center_id"))) {
                    double cap = ((Number)wc.get("capacity_hours")).doubleValue();
                    double util = wc.get("utilization_pct") != null ? ((Number)wc.get("utilization_pct")).doubleValue() : 0.0;
                    
                    double addedUtil = (consumedHours / cap) * 100.0;
                    double newUtil = util + addedUtil;
                    
                    if (newUtil > 100.0) {
                        throw new com.erp.exceptions.CapacityOverloadException("Work Center " + workCenterId + " utilization exceeds 100% for the period.");
                    }
                    mfg.update("work_centers", "work_center_id", workCenterId, Map.of("utilization_pct", newUtil), USERNAME);
                    break;
                }
            }
        }
    }

    // --- Production Plans (MRP) ---
    
    public List<Map<String, Object>> getAllProductionPlans() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        return mfg.readAll("production_plans", Map.of(), USERNAME);
    }
    
    public void createProductionPlan(int bomId, int qty, String startDate, double totalCost, double totalHours) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "bom_id", bomId,
            "planned_quantity", qty,
            "start_date", startDate,
            "total_cost", totalCost,
            "total_hours", totalHours,
            "status", "Draft"
        );
        mfg.create("production_plans", payload, USERNAME);
    }

    public void updateProductionPlanStatus(int planId, String status) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "status", status
        );
        mfg.update("production_plans", "plan_id", planId, payload, USERNAME);
    }

    public void updateProductionPlan(int planId, int qty, String startDate, double totalCost, double totalHours) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "planned_quantity", qty,
            "start_date", startDate,
            "total_cost", totalCost,
            "total_hours", totalHours
        );
        mfg.update("production_plans", "plan_id", planId, payload, USERNAME);
    }

    // --- Production Orders ---

    public List<Map<String, Object>> getAllProductionOrders() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        return mfg.readAll("production_orders", Map.of(), USERNAME);
    }

    public void createProductionOrder(int bomId, int qty, String startDate, String dueDate, int planId) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "bom_id", bomId,
            "order_quantity", qty,
            "order_status", "Active",
            "start_date", startDate,
            "due_date", dueDate,
            "plan_id", planId
        );
        mfg.create("production_orders", payload, USERNAME);
    }

    public void updateProductionOrder(int orderId, int bomId, int qty, String startDate, String dueDate) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "bom_id", bomId,
            "order_quantity", qty,
            "start_date", startDate,
            "due_date", dueDate
        );
        mfg.update("production_orders", "production_order_id", orderId, payload, USERNAME);
    }

    public void logShopFloorExecution(int orderId, int currentProduced, int newlyProduced) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        String ts = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        mfg.create("shop_floor_logs", Map.of(
            "production_order_id", orderId,
            "quantity_logged", newlyProduced,
            "log_timestamp", ts
        ), USERNAME);
        
        mfg.update("production_orders", "production_order_id", orderId, Map.of("produced_quantity", currentProduced + newlyProduced), USERNAME);
    }

    public List<Map<String, Object>> getAllShopFloorLogs() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        return mfg.readAll("shop_floor_logs", Map.of(), USERNAME);
    }

    // --- Assembly Lines ---
    
    public List<Map<String, Object>> getAllAssemblyLines() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        return mfg.readAll("assembly_lines", Map.of(), USERNAME);
    }

    // Called by our inbound InventoryApiServer when Supply Chain sends us a new material
    public void addMaterialFromAPI(String productName, int quantity, int minLevel) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        mfg.create("inventory_items", Map.of(
            "item_name", productName,
            "stock_qty", quantity,
            "reorder_level", minLevel,
            "uom", "units",
            "category", "API Imported"
        ), USERNAME);
    }
    
    // Called by our inbound InventoryApiServer when Supply Chain updates a material
    public void updateMaterialFromAPI(int itemId, String productName, int quantity, int minLevel) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        java.util.HashMap<String, Object> updates = new java.util.HashMap<>();
        if (productName != null) updates.put("item_name", productName);
        if (quantity != -1) updates.put("stock_qty", quantity);
        if (minLevel != -1) updates.put("reorder_level", minLevel);
        if (!updates.isEmpty()) {
            mfg.update("inventory_items", "item_id", itemId, updates, USERNAME);
        }
    }

    public void createAssemblyLine(String name, int sequence, String workCenterId, int orderId) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        mfg.create("assembly_lines", Map.of(
            "line_name", name,
            "sequence_num", sequence,
            "work_center_id", workCenterId,
            "production_order_id", orderId
        ), USERNAME);
    }
    
    public void assignOrderToLine(int orderId, int lineId) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        String ts = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        mfg.update("production_orders", "production_order_id", orderId, Map.of("current_line_id", lineId), USERNAME);
        mfg.create("assembly_movements", Map.of(
            "production_order_id", orderId,
            "line_id", lineId,
            "movement_timestamp", ts
        ), USERNAME);
    }

    public void moveOrderToNextLine(int orderId, int currentLineId) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        List<Map<String, Object>> lines = getAllAssemblyLines();
        int currentSeq = -1;
        for (Map<String, Object> l : lines) {
            if (((Number) l.get("line_id")).intValue() == currentLineId) {
                currentSeq = ((Number) l.get("sequence_num")).intValue();
                break;
            }
        }
        
        int nextLineId = -1;
        int nextSeq = Integer.MAX_VALUE;
        for (Map<String, Object> l : lines) {
            Number lineOrderId = (Number) l.get("production_order_id");
            if (lineOrderId != null && lineOrderId.intValue() == orderId) {
                int seq = ((Number) l.get("sequence_num")).intValue();
                if (seq > currentSeq && seq < nextSeq) {
                    nextSeq = seq;
                    nextLineId = ((Number) l.get("line_id")).intValue();
                }
            }
        }
        
        if (nextLineId != -1) {
            assignOrderToLine(orderId, nextLineId);
        } else {
            mfg.update("production_orders", "production_order_id", orderId, Map.of(
                "order_status", "Completed",
                "current_line_id", -1 // Clear it
            ), USERNAME);
        }
    }

    // --- Quality Control ---
    
    public void logQualityCheck(int orderId, int defects, int producedQty) throws Exception {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        double defectRate = producedQty > 0 ? ((double) defects / producedQty) * 100.0 : 0;
        String qcStatus = defectRate > 5.0 ? "FAILURE" : "PASSED";
        
        mfg.update("production_orders", "production_order_id", orderId, Map.of(
            "defects", defects,
            "qc_status", qcStatus
        ), USERNAME);
        
        if (defectRate > 5.0) {
            throw new com.erp.exceptions.QcDefectThresholdExceededException("Defect rate for production order #" + orderId + " has exceeded the acceptable threshold of 5%.");
        }
    }

    public void cancelProductionOrder(int orderId) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "order_status", "Cancelled"
        );
        mfg.update("production_orders", "production_order_id", orderId, payload, USERNAME);
    }

    public void addComponent(int itemId, String componentCode, String specification) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "item_id", itemId,
            "component_code", componentCode,
            "specification", specification
        );
        mfg.create("components", payload, USERNAME);
    }

    public void addMaterial(String name, String category, String uom, double stock, double reorder) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "item_name", name,
            "category", category,
            "uom", uom,
            "stock_qty", stock,
            "reorder_level", reorder
        );
        mfg.create("inventory_items", payload, USERNAME);
        
        InventoryApiClient.pushMaterialToSupplyChain(name, (int)stock, (int)reorder);
    }

    public boolean bomExists(String productName, String version) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        List<Map<String, Object>> boms = mfg.readAll(
            "bills_of_materials", 
            Map.of("product_name", productName, "bom_version", version), 
            USERNAME
        );
        return boms != null && !boms.isEmpty();
    }

    public void createBOM(String productName, String bomVersion, String materialListJson) throws Exception {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        if (bomExists(productName, bomVersion)) {
            throw new com.erp.exceptions.DuplicateBomVersionException("A BOM with version " + bomVersion + " already exists for product " + productName + ".");
        }
        Map<String, Object> payload = Map.of(
            "product_name", productName,
            "bom_version", bomVersion,
            "material_list", materialListJson,
            "is_active", true
        );
        mfg.create("bills_of_materials", payload, USERNAME);
    }

    public void updateBOM(int bomId, String materialListJson) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        mfg.update("bills_of_materials", "bom_id", bomId, Map.of("material_list", materialListJson), USERNAME);
    }
}
