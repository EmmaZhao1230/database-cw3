package uk.ac.bris.cs.databases.cwk3;

import java.sql.Connection;
import java.util.*;
import uk.ac.bris.cs.databases.api.APIProvider;
import uk.ac.bris.cs.databases.api.AdvancedForumSummaryView;
import uk.ac.bris.cs.databases.api.AdvancedForumView;
import uk.ac.bris.cs.databases.api.ForumSummaryView;
import uk.ac.bris.cs.databases.api.ForumView;
import uk.ac.bris.cs.databases.api.AdvancedPersonView;
import uk.ac.bris.cs.databases.api.PostView;
import uk.ac.bris.cs.databases.api.Result;
import uk.ac.bris.cs.databases.api.PersonView;
import uk.ac.bris.cs.databases.api.SimpleForumSummaryView;
import uk.ac.bris.cs.databases.api.SimpleTopicView;
import uk.ac.bris.cs.databases.api.TopicView;

import uk.ac.bris.cs.databases.api.SimplePostView;
import uk.ac.bris.cs.databases.api.SimpleTopicSummaryView;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author csxdb
 */
public class API implements APIProvider {

    private final Connection c;

    public API(Connection c) {
        this.c = c;
    }


     // @ Writed by All
     // First Checked by Luping Yu: Correct
    @Override
    public Result<Map<String, String>> getUsers() {
      if (c == null) { throw new IllegalStateException(); }
      Map<String, String> map = new HashMap<String, String>();

      try (PreparedStatement p = c.prepareStatement(
      "SELECT username, name FROM Person")) {
         ResultSet r = p.executeQuery();
         while (r.next()) {
            map.put(r.getString("username"), r.getString("name"));
         }
         return Result.success(map);
      } catch (SQLException e) {
         return Result.fatal("Something bad happened: " + e);
      }
    }
    /**
     * Get a list of all users in the system as a map username -> name.
     * @return A map with one entry per user of the form username -> name
     * (note that usernames are unique).
     */


     // @ Writed by Emma
     // First Checked by Luping Yu: Correct
    @Override
    public Result<PersonView> getPersonView(String username) {
      if (c == null) { throw new IllegalStateException(); }
      if (username == null || username.equals("")) {
         return Result.failure("Need a valid username");
      }

      try (PreparedStatement p = c.prepareStatement(
      "SELECT name, username, stuID FROM Person WHERE username = ?")) {
         p.setString(1, username);
         ResultSet r = p.executeQuery();
         if (r.next()) {
            PersonView pv = new PersonView(r.getString("name"), r.getString("username"), r.getString("stuID"));
            return Result.success(pv);
         } else {
            return Result.failure("No user with this username");
         }
      } catch (SQLException e) {
         return Result.fatal("Something bad happend: " + e);
      }
    }
    /**
     * Get a PersonView for the person with the given username.
     * @param username - the username to search for, cannot be empty.
     * @return If a person with the given username exists, a fully populated
     * PersonView. Otherwise, failure (or fatal on a database error).
     */


   // @ Writed by Khas
   // First Checked by Luping Yu: Correct
    @Override
    public Result<List<SimpleForumSummaryView>> getSimpleForums() {
      if (c == null) { throw new IllegalStateException(); }
      List<SimpleForumSummaryView> list = new LinkedList<>();
      // Why not ArrayList? Because LinkedList is more faster for insert objects.
      // ArrayList is more faster for traverse, but we won't traverse in this case.

      try (PreparedStatement p = c.prepareStatement(
      "SELECT id, title FROM Forum ORDER BY title ASC")) {
         ResultSet r = p.executeQuery();
         while (r.next()) {
            SimpleForumSummaryView sfsv = new SimpleForumSummaryView (r.getLong("id"), r.getString("title"));
            // int -> long, implicit conversion
            list.add(sfsv);
         }
         return Result.success(list);
      } catch (SQLException e) {
         return Result.fatal("Something bad happened: " + e);
      }
    }
    /**
     * Get the "main page" containing a list of forums ordered alphabetically
     * by title. Simple version that does not return any topic information.
     * @return the list of all forums; an empty list if there are no forums.
     */


     // @ Writed by Khas
     // First Checked by Luping Yu: SQL Correct
    @Override
    public Result<Integer> countPostsInTopic(long topicId) {
      if (c == null) { throw new IllegalStateException(); }

      try (PreparedStatement p = c.prepareStatement(
      "SELECT COUNT(id) AS count FROM Post WHERE topic = ?")) {
         p.setLong(1, topicId);
         // long -> int, constraint conversion "int(topicID)"
         ResultSet r = p.executeQuery();
         if (r.next()) {
            int count = r.getInt("count");
            return Result.success(count);
         } else {
            return Result.failure("No topic with this id");
         }
      } catch (SQLException e) {
         return Result.fatal("Something bad happened: " + e);
      }
    }
    /**
     * Count the number of posts in a topic (without fetching them all).
     * @param topicId - the topic to look at.
     * @return The number of posts in this topic if it exists, otherwise a
     * failure.
     */


     // @ Writed by Luping Yu
     // First Checked by Luping Yu: SQL Correct
    @Override
    public Result<List<PersonView>> getLikers(long topicId) {
      if (c == null) { throw new IllegalStateException(); }
      if (!existTable(topicId)) return Result.failure("No Topic with this id");

      List<PersonView> list = new LinkedList<>();

      try (PreparedStatement p = c.prepareStatement(
      "SELECT name, username, stuID FROM Person INNER JOIN LikeTopic ON (id = person) " +
      "WHERE topic = ? ORDER BY name ASC")) {
         p.setLong(1, topicId);
         ResultSet r = p.executeQuery();
         while (r.next()) {
            PersonView pv = new PersonView(r.getString("name"), r.getString("username"), r.getString("stuID"));
            list.add(pv);
         }
         return Result.success(list);
      } catch (SQLException e) {
         return Result.fatal("Something bad happened: " + e);
      }
   }
    /**
     * Get all people who have liked a particular topic, ordered by name
     * alphabetically.
     * @param topicId The topic id. Must exist.
     * @return Success (even if the list is empty) if the topic exists,
     * failure if it does not, fatal in case of database errors.
     */


     // @ Writed by Luping Yu
     // First Checked by Luping Yu: Correct
    @Override
    public Result<SimpleTopicView> getSimpleTopic(long topicId) {
      if (c == null) { throw new IllegalStateException(); }
      List<SimplePostView> list = new LinkedList<>();

      try (PreparedStatement p1 = c.prepareStatement(
      "SELECT title FROM Topic WHERE id = ?")) {
         p1.setLong(1, topicId);
         ResultSet r1 = p1.executeQuery();
         if (r1.next()) {
            try (PreparedStatement p2 = c.prepareStatement(
            "SELECT num, name, content, created FROM Post INNER JOIN Person ON (Person.id = author) WHERE topic = ?")) {
               p2.setLong(1, topicId);
               ResultSet r2 = p2.executeQuery();
               while (r2.next()) {
                  SimplePostView spv = new SimplePostView(r2.getInt("num"), r2.getString("name"), r2.getString("content"), r2.getInt("created"));
                  list.add(spv);
               }
               SimpleTopicView stv = new SimpleTopicView(topicId, r1.getString("title"), list);
               return Result.success(stv);
            } catch (SQLException e) {
               return Result.fatal("Something bad happened: " + e);
            }
         }
         else return Result.failure("No Topic with this id");
      } catch (SQLException e) {
         return Result.fatal("Something bad happened: " + e);
      }
    }
    /**
     * Get a simplified view of a topic.
     * @param topicId - the topic to get.
     * @return The topic view if one exists with the given id,
     * otherwise failure or fatal on database errors.
     */


     // @ Writed by Luping Yu
     // First Checked by Luping Yu: SQL Correct
    @Override
    public Result<PostView> getLatestPost(long topicId) {
      if (c == null) { throw new IllegalStateException(); }
      if (!existTable(topicId)) return Result.failure("No Topic with this id");

      try (PreparedStatement p = c.prepareStatement(
      "SELECT forum, topic, Post.num, name, username, content, Post.created" +
      "FROM Post INNER JOIN Topic ON (Post.topic = Topic.id)" +
      "INNER JOIN Person ON (Post.author = Person.id)" +
      "WHERE topic = ? ORDER BY Post.created DESC LIMIT 0,1")) {
         p.setLong(1, topicId);
         ResultSet r = p.executeQuery();
         if (r.next()) {
            PostView pv = new PostView(r.getLong("forum"), r.getLong("topic"),
            r.getInt("Post.num"), r.getString("name"), r.getString("username"),
            r.getString("content"), r.getInt("Post.created"), likes(r.getInt("Post.num"), topicId));
            return Result.success(pv);
         }
         else {
            return Result.failure("No Post in this topic");
         }
      } catch (SQLException e) {
         return Result.fatal("Something bad happened: " + e);
      }
    }
    /**
     * Get the latest post in a topic.
     * @param topicId The topic. Must exist.
     * @return Success and a view of the latest post if one exists,
     * failure if the topic does not exist, fatal on database errors.
     */


     // @ Writed by Luping Yu
     // First Checked by Luping Yu: Correct
    @Override
    public Result<List<ForumSummaryView>> getForums() {
      if (c == null) { throw new IllegalStateException(); }
      List<ForumSummaryView> list = new LinkedList<>();

      try (PreparedStatement p = c.prepareStatement(
      "SELECT id, title, tid, ttitle FROM Forum INNRE JOIN " +
      "(SELECT id AS tid, title AS ttitle, forum, max(created) FROM Topic GROUP BY forum) " +
      "ON (forum = id) ORDER BY title ASC")) {
         ResultSet r = p.executeQuery();
         while (r.next()) {
            SimpleTopicSummaryView stsv = new SimpleTopicSummaryView(r.getLong("tid"), r.getLong("id"), r.getString("ttitle"));
            ForumSummaryView fsv = new ForumSummaryView(r.getLong("id"), r.getString("title"), stsv);
            list.add(fsv);
         }
         return Result.success(list);
      } catch (SQLException e) {
         //throw new RuntimeException(e);
         return Result.fatal("Something bad happened: " + e);
      }
    }
    /**
     * Get the "main page" containing a list of forums ordered alphabetically
     * by title.
     * @return the list of all forums, empty list if there are none.
     */


     // @ Writed by Emma
   @Override
    public Result createForum(String title) {
      if (title == null || title.equals("")) {
        return Result.failure("Need a valid title");
      }
      final String SQL1 = "SELECT * FROM forum WHERE title = ?";
      try (PreparedStatement p = c.prepareStatement(SQL1)) {
           p.setString(1, title);
           ResultSet r = p.executeQuery();
           if (r.next()) {
                   return Result.failure("title is duplicated");
           }
      } catch (SQLException e) {
           return Result.fatal("Something bad happened: " + e);
      }
      final String SQL2 = "INSERT INTO forum (title) VALUES (?)";
      try (PreparedStatement p = c.prepareStatement(SQL2)) {
           p.setString(1, title);
           int iResult=p.executeUpdate();
           if(iResult==0){
              return Result.failure("insert  is failied ");
           }
           else
              return Result.success();

      } catch (SQLException e) {
           return Result.fatal("Something bad happened: " + e);
      }

    }
    /**
     * Create a new forum.
     * @param title - the title of the forum. Must not be null or empty and
     * no forum with this name must exist yet.
     * @return success if the forum was created, failure if the title was
     * null, empty or such a forum already existed; fatal on other errors.
     */


     // @ Writed by Emma
   @Override
    public Result createPost(long topicId, String username, String text) {
      if (text == null || text.equals("")) {
      return Result.failure("Need a valid text");
     }
     final String SQL1 = "SELECT * FROM topic WHERE topicId = ?";
     try (PreparedStatement p = c.prepareStatement(SQL1)) {
           p.setInt(1, (int)topicId);
           ResultSet r = p.executeQuery();
           if (!r.next()) {
              return Result.failure("Topic ID does not exist!");
           }
     } catch (SQLException e) {
           return Result.fatal("Something bad happened: " + e);
     }
     final String SQL2 = "SELECT * FROM person WHERE username = ?";
     try (PreparedStatement p = c.prepareStatement(SQL2)) {
           p.setString(1, username);
           ResultSet r = p.executeQuery();
           if (!r.next()) {
              return Result.failure("username does not exist!");
           }
     } catch (SQLException e) {
           return Result.fatal("Something bad happened: " + e);
     }
     final String SQL3 = "INSERT INTO post( topic,author,posttext,created) VALUES ( ?,?,?,?)";
     try (PreparedStatement p = c.prepareStatement(SQL3)) {
           p.setInt(1, (int)topicId);
           p.setString(2,username);
           p.setString(3,text);
           java.util.Date date=new java.util.Date();
           p.setInt(4,(int)date.getTime());

           int iResult = p.executeUpdate();
           if (iResult==0) {
              return Result.failure("Can not insert a post!");
           }
           else {
              return Result.success();
           }
     } catch (SQLException e) {
           return Result.fatal("Something bad happened: " + e);
     }
    }
    /**
     * Create a post in an existing topic.
     * @param topicId - the id of the topic to post in. Must refer to
     * an existing topic.
     * @param username - the name under which to post; user must exist.
     * @param text - the content of the post, cannot be empty.
     * @return success if the post was made, failure if any of the preconditions
     * were not met and fatal if something else went wrong.
     */


     // @ Writed by Emma
     @Override
    public Result addNewPerson(String name, String username, String studentId) {
      if (name == null || name.equals("")) {
              return Result.failure("Need a valid name");
         }
         if (username == null || username.equals("")) {
              return Result.failure("Need a valid username");
         }
         if (studentId != null && studentId.equals("")) {
              return Result.failure("Need a valid studentID");
         }
         final String SQL1 = "SELECT * FROM person WHERE username = ?";
         try (PreparedStatement p = c.prepareStatement(SQL1)) {
              p.setString(1, username);
              ResultSet r = p.executeQuery();
              if (r.next()) {
                 return Result.failure("username duplicates!");
              }
         } catch (SQLException e) {
              return Result.fatal("Something bad happened: " + e);
         }


         if(studentId==null) {
              final String SQL3 = "INSERT INTO person ( name,username) VALUES ( ?,?)";
              try (PreparedStatement p = c.prepareStatement(SQL3)) {
                 p.setString(1, name);
                 p.setString(2, username);
                 int iResult = p.executeUpdate();
                 if (iResult == 0) {
                      return Result.failure("Can not insert a person!");
                 } else {
                      return Result.success();
                 }
              } catch (SQLException e) {
                 return Result.fatal("Something bad happened: " + e);
              }
         }
         else
         {
              final String SQL3 = "INSERT INTO person ( name,username,stuID) VALUES ( ?,?,?)";
              try (PreparedStatement p = c.prepareStatement(SQL3)) {
                 p.setString(1, name);
                 p.setString(2, username);
                 p.setString(3, studentId);
                 int iResult = p.executeUpdate();
                 if (iResult == 0) {
                      return Result.failure("Can not insert a person!");
                 } else {
                      return Result.success();
                 }
              } catch (SQLException e) {
                 return Result.fatal("Something bad happened: " + e);
              }
         }
    }
    /**
     * Create a new person.
     * @param name - the person's name, cannot be empty.
     * @param username - the person's username, cannot be empty.
     * @param studentId - the person's student id. May be either NULL if the
     * person is not a student or a non-empty string if they are; can not be
     * an empty string.
     * @return Success if no person with this username existed yet and a new
     * one was created, failure if a person with this username already exists,
     * fatal if something else went wrong.
     */


     // @ Writed by Luping Yu
     // First Checked by Luping Yu: Correct
    @Override
    public Result<ForumView> getForum(long id) {
      if (c == null) { throw new IllegalStateException(); }
      List<SimpleTopicSummaryView> list = new LinkedList<>();

      try (PreparedStatement p1 = c.prepareStatement(
      "SELECT title FROM Forum WHERE id = ?")) {
         p1.setLong(1, id);
         ResultSet r1 = p1.executeQuery();
         if (r1.next()) {
            try (PreparedStatement p2 = c.prepareStatement(
            "SELECT id, forum, title FROM Topic WHERE forum = ?")) {
               p2.setLong(1, id);
               ResultSet r2 = p2.executeQuery();
               while (r2.next()) {
                  SimpleTopicSummaryView stsv = new SimpleTopicSummaryView(r2.getLong("id"), r2.getLong("forum"), r2.getString("title"));
                  list.add(stsv);
               }
               ForumView fv = new ForumView(id, r1.getString("title"), list);
               return Result.success(fv);
            } catch (SQLException e) {
               return Result.fatal("Something bad happened: " + e);
            }
         }
         else return Result.failure("No Forum with this id");
      } catch (SQLException e) {
         return Result.fatal("Something bad happened: " + e);
      }
   }
    /**
     * Get the detailed view of a single forum.
     * @param id - the id of the forum to get.
     * @return A view of this forum if it exists, otherwise failure.
     */


     // @ Writed by Luping Yu
     // First Checked by Luping Yu: Correct
    @Override
    public Result<TopicView> getTopic(long topicId, int page) {
      if (c == null) { throw new IllegalStateException(); }
      List<PostView> list = new LinkedList<>();

      try (PreparedStatement p1 = c.prepareStatement(
      "SELECT forum, Forum.title AS ftitle, Topic.title AS ttitle " +
      "FROM Topic INNER JOIN Forum ON (forum = Forum.id) WHERE Topic.id = ?")) {
         p1.setLong(1, topicId);
         ResultSet r1 = p1.executeQuery();
         if (r1.next()) {
            try (PreparedStatement p2 = c.prepareStatement(
            "SELECT num, name, username, content, created " +
            "FROM Post INNER JOIN Person ON (author = Person.id) " +
            "WHERE topic = ? AND num >= ? AND num <= ? ORDER BY num ASC")) {
               p2.setLong(1,topicId);
               p2.setLong(2,10*(page-1)+1);
               if (page != 0) p2.setLong(3,10*page);
               else p2.setLong(3,10000);
               // In our case, we regards that 100000 is the max posts in a topic
               ResultSet r2 = p2.executeQuery();
               while (r2.next()) {
                     PostView pv = new PostView(r1.getLong("forum"), topicId,
                     r2.getInt("num"), r2.getString("name"), r2.getString("username"),
                     r2.getString("content"), r2.getInt("created"), likes(r2.getInt("num"), topicId));
                     list.add(pv);
               }
               if (list.size() > 0) {
                  TopicView tv = new TopicView(r1.getLong("forum"), topicId, r1.getString("ftitle"), r1.getString("ttitle"), list, page);
                  return Result.success(tv);
               }
               else return Result.failure("No Post in appointed range");
            } catch (SQLException e) {
               return Result.fatal("Something bad happened: " + e);
            }
         }
         else return Result.failure("No Topic with this id");
      } catch (SQLException e) {
         return Result.fatal("Something bad happened: " + e);
      }
    }
    /**
     * Get the detailed view of a topic.
     * @param topicId - the topic to get.
     * @param page - if 0, fetch all posts, if n > 0, fetch posts
     * 10*(n-1)+1 up to 10*n, where the first post is number 1.
     * @return The topic view if one exists with the given id and range,
     * (i.e. for getTopic(tid, 3) there must be at least 31 posts)
     * otherwise failure (or fatal on database errors).
     */


     // @ Writed by Khas
    @Override
    public Result likeTopic(String username, long topicId, boolean like) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
    /**
     * Like or unlike a topic. A topic is either liked or not, when calling this
     * twice in a row with the same parameters, the second call is a no-op (this
     * function is idempotent).
     * @param username - the person liking the topic (must exist).
     * @param topicId - the topic to like (must exist).
     * @param like - true to like, false to unlike.
     * @return success (even if it was a no-op), failure if the person or topic
     * does not exist and fatal in case of db errors.
     */


     // @ Writed by Khas
    @Override
    public Result favouriteTopic(String username, long topicId, boolean fav) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
    /**
     * Set or unset a topic as favourite. Same semantics as likeTopic.
     * @param username - the person setting the favourite topic (must exist).
     * @param topicId - the topic to set as favourite (must exist).
     * @param fav - true to set, false to unset as favourite.
     * @return success (even if it was a no-op), failure if the person or topic
     * does not exist and fatal in case of db errors.
     */


    @Override
    public Result createTopic(long forumId, String username, String title, String text) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public Result<List<AdvancedForumSummaryView>> getAdvancedForums() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public Result<AdvancedPersonView> getAdvancedPersonView(String username) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public Result<AdvancedForumView> getAdvancedForum(long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
    public Result likePost(String username, long topicId, int post, boolean like) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // This methods used for check if there are topic exists with this topicID
    // @ Writed by Luping Yu
    private Boolean existTable(long topicId) {
       try (PreparedStatement p = c.prepareStatement(
       "SELECT * FROM Topic WHERE id = ?")) {
          p.setLong(1, topicId);
          ResultSet r = p.executeQuery();
          if(r.next()) return true;
          else return false;
       } catch (SQLException e) {return false;}
    }

    // This method object used for count likes of a specific post
    // @ Writed by Luping Yu
    private int likes(int postNum, long topicId) {
      try (PreparedStatement p = c.prepareStatement(
      "SELECT count(*) AS likes FROM LikePost WHERE postnum = ? AND postopic = ?")) {
         p.setLong(1, postNum);
         p.setLong(2, topicId);
         ResultSet r = p.executeQuery();
         if (r.next()) return r.getInt("likes");
         else return 0;
      } catch (SQLException e) {return 0;}
   }

   }
