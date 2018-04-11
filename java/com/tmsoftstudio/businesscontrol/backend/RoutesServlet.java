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

public class RoutesServlet extends HttpServlet {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.setCharacterEncoding("utf8");
            resp.setContentType("application/json");
            JSONObject json = new JSONObject();
            JSONArray routes = new JSONArray();
            JSONObject routeJ;

            String state=req.getParameter("state");
            Query.Filter routeF =  new Query.FilterPredicate("state", Query.FilterOperator.EQUAL, state);
            Query q = new Query("Route").setFilter(routeF);
            PreparedQuery pq = datastore.prepare(q);
            for (Entity result : pq.asIterable()) {
                String route = (String) result.getProperty("route");
                String date = (String) result.getProperty("date");
                routeJ = new JSONObject();
                routeJ.put("route", new JSONArray(route));
                routeJ.put("date", date);
                routes.put(routeJ);
            }
            json.put("routes", routes);
            String output = req.getParameter("callback") + "(" + json.toString() + ");";

            PrintWriter out = resp.getWriter();
            out.write(output);
            out.close();

    }

}
