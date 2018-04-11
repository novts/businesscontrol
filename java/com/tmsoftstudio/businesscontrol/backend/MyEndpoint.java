/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.tmsoftstudio.businesscontrol.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** An endpoint class we are exposing */
@Api(
  name = "myApi",
  version = "v1",
  namespace = @ApiNamespace(
    ownerDomain = "backend.businesscontrol.tmsoftstudio.com",
    ownerName = "backend.businesscontrol.tmsoftstudio.com",
    packagePath=""
  )
)
public class MyEndpoint {
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


    @ApiMethod(
            name = "setLocation",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public void setLocation(LocationBean req) {
        List<String> tasks=req.getTasks();
        for(String task:tasks){
            Query.Filter taskName =  new Query.FilterPredicate("name", Query.FilterOperator.EQUAL, task);
            Query q = new Query("Task").setFilter(taskName);
            PreparedQuery pq = datastore.prepare(q);
            for (Entity result : pq.asIterable()) {
                JSONObject json = new JSONObject();
                json.put("account",req.getAccount());
                json.put("date",req.getDate());
                json.put("lat",req.getLat());
                json.put("lng",req.getLng());
                if(!result.hasProperty("track")) {
                    result.setProperty("track", json.toString());
                    datastore.put(result);
                }else{
                    String track= (String) result.getProperty("track");
                    StringBuilder sb = new StringBuilder();
                    sb.append(track).append(";").append(json.toString());
                    result.setProperty("track", sb.toString());
                    datastore.put(result);
                }
                }
            }
        }


    @ApiMethod(
            name = "setAccount",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public MyBean setAccount(MyBean req) {
        String account=req.getAccount();
        Key key = KeyFactory.createKey("User", account);
        Entity user=null;
        try {
            user = datastore.get(key);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
        if (user==null) {
            user = new Entity("User", account);
            user.setProperty("Account", req.getAccount());
        }

        String dateEnter=req.getDateEnter();
        user.setProperty("dateEnter", dateEnter);

        datastore.put(user);

        return req;
    }

    @ApiMethod(
            name = "getTask",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public List<TaskBean> getTask(MyBean req) {
        List<TaskBean> list = new ArrayList<TaskBean>();
        String account=req.getAccount();
        Query.Filter taskState =  new Query.FilterPredicate("state", Query.FilterOperator.EQUAL, "new");
        Query q = new Query("Task").setFilter(taskState);
        PreparedQuery pq = datastore.prepare(q);
        TaskBean task;
        for (Entity result : pq.asIterable()) {
            String users = (String) result.getProperty("users");
            if(users.contains(account)) {
                task = new TaskBean();
                String date = (String) result.getProperty("date");
                task.setDate(date);
                String descs = (String) result.getProperty("descs");
                String desc=descs.replaceAll("\\[","")
                        .replaceAll("\\]","")
                        .replaceAll("\"","");
                task.setDescs(desc);
                String name = (String) result.getProperty("name");
                task.setName(name);
                String routes = (String) result.getProperty("routes");
                String route=routes.replaceAll("\\{","")
                        .replaceAll("\\}", "")
                        .replaceAll("\\[","")
                        .replaceAll("\\]","")
                        .replaceAll("\"","");
                task.setRoute(route);
                list.add(task);
            }
        }

        return list;
    }

    @ApiMethod(
            name = "setReport",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public ReportBean setReport(ReportBean req) {
        ReportBean res=new ReportBean();
        res.setState(false);
        List<String> tasks=req.getTasks();
        String account = req.getAccount();
        for(String task:tasks){
            Query.Filter taskName =  new Query.FilterPredicate("name", Query.FilterOperator.EQUAL, task);
            Query q = new Query("Task").setFilter(taskName);
            PreparedQuery pq = datastore.prepare(q);
            for (Entity result : pq.asIterable()) {

                JSONObject json = new JSONObject();

                List<String> images = req.getImages();
                if(images !=null) {
                    int k=0;
                    JSONObject jsonPhoto;
                    JSONArray jsonArray= new JSONArray();
                   for(String image:images) {
                        byte[] imageAsBytes = Base64.decodeBase64(image);

                        BlobstoreService blobService = BlobstoreServiceFactory.getBlobstoreService();

                       String imageName= null;
                       try {
                           imageName = URLEncoder.encode(task, "UTF-8")+account + "-" + k + ".jpeg";
                       } catch (UnsupportedEncodingException e) {
                           e.printStackTrace();
                       }

                       BlobInfoFactory bf = new BlobInfoFactory();
                       Iterator itr = bf.queryBlobInfos();
                       while (itr.hasNext()) {
                           BlobInfo bi = (BlobInfo) itr.next();
                           if (bi.getFilename().equals(imageName)) {
                               blobService.delete(bi.getBlobKey());
                           }
                       }

                        String uploadUrl = blobService.createUploadUrl("/blob/upload");
                        HttpEntity requestEntity = MultipartEntityBuilder.create()
                                .addBinaryBody("file", imageAsBytes, ContentType.create("image/jpg"), imageName)
                                .build();
                       k++;

                        try {
                            URL url = new URL(uploadUrl);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setDoOutput(true);
                            connection.setRequestMethod("POST");
                            connection.addRequestProperty("Content-length", requestEntity.getContentLength() + "");
                            connection.addRequestProperty(requestEntity.getContentType().getName(), requestEntity.getContentType().getValue());
                            requestEntity.writeTo(connection.getOutputStream());
                            String responseBody = IOUtils.toString(connection.getInputStream());

                            if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 400) {
                                throw new IOException("HTTP Status " + connection.getResponseCode() + ": " + connection.getHeaderFields() + "\n" + responseBody);
                            }

                            BlobKey blobKey = new BlobKey(responseBody);

                            ImagesService imagesService = ImagesServiceFactory.getImagesService();
                            ServingUrlOptions servingOptions = ServingUrlOptions.Builder.withBlobKey(blobKey);

                            String servingUrl = imagesService.getServingUrl(servingOptions);
                            jsonPhoto=new JSONObject();
                            jsonPhoto.put("servingUrl", servingUrl);
                            jsonPhoto.put("blobKey", responseBody);
                            jsonArray.put(jsonPhoto);


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    json.put("photos",jsonArray);
                }


                json.put("account",account);
                json.put("report",req.getReport());

                if(!result.hasProperty("reports")) {
                    Text reports = new Text(json.toString());
                    result.setProperty("reports", reports);
                    datastore.put(result);
                    res.setState(true);
                }else{
                    String reportsOld= ((Text)result.getProperty("reports")).getValue();
                    StringBuilder sb = new StringBuilder();
                    sb.append(reportsOld).append(";").append(json.toString());
                    Text reports = new Text(sb.toString());
                    result.setProperty("reports", reports);
                    datastore.put(result);
                    res.setState(true);
                }
            }
        }
        return res;
    }

}
