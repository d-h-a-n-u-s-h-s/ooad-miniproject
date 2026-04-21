package com.erp.service;

import java.util.List;
import java.util.Map;

/**
 * Interface for Finance Service — to be implemented by Finance team.
 */
public interface FinanceService {
    List<Map<String, Object>> getGeneralLedger(String filter) throws Exception;
    List<Map<String, Object>> getAccountsPayable(String filter) throws Exception;
    List<Map<String, Object>> getAccountsReceivable(String filter) throws Exception;
    Map<String, Object> getCashFlow() throws Exception;
    Map<String, Object> getMetrics() throws Exception;
}
