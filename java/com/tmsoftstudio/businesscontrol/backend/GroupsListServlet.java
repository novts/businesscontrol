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

public class GroupsListServlet extends HttpServlet {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            resp.setCharacterEncoding("utf8");
            resp.setContentType("application/json");
            JSONObject json = new JSONObject();
            JSONArray groups = new JSONArray();
            JSONObject groupJ;
            Query q = new Query("Group");
            PreparedQuery pq = datastore.prepare(q);
            for (Entity result : pq.asIterable()) {
                String groupP = (String) result.getProperty("group");
                groupJ = new JSONObject();
//-----------------------------------------------------------------------
               JSONArray users = new JSONArray();
                JSONObject user;
                Query.Filter groupF =  new Query.FilterPredicate("group", Query.FilterOperator.EQUAL, groupP);
                Query qu = new Query("User").setFilter(groupF);
                PreparedQuery pqu = datastore.prepare(qu);
                for (Entity res : pqu.asIterable()) {
                    String firstName = (String) res.getProperty("firstName");
                    String secondName = (String) res.getProperty("secondName");
                    String account = (String) res.getProperty("Account");
                    String phone = (String) res.getProperty("phone");
                    String position = (String) res.getProperty("position");
                    String groupU = (String) res.getProperty("group");
                    user = new JSONObject();
                    user.put("firstName", firstName);
                    user.put("secondName", secondName);
                    user.put("account", account);
                    user.put("phone", phone);
                    user.put("position", position);
                    user.put("group", groupU);
                    users.put(user);
                }

//---------------------------------------------------------------------
                groupJ.put(groupP, users);
                groups.put(groupJ);
            }
            json.put("groups", groups);
            String output = req.getParameter("callback") + "(" + json.toString() + ");";

            PrintWriter out = resp.getWriter();
            out.write(output);
            out.close();

        } catch (Exception e) {

        }
    }

}
