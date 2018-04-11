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

public class UsersListServlet extends HttpServlet {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            resp.setCharacterEncoding("utf8");
            resp.setContentType("application/json");
            JSONObject json = new JSONObject();
            JSONArray users = new JSONArray();
            JSONObject user;
            Query q = new Query("User");
            PreparedQuery pq = datastore.prepare(q);
            for (Entity result : pq.asIterable()) {
                String firstName = (String) result.getProperty("firstName");
                String secondName = (String) result.getProperty("secondName");
                String account = (String) result.getProperty("Account");
                String phone = (String) result.getProperty("phone");
                String position = (String) result.getProperty("position");
                String group = (String) result.getProperty("group");
                user = new JSONObject();
                user.put("firstName", firstName);
                user.put("secondName", secondName);
                user.put("account", account);
                user.put("phone", phone);
                user.put("position", position);
                user.put("group", group);
                users.put(user);
            }
            json.put("users", users);
            PrintWriter out = resp.getWriter();
            String output = req.getParameter("callback") + "(" + json.toString() + ");";
            out.write(output);
            out.close();

        } catch (Exception e) {

        }
    }

}
