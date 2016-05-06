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
import uk.ac.bris.cs.databases.api.TopicSummaryView;
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

     // @ Written by All
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


     // @ Written by Fan Zhao
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


   // @ Written by Khas_Ochir Sod-Erdene
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


     // @ Written by Khas_Ochir Sod-Erdene
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


     // @ Written by Luping Yu
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


     // @ Written by Luping Yu
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


     // @ Written by Luping Yu
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


     // @ Written by Luping Yu
     // First Checked by Luping Yu: Correct
    @Override
    public Result<List<ForumSummaryView>> getForums() {
      if (c == null) { throw new IllegalStateException(); }
      List<ForumSummaryView> list = new LinkedList<>();
      SimpleTopicSummaryView stsv = null;

      try (PreparedStatement p1 = c.prepareStatement(
      "SELECT id, title FROM Forum")) {
         ResultSet r1 = p1.executeQuery();
         while (r1.next()) {
            try (PreparedStatement p2 = c.prepareStatement(
            "SELECT id, forum, title, max(created) FROM Topic WHERE forum = ? GROUP BY forum")) {
               p2.setLong(1, r1.getLong("id"));
               ResultSet r2 = p2.executeQuery();
               if (r2.next()) {
                  stsv = new SimpleTopicSummaryView(r2.getLong("id"), r2.getLong("forum"), r2.getString("title"));
               }
            }
            ForumSummaryView fsv = new ForumSummaryView(r1.getLong("id"), r1.getString("title"), stsv);
            list.add(fsv);
         }
         return Result.success(list);
      } catch (SQLException e) {
         return Result.fatal("Something bad happened: " + e);
      }
   }


     // @ Written by Fan Zhao
     // Checked by Fan Zhao: Correct
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
            else {
                c.commit();
                return Result.success();
            }
       } catch (SQLException e) {
            return Result.fatal("Something bad happened: " + e);
       }
     }


     // @ Written by Fan Zhao
     // Checked by Fan Zhao: Correct
    @Override
    public Result createPost(long topicId, String username, String text) {
      if (text == null || text.equals("")) {
      return Result.failure("Need a valid text");
     }
        Result<Boolean> result=existATopic((int)topicId);
        if(!result.isSuccess())
            return result;
        if(!result.getValue().booleanValue())
            return Result.failure("topic does not exist!");
        result=existAPerson(username);
        if(!result.isSuccess() )
            return result;
        if(!result.getValue().booleanValue())
            return Result.failure("username does not exist!");
        Result<Integer> count= countPostsInTopic_2((int)topicId);
        if(!count.isSuccess())
           return count;
        final String SQL3 = "INSERT INTO post( num,topic,author,content,created) select ?,?, id ,?, ? from person where person.username=?";
     try (PreparedStatement p = c.prepareStatement(SQL3)) {
         int num=count.getValue().intValue();
           p.setInt(1,++num);
           p.setInt(2, (int)topicId);
           p.setString(3,text);
          // the count of ms since 1970-1-1 0:0:0
           java.util.Date date=new java.util.Date();
           p.setLong(4,date.getTime());
           p.setString(5,username);
           int iResult = p.executeUpdate();
           if (iResult==0) {
              return Result.failure("Can not insert a post!");
           }
           else {
               c.commit();
              return Result.success();
           }
     } catch (SQLException e) {
           return Result.fatal("Something bad happened: " + e);
     }
    }

     // @ Written by Fan Zhao
     // Checked by Fan Zhao: Correct
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
         Result<Boolean> result=existAPerson(username);
         if(!result.isSuccess())
             return result;
         if(result.getValue().booleanValue())
             return Result.failure("username duplicates!");
          if(studentId==null) {
               final String SQL3 = "INSERT INTO person ( name,username) VALUES ( ?,?)";
               try (PreparedStatement p = c.prepareStatement(SQL3)) {
                  p.setString(1, name);
                  p.setString(2, username);
                  int iResult = p.executeUpdate();
                  if (iResult == 0) {
                       return Result.failure("Can not insert a person!");
                  } else {
                      c.commit();
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
                      c.commit();
                       return Result.success();
                  }
               } catch (SQLException e) {
                  return Result.fatal("Something bad happened: " + e);
               }
          }
     }


     // @ Written by Luping Yu
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


     // @ Written by Luping Yu
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


     // @ Written by Khas_Ochir Sod-Erdene
     @Override
     public Result likeTopic(String username, long topicId, boolean like) {
         if (c == null) {
             throw new IllegalStateException();
         }
         if (!existTable(topicId)) return Result.failure("No topic with this id");
         if(!getPersonView(username).isSuccess()) return Result.failure("No user with this username");
         if (like) {
             try (PreparedStatement p = c.prepareStatement("INSERT OR IGNORE INTO LikeTopic(person,topic) Values(?, ?)")) {
                 p.setLong(2, topicId);
                 p.setString(1, username);
                 p.execute();
                 c.commit();
             }
             catch (SQLException e) {
                 try {
                     c.rollback();
                 } catch (SQLException e1) {
                     return Result.fatal("Error near rollback");
                 }
                 return Result.fatal("Something bad happened: " + e);
             }
         }
         else{
             try (PreparedStatement p = c.prepareStatement("DELETE FROM LikeTopic WHERE person=? and topic=?")) {
                 p.setLong(2, topicId);
                 p.setString(1, username);
                  p.execute();
                 c.commit();
             }
             catch (SQLException e) {
                 try {
                     c.rollback();
                 } catch (SQLException e1) {
                     return Result.fatal("Error near rollback");
                 }
                 return Result.fatal("Something bad happened: " + e);
             }
         }
         return Result.success();
     }


     // @ Written by Khas_Ochir Sod-Erdene
     @Override
      public Result favouriteTopic(String username, long topicId, boolean fav) {
              if (c == null) {
                  throw new IllegalStateException();
              }
              if (!existTable(topicId)) return Result.failure("No topic with this id");
              if(!getPersonView(username).isSuccess()) return Result.failure("No user with this username");
              if (fav) {
                  try (PreparedStatement p = c.prepareStatement("INSERT OR IGNORE INTO FavTopic(person,topic) Values(?, ?)")) {
                      p.setLong(2, topicId);
                      p.setString(1, username);
                      p.execute();
                      c.commit();
                  }
                  catch (SQLException e) {
                      try {
                          c.rollback();
                      } catch (SQLException e1) {
                          return Result.fatal("Error near rollback");
                      }
                      return Result.fatal("Something bad happened: " + e);
                  }
              }
              else{
                  try (PreparedStatement p = c.prepareStatement("DELETE FROM FavTopic WHERE person=? and topic=?")) {
                      p.setLong(2, topicId);
                      p.setString(1, username);
                      p.execute();
                      c.commit();
                  }
                  catch (SQLException e) {
                      try {
                          c.rollback();
                      } catch (SQLException e1) {
                          return Result.fatal("Error near rollback");
                      }
                      return Result.fatal("Something bad happened: " + e);
                  }
              }
              return Result.success();
          }


     // @ Written by Fan Zhao
     // Checked by Fan Zhao: Correct
     @Override
     public Result createTopic(long forumId, String username, String title, String text) {

         Result<Boolean> result=existAPerson(username);
         if(!result.isSuccess())
             return result;
         if(!result.getValue().booleanValue())
             return Result.failure(" can not find username!");
         result=existAForum((int)forumId);
         if(!result.isSuccess())
             return result;
         if( !result.getValue().booleanValue())
             return Result.failure(" can not find forumId!");
         if (title == null || title.equals("")) {
             return Result.failure("title should not be empty");
         }
         if (text == null || text.equals("")) {
             return Result.failure("text should not be empty");
         }

         final String SQL3 = "INSERT INTO Topic ( title, topictext ,forum,creator, created) VALUES ( ?,?,?,?,?)";
         try (PreparedStatement p = c.prepareStatement(SQL3)) {
             p.setString(1, title);
             p.setString(2, text);
             p.setInt(3, (int)forumId);
             p.setString(4,username);
             java.util.Date date=new java.util.Date();
             p.setInt(5,(int)date.getTime());
             int iResult = p.executeUpdate();
             if (iResult == 0) {
                 return Result.failure("Can not insert a Topic!");
             } else {
                 c.commit();
                 return Result.success();
             }
         } catch (SQLException e) {
             return Result.fatal("Something bad happened: " + e);
         }
     }


     // @ Written by Fan Zhao
     // @ Reconstructed by Luping Yu
    @Override
    public Result<List<AdvancedForumSummaryView>> getAdvancedForums() {
      if (c == null) { throw new IllegalStateException(); }
      List<AdvancedForumSummaryView> list = new LinkedList<>();

      try (PreparedStatement p = c.prepareStatement(
      "SELECT Forum.id AS fid, Forum.title AS ftitle, Topic.id AS tid, " +
      "Topic.title AS ttitle, created, ptime, pcount, pauthor, name, username " +
      "FROM Forum LEFT OUTER JOIN Topic ON (forum = Forum.id) " +
      "LEFT OUTER JOIN " +
      "(SELECT topic AS ptopic, name AS pauthor, count(*) AS pcount, max(created) AS ptime " +
      "FROM Post INNER JOIN Person ON (author = id) GROUP BY topic) " +
      "ON (Topic.id = ptopic) INNER JOIN Person ON (creator = Person.id)")) {
         ResultSet r = p.executeQuery();
         while (r.next()) {
            TopicSummaryView tsv = new TopicSummaryView(r.getLong("tid"),
            r.getLong("fid"), r.getString("ttitle"), r.getInt("pcount"),
            r.getInt("created"), r.getInt("ptime"), r.getString("pauthor"),
            likesCount(r.getLong("tid"), "Topic"), r.getString("name"), r.getString("username"));
            AdvancedForumSummaryView afsv = new AdvancedForumSummaryView(r.getLong("fid"),
            r.getString("ftitle"), tsv);
            list.add(afsv);
         }
         return Result.success(list);
      } catch (SQLException e) {
         return Result.fatal("Something bad happened: " + e);
      }
    }
    
    
     // @ Written by Luping Yu
     // Checked by Luping Yu
    @Override
    public Result<AdvancedPersonView> getAdvancedPersonView(String username) {
      if (c == null) { throw new IllegalStateException(); }
      if (username == null || username.equals("")) {
         return Result.failure("Need a valid username");
      }
      List<TopicSummaryView> list = new LinkedList<>();

      try (PreparedStatement p1 = c.prepareStatement(
      "SELECT id, name, username, stuId FROM Person WHERE username = ?")) {
         p1.setString(1, username);
         ResultSet r1 = p1.executeQuery();
         if (r1.next()) {
            try (PreparedStatement p2 = c.prepareStatement(
            "SELECT Topic.id AS tid, forum, title, created, ptime, pcount, pauthor, name AS pname, username AS pusername " +
            "FROM FavTopic INNER JOIN Topic ON (topic = Topic.id) LEFT OUTER JOIN " +
            "(SELECT topic AS ptopic, name AS pauthor, count(*) AS pcount, max(created) AS ptime " +
            "FROM Post INNER JOIN Person ON (author = id) GROUP BY topic) " +
            "ON (Topic.id = ptopic) INNER JOIN Person ON (creator = Person.id) WHERE person = ?")) {
               p2.setLong(1, r1.getLong("id"));
               ResultSet r2 = p2.executeQuery();
               while (r2.next()) {
                  TopicSummaryView tsv = new TopicSummaryView(r2.getLong("tid"),
                  r2.getLong("forum"), r2.getString("title"), r2.getInt("pcount"),
                  r2.getInt("created"), r2.getInt("ptime"), r2.getString("pauthor"),
                  likesCount(r2.getLong("tid"), "Topic"), r2.getString("pname"), r2.getString("pusername"));
                  list.add(tsv);
               }
               AdvancedPersonView apv = new AdvancedPersonView(r1.getString("name"),
               r1.getString("username"), r1.getString("stuId"),
               likesCount(r1.getLong("id"), "LikeTopic"), likesCount(r1.getLong("id"), "LikePost"),
               list);
               return Result.success(apv);
            } catch (SQLException e) {
               return Result.fatal("Something bad happened: " + e);
            }
         }
         else return Result.failure("No user with this username");
      }catch (SQLException e) {
         return Result.fatal("Something bad happened: " + e);
      }
   }


   // @ Written by Luping Yu
   // Checked by Luping Yu
    @Override
    public Result<AdvancedForumView> getAdvancedForum(long id) {
      if (c == null) { throw new IllegalStateException(); }
      List<TopicSummaryView> list = new LinkedList<>();

      try (PreparedStatement p1 = c.prepareStatement(
      "SELECT id, title FROM Forum WHERE id = ?")) {
         p1.setLong(1, id);
         ResultSet r1 = p1.executeQuery();
         if (r1.next()) {
            try (PreparedStatement p2 = c.prepareStatement(
            "SELECT Topic.id AS tid, forum, title, created, ptime, pcount, pauthor, name AS pname, username AS pusername FROM " +
            "Topic LEFT OUTER JOIN " +
            "(SELECT topic AS ptopic, name AS pauthor, count(*) AS pcount, max(created) AS ptime " +
            "FROM Post INNER JOIN Person ON (author = id) GROUP BY topic) " +
            "ON (Topic.id = ptopic) INNER JOIN Person ON (creator = Person.id) WHERE forum = ?")) {
               p2.setLong(1, r1.getLong("id"));
               ResultSet r2 = p2.executeQuery();
               while (r2.next()) {
                  TopicSummaryView tsv = new TopicSummaryView(r2.getLong("tid"),
                  r2.getLong("forum"), r2.getString("title"), r2.getInt("pcount"),
                  r2.getInt("created"), r2.getInt("ptime"), r2.getString("pauthor"),
                  likesCount(r2.getLong("tid"), "Topic"), r2.getString("pname"), r2.getString("pusername"));
                  list.add(tsv);
               }
               AdvancedForumView afv = new AdvancedForumView(r1.getLong("id"),
               r1.getString("title"), list);
               return Result.success(afv);
            } catch (SQLException e) {
               return Result.fatal("Something bad happened: " + e);
            }
         }
         else return Result.failure("No forum with this id");
      }catch (SQLException e) {
         return Result.fatal("Something bad happened: " + e);
      }
    }


    // @ Written by Khas_Ochir Sod-Erdene
    @Override
    public Result likePost(String username, long topicId, int post, boolean like) {
              if (c == null) {
                  throw new IllegalStateException();
              }
              if (!existTable(topicId)) return Result.failure("No topic with this id");
              if(!getPersonView(username).isSuccess()) return Result.failure("No user with this username");
              try (PreparedStatement q = c.prepareStatement("SELECT num FROM Post WHERE num = ? AND topic = ?")) {
                  q.setInt(1,post);
                  q.setLong(2,topicId);
                  ResultSet s = q.executeQuery();
                  if(!s.next()){
                      return Result.failure("No post with this post number");
                  }
              } catch (SQLException e) {
                  return Result.fatal("error " + e);
              }
              if (like) {
                  try (PreparedStatement p = c.prepareStatement("INSERT OR IGNORE INTO LikePost(person,postnum,postopic) Values(?, ?, ?)")) {
                      p.setLong(3, topicId);
                      p.setString(1, username);
                      p.setInt(2,post);
                      p.execute();
                      c.commit();
                  }
                  catch (SQLException e) {
                      try {
                          c.rollback();
                      } catch (SQLException e1) {
                          return Result.fatal("Error near rollback");
                      }
                      return Result.fatal("Something bad happened: " + e);
                  }
              }
              else{
                  try (PreparedStatement p = c.prepareStatement("DELETE FROM LikePost WHERE person=? and postopic=? and postnum=?")) {
                      p.setLong(3, topicId);
                      p.setString(1, username);
                      p.setInt(2,post);
                      p.execute();
                      c.commit();
                  }
                  catch (SQLException e) {
                      try {
                          c.rollback();
                      } catch (SQLException e1) {
                          return Result.fatal("Error near rollback");
                      }
                      return Result.fatal("Something bad happened: " + e);
                  }
              }
              return Result.success();
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
    // @ Written by Luping Yu
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

   // This method object used for count likes for general conditions
   // @ Written by Luping Yu
   private int likesCount(long Id, String tablename) {
      String sql;
      if (tablename.equals("LikeTopic")) {
            sql = "SELECT count(*) AS likes FROM LikeTopic WHERE person = ?";
      }
      else if (tablename.equals("LikePost")){
         sql = "SELECT count(*) AS likes FROM LikePost WHERE person = ?";
      }
      else {
         sql = "SELECT count(*) AS likes FROM LikeTopic WHERE topic = ?";
      }
      try (PreparedStatement p = c.prepareStatement(sql)) {
         p.setLong(1, Id);
         ResultSet r = p.executeQuery();
         if (r.next()) return r.getInt("likes");
         else return 0;
      } catch (SQLException e) {return 0;}
   }

   // @ Written by Fan Zhao
   // Used by Fan Zhao
   public Result<Integer> countPostsInTopic_2(long topicId) {
      if (c == null) { throw new IllegalStateException(); }

      try (PreparedStatement p = c.prepareStatement(
               "SELECT COUNT(num) AS count FROM Post WHERE topic = ?")) {
           p.setInt(1, (int)topicId);
           // long -> int, constraint conversion "int(topicID)"
           ResultSet r = p.executeQuery();
           int count=0;
           if (r.next()) {
               count = r.getInt("count");
           }
           return Result.success(new Integer(count));
      } catch (Exception e) {
           return Result.fatal("Something bad happened: " + e);
      }
   }

   //@ Written by Fan Zhao
   // Used by Fan Zhao
   public Result<Boolean> existATopic(int topicId)
   {
      final String SQL1 = "SELECT * FROM topic WHERE Id = ?";
      try (PreparedStatement p = c.prepareStatement(SQL1)) {
           p.setInt(1, (int)topicId);
           ResultSet r = p.executeQuery();
           if (r.next())
               return Result.success(new Boolean(true));
           else
               return Result.success(new Boolean(false));
      } catch (SQLException e) {
           return Result.fatal("Something bad happened: " + e);
      }
   }

   //@ Written by Fan Zhao
   // Used by Fan Zhao
   public Result<Boolean> existAPerson(String username)
   {
      final String SQL = "SELECT * FROM person WHERE username = ?";
      try (PreparedStatement p = c.prepareStatement(SQL)) {
           p.setString(1, username);
           ResultSet r = p.executeQuery();
           if (r.next())
               return Result.success(new Boolean(true));
           else
               return Result.success(new Boolean(false));
      } catch (SQLException e) {
           return Result.fatal("Something bad happened: " + e);
      }
   }

   //@ Written by Fan Zhao
   // Used by Fan Zhao
   public Result<Boolean> existAForum(int forumId )
   {
      final String SQL = "SELECT * FROM forum WHERE id = ?";
      try (PreparedStatement p = c.prepareStatement(SQL)) {
           p.setInt(1, forumId);
           ResultSet r = p.executeQuery();
           if (r.next())
               return Result.success(new Boolean(true));
           else
               return Result.success(new Boolean(false));
      } catch (SQLException e) {
           return Result.fatal("Something bad happened: " + e);
      }
   }
}
