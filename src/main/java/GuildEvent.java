import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.Random;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildEvent extends ListenerAdapter{

    private static MessageReactionAddEvent channel = null;
    private static Member member = null;
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        channel = event;
        member = event.getMember();
        if(!event.getReaction().isSelf()) {
            String emote = event.getReaction().getReactionEmote().getEmoji();
            if(emote.equals("ℹ")) {
                try {
                    MessagesFormat.editEmbed(MessagesFormat.getInfoEmbed("Welcome to the **@DopeBot** help menu.\n\nThis menu will show you all commands you can run!", "commandList"), emote);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }else if(emote.equals("💠")) {
                try {
                    MessagesFormat.editEmbed(MessagesFormat.getInfoEmbed("Dope 💠", "dope"), emote);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }else if(emote.equals("🎪")) {
                try {
                    MessagesFormat.editEmbed(MessagesFormat.getInfoEmbed("Fun 🎪", "fun"), emote);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }else if(emote.equals("💎")) {
                try {
                    MessagesFormat.editEmbed(MessagesFormat.getInfoEmbed("Moderation 💎", "moderator"), emote);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }else if(emote.equals("🎬")) {
                try {
                    MessagesFormat.editEmbed(MessagesFormat.getInfoEmbed("Media 🎬", "media"), emote);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }else if(emote.equals("🔦")) {
                try {
                    MessagesFormat.editEmbed(MessagesFormat.getInfoEmbed("Utility 🔦", "utility"), emote);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static MessageReactionAddEvent getChannel() {
        return channel;
    }
    public static Member getMember() {
        return member;
    }
}

class GuildMemberJoin extends ListenerAdapter {
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        int counter = 0;
        String result[] = new String[20];
        try {
            URL gifUrl = new URL("https://api.tenor.com/v1/search?q=hello&key=GAC5NZCLKTGO&limit=20");
            BufferedReader bf = new BufferedReader(new InputStreamReader(gifUrl.openConnection().getInputStream()));
            String input = "";
            while((input = bf.readLine()) != null) {
                if(input.contains("\"id\":")) {
                    result[counter++] = input.substring(13, input.length() - 1); //8606566 20 or 21
                }
            }
            event.getGuild().getTextChannels().get(6).sendMessage(new EmbedBuilder().setTitle("Hello! " + event.getUser().getName()).setColor(Color.BLUE)
                    .setFooter(SQLiteDataSource.getHelp("footer1"), SQLiteDataSource.getHelp("footer2")).addField("Need some help?", "Type @DopeBot help", true)
                    .setDescription("I'm **DopeBot.** Created by **Jaime 💎!**\nBuilt using [Java 14](https://docs.oracle.com/en/java/javase/14/) and [JDA 4](https://github.com/DV8FromTheWorld/JDA/wiki)").build()).queue();
            event.getJDA().getTextChannelsByName(event.getGuild().getTextChannels().get(6).getName(), true).get(0).sendMessage("https://tenor.com/view/rip-gif-" + result[new Random().nextInt(result.length)]).queue();
            event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRolesByName(event.getGuild().getRoles().get(1).getName(), true).get(0)).queue();
        }catch(Exception e) {
            e.printStackTrace();
        }

    }
}

class GuildMemberLeave extends ListenerAdapter {

    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        int counter = 0;
        String result[] = new String[20];
        try {
            URL gifUrl = new URL("https://api.tenor.com/v1/search?q=bye&key=GAC5NZCLKTGO&limit=20");
            BufferedReader bf = new BufferedReader(new InputStreamReader(gifUrl.openConnection().getInputStream()));
            String input = "";
            while((input = bf.readLine()) != null) {
                if(input.contains("\"id\":")) {
                    result[counter++] = input.substring(13, input.length() - 1); //8606566 20 or 21
                }
            }
            event.getJDA().getTextChannels().get(6).sendMessage("https://tenor.com/view/rip-gif-" + result[new Random().nextInt(result.length)]).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
