# ERP-UI — Tata Motors Enterprise Resource Planning (Swing)

A Java Swing ERP front-end covering 15 modules. **Five modules** are wired end-to-end
against a mock backend (`MockUIService`): **Orders**, **HR**, **Manufacturing**,
**Supply Chain**, and **Automation**. The remaining 10 are facade placeholders.

This README is the integration contract between the UI and the backend subsystem teams.
Each team owns one of the deeply integrated modules and may replace the mock with a
real transport without touching any UI code.

### Module navigation structure

| Module | Top-level Tabs | Inner Tabs |
|--------|----------------|-----------|
| **Order Processing** | Dashboard · Orders · Inventory · Reports | Orders → side nav: New Order / View Orders / Customers / Billing / Delivery / Payments; View Orders → All Orders / Pending / Approved / Rejected / Shipped / Delivered / Cancelled |
| **HR Management** | Employee Info Management · Recruitment & ATS · Onboarding Management · Payroll Management · Attendance & Leave Management · Performance Management · Workforce Planning & Budgeting · Benefits Administration | Attendance & Leave → Leave / Attendance; Benefits Administration → Benefits / Claims |
| **Manufacturing** | Dashboard · Assembly Lines · Production Orders · BOM Explorer · Routing · Work Centers · Quality Control · Planning (MPS/CRP) · Shop Floor | — |
| **Supply Chain** | Dashboard · Inventory · Purchase Orders · Suppliers · Goods Receipts · Shipments · Invoices · Requisitions | — |
| **Automation** | Dashboard · Expenses · Reports | — |

---

## 1. UI architecture overview

```
ERPApplication (main)
        │
        ▼
   LoginFrame ──► IUIService.sendData(AuthEndpoints.AUTH_LOGIN, ...)
        │
        ▼
   MainFrame ──► PanelRegistry.create(command)    # OCP — no switch in MainFrame
        │
        ├── OrdersHomePanel       (4 tabs → OrderController        → IUIService)
        ├── HRHomePanel           (8 tabs → HRController           → IUIService)
        ├── ManufacturingHomePanel(9 tabs → ManufacturingController → IUIService)
        ├── SupplyChainHomePanel  (8 tabs → SupplyChainController   → IUIService)
        ├── AutomationHomePanel   (3 tabs → OrderController         → IUIService)
        └── …facade placeholders (10 static modules)
```

Key rules for UI code:

* **EDT safety** — Swing lives on the EDT. Every controller call is wrapped in
  a `SwingWorker` (the `submit(owner, work, onOk, retry)` helper). Views stay
  responsive even on multi-second IO.
* **`BasePanel` lifecycle** — each module panel extends `BasePanel`, which
   builds only the common shell in the constructor. Module-specific UI is
   initialized lazily via `ensureInitialized()` (called by `MainFrame` before
   a panel is shown) so subclass fields are fully ready before tab/content
   construction. `refreshData()` is invoked every time the panel is displayed.
* **Tab `Refreshable` pattern** — each tabbed home panel (`OrdersHomePanel`,
  `HRHomePanel`, `ManufacturingHomePanel`, `SupplyChainHomePanel`) routes
  `refreshData()` to the currently-selected tab via a nested `Refreshable`
  interface.
* **Controller-per-module** — controllers own the `IUIService` reference,
  publish results via Observer (`*Listener` interfaces with default methods),
  and route all exceptions through `ExceptionHandler`.
* **`ServiceLocator`** — single replaceable seam for the `IUIService` impl.

---

## 2. The integration boundary — `IUIService`

`com.erp.integration.IUIService` is a **transport contract only**. It carries
two generic methods and zero endpoint strings:

```java
public interface IUIService {
    <T> T fetchData(String endpoint, Map<String, Object> params, Class<T> resultType)
            throws IntegrationException;

    <R> R sendData(String endpoint, Object payload, Class<R> resultType)
            throws IntegrationException;
}
```

Endpoint constants live in **per-module namespace interfaces** under
`com.erp.integration.endpoints.*`:

| Module        | Namespace                     | Constants prefix |
|---------------|-------------------------------|------------------|
| Auth          | `AuthEndpoints`               | `auth/…`         |
| Orders        | `OrdersEndpoints`             | `orders/…`       |
| HR            | `HREndpoints`                 | `hr/…`           |
| Manufacturing | `ManufacturingEndpoints`      | `mfg/…`          |
| Supply Chain  | `SupplyChainEndpoints`        | `scm/…`          |

> **SOLID: ISP.** Adding a new module adds a new namespace interface —
> existing controllers never see the new constants.

---

## 3. Wiring a real backend

1. **Implement `IUIService`** — e.g. `HttpUIService`. In the two generics,
   dispatch on the endpoint prefix:

   ```java
   public <T> T fetchData(String endpoint, Map<String, Object> p, Class<T> t) {
       if (endpoint.startsWith("mfg/"))    return mfgClient.fetch(endpoint, p, t);
       if (endpoint.startsWith("scm/"))    return scmClient.fetch(endpoint, p, t);
       if (endpoint.startsWith("hr/"))     return hrClient.fetch(endpoint, p, t);
       if (endpoint.startsWith("orders/")) return orderClient.fetch(endpoint, p, t);
       if (endpoint.startsWith("auth/"))   return authClient.fetch(endpoint, p, t);
       throw IntegrationException.fetchFailed(endpoint, "unrouted");
   }
   ```

2. **Adapt wire-format → UI DTOs** via
   `com.erp.integration.adapter.DTOAdapter` (Structural Adapter). The UI
   already uses the typed DTOs defined in `com.erp.model.dto.*`; your
   adapter just has to populate them from the JSON/XML/etc.

3. **Register** your impl before the main frame is shown:

   ```java
   // com/erp/ERPApplication.java
   if (Boolean.getBoolean("com.erp.mock")) {
       ServiceLocator.setUIService(new MockUIService());
   } else {
       ServiceLocator.setUIService(new HttpUIService(config));
   }
   new LoginFrame().setVisible(true);
   ```

4. **Keep the mock as the default fallback** so the UI always boots. All
   live demos in class use the mock; production builds flip the system
   property.

---

## 4. Throwing exceptions that the UI understands

`ExceptionHandler` dispatches on `ERPException.Severity` (INFO / WARNING /
MAJOR / FATAL). Backend teams throw one of the typed subclasses below;
no UI change is required to support new codes — that's the OCP contract.

| Code                                 | Class                       | Severity | Factory                                                          |
|--------------------------------------|-----------------------------|----------|------------------------------------------------------------------|
| `FETCH_DATA_FAILED`                  | `IntegrationException`      | MAJOR    | `IntegrationException.fetchFailed(module, detail)`               |
| `SEND_DATA_FAILED`                   | `IntegrationException`      | MAJOR    | `IntegrationException.sendFailed(module, detail)`                |
| `REQUIRED_FIELD`                     | `ValidationException`       | WARNING  | `ValidationException.requiredField(component, fieldName)`        |
| `INVALID_QUANTITY`                   | `ValidationException`       | WARNING  | `ValidationException.invalidQuantity(component)`                 |
| `UNAUTHORIZED_MODULE`                | `AuthException`             | MAJOR    | `AuthException.unauthorizedModule(module, role)`                 |
| `INVALID_CREDENTIALS`                | `AuthException`             | WARNING  | `AuthException.invalidCredentials()`                             |
| **Manufacturing**                    |                             |          |                                                                  |
| `INVALID_BOM_STRUCTURE`              | `BusinessRuleException`     | MAJOR    | `BusinessRuleException.invalidBomStructure(bomId)`               |
| `COMPONENT_STOCK_INSUFFICIENT`       | `BusinessRuleException`     | MAJOR    | `BusinessRuleException.componentStockInsufficient(partId, need, have)` |
| `ROUTING_STEP_GAP`                   | `BusinessRuleException`     | WARNING  | `BusinessRuleException.routingStepGap(productId, missingSeq)`    |
| `QC_DEFECT_THRESHOLD_EXCEEDED`       | `BusinessRuleException`     | MAJOR    | `BusinessRuleException.qcDefectThresholdExceeded(orderId, rate)` |
| `PRODUCTION_ORDER_CANCELLATION_BLOCKED` | `BusinessRuleException`  | WARNING  | `BusinessRuleException.productionOrderCancellationBlocked(id)`   |
| `CAPACITY_OVERLOAD`                  | `BusinessRuleException`     | WARNING  | `BusinessRuleException.capacityOverload(wcId)`                   |
| `DUPLICATE_BOM_VERSION`              | `BusinessRuleException`     | WARNING  | `BusinessRuleException.duplicateBomVersion(productId, version)`  |
| **Supply Chain**                     |                             |          |                                                                  |
| `SUPPLIER_NOT_FOUND`                 | `BusinessRuleException`     | MAJOR    | `BusinessRuleException.supplierNotFound(supplierId)`             |
| `GOODS_RECEIPT_MISMATCH`             | `BusinessRuleException`     | MAJOR    | `BusinessRuleException.goodsReceiptMismatch(poId, expected, actual)` |
| `INVOICE_MISMATCH`                   | `BusinessRuleException`     | MAJOR    | `BusinessRuleException.invoiceMismatch(invoiceId)`               |
| `DUPLICATE_PO`                       | `BusinessRuleException`     | WARNING  | `BusinessRuleException.duplicatePurchaseOrder(poId)`             |
| `STOCK_BELOW_THRESHOLD`              | `BusinessRuleException`     | WARNING  | `BusinessRuleException.stockBelowThreshold(partId)`              |
| `SHIPMENT_DELAYED`                   | `BusinessRuleException`     | WARNING  | `BusinessRuleException.shipmentDelayed(shipmentId)`              |
| `PAYMENT_PROCESSING_FAILED`          | `BusinessRuleException`     | MAJOR    | `BusinessRuleException.paymentProcessingFailed(invoiceId)`       |
| `FOUR_EYES_RULE_VIOLATION`           | `BusinessRuleException`     | MAJOR    | `BusinessRuleException.fourEyesRuleViolation(poId)`              |

`IntegrationException` gets a **retry dialog**; other severities get an
error/warning dialog only. Backend transport failures should always be
`IntegrationException`.

---

## 5. Per-team endpoints

### 5.1 Team HR — `HREndpoints` (`hr/…`)

| Constant                       | Verb  | Params                                     | Returns                         |
|--------------------------------|-------|--------------------------------------------|---------------------------------|
| `HR_EMPLOYEES`                 | fetch | `department`, `status`, `q` (all optional) | `List<EmployeeDTO>`             |
| `HR_RECRUITMENT`               | fetch | –                                          | `List<EmployeeDTO>`             |
| `HR_ONBOARDING`                | fetch | –                                          | `List<EmployeeDTO>`             |
| `HR_PAYROLL`                   | fetch | –                                          | `List<EmployeeDTO>`             |
| `HR_PERFORMANCE`               | fetch | –                                          | `List<EmployeeDTO>`             |
| `HR_ATTENDANCE`                | fetch | –                                          | `List<String[]>`                |
| `HR_LEAVE`                     | fetch | –                                          | `List<String[]>`                |
| `HR_STATS`                     | fetch | –                                          | `Map<String, Integer>`          |
| `HR_EMPLOYEE_UPDATE`           | send  | `EmployeeDTO`                              | `EmployeeDTO`                   |
| `HR_ONBOARDING_UPDATE`         | send  | `EmployeeDTO`                              | `EmployeeDTO`                   |
| `HR_RECRUITMENT_STAGE`         | send  | `{employeeId, stage, score}`               | `EmployeeDTO`                   |
| `HR_PAYROLL_TRANSFER`          | send  | `employeeId` (String)                      | `String` (txn ref)              |
| `HR_ATTENDANCE_LOG`            | send  | `{employeeId, checkIn, checkOut, overtime}`| `String` (ack)                  |
| `HR_LEAVE_ACTION`              | send  | `{id, action}`                             | `String` (ack)                  |

### 5.2 Team Orders — `OrdersEndpoints` (`orders/…`)

| Constant                          | Verb  | Params                                     | Returns                         |
|-----------------------------------|-------|--------------------------------------------|---------------------------------|
| `ORDERS_LIST`                     | fetch | `status`, `q` (optional)                   | `List<OrderDTO>`                |
| `ORDERS_PRODUCT_CATALOG`          | fetch | –                                          | `List<PartDTO>`                 |
| `ORDERS_STATS`                    | fetch | –                                          | `Map<String, Integer>`          |
| `ORDERS_EXPENSE_LIST`             | fetch | –                                          | `List<Map<String, Object>>`     |
| `ORDERS_REPORT_SUMMARY`           | fetch | –                                          | `Map<String, Object>`           |
| `ORDERS_CUSTOMER_INVOICE_LIST`    | fetch | –                                          | `List<Map<String, Object>>`     |
| `ORDERS_CREATE`                   | send  | `OrderDTO`                                 | `OrderDTO`                      |
| `ORDERS_APPROVE`                  | send  | `orderId` (String)                         | `OrderDTO`                      |
| `ORDERS_REJECT`                   | send  | `orderId`                                  | `OrderDTO`                      |
| `ORDERS_REVISION`                 | send  | `orderId`                                  | `OrderDTO`                      |
| `ORDERS_REVISION_UPDATE`          | send  | `OrderDTO`                                 | `OrderDTO`                      |
| `ORDERS_SHIP`                     | send  | `{orderId, courier, tracking}`             | `OrderDTO`                      |
| `ORDERS_PAY`                      | send  | `{orderId, amount, simulateFail}`          | `OrderDTO`                      |
| `ORDERS_CANCEL`                   | send  | `{orderId, reason}`                        | `OrderDTO`                      |
| `ORDERS_EXPENSE_CREATE`           | send  | `Map<String, Object>`                      | `Map<String, Object>`           |
| `ORDERS_CUSTOMER_INVOICE_GENERATE`| send | `{orderId}`                                | `Map<String, Object>`           |

### 5.3 Team Manufacturing — `ManufacturingEndpoints` (`mfg/…`)

| Constant                          | Verb  | Params                           | Returns                      |
|-----------------------------------|-------|----------------------------------|------------------------------|
| `MFG_CARS_LIST`                   | fetch | –                                | `List<CarModelDTO>`          |
| `MFG_PRODUCTION_ORDERS`           | fetch | –                                | `List<ProductionOrderDTO>`   |
| `MFG_BOM_LIST`                    | fetch | –                                | `List<BomDTO>`               |
| `MFG_BOM_DETAILS`                 | fetch | `bomId`                          | `BomDTO`                     |
| `MFG_ROUTING`                     | fetch | `productId` (optional)           | `List<RoutingStepDTO>`       |
| `MFG_WORK_CENTERS`                | fetch | –                                | `List<WorkCenterDTO>`        |
| `MFG_STATS`                       | fetch | –                                | `Map<String, Integer>`       |
| `MFG_CAR_STATUS_UPDATE`           | send  | `{vin, status}`                  | `CarModelDTO`                |
| `MFG_PRODUCTION_ORDER_CREATE`     | send  | `ProductionOrderDTO`             | `ProductionOrderDTO`         |
| `MFG_PRODUCTION_ORDER_CANCEL`     | send  | `orderId`                        | `ProductionOrderDTO`         |
| `MFG_EXECUTION_LOG`               | send  | `{orderId, note}`                | `String` (ack)               |
| `MFG_QC_SUBMIT`                   | send  | `QCCheckDTO`                     | `QCCheckDTO`                 |

### 5.4 Team Supply Chain (SwiftChain) — `SupplyChainEndpoints` (`scm/…`)

| Constant                | Verb  | Params                               | Returns                   |
|-------------------------|-------|--------------------------------------|---------------------------|
| `SCM_SUPPLIERS`         | fetch | –                                    | `List<SupplierDTO>`       |
| `SCM_PO_LIST`           | fetch | `status` (optional)                  | `List<PurchaseOrderDTO>`  |
| `SCM_INVENTORY`         | fetch | –                                    | `List<PartDTO>`           |
| `SCM_LOW_STOCK`         | fetch | –                                    | `List<PartDTO>`           |
| `SCM_STATS`             | fetch | –                                    | `Map<String, Integer>`    |
| `SCM_PO_CREATE`         | send  | `PurchaseOrderDTO`                   | `PurchaseOrderDTO`        |
| `SCM_PO_APPROVE`        | send  | `{poId, approverUserId}`             | `PurchaseOrderDTO`        |
| `SCM_REORDER`           | send  | `{partId, quantity}`                 | `PartDTO`                 |
| `SCM_GRN_CREATE`        | send  | `GoodsReceiptDTO`                    | `GoodsReceiptDTO`         |
| `SCM_SHIPMENT_UPDATE`   | send  | `{shipmentId, status}`               | `ShipmentDTO`             |
| `SCM_INVOICE_CREATE`    | send  | `InvoiceDTO`                         | `InvoiceDTO`              |
| `SCM_INVOICE_VERIFY`    | send  | `{invoiceId, expectedAmount}`        | `InvoiceDTO`              |
| `SCM_INVOICE_PAY`       | send  | `invoiceId`                          | `InvoiceDTO`              |

Business rules the Supply Chain backend must enforce:
* **Four-eyes rule** — `createdBy != approverUserId` on `SCM_PO_APPROVE` (else `FOUR_EYES_RULE_VIOLATION`).
* `SCM_GRN_CREATE` — if `receivedQty != expectedQty`, throw `GOODS_RECEIPT_MISMATCH`.
* `SCM_INVOICE_VERIFY` — if `invoiceAmount != expectedAmount`, throw `INVOICE_MISMATCH`.
* `SCM_INVOICE_PAY` — only allowed when status is `AUTHORIZED`; else `PAYMENT_PROCESSING_FAILED`.
* `SCM_PO_CREATE` — supplier must exist and be approved, else `SUPPLIER_NOT_FOUND`.

Business rules the Manufacturing backend must enforce:
* `MFG_ROUTING` — missing sequence numbers (e.g. 1,2,4) → `ROUTING_STEP_GAP` (warning).
* `MFG_PRODUCTION_ORDER_CANCEL` — if any work order is `IN_PROGRESS`, throw `PRODUCTION_ORDER_CANCELLATION_BLOCKED`.
* `MFG_QC_SUBMIT` — `defectsCount / sampleSize > 0.05` → `QC_DEFECT_THRESHOLD_EXCEEDED`.
* `MFG_PRODUCTION_ORDER_CREATE` — component stock must cover planned qty × BOM; else `COMPONENT_STOCK_INSUFFICIENT`.

---

## 6. Compile & run

Requirements: **JDK 11+**.

```bash
./run.sh                      # compiles src/ → out/ and launches ERPApplication
# or
java -Dcom.erp.mock=false -cp out com.erp.ERPApplication    # real-backend mode (after wiring)
```

Demo credentials (defined in `MockUIService`):

| Role      | Username   | Password   |
|-----------|------------|------------|
| Admin     | `admin`    | `admin123` |
| Manager   | `manager`  | `manager123` |
| Employee  | `emp001`   | `emp123`   |
| HR        | `hr_admin` | `hr123`    |
| Sales     | `sales01`  | `sales123` |
| Manufacturing | `mfg_admin` | `mfg123` |
| Supply Chain  | `scm_admin` | `scm123` |

---

## 7. Design patterns & principles cheat sheet

| Where                                              | Tag                                                    |
|----------------------------------------------------|--------------------------------------------------------|
| `IUIService`, `*Endpoints` interfaces              | SOLID: ISP                                             |
| `PanelRegistry`, `MainFrame`                       | SOLID: OCP · Factory Method + Registry (Creational)    |
| `BusinessRuleException`                            | SOLID: OCP (extends without modifying ExceptionHandler) |
| `OrderController`, `HRController`, `ManufacturingController`, `SupplyChainController` | GRASP: Controller · GRASP: Pure Fabrication · Observer (Behavioral) |
| `MockUIService`                                    | GRASP: Information Expert                              |
| `DTOAdapter`                                       | Adapter (Structural)                                   |
| `OrdersHomePanel`, `HRHomePanel`, `ManufacturingHomePanel`, `SupplyChainHomePanel` | Composite (Structural) — tabbed Swing tree |
| `BasePanel`                                        | Template Method (Behavioral)                           |
| `ServiceLocator`                                   | Service Locator (GoF-adjacent) — single replaceable seam |
| `*DTO` classes                                     | DTO (data-carrier)                                     |
| `SwingWorker submit()` helper in each controller   | Asynchronous Producer-Consumer (keeps EDT responsive)  |

---

## 8. Implementation guide for future subsystem integrations

Use this runbook when integrating a new subsystem (example: `quality`,
`fleet`, `procurement-analytics`) without breaking existing modules.

### 8.1 Define the subsystem contract first

1. Create endpoint namespace interface:
   * `src/com/erp/integration/endpoints/<Subsystem>Endpoints.java`
   * Keep constants as `<prefix>/<action>` (for routing by prefix).
2. Add DTOs under `src/com/erp/model/dto/`.
3. Add business exceptions as factory methods in
   `src/com/erp/exception/BusinessRuleException.java`.

### 8.2 Build integration in vertical slices

1. Add controller:
   * `src/com/erp/controller/<Subsystem>Controller.java`
   * Implement listener interface with default methods.
   * Route all calls through `submit(...)` to preserve EDT responsiveness.
2. Add tabs and home panel:
   * `src/com/erp/view/panels/<subsystem>/...`
   * Make each tab implement `<Subsystem>HomePanel.Refreshable`.
   * Share one controller instance across all tabs in the home panel.
3. Register module in panel registry:
   * update `src/com/erp/view/PanelRegistry.java`.
4. Expose module in sidebar and RBAC:
   * update `src/com/erp/view/components/Sidebar.java`.
   * update `src/com/erp/session/RoleAccess.java`.

### 8.3 Backend transport integration

1. Extend your `IUIService` implementation to route the new endpoint prefix.
2. Add mock handlers in `src/com/erp/integration/MockUIService.java` for:
   * list/stats fetches,
   * create/update actions,
   * failure scenarios (`IntegrationException`, `BusinessRuleException`).
3. If needed, add adapter mappings in
   `src/com/erp/integration/adapter/DTOAdapter.java`.

### 8.4 UI lifecycle and navigation safety rules

1. Never build tab content from `BasePanel` constructor assumptions.
2. Keep module construction safe with `BasePanel.ensureInitialized()`.
3. In navigation, always follow order:
   * create panel,
   * `ensureInitialized()`,
   * add to card layout,
   * `refreshData()`,
   * show card.
4. Sidebar menu items must dispatch click events from both row container and
   inner label/component to avoid "dead click" behavior.

### 8.5 Completion checklist (must pass before merge)

1. Compile check:
   * `javac -d out -sourcepath src src/com/erp/ERPApplication.java`
2. Runtime smoke check (`./run.sh`):
   * login,
   * switch repeatedly between Dashboard/Orders/HR/Manufacturing/Supply Chain,
   * verify card content changes every click,
   * verify each integrated module loads at least one dataset.
3. Failure-path check:
   * trigger one mocked integration failure and confirm retry dialog appears.
4. RBAC check:
   * verify at least two roles with/without access to the new module.
