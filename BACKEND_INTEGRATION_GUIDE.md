# Backend Integration Guide for ERP-UI

## 🎯 Overview

The ERP-UI is now **UI-only** with **interfaces** for backend teams to implement. All data access goes through:
1. **UIAuthenticator** — validates login credentials from `erp_ui_auth.app_users` table
2. **Service Interfaces** — CRMService, SalesService, FinanceService, HRService (more to come)

---

## 📋 Database Schema

### **Authentication Database: `erp_ui_auth`**

```sql
-- User credentials
CREATE TABLE app_users (
    user_id VARCHAR(40) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    role VARCHAR(30) NOT NULL,  -- ADMIN, MANAGER, HR, SALES, MFG, SCM, EMPLOYEE
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Login audit trail
CREATE TABLE login_audit (
    audit_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    requested_role VARCHAR(30),
    resolved_role VARCHAR(30),
    success TINYINT(1) NOT NULL,
    failure_reason VARCHAR(120),
    attempted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### **Example Seed Data**
```sql
INSERT INTO app_users VALUES 
('USR-001', 'admin', 'BCRYPT_HASH_HERE', 'Administrator', 'ADMIN', 1, NOW()),
('USR-002', 'manager', 'BCRYPT_HASH_HERE', 'Manager User', 'MANAGER', 1, NOW()),
('USR-003', 'hr_user', 'BCRYPT_HASH_HERE', 'HR User', 'HR', 1, NOW()),
('USR-004', 'sales_user', 'BCRYPT_HASH_HERE', 'Sales User', 'SALES', 1, NOW()),
('USR-005', 'mfg_user', 'BCRYPT_HASH_HERE', 'Manufacturing User', 'MFG', 1, NOW()),
('USR-006', 'scm_user', 'BCRYPT_HASH_HERE', 'Supply Chain User', 'SCM', 1, NOW());
```

---

## 🔐 Step 1: Implement UIAuthenticator (CRM Team)

**File to implement:** `src/com/erp/service/UIAuthenticator.java` (interface provided)

**Implementation class:** `src/com/erp/service/RDSUIAuthenticator.java`

```java
package com.erp.service;

import com.erp.sdk.config.DatabaseConfig;
import com.erp.sdk.factory.SubsystemFactory;
import com.erp.sdk.subsystem.UI;
import com.erp.sdk.subsystem.SubsystemName;
import java.nio.file.Path;
import java.util.Map;

public class RDSUIAuthenticator implements UIAuthenticator {
    
    private final UI uiService;
    
    public RDSUIAuthenticator() throws Exception {
        DatabaseConfig config = DatabaseConfig.fromProperties(
            Path.of("src", "main", "resources", "application-rds.properties")
        );
        this.uiService = (UI) SubsystemFactory.create(SubsystemName.UI, config);
    }
    
    @Override
    public AuthResult authenticate(String username, String password, String requestedRole) {
        try {
            // Query app_users table
            var results = uiService.readAll("app_users", 
                Map.of("username", username), 
                "integration_lead"  // service account username
            );
            
            if (results == null || results.isEmpty()) {
                // Log failed attempt
                logLoginAttempt(username, requestedRole, null, false, "INVALID_CREDENTIALS");
                return null;
            }
            
            Map<String, Object> user = results.get(0);
            
            // Verify password (example using bcrypt)
            String storedHash = (String) user.get("password_hash");
            if (!verifyPassword(password, storedHash)) {
                logLoginAttempt(username, requestedRole, null, false, "INVALID_CREDENTIALS");
                return null;
            }
            
            // Check if active
            Object isActive = user.get("is_active");
            if (!(boolean) isActive) {
                logLoginAttempt(username, requestedRole, null, false, "USER_INACTIVE");
                return null;
            }
            
            // Success
            String resolvedRole = requestedRole != null ? requestedRole : (String) user.get("role");
            logLoginAttempt(username, requestedRole, resolvedRole, true, null);
            
            return new AuthResult(
                (String) user.get("user_id"),
                username,
                (String) user.get("display_name"),
                resolvedRole,
                true
            );
            
        } catch (Exception e) {
            logLoginAttempt(username, requestedRole, null, false, "DATABASE_ERROR");
            return null;
        }
    }
    
    private void logLoginAttempt(String username, String requestedRole, 
                                String resolvedRole, boolean success, String failureReason) {
        try {
            uiService.create("login_audit", Map.of(
                "username", username,
                "requested_role", requestedRole,
                "resolved_role", resolvedRole,
                "success", success ? 1 : 0,
                "failure_reason", failureReason,
                "attempted_at", java.time.LocalDateTime.now()
            ), "integration_lead");
        } catch (Exception e) {
            System.err.println("Failed to log login attempt: " + e.getMessage());
        }
    }
    
    private boolean verifyPassword(String plaintext, String hash) {
        // Use bcrypt, scrypt, or PBKDF2 to verify
        // Example: org.mindrot.jbcrypt.BCrypt.checkpw(plaintext, hash)
        return true; // TODO: implement password hashing
    }
}
```

**Update LoginFrame to use it:**
```java
// Line 32 in LoginFrame.java
private final UIAuthenticator authenticator = new RDSUIAuthenticator();  // instead of MockUIAuthenticator
```

---

## 🔗 Step 2: Implement Service Interfaces

### **CRMService (CRM Team)**

**File:** `src/com/erp/service/CRMService.java` (interface provided)

**Implementation:**
```java
package com.erp.service;

import com.erp.sdk.config.DatabaseConfig;
import com.erp.sdk.factory.SubsystemFactory;
import com.erp.sdk.subsystem.CRM;
import com.erp.sdk.subsystem.SubsystemName;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class CRMServiceImpl implements CRMService {
    
    private final CRM crm;
    
    public CRMServiceImpl() throws Exception {
        DatabaseConfig config = DatabaseConfig.fromProperties(
            Path.of("src", "main", "resources", "application-rds.properties")
        );
        this.crm = (CRM) SubsystemFactory.create(SubsystemName.CRM, config);
    }
    
    @Override
    public List<Map<String, Object>> getContacts(String filter) throws Exception {
        return crm.readAll("customers", parseFilter(filter), "crm_user");
    }
    
    @Override
    public List<Map<String, Object>> getLeads(String filter) throws Exception {
        return crm.readAll("leads", parseFilter(filter), "crm_user");
    }
    
    @Override
    public List<Map<String, Object>> getOpportunities(String filter) throws Exception {
        return crm.readAll("opportunities", parseFilter(filter), "crm_user");
    }
    
    @Override
    public Map<String, Object> getMetrics() throws Exception {
        // Return aggregated metrics
        return Map.of(
            "total_contacts", crm.readAll("customers", Map.of(), "crm_user").size(),
            "active_leads", crm.readAll("leads", Map.of("status", "ACTIVE"), "crm_user").size(),
            "open_opportunities", crm.readAll("opportunities", Map.of("status", "OPEN"), "crm_user").size()
        );
    }
    
    private Map<String, Object> parseFilter(String filter) {
        // Parse filter string (e.g., "status=ACTIVE") into Map
        return Map.of();
    }
}
```

**Update CRMFacadePanel to use it:**
```java
// In CRMFacadePanel.java
private final CRMService crmService = new CRMServiceImpl();

@Override
protected void layoutComponents() {
    // ... tabs setup ...
    
    // In dashboard tab, call:
    new StubTabPanel("CRM Dashboard", "...", columns, buttons) {
        @Override
        public void refresh() {
            try {
                Map<String, Object> metrics = crmService.getMetrics();
                // Populate UI with metrics
            } catch (Exception e) {
                // Handle error
            }
        }
    };
}
```

---

### **Similar for SalesService, FinanceService, HRService**
Each team implements their service interface following the same pattern.

---

## 📦 Configuration

**File:** `src/main/resources/application-rds.properties`

```properties
# Update these with actual RDS endpoint and credentials
db.host=erp-mysql.ap-south-1.rds.amazonaws.com
db.port=3306
db.name=erp_ui_auth
db.username=erp_ui_user
db.password=your_secure_password
db.pool.maxSize=10
```

---

## ✅ Integration Checklist

### **Database Team**
- [ ] Create `erp_ui_auth` database on RDS
- [ ] Create `app_users` and `login_audit` tables
- [ ] Seed initial users with bcrypt-hashed passwords
- [ ] Grant permissions to `erp_ui_user` account
- [ ] Test connectivity from UI

### **CRM Team**
- [ ] Implement `RDSUIAuthenticator`
- [ ] Implement `CRMService`
- [ ] Update `CRMFacadePanel` to call service methods
- [ ] Test login + CRM tabs

### **Other Teams (Sales, Finance, HR, etc.)**
- [ ] Implement your service interface
- [ ] Update corresponding facade panel
- [ ] Test with real data

### **UI Team**
- [ ] Replace `MockUIAuthenticator` with `RDSUIAuthenticator`
- [ ] Inject service implementations into panels
- [ ] Test full login → dashboard → module flow

---

## 🔄 Data Flow

```
User Login
    ↓
LoginFrame calls UIAuthenticator.authenticate(username, password, role)
    ↓
RDSUIAuthenticator queries erp_ui_auth.app_users via SDK
    ↓
Password verification (bcrypt comparison)
    ↓
Log attempt to login_audit table
    ↓
Return AuthResult (userId, displayName, role, etc.)
    ↓
MainFrame displays user in header + status bar
    ↓
User clicks sidebar module
    ↓
Panel calls its Service (e.g., CRMService.getContacts())
    ↓
Service queries RDS via SDK
    ↓
Data displayed in empty JTable
```

---

## 🚨 Important Rules

1. **Use the SDK, not JDBC** — All RDS access via `erp-subsystem-sdk` only
2. **Audit logins** — Every login attempt logged to `login_audit` table
3. **Hash passwords** — Never store plaintext, use bcrypt/scrypt/PBKDF2
4. **Service usernames** — Use the system accounts provided (e.g., `crm_user`, `integration_lead`)
5. **Handle errors gracefully** — Try-catch all DB operations, log errors

---

## 📞 Support

If you have questions about:
- **SDK usage** → Ask Integration Team
- **Database schema** → Ask Database Team
- **UI integration** → Ask UI Team
- **Your subsystem** → Ask your team lead

---

**Last Updated:** 2026-04-21  
**Status:** Ready for implementation
