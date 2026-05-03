package com.trackingpath.configs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.entities.Users;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebFilter("/*")
public class PermissionFilter implements Filter {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No init required
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        String url = httpReq.getRequestURI().replace(httpReq.getContextPath(), "");
        String action = getActionType(httpReq);
        String module = getModuleName(url);

        // Public URLs bypass
        if (url.startsWith("/login") ||
                url.startsWith("/logout") ||
                url.equals("/") ||
                url.startsWith("/reset-password") ||
                url.startsWith("/resetPassword") ||
                url.startsWith("/users_verfication") ||
                url.startsWith("/weblive") ||
                url.startsWith("/upload") ||
                url.startsWith("/traccar/") ||
                url.startsWith("/resources/") ||
                url.startsWith("/api/") ||
                url.startsWith("/oauth/token")) {

            chain.doFilter(request, response);
            return;
        }

        Users user = (Users) httpReq.getSession().getAttribute("sessiondata");

        if (user == null) {
            httpRes.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpRes.getWriter().write("Session expired. Please login again.");
            return;
        }

        if (module != null && action != null) {
            boolean allowed = hasPermission(user, module, action);

            if (!allowed) {
                httpRes.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpRes.getWriter().write("No " + action + " permission for module: " + module);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private String getActionType(HttpServletRequest request) {

        String method = request.getMethod();

        switch (method) {
            case "GET":
                return "read";

            case "POST":
            case "PUT":
                return "write";

            case "DELETE":
                return "delete";

            default:
                return null;
        }
    }

    private String getModuleName(String url) {

        url = url.toLowerCase();

        if (url.contains("expense")) return "Expense";
        if (url.contains("device")) return "Object";
        if (url.contains("dashboard")) return "Dashboard";
        if (url.contains("service")) return "Services";
        if (url.contains("driver")) return "Driver";
        if (url.contains("user")) return "User";
        if (url.contains("geofence")) return "Geofence";
        if (url.contains("maintenance")) return "Maintenance";
        if (url.contains("alert")) return "Alert";
        if (url.contains("sms")) return "Sms";
        if (url.contains("email")) return "Email";
        if (url.contains("gprs")) return "Gprs";
        if (url.contains("sensor")) return "Sensor";
        if (url.contains("task")) return "Task";
        if (url.contains("subscription")) return "Subscription";
        if (url.contains("setup")) return "Setup";
        if (url.contains("route")) return "Route";
        if (url.contains("poi")) return "Poi";
        if (url.contains("report")) return "Report";
        if (url.contains("chat")) return "Chat";
        return null;
    }

    private boolean hasPermission(Users user, String module, String actionType) {

        try {

            String json = user.getPermissions();

            if (json == null || json.isEmpty()) {
                return false;
            }

            List<Map<String, Object>> permissionsList =
                    mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});

            for (Map<String, Object> perm : permissionsList) {

                if (module.equalsIgnoreCase(String.valueOf(perm.get("permission")))) {

                    Object actionValue = perm.get(actionType);

                    if (actionValue instanceof Boolean && (Boolean) actionValue) {
                        return true;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}