package com.erp.service;

import java.util.List;
import java.util.Map;

/**
 * Interface for CRM Service — to be implemented by CRM team.
 * All methods should use the erp-subsystem-sdk to access RDS.
 */
public interface CRMService {
    List<Map<String, Object>> getContacts(String filter) throws Exception;
    List<Map<String, Object>> getLeads(String filter) throws Exception;
    List<Map<String, Object>> getOpportunities(String filter) throws Exception;
    Map<String, Object> getMetrics() throws Exception;
}
