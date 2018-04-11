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

public class PutTaskServlet extends HttpServlet {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String date=req.getParameter("date");
        String users=req.getParameter("users");
        String descs=req.getParameter("descs");
        String routes=req.getParameter("routes");

        Entity task=new Entity("Task");
        task.setProperty("name", name);
        task.setProperty("date", date);
        task.setProperty("users", users);
        task.setProperty("descs", descs);
        task.setProperty("routes", routes);
        task.setProperty("state", "new");
        datastore.put(task);

        resp.setCharacterEncoding("utf8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String output = req.getParameter("callback") + "();";
        out.write(output);
        out.close();

    }

}
