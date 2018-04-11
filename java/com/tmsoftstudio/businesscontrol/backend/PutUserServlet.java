package com.tmsoftstudio.businesscontrol.backend;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PutUserServlet extends HttpServlet {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String account = req.getParameter("account");
        String firstName = req.getParameter("firstName");
        String secondName = req.getParameter("secondName");
        String phone = req.getParameter("phone");
        String position = req.getParameter("position");
        String group = req.getParameter("group");

        Key key = KeyFactory.createKey("Group", group);
        Entity groupE=null;
        try {
            groupE = datastore.get(key);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
        if (groupE==null) {
            groupE = new Entity("Group", group);
        }

        groupE.setProperty("group", group);

        datastore.put(groupE);

        key = KeyFactory.createKey("User", account);
        try {
            Entity user = datastore.get(key);
            user.setProperty("firstName", firstName);
            user.setProperty("secondName", secondName);
            user.setProperty("phone", phone);
            user.setProperty("position", position);
            user.setProperty("group", group);
            datastore.put(user);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        resp.setCharacterEncoding("utf8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String output = req.getParameter("callback") + "();";
        out.write(output);
        out.close();

    }

}

