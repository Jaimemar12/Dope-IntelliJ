import java.awt.Color;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageEvents extends ListenerAdapter {

    private static MessageReceivedEvent event;
    private static String[] args;
    private static int size = 0;
    private static String command = "";
    private static String postCommand = "";
    private static List<Member> members = null;
    private static String userName = "";
    private static String selfName = "";
    private static Member user = null;
    private static Guild guild;
    public static GuildMusicManager musicManager;
    public static BlockingQueue<AudioTrack> queue;
    public static AudioPlayer player;
    public static AudioTrackInfo info = null;
    public static TrackScheduler scheduler;
    public static GuildVoiceState selfVoiceState;
    public static GuildVoiceState memberVoiceState;
    private static Member member;
    private static String messageId = "";
    private static boolean getID = false;
    private static HashMap<String, BotActions> botCommands = new HashMap<String, BotActions>();

    public void onMessageReceived(MessageReceivedEvent event) {
        args = event.getMessage().getContentRaw().split("\\s+");

        if(event.getAuthor().isBot() && getID) {
            messageId = event.getMessageId();
            getID = false;
            try {
                SQLiteDataSource.setHelpID();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if((args[0].equals(Dope.prefixM) || args[0].equals(Dope.prefix)) && args.length == 1 && !event.getAuthor().isBot())
            try {
                noCommand();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        else if(!event.getAuthor().isBot() && (args[0].equals(Dope.prefixM) || args[0].equals(Dope.prefix))) {
            size = args.length;
            MessageEvents.event = event;

            command = args[1].toLowerCase();
            if(args.length > 2)
                postCommand = args[2].toLowerCase();
            if(event.isFromGuild()) {
                members = event.getMessage().getMentionedMembers();
                member = event.getMember();
                selfName = member.getUser().getName();
                if(members.size() > 1) {
                    user = members.get(1);
                    userName = user.getUser().getName();
                }
                guild = event.getGuild();
                @SuppressWarnings("unused")
                PlayerManager playerManager = PlayerManager.getInstance();
                musicManager = PlayerManager.getMusicManager(guild);
                queue = musicManager.scheduler.getQueue();
                player = musicManager.audioPlayer;
                if(player.getPlayingTrack() != null)
                    info = player.getPlayingTrack().getInfo();
                scheduler = musicManager.scheduler;
                selfVoiceState = event.getMessage().getMentionedMembers().get(0).getVoiceState();
                memberVoiceState = event.getMember().getVoiceState();
            }
            if(botCommands.get(command) != null) {
                try {
                    botCommands.get(command).execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    noCommand();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    MessageEvents(){
        botCommands.put("8ball", new Ball());
        botCommands.put("about", new About());
        botCommands.put("addrole", new AddRole());
        botCommands.put("alert", new Alert());
        botCommands.put("avatar", new Avatar());
        botCommands.put("ban", new Ban());
        botCommands.put("cat", new Cat());
        botCommands.put("clear", new Clear());
        botCommands.put("coin", new Coin());
        botCommands.put("dog", new Dog());
        botCommands.put("easteregg", new EasterEgg());
        botCommands.put("gif", new Gif());
        botCommands.put("help", new Help());
        botCommands.put("invitation", new Invitation());
        botCommands.put("join", new Join());
        botCommands.put("joke", new Joke());
        botCommands.put("kick", new Kick());
        botCommands.put("leave", new Leave());
        botCommands.put("meme", new Meme());
        botCommands.put("mute", new Mute());
        botCommands.put("nick", new Nick());
        botCommands.put("pause", new Pause());
        botCommands.put("pin", new Pin());
        botCommands.put("ping", new Ping());
        botCommands.put("play", new Play());
        botCommands.put("playing", new Playing());
        botCommands.put("pm", new PM());
        botCommands.put("purge", new Purge());
        botCommands.put("queue", new Queue());
        botCommands.put("quite", new Quite());
        botCommands.put("removenick", new RemoveNick());
        botCommands.put("removerole", new RemoveRole());
        botCommands.put("repeat", new Repeat());
        botCommands.put("roll", new Roll());
        botCommands.put("say", new Say());
        botCommands.put("search", new Search());
        botCommands.put("serverinfo", new ServerInfo());
        botCommands.put("shutdown", new Shutdown());
        botCommands.put("skip", new Skip());
        botCommands.put("softban", new SoftBan());
        botCommands.put("stop", new Stop());
        botCommands.put("ud", new UD());
        botCommands.put("unban", new Unban());
        botCommands.put("unmute", new Unmute());
        botCommands.put("unpin", new Unpin());
        botCommands.put("unquite", new Unquite());
        botCommands.put("user", new Users());
        botCommands.put("volume", new Volume());
        botCommands.put("warn", new Warn());
        botCommands.put("calc", new Calc());
//		botCommands.put("prefix", new Prefix());
//		botCommands.put("reddit", new Reddit());
    }
    public static MessageReceivedEvent getEvent() {
        return event;
    }
    public static String getCommand() {
        return command;
    }
    public static String getPostCommand() {
        return postCommand;
    }
    public static String[] getArgs() {
        return args;
    }
    public static int getSize() {
        return size;
    }
    public static List<Member> getMembers(){
        return members;
    }
    public static String getUsername() {
        return userName;
    }
    public static String getSelfname() {
        return selfName;
    }
    public static Member getUser() {
        return user;
    }
    public static Guild getGuild() {
        return guild;
    }
    public static Member getMember() {
        return member;
    }
    public static String getMessageId() {
        return messageId;
    }
    public static void setId() {
        getID = true;
    }
    public static HashMap<String, BotActions> getBotCommands() {
        return botCommands;
    }
    private void noCommand() throws SQLException {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Hey there " + event.getMessage().getAuthor().getName() + ", it seems like you are lost!"); //This works in private and server
        embed.setDescription("Mention **@DopeBot** help to learn how to use me on this server! :smile:");
        embed.addField("Add **@DopeBot** to your server!", "[Click here](https://discord.com/api/oauth2/authorize?client_id=709436765693542450&permissions=8&scope=bot)", true);
        embed.setColor(Color.BLUE);
        embed.setFooter(SQLiteDataSource.getHelp("footer1"), SQLiteDataSource.getHelp("footer2"));
        try {
            event.getChannel().sendMessage(embed.build()).queue();
        }catch (java.lang.NullPointerException e) {
            event.getPrivateChannel().sendMessage(embed.build()).queue();
        }
    }
}