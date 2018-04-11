package com.tmsoftstudio.businesscontrol.backend;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TasksServlet extends HttpServlet {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf8");
        resp.setContentType("application/json");
        JSONObject json = new JSONObject();
        JSONArray tasks = new JSONArray();
        JSONObject taskJ;
        JSONArray tracksJ;
        JSONArray reportJ;

        String state=req.getParameter("state");
        Query.Filter taskF =  new Query.FilterPredicate("state", Query.FilterOperator.EQUAL, state);
        Query q = new Query("Task").setFilter(taskF);
        PreparedQuery pq = datastore.prepare(q);
        for (Entity result : pq.asIterable()) {
            String date = (String) result.getProperty("date");
            String descs = (String) result.getProperty("descs");
            String name = (String) result.getProperty("name");
            String routes = (String) result.getProperty("routes");
            String users = (String) result.getProperty("users");

            taskJ = new JSONObject();
            taskJ.put("descs", new JSONArray(descs));
            taskJ.put("routes", new JSONArray(routes));
            taskJ.put("users", new JSONArray(users));
            taskJ.put("date", date);
            taskJ.put("name", name);

            if(result.hasProperty("track")) {
                String track = (String) result.getProperty("track");
                String [] trackArr = track.split(";");
                tracksJ=new JSONArray();
                for(String tmp:trackArr ){
                    JSONObject tmpJ = new JSONObject(tmp);
                    tracksJ.put(tmpJ);
                }

                taskJ.put("track", tracksJ);
            }

            if(result.hasProperty("reports")) {
                String reports = ((Text)result.getProperty("reports")).getValue();
                String [] reportArr = reports.split(";");
                reportJ=new JSONArray();
                for(String tmp:reportArr ){
                    JSONObject tmpJ = new JSONObject(tmp);
                    reportJ.put(tmpJ);
                }

                taskJ.put("reports", reportJ);
            }

            tasks.put(taskJ);
        }
        json.put("tasks", tasks);
        String output = req.getParameter("callback") + "(" + json.toString() + ");";

        PrintWriter out = resp.getWriter();
        out.write(output);
        out.close();

    }

}
