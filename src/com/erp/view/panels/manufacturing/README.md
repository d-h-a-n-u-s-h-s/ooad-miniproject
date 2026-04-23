# Manufacturing Subsystem - ERP UI

Welcome to the **Manufacturing Subsystem** of the ERP-UI project. This module handles all aspects of production planning, shop floor execution, bill of materials (BOM) management, routing, and quality control.

## 🚀 Key Features
1. **Supply Chain Management (SCM) Integration:** Real-time, bi-directional asynchronous network syncing via dedicated Ngrok tunnels.
2. **Database SDK Abstraction:** Built on a centralized `AbstractSubsystem` SDK, eliminating raw SQL from business logic.
3. **Multilevel Bill of Materials (BOM) Engine:** Supports recursive, multi-tiered composite tree structures for sub-assemblies.
4. **Material Requirements Planning (MRP) Estimation:** Automatically calculates total manufacturing costs (materials, setup, run times) before committing a plan.
5. **Automated Production Order Lifecycle:** Standardizes transition of a Production Plan into an actionable Order with lead-time calculations.
6. **Dynamic Work Center Routing:** Provides sequential assembly-line logic and tracks Work Center utilization/capacities.
7. **Shop Floor Yield Execution:** Real-time shop floor data logging for continuous input of incremental quantities produced.
8. **Automated Quality Control Gates:** Strict logical barriers ensuring inspection only happens after the physical yield matches planned quantity and routing is 'Completed'.
9. **Smart Defect Threshold Alerts:** Mathematically calculates Pass/Fail ratings during final inspection with a 5% warning threshold.
10. **Event-Driven UI Synchronization:** Purely event-driven Model-View-Controller architecture using Java Reflection for real-time UI updates.

## 🏗️ Architecture & Workflows

### Primary Use Cases
- **Manage Bill of Materials (BOM)** (Includes structure validation and circular dependency checks)
- **Create Capacity Production Plan (MRP)** (Generates detailed cost reports)
- **Define Assembly Line Routing**
- **Execute Production Order** (Logs shop floor quantity)
- **Perform Quality Control Check** (Marks order as PASSED or FAILURE based on defect thresholds)
- **Synchronize Materials** (Supply Chain API Integration)

### Core Components
- **BOMService:** The central Singleton facade connecting the UI to the Database SDK.
- **InventoryApiServer & InventoryApiClient:** Handles the HTTP communication and pushes materials to the external Supply Chain.
- **UI Tabs:** Distinct tabs handling single responsibilities (e.g., `BOMExplorerTab`, `ManufacturingPlanningTab`, `AssemblyLinesTab`, `ShopFloorExecutionTab`, `QualityControlTab`).
- **JSONUtil:** Manages the serialization and deserialization of the recursive BOM composite tree data structure.

### End-to-End Workflow
1. **Material & BOM Creation:** Users define raw materials, sync with the external API, and construct structured BOM hierarchies.
2. **Planning & Cost Estimation:** MRP is run to generate a Production Plan with recursively calculated multi-level costs.
3. **Order Conversion & Routing:** The plan is converted into an active Production Order and routed sequentially through assigned Work Centers/Assembly Lines.
4. **Shop Floor Execution:** Operators build the order and log physical quantities produced at each step.
5. **Quality Control:** Once routing is complete, final products are inspected. If the defect rate exceeds 5%, the batch is flagged; otherwise, it passes.

## 🧩 Design Patterns
The Manufacturing subsystem heavily relies on industry-standard design patterns:
- **Facade Pattern** (`BOMService`)
- **Composite Pattern** (`JSONUtil.BOMNode` & `BOMExplorerTab`)
- **Singleton Pattern** (`BOMService`, `InventoryApiServer`)
- **Observer/Event-Listener Pattern** (`ManufacturingHomePanel` auto-refresh)
- **Factory Method Pattern** (`UIHelper`)
- **Strategy Pattern** (Dynamic log sorting)
- **Data Transfer Object (DTO)** (`BomItem`, `OrderItem`)
- **Proxy Pattern** (`InventoryApiClient`)
- **Template Method Pattern** (`BasePanel` initialization)
- **Model-View-Controller (MVC)** (Global Architectural Pattern)

## 📐 Design Principles
- **Single Responsibility Principle (SRP):** Isolated logic per UI Tab.
- **Open/Closed Principle (OCP):** Easily extensible tab structure.
- **Dependency Inversion (DIP):** UI depends on `BOMService` abstractions, not raw SQL.
- **Separation of Concerns (SoC):** Serialization logic is strictly separated from Swing UI rendering.
- **Encapsulation:** Database properties and SDK connections are perfectly abstracted from the frontend.

## 📂 Directory Structure
This package (`com.erp.view.panels.manufacturing`) contains all the UI components, dialogs, and tabs necessary to render the Manufacturing Subsystem natively within the ERP-UI Swing application.

---
*For more detailed class relations, state transitions, and sequence flows, refer to `diagram.txt` located in the project root.*
