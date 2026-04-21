package com.erp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSONUtil handles serialization and deserialization of the BOM tree.
 * 
 * Principles & Patterns Used:
 * 1. Single Responsibility Principle (SOLID): This class has only one reason to change - JSON format updates.
 * 2. High Cohesion (GRASP): All methods are strictly related to JSON parsing.
 * 3. Pure Fabrication (GRASP): A utility class fabricated to achieve low coupling and high cohesion.
 */
public class JSONUtil {

    public static class BOMNode {
        public String name;
        public double qty;
        public String uom;
        public double cost;
        public List<BOMNode> children = new ArrayList<>();
        
        public BOMNode(String name, double qty, String uom, double cost) {
            this.name = name;
            this.qty = qty;
            this.uom = uom;
            this.cost = cost;
        }
        
        @Override
        public String toString() {
            return name + " (" + qty + " " + uom + ") - ₹" + String.format("%.2f", cost);
        }
    }

    public static String toJSON(List<BOMNode> nodes) {
        if (nodes == null) return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < nodes.size(); i++) {
            BOMNode node = nodes.get(i);
            sb.append("{");
            sb.append("\"name\":\"").append(escape(node.name)).append("\",");
            sb.append("\"qty\":").append(node.qty).append(",");
            sb.append("\"uom\":\"").append(escape(node.uom)).append("\",");
            sb.append("\"cost\":").append(node.cost).append(",");
            sb.append("\"children\":").append(toJSON(node.children));
            sb.append("}");
            if (i < nodes.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static List<BOMNode> fromJSON(String json) {
        List<BOMNode> result = new ArrayList<>();
        if (json == null || json.trim().isEmpty() || !json.contains("[")) return result;
        
        json = json.trim();
        if (json.startsWith("[")) json = json.substring(1, json.length() - 1);
        
        int i = 0;
        while (i < json.length()) {
            if (json.charAt(i) == '{') {
                int start = i;
                int depth = 1;
                i++;
                while (i < json.length() && depth > 0) {
                    if (json.charAt(i) == '{') depth++;
                    else if (json.charAt(i) == '}') depth--;
                    i++;
                }
                String objStr = json.substring(start, i);
                result.add(parseObject(objStr));
            } else {
                i++;
            }
        }
        
        return result;
    }

    private static BOMNode parseObject(String objStr) {
        String name = extractStringValue(objStr, "name");
        double qty = extractDoubleValue(objStr, "qty");
        String uom = extractStringValue(objStr, "uom");
        double cost = extractDoubleValue(objStr, "cost");
        
        BOMNode node = new BOMNode(name, qty, uom, cost);
        
        Matcher cm = Pattern.compile("\"children\"\\s*:\\s*\\[").matcher(objStr);
        if (cm.find()) {
            int start = cm.end() - 1; // points to '['
            int depth = 1;
            int i = start + 1;
            while (i < objStr.length() && depth > 0) {
                if (objStr.charAt(i) == '[') depth++;
                else if (objStr.charAt(i) == ']') depth--;
                i++;
            }
            if (i <= objStr.length()) {
                String childrenStr = objStr.substring(start, i);
                node.children = fromJSON(childrenStr);
            }
        }
        
        return node;
    }

    private static String extractStringValue(String src, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"(.*?)\"").matcher(src);
        if (m.find()) {
            return m.group(1).replace("\\\"", "\"").replace("\\\\", "\\");
        }
        return "";
    }

    private static double extractDoubleValue(String src, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*([0-9.]+)").matcher(src);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1));
            } catch (Exception e) {}
        }
        return 0;
    }
}
