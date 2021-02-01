import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import net.dv8tion.jda.api.entities.User;

public class SQLiteDataSource {

    private SQLiteDataSource() { }

    public static Connection getConnection() throws SQLException{

        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:/Users/jaimemartinez/IdeaProjects/Projects/Dope/src/main/resources/DopeBot.db");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return con;
    }

    public static String getHelp(String guildID, String command) throws SQLException{

        PreparedStatement ps = null;
        Connection con = null;
        ResultSet  rs = null;
        try {
            con = getConnection();
            String sql =  "Select menu from menu where guildID = ? AND command  = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, guildID);
            ps.setString(2, command);
            rs = ps.executeQuery();

            //checks if it exists
            if(rs.next())
                return rs.getString(1);
        }finally {
            try {
                rs.close();
                ps.close();
                con.close();
            } catch(SQLException e) {

            }
        }

        return "";
    }

    public static String getHelp(String command) throws SQLException {

        PreparedStatement ps = null;
        Connection con = null;
        ResultSet  rs = null;
        try {
            con = getConnection();
            String sql =  "Select menu from menu where guildID = ? AND command  = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, "712375953795579965");
            ps.setString(2, command);
            rs = ps.executeQuery();

            //checks if it exists
            if(rs.next())
                return rs.getString(1);
        }finally {
            try {
                rs.close();
                ps.close();
                con.close();
            } catch(SQLException e) {

            }
        }

        return "";
    }

    public static void addBanned(User user) throws SQLException {
        if(getUserID(user.getName()).equals("")) {
            PreparedStatement ps = null;
            Connection con = null;
            con = getConnection();
            String sql =  "INSERT INTO guild(guildID, username, userID, banned) VALUES(?,?,?,?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, MessageEvents.getGuild().getId());
            ps.setString(2, user.getName().replace(" ", ""));
            ps.setString(3, user.getId());
            ps.setString(4, "true");
            ps.execute();
        }else {
            updateBanned(user.getName(), "true");
        }
    }

    public static boolean isBanned(String username) {
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet  rs = null;
        try {
            con = getConnection();
            String sql =  "Select banned from guild where guildID = ? AND username = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, MessageEvents.getGuild().getId());
            ps.setString(2, username);
            rs = ps.executeQuery();

            //checks if it exists
            if(rs.next())
                if(rs.getString(1).equals("true"))
                    return true;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
                con.close();
            } catch(SQLException e) {

            }
        }
        return false;
    }

    public static String getUserID(String username) throws SQLException {

        PreparedStatement ps = null;
        Connection con = null;
        ResultSet  rs = null;
        try {
            con = getConnection();
            String sql =  "Select userID from guild where username  = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();

            //checks if it exists
            if(rs.next())
                return rs.getString(1);
        }finally {
            try {
                rs.close();
                ps.close();
                con.close();
            } catch(SQLException e) {

            }
        }
        return "";
    }

    public static void updateBanned(String username, String condition) {
        PreparedStatement ps = null;
        Connection con = null;
        try {
            con = getConnection();
            String sql =  "UPDATE guild set banned = ? where guildID = ? AND username = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, condition);
            ps.setString(2, MessageEvents.getGuild().getId());
            ps.setString(3, username);
            ps.execute();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            try {
                ps.close();
                con.close();
            } catch(SQLException e) {

            }
        }
    }

    public static String getWarnings() {
        User user = MessageEvents.getUser().getUser();
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet  rs = null;
        try {
            con = getConnection();
            String sql =  "Select warnings from guild where guildID = ? AND userID = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, MessageEvents.getGuild().getId());
            ps.setString(2, user.getId());
            rs = ps.executeQuery();

            //checks if it exists
            if(rs.next())
                return rs.getString(1);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
                con.close();
            } catch(SQLException e) {

            }
        }
        return "0";
    }

    public static void setWarnings(String warningNum) throws SQLException {
        User user = MessageEvents.getUser().getUser();
        PreparedStatement ps = null;
        Connection con = null;
        if(getUserID(user.getName()).equals("")) {
            con = getConnection();
            String sql =  "INSERT INTO guild(guildID, username, userID, warnings) VALUES(?,?,?,?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, MessageEvents.getGuild().getId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getId());
            ps.setString(4, "1");
            ps.execute();
        }else {
            try {
                con = getConnection();
                String sql =  "UPDATE guild set warnings = ? where guildID = ? AND userID = ?";
                ps = con.prepareStatement(sql);
                ps.setString(1, warningNum);
                ps.setString(2, MessageEvents.getGuild().getId());
                ps.setString(3, user.getId());
                ps.execute();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }finally {
                try {
                    ps.close();
                    con.close();
                } catch(SQLException e) {

                }
            }
        }
    }

    public static String setPin() {
        PreparedStatement ps = null;
        Connection con = null;
        String uniqueID = UUID.randomUUID().toString();
        try {
            con = getConnection();
            String sql =  "INSERT INTO admin(guildID, username, pinID, messageID) VALUES(?,?,?,?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, MessageEvents.getGuild().getId());
            ps.setString(2, MessageEvents.getMember().getUser().getName());
            ps.setString(3, MessageEvents.getEvent().getMessageId());
            ps.setString(4, uniqueID);
            ps.execute();
        }catch (Exception e) {

        }finally {
            try {
                ps.close();
                con.close();
            } catch(SQLException e) {

            }
        }
        return uniqueID;
    }

    @SuppressWarnings("resource")
    public static String unPin(String id) {
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet  rs = null;
        String pinID = "";
        try {
            con = getConnection();
            String sql =  "Select pinID from admin where guildID = ? AND messageID = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, MessageEvents.getGuild().getId());
            ps.setString(2, id);
            rs = ps.executeQuery();

            //checks if it exists
            if(rs.next())
                pinID = rs.getString(1);
            else {
                return "";
            }
            sql =  "delete from admin WHERE messageID = ? AND guildID = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, id);
            ps.setString(2, MessageEvents.getGuild().getId());
            ps.execute();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            try {
                rs.close();
                ps.close();
                con.close();
            } catch(SQLException e) {

            }
        }
        return pinID;
    }

    public static void setHelpID () throws SQLException {
        User user = null;
        if(MessageEvents.getEvent().isFromGuild())
            user = MessageEvents.getMember().getUser();
        else
            user = MessageEvents.getEvent().getAuthor();
        PreparedStatement ps = null;
        Connection con = null;
        if(getUserID(user.getName()).equals("")) {
            con = getConnection();
            String sql =  "INSERT INTO guild(username, userID, helpID) VALUES(?,?,?)";
            ps = con.prepareStatement(sql);
            ps.setString(1, user.getName());
            ps.setString(2, user.getId());
            ps.setString(3, MessageEvents.getMessageId());
            ps.execute();
        }else {
            try {
                con = getConnection();
                String sql =  "UPDATE guild set helpID = ? where userID = ?";
                ps = con.prepareStatement(sql);
                ps.setString(1, MessageEvents.getMessageId());
                ps.setString(2, user.getId());
                ps.execute();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }finally {
                try {
                    ps.close();
                    con.close();
                } catch(SQLException e) {

                }
            }
        }
    }

    public static String getHelpID() {
        User user = null;
        if(MessageEvents.getEvent().isFromGuild())
            user = MessageEvents.getMember().getUser();
        else
            user = MessageEvents.getEvent().getAuthor();

        PreparedStatement ps = null;
        Connection con = null;
        ResultSet  rs = null;
        try {
            con = getConnection();
            String sql =  "Select helpID from guild where userID = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, user.getId());
            rs = ps.executeQuery();
            //checks if it exists
            if(rs.next())
                return rs.getString(1);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
                con.close();
            } catch(SQLException e) {

            }
        }
        return null;
    }

    public static String editHelpID() {
        User user = GuildEvent.getChannel().getUser();
        PreparedStatement ps = null;
        Connection con = null;
        ResultSet  rs = null;
        try {
            con = getConnection();
            String sql =  "Select helpID from guild where userID = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, user.getId());
            rs = ps.executeQuery();
            //checks if it exists
            if(rs.next())
                return rs.getString(1);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                ps.close();
                con.close();
            } catch(SQLException e) {

            }
        }
        return null;
    }

    /**
     * //Counts columns
     PreparedStatement ps = null;
     Connection con = null;
     ResultSet rs = null;
     try {
     Class.forName("org.sqlite.JDBC");
     con = DriverManager.getConnection("jdbc:sqlite:DopeBot.db");
     String sql =  "select count(prefix) from main ";
     ps = con.prepareStatement(sql);
     rs  = ps.executeQuery();

     int size = rs.getInt(1);
     System.out.println("Connected! "  + size);
     } catch (ClassNotFoundException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     }
     */

    /**
     * //DELETES ALL ROWS
     * CLOSE THE VARABLES FOR ALL METHODS
     PreparedStatement ps = null;
     Connection con = null;
     try {
     Class.forName("org.sqlite.JDBC");
     con = DriverManager.getConnection("jdbc:sqlite:DopeBot.db");
     String sql =  "delete from main WHERE prefixM = ? ";
     ps = con.prepareStatement(sql);
     ps.setString(1, "Hello");
     ps.execute();
     System.out.println("Connected!");
     } catch (ClassNotFoundException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     }
     */

    //CHANGES ALL WITH THAT KEY
    //  update values
//			PreparedStatement ps = null;
//			Connection con = null;
//			try {
//				Class.forName("org.sqlite.JDBC");
//				con = DriverManager.getConnection("jdbc:sqlite:DopeBot.db");
//				String sql =  "UPDATE main set prefixM = ? WHERE prefix = ? ";
//				ps = con.prepareStatement(sql);
//				ps.setString(2, "<@!709436765693542450>");
//				ps.setString(1, "Hello");
//				ps.execute();
//				System.out.println("Connected!");
//			} catch (ClassNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

    /**  Read single
     * PreparedStatement ps = null;
     Connection con = null;
     ResultSet  rs = null;
     try {
     Class.forName("org.sqlite.JDBC");
     con = DriverManager.getConnection("jdbc:sqlite:DopeBot.db");
     String sql =  "Select prefixM from main where prefix = ? ";
     ps = con.prepareStatement(sql);
     ps.setString(1, "<@!709436765693542450>");
     rs = ps.executeQuery();

     System.out.println(rs.getString(1));

     System.out.println("Connected!");
     } catch (SQLException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     } catch (ClassNotFoundException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     }finally {
     try {
     rs.close();
     ps.close();
     con.close();
     } catch(SQLException e) {

     }
     }




     PreparedStatement ps = null;
     Connection con = null;
     ResultSet  rs = null;
     try {
     Class.forName("org.sqlite.JDBC");
     con = DriverManager.getConnection("jdbc:sqlite:DopeBot.db");
     String sql =  "Select guildID from main where prefix = ? ";
     ps = con.prepareStatement(sql);
     ps.setString(1, "<@!709436765693542450>");
     rs = ps.executeQuery();

     //checks if it exists
     if(rs.next())
     System.out.println(rs.getString(1));

     System.out.println("Connected!");
     } catch (SQLException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     } catch (ClassNotFoundException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     }finally {
     try {
     rs.close();
     ps.close();
     con.close();
     } catch(SQLException e) {

     }
     }
     */


    /** Read multiples
     * PreparedStatement ps = null;
     Connection con = null;
     ResultSet  rs = null;
     try {
     Class.forName("org.sqlite.JDBC");
     con = DriverManager.getConnection("jdbc:sqlite:DopeBot.db");
     String sql =  "SELECT * FROM main";
     ps = con.prepareStatement(sql);
     rs = ps.executeQuery();
     while(rs.next()) {
     String mobile = rs.getString("prefixM");
     String computer = rs.getString("prefix");
     System.out.println(mobile + computer);
     }
     System.out.println("Connected!");
     } catch (SQLException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     } catch (ClassNotFoundException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     }finally {
     try {
     rs.close();
     ps.close();
     con.close();
     } catch(SQLException e) {

     }
     }

     PreparedStatement ps = null;
     Connection con = null;
     ResultSet  rs = null;
     try {
     Class.forName("org.sqlite.JDBC");
     con = DriverManager.getConnection("jdbc:sqlite:DopeBot.db");
     String sql =  "SELECT * FROM main WHERE guildID = ? ";
     ps = con.prepareStatement(sql);
     ps.setString(1, "712375953795579965");
     rs = ps.executeQuery();
     while(rs.next()) {
     String mobile = rs.getString("prefix");
     System.out.println(mobile);
     }
     System.out.println("Connected!");
     } catch (SQLException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     } catch (ClassNotFoundException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     }finally {
     try {
     rs.close();
     ps.close();
     con.close();
     } catch(SQLException e) {

     }
     }
     */

    /**		insert
     * PreparedStatement ps = null;
     Connection con = null;
     try {
     Class.forName("org.sqlite.JDBC");
     con = DriverManager.getConnection("jdbc:sqlite:DopeBot.db");
     String sql =  "INSERT INTO main(prefixM,  prefix, command, menu) VALUES(?,?,?,?)";
     ps = con.prepareStatement(sql);
     ps.setString(1, "<@!709436765693542450>");
     ps.setString(2, "<@709436765693542450>");
     ps.execute();
     System.out.println("Connected!");
     } catch (ClassNotFoundException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     }


     PreparedStatement ps = null;
     Connection con = null;
     try {
     Class.forName("org.sqlite.JDBC");
     con = DriverManager.getConnection("jdbc:sqlite:DopeBot.db");
     String sql =  "INSERT INTO main(prefix, guildID) VALUES(?,?)";
     ps = con.prepareStatement(sql);
     ps.setString(1, "<@!709436765693542450>");
     ps.setString(2, MessageEvents.event.getGuild().getId());
     ps.execute();
     System.out.println("Connected!");
     } catch (ClassNotFoundException e) {
     // TODO Auto-generated catch block
     e.printStackTrace();
     }
     */
}