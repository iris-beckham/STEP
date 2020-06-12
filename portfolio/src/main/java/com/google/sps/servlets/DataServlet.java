// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@WebServlet("/data")
public class DataServlet extends HttpServlet {
  
  private class Comment {
    long id;
    String name;
    String comment;
    long timestamp;
  private Comment(long id, String name, String comment, long timestamp) {
    this.id = id;
    this.name = name;
    this.comment = comment;
    this.timestamp = timestamp;
    }
  }

  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    response.setContentType("text/html");
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);

      response.getWriter().println("<p>Hello " + userEmail + "!</p>");
      response.getWriter().println("<p>Logout <a href=\"" + logoutUrl + "\">here</a>.</p>");
    } else {
      String urlToRedirectToAfterUserLogsIn = "/";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);

      response.getWriter().println("<p>Hello stranger.</p>");
      response.getWriter().println("<p>Login <a href=\"" + loginUrl + "\">here</a>.</p>");
    }
    
    
    
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String name = (String) entity.getProperty("name");
      String comment = (String) entity.getProperty("comment");
      long timestamp = (long) entity.getProperty("timestamp");

      Comment commentObject = new Comment(id, name, comment, timestamp);
      comments.add(commentObject);
    }

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));

  }

 @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String id = request.getParameter("id");
    String name = request.getParameter("name");
    String commentString = request.getParameter("comment");
    long timestamp = System.currentTimeMillis();
    String email = userService.getCurrentUser().getEmail();


    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("id", id);
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("comment", commentString);
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("email", email);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
    response.sendRedirect("/index.html"); 
  }

}
