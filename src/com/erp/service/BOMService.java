package com.erp.service;

import com.erp.sdk.config.DatabaseConfig;
import com.erp.sdk.factory.SubsystemFactory;
import com.erp.sdk.subsystem.Manufacturing;
import com.erp.sdk.subsystem.SubsystemName;
import com.erp.model.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BOMService acts as a GRASP Controller and Information Expert for Manufacturing logic.
 * It separates the UI from the database access layer (Low Coupling).
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
            mfg = (Manufacturing) SubsystemFactory.create(SubsystemName.MANUFACTURING, config);
        } catch (Exception e) {
            System.err.println("Failed to initialize BOMService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static BOMService getInstance() {
        if (instance == null) {
            instance = new BOMService();
        }
        return instance;
    }

    // --- Helper for case-insensitive map access ---
    private Object getVal(Map<String, Object> m, String key) {
        if (m == null) return null;
        if (m.containsKey(key)) return m.get(key);
        if (m.containsKey(key.toUpperCase())) return m.get(key.toUpperCase());
        if (m.containsKey(key.toLowerCase())) return m.get(key.toLowerCase());
        return null;
    }

    // --- Model Mappers (Internal implementation detail) ---

    private Material mapMaterial(Map<String, Object> m) {
        if (m == null) return null;
        Material mat = new Material();
        Object idObj = getVal(m, "item_id");
        if (idObj == null) idObj = getVal(m, "product_id");
        mat.setId(idObj != null ? ((Number) idObj).intValue() : -1);
        
        Object nameObj = getVal(m, "item_name");
        if (nameObj == null) nameObj = getVal(m, "product_name");
        mat.setName((String) nameObj);
        
        mat.setCategory((String) getVal(m, "category"));
        mat.setUom((String) getVal(m, "uom"));
        
        Object stock = getVal(m, "stock_qty");
        mat.setStockQuantity(stock != null ? ((Number) stock).doubleValue() : 0.0);
        
        Object reorder = getVal(m, "reorder_level");
        mat.setReorderLevel(reorder != null ? ((Number) reorder).doubleValue() : 0.0);
        
        Object cost = getVal(m, "unit_cost");
        mat.setUnitCost(cost != null ? ((Number) cost).doubleValue() : 0.0);
        return mat;
    }

    private Component mapComponent(Map<String, Object> m) {
        if (m == null) return null;
        Component c = new Component();
        Object id = getVal(m, "component_id");
        c.setId(id != null ? ((Number) id).intValue() : -1);
        Object itemId = getVal(m, "item_id");
        c.setItemId(itemId != null ? ((Number) itemId).intValue() : -1);
        c.setComponentCode((String) getVal(m, "component_code"));
        c.setSpecification((String) getVal(m, "specification"));
        return c;
    }

    private BOM mapBOM(Map<String, Object> m) {
        if (m == null) return null;
        BOM b = new BOM();
        Object id = getVal(m, "bom_id");
        b.setId(id != null ? ((Number) id).intValue() : -1);
        b.setProductName((String) getVal(m, "product_name"));
        b.setVersion((String) getVal(m, "bom_version"));
        b.setMaterialListJson((String) getVal(m, "material_list"));
        Object active = getVal(m, "is_active");
        b.setActive(active != null && (boolean) active);
        return b;
    }

    private WorkCenter mapWorkCenter(Map<String, Object> m) {
        if (m == null) return null;
        WorkCenter wc = new WorkCenter();
        wc.setId((String) getVal(m, "work_center_id"));
        wc.setName((String) getVal(m, "work_center_name"));
        wc.setType((String) getVal(m, "work_center_type"));
        Object cap = getVal(m, "capacity_hours");
        wc.setCapacityHours(cap != null ? ((Number) cap).doubleValue() : 0.0);
        Object util = getVal(m, "utilization_pct");
        wc.setUtilizationPct(util != null ? ((Number) util).doubleValue() : 0.0);
        wc.setLocation((String) getVal(m, "location"));
        return wc;
    }

    private RoutingStep mapRoutingStep(Map<String, Object> m) {
        if (m == null) return null;
        RoutingStep s = new RoutingStep();
        Object rid = getVal(m, "routing_id");
        s.setRoutingId(rid != null ? ((Number) rid).intValue() : -1);
        s.setOperationId((String) getVal(m, "operation_id"));
        Object seq = getVal(m, "sequence_number");
        s.setSequenceNumber(seq != null ? ((Number) seq).intValue() : 0);
        s.setOperationName((String) getVal(m, "operation_name"));
        s.setWorkCenterId((String) getVal(m, "work_center_id"));
        Object setup = getVal(m, "setup_time");
        s.setSetupTime(setup != null ? ((Number) setup).doubleValue() : 0.0);
        Object run = getVal(m, "run_time");
        s.setRunTime(run != null ? ((Number) run).doubleValue() : 0.0);
        return s;
    }

    private ProductionPlan mapProductionPlan(Map<String, Object> m) {
        if (m == null) return null;
        ProductionPlan p = new ProductionPlan();
        Object id = getVal(m, "plan_id");
        p.setId(id != null ? ((Number) id).intValue() : -1);
        Object bid = getVal(m, "bom_id");
        p.setBomId(bid != null ? ((Number) bid).intValue() : -1);
        Object qty = getVal(m, "planned_quantity");
        p.setPlannedQuantity(qty != null ? ((Number) qty).intValue() : 0);
        Object start = getVal(m, "start_date");
        p.setStartDate(start != null ? start.toString() : "");
        Object cost = getVal(m, "total_cost");
        p.setTotalCost(cost != null ? ((Number) cost).doubleValue() : 0.0);
        Object hours = getVal(m, "total_hours");
        p.setTotalHours(hours != null ? ((Number) hours).doubleValue() : 0.0);
        p.setStatus((String) getVal(m, "status"));
        return p;
    }

    private ProductionOrder mapProductionOrder(Map<String, Object> m) {
        if (m == null) return null;
        ProductionOrder o = new ProductionOrder();
        Object id = getVal(m, "production_order_id");
        o.setId(id != null ? ((Number) id).intValue() : -1);
        Object bid = getVal(m, "bom_id");
        o.setBomId(bid != null ? ((Number) bid).intValue() : -1);
        Object qty = getVal(m, "order_quantity");
        o.setOrderQuantity(qty != null ? ((Number) qty).intValue() : 0);
        o.setOrderStatus((String) getVal(m, "order_status"));
        Object start = getVal(m, "start_date");
        o.setStartDate(start != null ? start.toString() : null);
        Object due = getVal(m, "due_date");
        o.setDueDate(due != null ? due.toString() : null);
        Object pid = getVal(m, "plan_id");
        o.setPlanId(pid != null ? ((Number) pid).intValue() : -1);
        Object prod = getVal(m, "produced_quantity");
        o.setProducedQuantity(prod != null ? ((Number) prod).intValue() : 0);
        Object def = getVal(m, "defects");
        o.setDefects(def != null ? ((Number) def).intValue() : 0);
        o.setQcStatus((String) getVal(m, "qc_status"));
        Object lid = getVal(m, "current_line_id");
        o.setCurrentLineId(lid != null ? ((Number) lid).intValue() : -1);
        return o;
    }

    private AssemblyLine mapAssemblyLine(Map<String, Object> m) {
        if (m == null) return null;
        AssemblyLine l = new AssemblyLine();
        Object id = getVal(m, "line_id");
        l.setId(id != null ? ((Number) id).intValue() : -1);
        l.setName((String) getVal(m, "line_name"));
        Object seq = getVal(m, "sequence_num");
        l.setSequenceNum(seq != null ? ((Number) seq).intValue() : 0);
        l.setWorkCenterId((String) getVal(m, "work_center_id"));
        Object poId = getVal(m, "production_order_id");
        l.setProductionOrderId(poId != null ? ((Number) poId).intValue() : -1);
        return l;
    }

    private AssemblyMovement mapAssemblyMovement(Map<String, Object> m) {
        if (m == null) return null;
        AssemblyMovement am = new AssemblyMovement();
        Object poId = getVal(m, "production_order_id");
        am.setProductionOrderId(poId != null ? ((Number) poId).intValue() : -1);
        Object lid = getVal(m, "line_id");
        am.setLineId(lid != null ? ((Number) lid).intValue() : -1);
        Object ts = getVal(m, "movement_timestamp");
        am.setTimestamp(ts != null ? ts.toString() : null);
        return am;
    }

    private ShopFloorLog mapShopFloorLog(Map<String, Object> m) {
        if (m == null) return null;
        ShopFloorLog l = new ShopFloorLog();
        Object poId = getVal(m, "production_order_id");
        l.setProductionOrderId(poId != null ? ((Number) poId).intValue() : -1);
        Object qty = getVal(m, "quantity_logged");
        l.setQuantityLogged(qty != null ? ((Number) qty).intValue() : 0);
        Object ts = getVal(m, "log_timestamp");
        l.setTimestamp(ts != null ? ts.toString() : null);
        return l;
    }

    // --- Service Methods (Read Operations - Strongly Typed) ---

    public List<Material> getAllMaterials() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        List<Map<String, Object>> data = mfg.readAll("inventory_items", Map.of(), USERNAME);
        if (data == null) return new java.util.ArrayList<>();
        return data.stream().map(this::mapMaterial).collect(Collectors.toList());
    }

    public List<Component> getAllComponents() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        List<Map<String, Object>> data = mfg.readAll("components", Map.of(), USERNAME);
        if (data == null) return new java.util.ArrayList<>();
        return data.stream().map(this::mapComponent).collect(Collectors.toList());
    }

    public List<BOM> getAllBOMs() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        List<Map<String, Object>> data = mfg.readAll("bills_of_materials", Map.of(), USERNAME);
        if (data == null) return new java.util.ArrayList<>();
        return data.stream().map(this::mapBOM).collect(Collectors.toList());
    }

    public List<WorkCenter> getAllWorkCenters() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        List<Map<String, Object>> data = mfg.readAll("work_centers", Map.of(), USERNAME);
        if (data == null) return new java.util.ArrayList<>();
        return data.stream().map(this::mapWorkCenter).collect(Collectors.toList());
    }

    public List<RoutingStep> getRoutingSteps() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        List<Map<String, Object>> data = mfg.readAll("routing_steps", Map.of(), USERNAME);
        if (data == null) return new java.util.ArrayList<>();
        return data.stream().map(this::mapRoutingStep).collect(Collectors.toList());
    }

    public List<ProductionPlan> getAllProductionPlans() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        List<Map<String, Object>> data = mfg.readAll("production_plans", Map.of(), USERNAME);
        if (data == null) return new java.util.ArrayList<>();
        return data.stream().map(this::mapProductionPlan).collect(Collectors.toList());
    }

    public List<ProductionOrder> getAllProductionOrders() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        List<Map<String, Object>> data = mfg.readAll("production_orders", Map.of(), USERNAME);
        if (data == null) return new java.util.ArrayList<>();
        return data.stream().map(this::mapProductionOrder).collect(Collectors.toList());
    }

    public List<ShopFloorLog> getAllShopFloorLogs() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        List<Map<String, Object>> data = mfg.readAll("shop_floor_logs", Map.of(), USERNAME);
        if (data == null) return new java.util.ArrayList<>();
        return data.stream().map(this::mapShopFloorLog).collect(Collectors.toList());
    }

    public List<AssemblyLine> getAllAssemblyLines() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        List<Map<String, Object>> data = mfg.readAll("assembly_lines", Map.of(), USERNAME);
        if (data == null) return new java.util.ArrayList<>();
        List<AssemblyLine> lines = data.stream().map(this::mapAssemblyLine).collect(Collectors.toList());
        System.out.println("DEBUG: Loaded " + lines.size() + " assembly lines.");
        for (AssemblyLine l : lines) {
            System.out.println("DEBUG: Line '" + l.getName() + "' (ID: " + l.getId() + ") for Order #" + l.getProductionOrderId());
        }
        return lines;
    }

    public List<AssemblyMovement> getAllAssemblyMovements() {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        List<Map<String, Object>> data = mfg.readAll("assembly_movements", Map.of(), USERNAME);
        if (data == null) return new java.util.ArrayList<>();
        return data.stream().map(this::mapAssemblyMovement).collect(Collectors.toList());
    }

    // --- Service Methods (Write Operations - Strongly Typed) ---

    public void addWorkCenter(WorkCenter wc) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "work_center_id", wc.getId(),
            "work_center_name", wc.getName(),
            "work_center_type", wc.getType(),
            "capacity_hours", wc.getCapacityHours(),
            "utilization_pct", wc.getUtilizationPct(),
            "location", wc.getLocation()
        );
        mfg.create("work_centers", payload, USERNAME);
    }

    public void addRoutingStep(RoutingStep step) throws Exception {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        
        List<RoutingStep> steps = getRoutingSteps();
        int maxSeq = 0;
        if (steps != null) {
            for (RoutingStep s : steps) {
                if (s.getRoutingId() == step.getRoutingId()) {
                    int seq = s.getSequenceNumber();
                    if (seq > maxSeq) maxSeq = seq;
                }
            }
        }
        if (step.getSequenceNumber() > maxSeq + 1) {
            throw new com.erp.exceptions.RoutingStepSequenceGapException("Routing for BOM #" + step.getRoutingId() + " has a gap in step sequence.");
        }
        
        Map<String, Object> payload = Map.of(
            "routing_id", step.getRoutingId(),
            "operation_id", step.getOperationId(),
            "sequence_number", step.getSequenceNumber(),
            "operation_name", step.getOperationName(),
            "work_center_id", step.getWorkCenterId(),
            "setup_time", step.getSetupTime(),
            "run_time", step.getRunTime()
        );
        mfg.create("routing_steps", payload, USERNAME);
    }

    public void createProductionPlan(ProductionPlan p) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "bom_id", p.getBomId(),
            "planned_quantity", p.getPlannedQuantity(),
            "start_date", p.getStartDate(),
            "total_cost", p.getTotalCost(),
            "total_hours", p.getTotalHours(),
            "status", p.getStatus() != null ? p.getStatus() : "Draft"
        );
        mfg.create("production_plans", payload, USERNAME);
    }

    public void updateProductionPlan(ProductionPlan p) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "planned_quantity", p.getPlannedQuantity(),
            "start_date", p.getStartDate(),
            "total_cost", p.getTotalCost(),
            "total_hours", p.getTotalHours()
        );
        mfg.update("production_plans", "plan_id", p.getId(), payload, USERNAME);
    }

    public void updateProductionPlanStatus(int planId, String status) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        mfg.update("production_plans", "plan_id", planId, Map.of("status", status), USERNAME);
    }

    public void createProductionOrder(ProductionOrder o) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "bom_id", o.getBomId(),
            "order_quantity", o.getOrderQuantity(),
            "order_status", o.getOrderStatus() != null ? o.getOrderStatus() : "Active",
            "start_date", o.getStartDate(),
            "due_date", o.getDueDate(),
            "plan_id", o.getPlanId()
        );
        mfg.create("production_orders", payload, USERNAME);
    }

    public void updateProductionOrder(ProductionOrder o) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "bom_id", o.getBomId(),
            "order_quantity", o.getOrderQuantity(),
            "start_date", o.getStartDate(),
            "due_date", o.getDueDate()
        );
        mfg.update("production_orders", "production_order_id", o.getId(), payload, USERNAME);
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

    public void addMaterialFromAPI(String productName, int quantity, int minLevel) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        mfg.create("inventory_items", Map.of(
            "item_name", productName,
            "stock_qty", (double) quantity,
            "reorder_level", (double) minLevel,
            "uom", "units",
            "category", "API Imported"
        ), USERNAME);
    }
    
    public void updateMaterialFromAPI(int itemId, String productName, int quantity, int minLevel) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        java.util.HashMap<String, Object> updates = new java.util.HashMap<>();
        if (productName != null) updates.put("item_name", productName);
        if (quantity != -1) updates.put("stock_qty", (double) quantity);
        if (minLevel != -1) updates.put("reorder_level", (double) minLevel);
        if (!updates.isEmpty()) {
            mfg.update("inventory_items", "item_id", itemId, updates, USERNAME);
        }
    }

    public void createAssemblyLine(AssemblyLine l) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        System.out.println("DEBUG: Creating assembly line '" + l.getName() + "' for Order #" + l.getProductionOrderId());
        mfg.create("assembly_lines", Map.of(
            "line_name", l.getName(),
            "sequence_num", l.getSequenceNum(),
            "work_center_id", l.getWorkCenterId(),
            "production_order_id", l.getProductionOrderId()
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
        List<AssemblyLine> lines = getAllAssemblyLines();
        int currentSeq = -1;
        for (AssemblyLine l : lines) {
            if (l.getId() == currentLineId) {
                currentSeq = l.getSequenceNum();
                break;
            }
        }
        int nextLineId = -1;
        int nextSeq = Integer.MAX_VALUE;
        for (AssemblyLine l : lines) {
            if (l.getProductionOrderId() == orderId) {
                int seq = l.getSequenceNum();
                if (seq > currentSeq && seq < nextSeq) {
                    nextSeq = seq;
                    nextLineId = l.getId();
                }
            }
        }
        if (nextLineId != -1) {
            assignOrderToLine(orderId, nextLineId);
        } else {
            mfg.update("production_orders", "production_order_id", orderId, Map.of("order_status", "Completed", "current_line_id", -1), USERNAME);
        }
    }

    public void logQualityCheck(int orderId, int defects, int producedQty) throws Exception {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        double defectRate = producedQty > 0 ? ((double) defects / producedQty) * 100.0 : 0;
        String qcStatus = defectRate > 5.0 ? "FAILURE" : "PASSED";
        mfg.update("production_orders", "production_order_id", orderId, Map.of("defects", defects, "qc_status", qcStatus), USERNAME);
        if (defectRate > 5.0) {
            throw new com.erp.exceptions.QcDefectThresholdExceededException("Defect rate threshold exceeded.");
        }
    }

    public void cancelProductionOrder(int orderId) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        mfg.update("production_orders", "production_order_id", orderId, Map.of("order_status", "Cancelled"), USERNAME);
    }

    public void addComponent(Component c) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "item_id", c.getItemId(),
            "component_code", c.getComponentCode(),
            "specification", c.getSpecification()
        );
        mfg.create("components", payload, USERNAME);
    }

    public void addMaterial(Material mat) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        Map<String, Object> payload = Map.of(
            "item_name", mat.getName(),
            "category", mat.getCategory(),
            "uom", mat.getUom(),
            "stock_qty", mat.getStockQuantity(),
            "reorder_level", mat.getReorderLevel()
        );
        mfg.create("inventory_items", payload, USERNAME);
        InventoryApiClient.pushMaterialToSupplyChain(mat.getName(), (int)mat.getStockQuantity(), (int)mat.getReorderLevel());
    }

    public boolean bomExists(String productName, String version) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        List<Map<String, Object>> boms = mfg.readAll("bills_of_materials", Map.of("product_name", productName, "bom_version", version), USERNAME);
        return boms != null && !boms.isEmpty();
    }

    public void createBOM(BOM bom) throws Exception {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        if (bomExists(bom.getProductName(), bom.getVersion())) {
            throw new com.erp.exceptions.DuplicateBomVersionException("Duplicate BOM version.");
        }
        Map<String, Object> payload = Map.of(
            "product_name", bom.getProductName(),
            "bom_version", bom.getVersion(),
            "material_list", bom.getMaterialListJson(),
            "is_active", bom.isActive()
        );
        mfg.create("bills_of_materials", payload, USERNAME);
    }

    public void updateBOM(BOM bom) {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        mfg.update("bills_of_materials", "bom_id", bom.getId(), Map.of("material_list", bom.getMaterialListJson()), USERNAME);
    }
    
    public void updateWorkCenterUtilization(String workCenterId, double consumedHours) throws Exception {
        if (mfg == null) throw new RuntimeException("Database connection not initialized");
        List<WorkCenter> wcs = getAllWorkCenters();
        if (wcs != null) {
            for (WorkCenter wc : wcs) {
                if (workCenterId.equals(wc.getId())) {
                    double cap = wc.getCapacityHours();
                    double util = wc.getUtilizationPct();
                    double addedUtil = (consumedHours / cap) * 100.0;
                    double newUtil = util + addedUtil;
                    if (newUtil > 100.0) {
                        throw new com.erp.exceptions.CapacityOverloadException("Capacity overload.");
                    }
                    mfg.update("work_centers", "work_center_id", workCenterId, Map.of("utilization_pct", newUtil), USERNAME);
                    break;
                }
            }
        }
    }
}
