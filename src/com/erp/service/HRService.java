package com.erp.service;

import java.util.List;
import java.util.Map;

/**
 * Interface for HR Service — to be implemented by HR team.
 */
public interface HRService {
    List<Map<String, Object>> getEmployees(String filter) throws Exception;
    List<Map<String, Object>> getAttendance(String filter) throws Exception;
    List<Map<String, Object>> getPayroll(String filter) throws Exception;
    Map<String, Object> getMetrics() throws Exception;
}
