package com.erp.service;

import java.util.List;
import java.util.Map;

/**
 * Interface for Sales Service — to be implemented by Sales team.
 */
public interface SalesService {
    List<Map<String, Object>> getOrders(String filter) throws Exception;
    List<Map<String, Object>> getQuotations(String filter) throws Exception;
    List<Map<String, Object>> getDealers(String filter) throws Exception;
    Map<String, Object> getMetrics() throws Exception;
}
