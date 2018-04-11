package com.tmsoftstudio.businesscontrol.backend;


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PutDescStateServlet extends HttpServlet {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String date=req.getParameter("date");
        String state=req.getParameter("state");

        Query.Filter descF =  new Query.FilterPredicate("date", Query.FilterOperator.EQUAL, date);
        Query q = new Query("Desc").setFilter(descF);
        PreparedQuery pq = datastore.prepare(q);
        for (Entity result : pq.asIterable()) {
            result.setProperty("state", state);
            datastore.put(result);
        }

        resp.setCharacterEncoding("utf8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String output = req.getParameter("callback") + "();";
        out.write(output);
        out.close();

    }

}

