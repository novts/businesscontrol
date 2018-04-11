package com.tmsoftstudio.businesscontrol.backend;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BlobUploadServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        BlobstoreService blobService = BlobstoreServiceFactory.getBlobstoreService();
        List<BlobKey> blobs = blobService.getUploads(req).get("file");
        if(blobs == null || blobs.isEmpty()) throw new IllegalArgumentException("No blobs given");

        BlobKey blobKey = blobs.get(0);
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("text/plain");
        PrintWriter out = res.getWriter();
        out.print(blobKey.getKeyString());
        out.flush();
        out.close();
    }
}
