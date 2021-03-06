package com.tmsoftstudio.businesscontrol.backend;


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PutRouteServlet extends HttpServlet {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String json = req.getParameter("json");
        String date=req.getParameter("date");
        String state=req.getParameter("state");

        Entity route=new Entity("Route");
        route.setProperty("route", json);
        route.setProperty("date", date);
        route.setProperty("state", state);
        datastore.put(route);

        resp.setCharacterEncoding("utf8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String output = req.getParameter("callback") + "();";
        out.write(output);
        out.close();

    }

}

