import java.awt.Color;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Formats extends ListenerAdapter{
    public void onMessageReceived(MessageReceivedEvent event) {
        //These are provided with every event in JDA
        Logger LOGGER = LoggerFactory.getLogger(Formats.class);
        LOGGER.info("{} is ready", event.getJDA().getSelfUser().getAsTag());


        //Event specific information
        User author = event.getAuthor();                //The user that sent the message
        Message message = event.getMessage();           //The message that was received.

        String msg = message.getContentDisplay();              //This returns a human readable version of the Message. Similar to
        // what you would see in the client.

        // sent the Message is a BOT or not!

        if (event.isFromType(ChannelType.TEXT)) {
            //Because we now know that this message was sent in a Guild, we can do guild specific things
            // Note, if you don't check the ChannelType before using these methods, they might return null due
            // the message possibly not being from a Guild!

            Guild guild = event.getGuild();             //The Guild that this message was sent in. (note, in the API, Guilds are Servers)
            TextChannel textChannel = event.getTextChannel(); //The TextChannel that this message was sent to.
            Member member = event.getMember();          //This Member that sent the message. Contains Guild specific information about the User!

            String name;
            if (message.isWebhookMessage())
                name = author.getName();                                           // with the User, thus we default to the author for name.
            else
                name = member.getEffectiveName();       //This will either use the Member's nickname if they have one,
            // otherwise it will default to their username. (User#getName())

            System.out.printf("(%s)[%s]<%s>: %s\n", guild.getName(), textChannel.getName(), name, msg);
        }
        else if (event.isFromType(ChannelType.PRIVATE))
            //The message was sent in a PrivateChannel.
            //In this example we don't directly use the privateChannel, however, be sure, there are uses for it!
            System.out.printf("[PRIV]<%s>: %s\n", author.getName(), msg);
    }

}

class MessagesFormat {

    public static String getSentence(int start) {
        String[] args = MessageEvents.getArgs();
        String sentence = args[start];
        for(int i = start+1; i < args.length; i++) {
            sentence += " " + args[i] ;
        }
        return sentence;
    }

    public static String searchYoutube() {

        String input = getSentence(2);
        YouTube youTube = null;
        try {
            youTube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    null
            )
                    .setApplicationName("Dope")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            List<SearchResult> results = youTube.search()
                    .list("id,snippet")
                    .setQ(input)
                    .setMaxResults(1L)
                    .setType("video")
                    .setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)")
                    .setOrder("relevance")
                    .setKey("AIzaSyAUZ3nR3vEalbYd2izFFJBlOBO3DNAxtIs")
                    .execute()
                    .getItems();

            if (!results.isEmpty()) {
                return "https://www.youtube.com/watch?v=" + results.get(0).getId().getVideoId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static MessageEmbed createEmbed(Color color, String description) throws SQLException {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(MessageEvents.getCommand().toUpperCase() + " COMMAND");
        embed.setFooter(SQLiteDataSource.getHelp("footer1"), SQLiteDataSource.getHelp("footer2"));
        embed.setColor(color);
        embed.setDescription(description);
        return embed.build();
    }
    public static EmbedBuilder createHelpEmbed(Color color, String command, String description) throws SQLException {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("HELP MENU FOR " + command.toUpperCase());
        embed.setFooter(SQLiteDataSource.getHelp("footer1"), SQLiteDataSource.getHelp("footer2"));
        embed.setColor(color);
        embed.setDescription(description);
        return embed;
    }
    public static void editEmbed(EmbedBuilder embed, String emote) throws SQLException{
        if(SQLiteDataSource.editHelpID().equals(GuildEvent.getChannel().getMessageId()))
            GuildEvent.getChannel().getChannel().editMessageById(SQLiteDataSource.editHelpID(), embed.build()).queue();
        else {
            try {
                GuildEvent.getChannel().getPrivateChannel().sendMessage(createEmbed(Color.BLUE, "You cannot edit someone else's help menu.\n To get your own enter @DopeBot help")).queue();
            } catch (Exception e) {
                GuildEvent.getChannel().getChannel().sendMessage(createEmbed(Color.BLUE, "You cannot edit someone else's help menu.\n To get your own enter @DopeBot help")).queue();
            }
        }

    }
    static EmbedBuilder getInfoEmbed(String title, String titleCommand) throws SQLException{
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        embed.setDescription(SQLiteDataSource.getHelp(titleCommand));
        embed.setColor(Color.BLUE);
        embed.setFooter(SQLiteDataSource.getHelp("footer1"), SQLiteDataSource.getHelp("footer2"));
        return embed;
    }
    public static void send(MessageEmbed embed) {
        try {
            MessageEvents.getEvent().getPrivateChannel().sendMessage(embed).queue();
        }catch(Exception e) {
            MessageEvents.getEvent().getChannel().sendMessage(embed).queue();
        }
    }
    public static void send(String message) {
        try {
            MessageEvents.getEvent().getPrivateChannel().sendMessage(message).queue();
        }catch(Exception e) {
            MessageEvents.getEvent().getChannel().sendMessage(message).queue();
        }
    }

    public static void sendHelp() throws SQLException {
        try {
            send(createHelpEmbed(Color.BLACK, MessageEvents.getCommand(), SQLiteDataSource.getHelp(MessageEvents.getEvent().getGuild().getId(), MessageEvents.getCommand())).build());
        } catch (Exception e){
            send(createHelpEmbed(Color.BLACK, MessageEvents.getCommand(), SQLiteDataSource.getHelp(MessageEvents.getCommand())).build());
        }
    }
    public static void sendHelp(String description) throws SQLException {
        try {
            send(createHelpEmbed(Color.BLACK, MessageEvents.getCommand(), SQLiteDataSource.getHelp(MessageEvents.getEvent().getGuild().getId(), MessageEvents.getCommand()) + "\n\n**" + description + "**").build());
        } catch (Exception e){
            send(createHelpEmbed(Color.BLACK, MessageEvents.getCommand(), SQLiteDataSource.getHelp(MessageEvents.getCommand()) + "\n\n**" + description + "**").build());
        }
    }
    public static boolean hasPermission(Permission permission) {
        return !(MessageEvents.getGuild().getSelfMember().hasPermission(permission) && MessageEvents.getEvent().getMember().hasPermission(permission));
    }
}
