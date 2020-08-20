package com.redhat.jdg.debugger;

import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.*;
import javax.servlet.http.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.infinispan.client.hotrod.RemoteCache;
import org.jboss.logging.Logger;

public class DebuggerServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(Counter.class);

    private String titleMessage = "Debugger Servlet";
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.titleMessage = config.getInitParameter("titleMessage");
    }
    
    public void destroy() { }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        LOGGER.debug("method called: private void processRequest(HttpServletRequest request, HttpServletResponse response)");

        response.setContentType("application/json");
        HttpSession session = request.getSession();
        
        PrintWriter out = response.getWriter();
        
        Counter counter = getSessionObj(session);
        counter.increment();

        LOGGER.info("counter.......: " + counter.getValue());
        LOGGER.info("JSESSIONID....: " + session.getId());

        StringBuilder uglyJson = new StringBuilder();
        StringBuilder jsonAttributes = new StringBuilder();

        uglyJson.append("{");

        uglyJson.append("\"counter\":" + "\"").append(counter.getValue()).append("\",");
        uglyJson.append("\"creationTime\":" + "\"").append(new Date(session.getCreationTime())).append("\",");
        uglyJson.append("\"lastAccessedTime\":" + "\"").append(new Date(session.getLastAccessedTime())).append("\",");
        uglyJson.append("\"serverInfo\":" + "\"").append(session.getServletContext().getServerInfo()).append("\",");
        uglyJson.append("\"virtualServerName\":" + "\"").append(session.getServletContext().getVirtualServerName()).append("\",");
        uglyJson.append("\"remoteAddr\":" + "\"").append(request.getRemoteAddr()).append("\",");
        uglyJson.append("\"remoteHost\":" + "\"").append(request.getRemoteHost()).append("\",");
        uglyJson.append("\"remotePort\":" + "\"").append(request.getRemotePort()).append("\",");
        uglyJson.append("\"JSESSIONID\":" + "\"").append(session.getId()).append("\",");

        Enumeration<String> attributes = session.getAttributeNames();
        String attributesStr = null;

        if (attributes.hasMoreElements()) {
            uglyJson.append("\"attributes\": " + "[");

            while (attributes.hasMoreElements()) {
                String attribute = (String) attributes.nextElement();
                jsonAttributes.append("{\"").append(attribute).append("\":").append("\"").append(session.getAttribute(attribute)).append("\"},");
            }
        }

        attributesStr = jsonAttributes.toString();
        attributesStr = attributesStr.replaceAll(",$", "");

        uglyJson.append(attributesStr);
        uglyJson.append("]");
        uglyJson.append("}");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(uglyJson.toString());

        out.println(gson.toJson(je));

        out.close();
    }
    
    private Counter getSessionObj(HttpSession session) {
        Counter counter = (Counter) session.getAttribute("obj");
        if (counter == null) {
            counter = new Counter();
            session.setAttribute("obj", counter);
        }
        
        return counter;
    }

    /** Acquire cache stats
     * @param request servlet request
     * @param response servlet response
     */
    private void cacheStats(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String query = request.getQueryString();
        PrintWriter out = response.getWriter();

        RemoteCache<Object, Object> cache = JdgResourcesConfig.remoteCache(query);

        JsonObjectBuilder builder = Json.createObjectBuilder();
        JsonArrayBuilder jsonArray = Json.createArrayBuilder();

        jsonArray.add(Json.createObjectBuilder().add("name", cache.getName() ));
        jsonArray.add(Json.createObjectBuilder().add("size", cache.size() ));

        for (Map.Entry<String, String> entry : cache.stats().getStatsMap().entrySet()){
            jsonArray.add(Json.createObjectBuilder().add(entry.getKey() , entry.getValue()));
        }

        builder.add("result", jsonArray);

        out.println(builder.build().toString());
        out.close();
    }

    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        LOGGER.debug("method called: protected void doGet(HttpServletRequest request, HttpServletResponse response)");

        LOGGER.infov(" ");
        LOGGER.infov("request.getContextPath().......: {0}", request.getContextPath());
        LOGGER.infov("request.getPathInfo()..........: {0}", request.getPathInfo());
        LOGGER.infov("request.getQueryString().......: {0}", request.getQueryString());

        String pathInfo = request.getPathInfo();

        if (pathInfo != null && pathInfo.contains("stats")) {
            cacheStats(request, response);
        } else {
            processRequest(request, response);
        }

    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        LOGGER.debug("method called: protected void doPost(HttpServletRequest request, HttpServletResponse response)");

        processRequest(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LOGGER.debug("method called: protected void doPut(HttpServletRequest req, HttpServletResponse resp)");

        resp.setContentType("application/json");
        BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));

        String data = br.readLine();

        if (data == null || "".equals(data.trim())) {
            resp.setStatus(400);
        } else {
            try {
                String[] entry = data.split("=");
                HttpSession session = req.getSession(false);

                LOGGER.infov("putting new attribute in session. JESSIONID={0}, k={1}, v={2}", session.getId(), entry[0], entry[1]);
                session.setAttribute(entry[0], entry[1]);
                resp.setStatus(204);
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage());
                resp.setStatus(400);
            }
        }
    }
    
    /** Returns a short description of the servlet. */
    public String getServletInfo() {
        return "Counter Servlet";
    }
    
}
