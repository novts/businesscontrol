package com.tmsoftstudio.businesscontrol.backend;


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TracksServlet extends HttpServlet {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf8");
        resp.setContentType("application/json");
        JSONObject json = new JSONObject();
        JSONArray tracks=new JSONArray();
        JSONObject trackJ;
        JSONArray tracksJ;

        Query.Filter taskF =  new Query.FilterPredicate("state", Query.FilterOperator.EQUAL, "new");
        Query q = new Query("Task").setFilter(taskF);
        PreparedQuery pq = datastore.prepare(q);
        for (Entity result : pq.asIterable()) {
            if(result.hasProperty("track")) {
                String track = (String) result.getProperty("track");
                String name = (String) result.getProperty("name");
                String [] trackArr = track.split(";");
                tracksJ=new JSONArray();
for(String tmp:trackArr ){
    JSONObject tmpJ = new JSONObject(tmp);
    tracksJ.put(tmpJ);
}
                trackJ=new JSONObject();
                trackJ.put("track",tracksJ);
                trackJ.put("name",name);
                tracks.put(trackJ);
            }
        }
        json.put("tracks", tracks);
        String output = req.getParameter("callback") + "(" + json.toString() + ");";

        PrintWriter out = resp.getWriter();
        out.write(output);
        out.close();

    }

}
