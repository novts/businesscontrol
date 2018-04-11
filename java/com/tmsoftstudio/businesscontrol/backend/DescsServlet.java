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

public class DescsServlet extends HttpServlet {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf8");
        resp.setContentType("application/json");
        JSONObject json = new JSONObject();
        JSONArray descs = new JSONArray();
        JSONObject descJ;

        String state=req.getParameter("state");
        Query.Filter descF =  new Query.FilterPredicate("state", Query.FilterOperator.EQUAL, state);
        Query q = new Query("Desc").setFilter(descF);
        PreparedQuery pq = datastore.prepare(q);
        for (Entity result : pq.asIterable()) {
            String desc = (String) result.getProperty("desc");
            String date = (String) result.getProperty("date");
            descJ = new JSONObject();
            descJ.put("desc", desc);
            descJ.put("date", date);
            descs.put(descJ);
        }
        json.put("descs", descs);
        String output = req.getParameter("callback") + "(" + json.toString() + ");";

        PrintWriter out = resp.getWriter();
        out.write(output);
        out.close();

    }

}
