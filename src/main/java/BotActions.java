import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.HierarchyException;

public interface BotActions {
    public void execute() throws SQLException;
}

//**@DopeBot** 8ball [question]
class Ball implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If user's input was only 2 words the help menu will be sent to explain how the command works
        if(MessageEvents.getSize() == 2)
            MessagesFormat.sendHelp();

            //Else a random phrase from ball[] will be chosen to be sent back to the user
        else {
            String[] ball = {"It is certain", "It is decidedly so", "Without a doubt", "Yes - definitely", "You may rely on iy", "As I see it, yes", "Most likely", "Outlook good",
                    "Yes", "Signs point to yes", "Reply hazy, try again", "Ask again later", "Better not tell you now", "Cannot predict now", "Concentrated and ask again",
                    "Don't count on it", "My reply is no", "My sources say no", "Outlook not so good", "Very doubtful"};
            MessagesFormat.send(ball[new Random().nextInt(ball.length)] + " "  + MessageEvents.getSelfname());
        }
    }
}

//**@DopeBot** addrole [@Member] [Role]
class AddRole implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message was less than 4 words or was not sent from a guild the help menu will be sent to explain how the command works
        if(MessageEvents.getSize() < 4 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the member that requested the command has permission to manage roles
        else if(MessagesFormat.hasPermission(Permission.MANAGE_ROLES))
            MessagesFormat.sendHelp();

            //Else If only one user was mention the help menu with an example will be sent
        else if(MessageEvents.getMembers().size() != 2)
            MessagesFormat.sendHelp();

            //Else it will add the requested role to the mentioned user. It will only work when the role a role which is less than the bot's and can be found in the guild role list
        else {
            try {
                MessageEvents.getGuild().addRoleToMember(MessageEvents.getUser(), MessageEvents.getGuild().getRolesByName(MessagesFormat.getSentence(3), true).get(0)).queue();
                MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, MessagesFormat.getSentence(3) + " role has been added to " + MessageEvents.getUsername()));

            }catch(HierarchyException e) {
                MessagesFormat.sendHelp("Can't add a role to " + MessageEvents.getUsername() + ", I have equal or lower hierchy than the person");

            }catch(IndexOutOfBoundsException ex) {
                String roleList = "";
                for(Role role: MessageEvents.getGuild().getRoles()) {
                    roleList += role.getName() + "\n";
                }
                MessagesFormat.sendHelp("Could not find role. Choose one from the list below.");
                MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, roleList));
            }
        }
    }
}

//@DopeBot alert [message]
class Alert implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message was only 2 words or it was not from a guild the help menu for the command will be sent
        if(MessageEvents.getSize() == 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or user did not have administrator permission this will be sent
        else if(MessagesFormat.hasPermission(Permission.ADMINISTRATOR))
            MessagesFormat.sendHelp();

            //Else the message will be sent to the announcement channel if it was properly set up
        else {
            try {
                MessageEvents.getGuild().getTextChannelsByName("announcements📢", true).get(0).sendMessage("@everyone " + MessagesFormat.getSentence(2)).queue();
                MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, MessageEvents.getSelfname() + " your message has been sent to announcements📢"));
            }catch (Exception e) {
                MessagesFormat.sendHelp("announcements📢 channel was not found");
            }
        }
    }
}

//@DopeBot avatar [@member] [@member]
class Avatar implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message was not sent from the guild the help menu will be sent
        if(!MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else if the bot or user does not have permission to manage the server the help menu will be sent with an explanation
        else if(MessagesFormat.hasPermission(Permission.MANAGE_SERVER))
            MessagesFormat.sendHelp();

            //Else If only one user was mention the help menu with an example will be sent
        else if(MessageEvents.getMembers().size() == 1)
            MessagesFormat.sendHelp();

            //Else it will print all the avatar of the mentioned users
        else {
            for (Member user : MessageEvents.getMembers().subList(1, MessageEvents.getMembers().size())){
                try {
                    if(!user.getUser().getAvatarUrl().equals(null))
                        MessagesFormat.send(user.getUser().getAvatarUrl());

                }catch(Exception e) {
                    MessagesFormat.send(user.getUser().getDefaultAvatarUrl());
                }
            }
        }
    }
}

//**@DopeBot** ban [@member] [@member]
class Ban implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is not from the guild then it will send them the help menu
        if (!MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or the user does not have ban permissions the help menu with an explanation will be sent
        else if(MessagesFormat.hasPermission(Permission.BAN_MEMBERS))
            MessagesFormat.sendHelp();

            //Else If message only has one mentioned person the help menu with an explanation will be sent
        else if(MessageEvents.getMembers().size() == 1)
            MessagesFormat.sendHelp();

            //Else it will go trough the mentioned members and ban them if it can
        else {
            for(Member user : MessageEvents.getMembers().subList(1, MessageEvents.getMembers().size())) {
                try {
                    MessageEvents.getGuild().ban(user, 0).complete();
                    SQLiteDataSource.addBanned(user.getUser());
                    MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, user.getUser().getName() + " has  been banned"));
                }catch(HierarchyException e) {
                    MessagesFormat.sendHelp("Cannot ban " + user.getUser().getName() + ", I have equal or lower hierchy than the person");
                }catch(ErrorResponseException ex) {
                    MessagesFormat.sendHelp("Cannot ban " + user.getUser().getName() + ", I have equal or lower hierchy than the person");
                }
            }
        }
    }
}

//**@DopeBot** cat
class Cat implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is not composed of 2 words it will send the help menu
        if(MessageEvents.getSize() != 2)
            MessagesFormat.sendHelp();

            //Else it will get the API and send a random picture of a cat
        else
            WebUtils.ins.scrapeWebPage("https://api.thecatapi.com/api/images/get?format=xml&results_per_page=1").async((document) -> {
                MessageEvents.getEvent().getChannel().sendMessage(document.getElementsByTag("url").first().html()).queue();});
    }
}

//**@DopeBot** clear #
class Clear implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message was not from the guild or it was more than 3 words the help menu with an explenation will be sent
        if(!MessageEvents.getEvent().isFromGuild() || MessageEvents.getSize() > 3)
            MessagesFormat.sendHelp();

            //Else If the bot or the user does not have message manage permission the help menu will be sent with an explenation
        else if(MessagesFormat.hasPermission(Permission.MESSAGE_MANAGE))
            MessagesFormat.sendHelp();

            //Else If the message only has 2 words it will clear 20 messages by default
        else if(MessageEvents.getSize() == 2) {
            try {
                MessageEvents.getEvent().getChannel().purgeMessages(MessageEvents.getEvent().getChannel().getHistory().retrievePast(20).complete());
                MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, "Successfully deleted 20 messages"));
            }catch(Exception e) {
                MessagesFormat.sendHelp("Messages older than 2 weeks cannot be deleted");
            }

            //Else If it has a valid numeric value from 1-100 and its before 2 weeks the amount of messages will be deleted
        }else if(MessageEvents.getPostCommand().matches("[0-9]+") && Integer.parseInt(MessageEvents.getPostCommand()) > 0 && Integer.parseInt(MessageEvents.getPostCommand()) <= 100) {
            try {
                MessageEvents.getEvent().getChannel().purgeMessages(MessageEvents.getEvent().getChannel().getHistory().retrievePast(Integer.parseInt(MessageEvents.getPostCommand())).complete());
                MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, "Successfully deleted " + MessageEvents.getPostCommand() + " messages"));
            }catch(Exception e) {
                MessagesFormat.sendHelp("Messages older than 2 weeks cannot be deleted");
            }

            //Else the help menu with an explanation will be sent
        }else
            MessagesFormat.sendHelp("Make sure you use a valid numeric value from 1-100");
    }
}

//**@DopeBot** coin
class Coin implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message has less than 3 words the help menu will be sent
        if(MessageEvents.getSize() < 3)
            MessagesFormat.sendHelp();

            //Else heads or tails will be sent
        else {
            String[] coin = {"Heads", "Tails"};
            MessagesFormat.send(MessagesFormat.createEmbed(Color.CYAN, MessageEvents.getMember().getUser().getName() +  " you got " + coin[new Random().nextInt(2)]));
        }
    }
}

//**@DopeBot** dog
class Dog implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is not composed of 2 words it will send the help menu
        if(MessageEvents.getSize() != 2)
            MessagesFormat.sendHelp();

            //Else it will get the API and send a random picture of a cat
        else
            WebUtils.ins.getJSONObject("https://random.dog/woof.json").async((json) -> {MessagesFormat.send(json.get("url").asText());});
    }
}

//**@DopeBot** easteregg
class EasterEgg implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is not composed of 2 words it will send the help menu
        if(MessageEvents.getSize() != 2)
            MessagesFormat.sendHelp();

            //Else it will send this link
        else
            MessagesFormat.send("https://youtu.be/dQw4w9WgXcQ");
    }
}

//**@DopeBot** gif [sentence]
class Gif implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is composed of only 2 words the help menu will be sent
        if(MessageEvents.getSize() == 2) {
            MessagesFormat.sendHelp();

            //Else it will send the searched gif
        }else {
            try {
                int counter = 0;
                String result[] = new String[25];
                URL gifUrl = new URL("https://api.tenor.com/v1/search?q=" + MessagesFormat.getSentence(2).replace(" ", "-") + "&key=GAC5NZCLKTGO&limit=20");
                BufferedReader bf = new BufferedReader(new InputStreamReader(gifUrl.openConnection().getInputStream()));
                String input = "";

                while((input = bf.readLine()) != null) {
                    if(input.contains("\"id\":")) {
                        result[counter++] = input.substring(13, input.length() - 1); //8606566 20 or 21
                    }
                }
                String id = result[new Random().nextInt(result.length)];
                if(!id.equals("null"))
                    MessagesFormat.send("https://tenor.com/view/" + MessagesFormat.getSentence(2).replace(" ", "-") + "-gif-" + id);
                else
                    MessagesFormat.sendHelp("There was no results for " + MessagesFormat.getSentence(2));
            } catch (Exception e) {
                MessagesFormat.sendHelp("There was an error while sending the GIF");
            }
        }
    }
}

//**@DopeBot** help [Command]
class Help implements BotActions {

    public void execute() throws SQLException {

        //If the message has more than 3 words this will be sent
        if(MessageEvents.getSize() > 3)
            MessagesFormat.send(new EmbedBuilder().setTitle(MessageEvents.getCommand().toUpperCase() + " COMMAND").setFooter(SQLiteDataSource.getHelp("footer1"), SQLiteDataSource.getHelp("footer2"))
                    .setColor(Color.BLACK).setDescription("Use @DopeBot help [Command]").build());

            //Else If there are 3 words the help menu of the 3rd word will be sent
        else if (MessageEvents.getSize() == 3) {
            if(MessageEvents.getBotCommands().containsKey(MessageEvents.getPostCommand())) {
                try {
                    MessagesFormat.send(MessagesFormat.createHelpEmbed(Color.BLACK, MessageEvents.getPostCommand(), SQLiteDataSource.getHelp(MessageEvents.getEvent().getGuild().getId(), MessageEvents.getPostCommand())).build());
                } catch (Exception e){
                    MessagesFormat.send(MessagesFormat.createHelpEmbed(Color.BLACK, MessageEvents.getPostCommand(), SQLiteDataSource.getHelp(MessageEvents.getPostCommand())).build());
                }
            }
        }

        //Else the general help menu will be sent with some reactions to it
        else {
            if(SQLiteDataSource.getHelpID() != null) {
                try {
                    MessageEvents.getEvent().getPrivateChannel().deleteMessageById(SQLiteDataSource.getHelpID()).queue();
                }catch(Exception e) {
                    MessageEvents.getEvent().getChannel().deleteMessageById(SQLiteDataSource.getHelpID()).queue();
                }
            }
            MessageEvents.setId();
            EmbedBuilder info = MessagesFormat.getInfoEmbed("Welcome to the **@DopeBot** help menu.\n\nThis menu will show you all commands you can run!", "commandList");
            try {
                MessageEvents.getEvent().getChannel().sendMessage(info.build()).queue(message -> {
                    message.addReaction("ℹ").queue();
                    message.addReaction("💠").queue();
                    message.addReaction("🎪").queue();
                    message.addReaction("💎").queue();
                    message.addReaction("🎬").queue();
                    message.addReaction("🔦").queue();
                });
            }catch (NullPointerException e) {
                MessageEvents.getEvent().getPrivateChannel().sendMessage(info.build()).queue(message -> {
                    message.addReaction("ℹ").queue();
                    message.addReaction("💠").queue();
                    message.addReaction("🎪").queue();
                    message.addReaction("💎").queue();
                    message.addReaction("🎬").queue();
                    message.addReaction("🔦").queue();
                });
            }
        }
    }
}

//**@DopeBot** invitation [1-1400]
class Invitation implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message has more than 3 words the help menu will be sent with an example
        if(MessageEvents.getSize() > 3 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot does not have permission to create invite then the help menu will be printed with this error message
        else if(!MessageEvents.getGuild().getSelfMember().hasPermission(Permission.CREATE_INSTANT_INVITE))
            MessagesFormat.sendHelp();

            //Else If the message is composed of only 2 words then the defult invitation will be created
        else if (MessageEvents.getSize() == 2)
            MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, "Give them this inite link: " + MessageEvents.getGuild().getTextChannels().get(0).createInvite().setMaxAge(Long.parseLong("10"), TimeUnit.MINUTES)
                    .complete().getUrl()+ "\n Link Expires in 10 minutes"));

            //Else If the message has 3 words and the post command has words and the member has permission to create invites an invitation will be created an sent
        else if(MessageEvents.getPostCommand().matches("[0-9]+") && Long.parseLong(MessageEvents.getPostCommand()) < 1401L && MessageEvents.getMember().hasPermission(Permission.CREATE_INSTANT_INVITE))
            MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, "Give them this inite link: " + MessageEvents.getGuild().getTextChannels().get(0).createInvite().setMaxAge(Long.parseLong(MessageEvents.getPostCommand()), TimeUnit.MINUTES)
                    .complete().getUrl()+ "\n Link Expires in " + MessageEvents.getPostCommand() + " minutes"));

            //Else the help menu will be sent
        else
            MessagesFormat.sendHelp();
    }
}

//**@DopeBot** join
class Join implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is not two words or did not come from the server the help menu with an example will be sent
        if(MessageEvents.getSize() != 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the user or bot does not have permission to manage the server this will be sent
        else if(MessagesFormat.hasPermission(Permission.PRIORITY_SPEAKER))
            MessagesFormat.sendHelp();

            //Else If the bot is already in a voice channel this warning will be sent
        else if (MessageEvents.selfVoiceState.inVoiceChannel())
            MessagesFormat.sendHelp("I'm already in a voice channel");

            //Else If the user is not in a voice channel this will be sent
        else if (!MessageEvents.memberVoiceState.inVoiceChannel())
            MessagesFormat.sendHelp("You need to be in a voice channel for this command to work");

            //Else the bot will join the voice channel
        else {
            MessageEvents.getGuild().getAudioManager().openAudioConnection(MessageEvents.memberVoiceState.getChannel());
            MessagesFormat.send(MessagesFormat.createEmbed(Color.RED, "Connecting to " + MessageEvents.memberVoiceState.getChannel().getName() + " 🔊"));
        }
    }
}

//@DopeBot joke
class Joke implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is not 2 words this will be sent
        if(MessageEvents.getSize() != 2)
            MessagesFormat.sendHelp();

            //Else it will try to get a joke from the API and send it
        else {
            WebUtils.ins.getJSONObject("https://apis.duncte123.me/joke").async((json) -> {
                if (!json.get("success").asBoolean()) {

                    try {
                        MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, "Something went wrong, try again later"));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                }else
                    MessagesFormat.send(new EmbedBuilder().setTitle(json.get("data").get("title").asText(), json.get("data").get("url").asText()).setDescription(json.get("data").get("body").asText()).build());
            });
        }
    }
}

//**@DopeBot** kick [@Member] [@Member]
class Kick implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is not from the guild the help menu will be sent
        if (!MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or member do not have permission to kick members the help menu with an explanation will be sent
        else if(MessagesFormat.hasPermission(Permission.KICK_MEMBERS))
            MessagesFormat.sendHelp();

            //Else If the member only mentioned the bot the help menu with an example will be sent
        else if(MessageEvents.getMembers().size() == 1)
            MessagesFormat.sendHelp();

            //Else it will try to kick all possible mentioned members
        else {
            for(Member user : MessageEvents.getMembers().subList(1, MessageEvents.getMembers().size())) {
                try {
                    MessageEvents.getGuild().kick(user).queue();
                    MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, user.getUser().getName() + " has  been kicked"));
                }catch(HierarchyException e) {
                    MessagesFormat.sendHelp("Cannot kick " + user.getUser().getName() + ", I have equal or lower hierchy than the person");
                }
            }
        }
    }
}

//**@DopeBot** leave
class Leave implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message does not have 2 words or is not from the guild the help menu with an example will be sent
        if(MessageEvents.getSize() != 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else if the bot or member does not have permission to manage the server the help menu with an example will be sent
        else if(MessagesFormat.hasPermission(Permission.PRIORITY_SPEAKER))
            MessagesFormat.sendHelp();

            //Else If the bot and member are not in the same voice channel this will be printed
        else if (!MessageEvents.getGuild().getAudioManager().getConnectedChannel().getMembers().contains(MessageEvents.getEvent().getMember())) {
            MessagesFormat.sendHelp("You have to be in the same voice channel as me to use this");
        }

        //Else it will try to disconnect from the voice channel if it is connected to one
        else {

            try {
                MessageEvents.getGuild().getAudioManager().closeAudioConnection();
                MessagesFormat.send(MessagesFormat.createEmbed(Color.RED, "Disconnected from " + MessageEvents.memberVoiceState.getChannel().getName() + " 🔇"));
            }catch(Exception e) {
                MessagesFormat.sendHelp("I'm not connected to a voice channel");
            }
        }
    }
}

//**@DopeBot** meme
class Meme implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is not 2 words the help menu with an example will be sent
        if(MessageEvents.getSize() != 2)
            MessagesFormat.sendHelp();

            //Else it will get a meme from this API
        else {
            try {
                String input = new BufferedReader(new InputStreamReader(new URL("https://meme-api.herokuapp.com/gimme").openConnection().getInputStream())).readLine();
                MessagesFormat.send(new EmbedBuilder()
                        .setTitle(input.substring(input.indexOf("\"title\":") + "\"title\":\"".length(), input.indexOf("\",\"url\":")))
                        .setImage(input.substring(input.indexOf("\"url\":\"") + "\"url\":\"".length(), input.indexOf("\",\"nsfw\":")))
                        .setDescription(input.substring(input.indexOf("\"postLink\":\"") + "\"postLink\":\"".length(), input.indexOf("\",\"subreddit\":")))
                        .setFooter(SQLiteDataSource.getHelp("footer1"), SQLiteDataSource.getHelp("footer2"))
                        .setColor(Color.CYAN).build());
            } catch(Exception e) {
                MessagesFormat.send(MessagesFormat.createEmbed(Color.CYAN, "Something went wrong please try again"));
            }
        }
    }
}

//**@DopeBot** mute [@Member] [@Member]
class Mute implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is not from the guild the help menu will be sent
        if(!MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or member do not have permission to manage roles the help menu  with an explanation will be sent
        else if(MessagesFormat.hasPermission(Permission.MANAGE_ROLES))
            MessagesFormat.sendHelp();

            //Else If the mentioned members are only 1 the help menu with an example will be sent
        else if(MessageEvents.getMembers().size() == 1)
            MessagesFormat.sendHelp();

            //Else it will mute all possible members
        else {
            for(Member user : MessageEvents.getMembers().subList(1, MessageEvents.getMembers().size())) {
                try {
                    MessageEvents.getGuild().addRoleToMember(user, MessageEvents.getGuild().getRolesByName("mute", true).get(0)).queue();
                    MessagesFormat.send(user.getUser().getName() + " has been muted");
                }catch(HierarchyException e) {
                    MessagesFormat.sendHelp("Can't mute " + user.getUser().getName() + ", I have equal or lower hierchy than the person");
                }
            }
        }
    }
}

//**@DopeBot** nick [@Member] [nickname]
class Nick implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message does not have 2  words or is not from the guild
        if(MessageEvents.getMembers().size() != 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or member do not have permission to change a nickname the help menu will be sent with an explanation
        else if(MessagesFormat.hasPermission(Permission.NICKNAME_CHANGE))
            MessagesFormat.sendHelp();

            //Else If the message does not have 2 mentioned members the help menu will be sent
        else if(MessageEvents.getMembers().size() != 2)
            MessagesFormat.sendHelp();

            //Else it will assign the nickname to the member if it is possible
        else {
            try{
                MessageEvents.getUser().modifyNickname(MessagesFormat.getSentence(3)).queue();
                MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, MessageEvents.getUsername() + " nickname has been set to " + MessagesFormat.getSentence(3)));
            }catch(Exception e) {
                MessagesFormat.sendHelp("Can't modify " + MessageEvents.getUsername() + " nickname, I have equal or lower hierchy than the person");
            }
        }
    }
}


//**@DopeBot** pause
class Pause implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the  message is not 2 words or it is not from the guild the help menu with an example will be sent
        if(MessageEvents.getSize() != 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or member does not have permission to manage the server the help menu will be sent
        else if(MessagesFormat.hasPermission(Permission.PRIORITY_SPEAKER))
            MessagesFormat.sendHelp();

            //Else If no track is playing the help menu will be sent with additional information
        else if (MessageEvents.player.getPlayingTrack() == null)
            MessagesFormat.sendHelp("Cannot pause or resume player because no track is loaded for playing.");

            //Else the music will be paused
        else {
            MessageEvents.player.setPaused(!MessageEvents.player.isPaused());

            if (MessageEvents.player.isPaused())
                MessagesFormat.send(MessagesFormat.createEmbed(Color.RED, "The player has been paused."));
            else
                MessagesFormat.send(MessagesFormat.createEmbed(Color.RED, "The player has resumed playing."));
        }
    }
}

//**@DopeBot** pin [Message]
class Pin implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message only has 2 words or is not from the guild then the help menu will be sent
        if(MessageEvents.getSize() == 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or member does not have permission to manage messages the help menu with an explanation will be sent
        else if(MessagesFormat.hasPermission(Permission.MESSAGE_MANAGE))
            MessagesFormat.sendHelp();

            //Else the message will be pinned and the code to unpin it will be given
        else {
            MessageEvents.getEvent().getMessage().pin().queue();
            MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, MessageEvents.getSelfname() + " your message has been pinned. To unpin use this ID: " + SQLiteDataSource.setPin()));
        }
    }
}

//**@DopeBot** ping
class Ping implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message does not have 2 words the help menu will be sent with an example
        if(MessageEvents.getSize() != 2)
            MessagesFormat.sendHelp();

            //Else the ping will be sent
        else
            MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, "\n:ping_pong:   **Pong!** \n\nYour ping is `" + MessageEvents.getEvent().getJDA().getGatewayPing() + "` milliseconds."));
    }
}

//**@DopeBot** play
class Play implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If message does not have 2 words or was not sent from the guild then the help menu with an example will be sent
        if(MessageEvents.getSize() == 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or member do not have permission to manage the server then the help menu will be sent
        else if(MessagesFormat.hasPermission(Permission.PRIORITY_SPEAKER))
            MessagesFormat.sendHelp();

            //Else If the bot is not in a voice channel the help menu will be sent with this explanation
        else if (!MessageEvents.selfVoiceState.inVoiceChannel())
            MessagesFormat.sendHelp("I need to be in a voice channel for this to work.");

            //Else If the member is not in a voice channel the help menu will be sent with this explanation
        else if (!MessageEvents.memberVoiceState.inVoiceChannel())
            MessagesFormat.sendHelp("You need to be in a voice channel for this command to work");

            //Else If the bot and the member are not in the same voice channel the help menu will be sent with this explanation
        else if (!MessageEvents.memberVoiceState.getChannel().equals(MessageEvents.selfVoiceState.getChannel()))
            MessagesFormat.sendHelp("You need to be in the same voice channel as me for this command to work");

            //Else the youtube serched sentenced will be sent and the music will start
        else {
            String ytSearched = MessagesFormat.searchYoutube();

            if(ytSearched != null) {
                MessagesFormat.send(ytSearched);
                PlayerManager.getInstance().loadAndPlay(MessageEvents.getEvent().getTextChannel(), ytSearched);
            }else
                MessagesFormat.send(MessagesFormat.createEmbed(Color.RED, "An error occured"));
        }
    }
}

//**@DopeBot** playing
class Playing implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message does not have 2 words or was not from a guild then the help menu with an example will be sent
        if(MessageEvents.getSize() != 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or member do not have permission to manage the server the help menu will be sent
        else if(MessagesFormat.hasPermission(Permission.PRIORITY_SPEAKER))
            MessagesFormat.sendHelp();

            //Else If there it nothing playing the help menu will be sent with an explanation
        else if (MessageEvents.player.getPlayingTrack() == null)
            MessagesFormat.sendHelp("The player is not playing any song.");

            //Else the song will be sent with the time it currently has
        else
            MessagesFormat.send(MessagesFormat.createEmbed(Color.RED, String.format("**Playing** [%s] \n(%s) \n%s %s - %s", MessageEvents.info.title, MessageEvents.info.uri, MessageEvents.player.isPaused() ? "⏸" : "▶",
                    String.format("%02d:%02d:%02d", MessageEvents.player.getPlayingTrack().getPosition() / TimeUnit.HOURS.toMillis(1), MessageEvents.player.getPlayingTrack().getPosition() / TimeUnit.MINUTES.toMillis(1),
                            MessageEvents.player.getPlayingTrack().getPosition() % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1)),
                    String.format("%02d:%02d:%02d", MessageEvents.player.getPlayingTrack().getDuration() / TimeUnit.HOURS.toMillis(1), MessageEvents.player.getPlayingTrack().getDuration() / TimeUnit.MINUTES.toMillis(1),
                            MessageEvents.player.getPlayingTrack().getDuration() % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1)))));
    }
}

//**@DopeBot** pm [@member] [message]
class PM implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message has less than  4 words or is not from a guild the help  menu with an example will be sent
        if(!MessageEvents.getEvent().isFromGuild() || MessageEvents.getSize() < 4)
            MessagesFormat.sendHelp();

            //Else  If the bot or user do not have administrator permission the help menu will be sent
        else if(MessagesFormat.hasPermission(Permission.ADMINISTRATOR) || MessageEvents.getMembers().size() != 2)
            MessagesFormat.sendHelp();

            //Else the bot will send a  private message to the mentioned member
        else {
            MessageEvents.getUser().getUser().openPrivateChannel().queue((textChannel) -> {textChannel.sendMessage(MessagesFormat.getSentence(3)).queue();});
            MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, "Message Sent to " + MessageEvents.getUsername() + "!"));
        }
    }
}

//**@DopeBot** purge
class Purge implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message was not sent from a guild or is not 2 words the help menu with an example will be sent
        if(MessageEvents.getSize() != 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or user does not have permission to kick members the help menu will be sent
        else if(MessagesFormat.hasPermission(Permission.KICK_MEMBERS))
            MessagesFormat.sendHelp();

            //Else the bot will kick all possible members in the guild
        else {
            for(int i = 1; i < MessageEvents.getGuild().getMemberCount(); i++) {
                try {
                    MessageEvents.getGuild().kick(MessageEvents.getGuild().getMembers().get(i));
                }catch(Exception e) {
                    MessagesFormat.sendHelp("Could not kick " + MessageEvents.getGuild().getMembers().get(i).getUser().getName());
                }
            }
        }
    }
}

//**@DopeBot** queue
class Queue implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is not 2 words or was not sent from a guild the help menu with an example will be sent
        if(MessageEvents.getSize() != 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or user does not have permission to manage the server the help menu will be sent
        else if(MessagesFormat.hasPermission(Permission.PRIORITY_SPEAKER))
            MessagesFormat.sendHelp();

            //Else If the queue is empty then the bot will let the user know
        else if (MessageEvents.queue.isEmpty())
            MessagesFormat.send(MessagesFormat.createEmbed(Color.RED, "The queue is empty"));

            //Else it will send all available songs in the queue
        else {
            List<AudioTrack> tracks = new ArrayList<>(MessageEvents.queue);
            String trackInfo = "";
            for (int i = 0; i < Math.min(MessageEvents.queue.size(), 20); i++) {
                AudioTrack track = tracks.get(i);
                AudioTrackInfo info = track.getInfo();
                trackInfo += info.title + " - " + info.author + "\n";
            }
            MessagesFormat.send(MessagesFormat.createEmbed(Color.RED, "\n**Current Queue (Total: " + MessageEvents.queue.size() + ") \n\n" + trackInfo));
        }
    }
}

//**@DopeBot** quite
class Quite implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message does not have 2 words or is not from a guild the help menu with an example will be sent
        if(MessageEvents.getSize() != 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or member does not have permission to manage roles the help menu with an explanation will be sent
        else if(MessagesFormat.hasPermission(Permission.MANAGE_ROLES))
            MessagesFormat.sendHelp();

            //Else it will mute every possible member
        else {
            for (Member user : MessageEvents.getEvent().getGuild().getMemberCache()){
                try {
                    MessageEvents.getGuild().addRoleToMember(user, MessageEvents.getGuild().getRolesByName("mute", true).get(0));
                    MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, user.getUser().getName() + ", has been muted"));
                }catch(Exception e) {
                    MessagesFormat.sendHelp("Could not mute " + user.getUser().getName());
                }
            }
        }
    }
}

//**@DopeBot** removenick [@Member]
class RemoveNick implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message does not have 3 words or does not come from a guild the help menu with an example will be sent
        if(MessageEvents.getSize() != 3 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the member did not mentioned 2 users then the help menu will be sent
        else if(MessageEvents.getMembers().size() != 2 || MessagesFormat.hasPermission(Permission.NICKNAME_CHANGE))
            MessagesFormat.sendHelp();

            //Else his nickname will be removed
        else {
            try{
                MessageEvents.getUser().modifyNickname(null).queue();
                MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, MessageEvents.getUsername() + " nickname has been deleted."));
            }catch(Exception e) {
                MessagesFormat.sendHelp("Can't modify " + MessageEvents.getUsername() + " nickname, I have equal or lower hierchy than the person");
            }
        }
    }
}

//**@DopeBot** removerole [@Member] [Role]
class RemoveRole implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message has less than 4 words the help menu with an example will be sent
        if(MessageEvents.getSize() < 4 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or member do not have permission to manage roles or did not mention 2 members the help menu will be sent
        else if(MessagesFormat.hasPermission(Permission.MANAGE_ROLES) || MessageEvents.getMembers().size() != 2)
            MessagesFormat.sendHelp();

            //Else the bot will try to remove the role to the mentioned member
        else {
            try {
                MessageEvents.getGuild().removeRoleFromMember(MessageEvents.getUser(), MessageEvents.getGuild().getRolesByName(MessagesFormat.getSentence(3), true).get(0)).queue();
                MessagesFormat.send(MessagesFormat.getSentence(3) + " role has been removed to " + MessageEvents.getUsername());
            }catch(HierarchyException e) {
                MessagesFormat.sendHelp("Can't remove a role to " + MessageEvents.getUsername() + ", I have equal or lower hierchy than the person");
            }catch(IndexOutOfBoundsException es) {
                String roleList = "";
                for(Role role: MessageEvents.getGuild().getRoles()) {
                    roleList += role.getName() + "\n";
                }
                MessagesFormat.sendHelp("Could not find role. Choose one from the list below.");
                MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, roleList));
            }
        }
    }
}

//**@DopeBot** repeat
class Repeat implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message does not have 2 words or was not from a guild the help menu with an example will be sent
        if(MessageEvents.getSize() != 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or user does not have permission to manage the server the help menu will be sent with an explanation
        else if(MessagesFormat.hasPermission(Permission.PRIORITY_SPEAKER))
            MessagesFormat.sendHelp();

            //Else If the track is not null it will repeat the current track
        else if (MessageEvents.player.getPlayingTrack() != null) {
            MessagesFormat.send(MessagesFormat.createEmbed(Color.RED, "Restarting track: " + MessageEvents.player.getPlayingTrack().getInfo().title + " - " + MessageEvents.player.getPlayingTrack().getInfo().author));
            MessageEvents.player.playTrack(MessageEvents.player.getPlayingTrack().makeClone());
        }

        //Else it will send that no track is playing
        else {
            MessagesFormat.sendHelp("No track has been previously started, so the player cannot replay a track!");
        }
    }
}

//@DopeBot roll [#Dices] [#Sides]
class Roll implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message has more than 4 words the help menu with an example will be sent
        if(MessageEvents.getSize() > 4)
            MessagesFormat.sendHelp();

            //Else if the message has 2 words it will sent the default results
        else if(MessageEvents.getSize() == 2)
            MessagesFormat.send(MessagesFormat.createEmbed(Color.CYAN, "🎲 #1: **" + ThreadLocalRandom.current().nextInt(1, 6) + "**\n"));

            //Else If the message has 3 words and is a valid number it will send the result with custom #Dices
        else if(MessageEvents.getSize() == 3){
            if(MessageEvents.getPostCommand().matches("[0-9]+") && Integer.parseInt(MessageEvents.getPostCommand()) <= 20 && Integer.parseInt(MessageEvents.getPostCommand()) > 0) {
                String result = "";
                for(int i = 0; i < Integer.parseInt(MessageEvents.getPostCommand()); i++) {
                    result += "🎲 #" + (i+1) + ": **" + ThreadLocalRandom.current().nextInt(1, 6) + "**\n";
                }
                MessagesFormat.send(MessagesFormat.createEmbed(Color.CYAN, result));
            }else
                MessagesFormat.sendHelp("Make sure to use a number 1 - 20 for #Dices");

            //Else If the message has valid numbers for #Dices and #Sides it will print the custom number of dices and sides
        }else if(MessageEvents.getPostCommand().matches("[0-9]+") && MessageEvents.getArgs()[3].matches("[0-9]+") && Integer.parseInt(MessageEvents.getPostCommand()) <= 20 &&
                Integer.parseInt(MessageEvents.getPostCommand()) > 0 && Integer.parseInt(MessageEvents.getArgs()[3]) <= 100 && Integer.parseInt(MessageEvents.getArgs()[3]) > 0) {
            String result = "";
            for(int i = 0; i < Integer.parseInt(MessageEvents.getPostCommand()); i++) {
                result += "🎲 #" + (i+1) + ": **" + ThreadLocalRandom.current().nextInt(1, Integer.parseInt(MessageEvents.getArgs()[3])) + "**\n";
            }
            MessagesFormat.send(MessagesFormat.createEmbed(Color.CYAN, result));

            //Else it will send the help command with an explanation why it was invalid
        }else
            MessagesFormat.sendHelp("Make sure to use a number 1 - 20 for #Dices and 1 - 100 for #Sides");
    }
}

//**@DopeBot** say [Message]
class Say implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is only 2 words or is not from a guild the help menu with an example will be sent
        if(MessageEvents.getSize() == 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else if the bot or user does not have administrator permission the help menu with an explanation will be sent
        else if(MessagesFormat.hasPermission(Permission.ADMINISTRATOR))
            MessagesFormat.sendHelp();

            //Else the bot will send the message asked for
        else
            MessagesFormat.send(MessagesFormat.getSentence(2));
    }
}

//**@DopeBot** search [question]
class Search implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is only 2 words it will send the help menu with an example
        if(MessageEvents.getSize() == 2) {
            MessagesFormat.sendHelp();

            //Else it will send a search result for user's input
        }else {
            try {
                new BufferedReader(new InputStreamReader(new URL("https://api.wolframalpha.com/v1/simple?i=" + MessagesFormat.getSentence(2).replace(" ", "+") + "%3F&appid=GTX3V2-2RJEHJRYJA").openConnection().getInputStream())).readLine();
                MessagesFormat.send("https://api.wolframalpha.com/v1/simple?i=" + MessagesFormat.getSentence(2).replace(" ", "+") + "%3F&appid=GTX3V2-2RJEHJRYJA");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                MessagesFormat.sendHelp("Nothing was returned for your input");
            }
        }
    }
}

//**@DopeBot** serverinfo
class ServerInfo implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message does not have 2 words or was not from a guild the help menu with an example will be sent
        if (MessageEvents.getSize() != 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else it will print the information of the server
        else {
            String test = "";
            for (Member user : MessageEvents.getEvent().getGuild().getMemberCache()){
                test += user.getUser().getName() + ". ";
            }
            MessagesFormat.send(new EmbedBuilder().setColor(Color.BLUE).setAuthor(MessageEvents.getEvent().getGuild().getName()).setThumbnail(MessageEvents.getEvent().getGuild().getIconUrl())
                    .addField("Server Owner:", MessageEvents.getEvent().getGuild().getOwner().getEffectiveName(), true).addField("Member Count:", Integer.toString(MessageEvents.getEvent().getGuild()
                            .getMembers().toArray().length), true).setDescription("**Members:** \n" + test + "\n **Invite link:** \n" + "https://discord.gg/8xYBWmt").build());
        }
    }
}

//**@DopeBot** shutdown
class Shutdown implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is not 2 words or was not from a guild the help menu with an example will be sent
        if(MessageEvents.getSize() != 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or user does not have administrator permission the help menu will be sent
        else if(MessagesFormat.hasPermission(Permission.ADMINISTRATOR))
            MessagesFormat.sendHelp();

            //Else it will send a message when the bot shuts down
        else {
            try {
                Dope.jda.shutdown();
                MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, "Extinction in progress..."));
            } catch(Exception e) {
                MessagesFormat.sendHelp("Something went wrong with the command");
            }
        }

//		MessageEvents.getEvent().getChannel().sendMessage("This is a test").queue((message) -> {message.delete().queueAfter(3, TimeUnit.SECONDS);});
    }
}

//**@DopeBot skip
class Skip implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message does not have 2 words or was not sent from a guild the help menu with an example will be sent
        if(MessageEvents.getSize()  != 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or member does not have permission to manage the server the help menu will be sent
        else if(MessagesFormat.hasPermission(Permission.PRIORITY_SPEAKER))
            MessagesFormat.sendHelp();

            //Else If the player is not playing anything the help menu will be sent with an explanation
        else if (PlayerManager.getMusicManager(MessageEvents.getGuild()).audioPlayer.getPlayingTrack() == null)
            MessagesFormat.sendHelp("The player isn't playing aything");

            //Else the bot will try to skip the track if there is one after it
        else {

            int trackCount = Math.min(MessageEvents.queue.size(), 20);
            if(trackCount == 0)
                MessagesFormat.send(MessagesFormat.createEmbed(Color.RED, "The player does not have a song after this"));
            else {
                MessageEvents.scheduler.nextTrack();
                MessagesFormat.send(MessagesFormat.createEmbed(Color.RED, "Skipping the current track"));
            }
        }
    }
}

//**@DopeBot** softban [minutes] [@member] [@member]
class SoftBan implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message has less than 4 words or was not sent from a guild the help menu with an example will be sent
        if(MessageEvents.getSize() < 4 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or member does not have permission to manage roles the help menu will be sent
        else if(MessagesFormat.hasPermission(Permission.MANAGE_ROLES))
            MessagesFormat.sendHelp();

            //Else If the post command does not contain numbers or the mentioned members are 1 the help menu will be sent
        else if(!MessageEvents.getPostCommand().matches("[0-9]+") || MessageEvents.getMembers().size() == 1)
            MessagesFormat.sendHelp();

            //Else the mentioned members will obtain the softban role for the requested time
        else {
            for (Member user : MessageEvents.getMembers().subList(1, MessageEvents.getMembers().size())){
                MessageEvents.getGuild().addRoleToMember(user, MessageEvents.getGuild().getRolesByName("softban", true).get(0)).queue();
                MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, user.getUser().getName() +", has been set to softban for " + MessageEvents.getPostCommand() + " minute(s)."));
                new java.util.Timer().schedule(new java.util.TimerTask() { //runs after x amount of seconds

                    @Override
                    public void run() {
                        MessagesFormat.send("Softban removed for " + user.getUser().getName() + ".");
                        MessageEvents.getGuild().removeRoleFromMember(user, MessageEvents.getGuild().getRolesByName("softban", true).get(0)).complete(); //
                    }
                }, Long.parseLong(MessageEvents.getPostCommand()) * 60000L);
            }
        }
    }
}

//**@DopeBot** stop
class Stop implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message does not have 2 words or is not from a guild the help menu with an example will be sent
        if(MessageEvents.getSize() != 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or member does not have permission to manage the server the help menu will be sent
        else if(MessagesFormat.hasPermission(Permission.PRIORITY_SPEAKER))
            MessagesFormat.sendHelp();

            //Else If the player is not playing a track the help menu will be sent with an explanation
        else if (MessageEvents.player.getPlayingTrack() == null)
            MessagesFormat.sendHelp("The player is not playing any song.");

            //Else the queue will be cleared and it will stop the track
        else {
            MessageEvents.queue.clear();
            MessageEvents.player.stopTrack();
            MessageEvents.player.setPaused(false);
            MessagesFormat.send(MessagesFormat.createEmbed(Color.RED, "Stopping the player and clearing the queue"));
        }
    }
}

//**@DopeBot** ud [word]
class UD implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message has 2 words the help menu with an example will be sent
        if(MessageEvents.getSize() == 2)
            MessagesFormat.sendHelp();

            //Else it gets all valid definitions and sends one to the user
        else {
            ArrayList<String> words = new ArrayList<String>();
            try {
                URL udUrl = new URL("http://api.urbandictionary.com/v0/define?term=" + MessagesFormat.getSentence(2).replace(" ", "%20"));
                BufferedReader bf = new BufferedReader(new InputStreamReader(udUrl.openConnection().getInputStream()));
                String input = bf.readLine();
                while(true) {
                    if(input.contains("\"definition\":")) {
                        words.add(input.substring(input.indexOf("\"definition\":") + "\"definition\":\"".length(), input.indexOf("\",\"permalink\":")).replace("\\r", "")
                                .replace("\\n", "").replace("[", "").replace("]", ""));
                        input = input.substring(input.indexOf("\",\"permalink\":") + "\",\"permalink\":\"".length(), input.length());
                    }else {
                        break;
                    }
                }
                MessagesFormat.send(new EmbedBuilder().setTitle("UD: " + MessagesFormat.getSentence(2).toUpperCase()).setDescription(words.get(new Random().nextInt(words.size())))
                        .setThumbnail("https://wjlta.files.wordpress.com/2013/07/ud-logo.jpg").setColor(Color.CYAN).build());
            }catch (Exception e){
                MessagesFormat.sendHelp("There was an error while trying to find a definition for " + MessagesFormat.getSentence(2));
            }
        }
    }
}

//**@DopeBot** unban [Member] [Member]
class Unban implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is only 2 words or did not come from a guild the help menu will be sent with an example
        if(MessageEvents.getSize() == 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else if the bot or member does not have permission to ban the help menu will be sent
        else if(MessagesFormat.hasPermission(Permission.BAN_MEMBERS))
            MessagesFormat.sendHelp();

            //Else it will go through the list and unban all possible members
        else {
            for(int i = 2; i < MessageEvents.getArgs().length; i++) {
                String[] user = MessageEvents.getArgs();

                if(SQLiteDataSource.isBanned(user[i])) {
                    try {
                        MessageEvents.getGuild().unban(SQLiteDataSource.getUserID(user[i])).queue();
                        MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, user[i] + " Unbanned"));
                        SQLiteDataSource.updateBanned(user[i], "false");
                    }catch(ErrorResponseException ex) {
                        MessagesFormat.sendHelp();
                    }
                }else {
                    MessagesFormat.sendHelp("Could not find " + user[i] + " in the banned list");
                }
            }
        }
    }
}

//**@DopeBot** unmute [@Member] [@Member]
class Unmute implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is 2 words or was not sent from a guild the help menu with an example will be sent
        if(MessageEvents.getSize() == 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else if the bot or member do not have permission to manage roles or only one member was mentioned the help menu will be sent
        else if(MessageEvents.getMembers().size() == 1 || MessagesFormat.hasPermission(Permission.MANAGE_ROLES))
            MessagesFormat.sendHelp();

            //Else it will try to unmute all mentioned members
        else {
            for (Member user: MessageEvents.getMembers().subList(1, MessageEvents.getMembers().size())){
                try {
                    MessageEvents.getGuild().removeRoleFromMember(user, MessageEvents.getGuild().getRolesByName("mute", true).get(0)).queue();
                    MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, user.getUser().getName() + " has been unmuted"));
                }catch(HierarchyException e) {
                    MessagesFormat.sendHelp("Can't unmute " + user.getUser().getName() + ", I have equal or lower hierchy than the person");
                }
            }
        }
    }
}

//**@DopeBot** unpin [ID]
class Unpin implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is not 3 words or was not from a guild the help menu will be sent with an example
        if(MessageEvents.getSize() != 3 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or member do not have permission to manage messages the help menu will be sent
        else if(MessagesFormat.hasPermission(Permission.MESSAGE_MANAGE))
            MessagesFormat.sendHelp();

            //Else it will try to unpin the message with the given id
        else {
            try {
                MessageEvents.getEvent().getChannel().unpinMessageById(SQLiteDataSource.unPin(MessageEvents.getPostCommand())).queue();
            }catch (Exception e) {
                MessagesFormat.sendHelp("pinID not available");
                return;
            }
            MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, "Message Unpinned"));
        }
    }
}

//**@DopeBot** unquite
class Unquite implements BotActions {

    @Override
    public void execute() throws SQLException {

        //
        if(MessageEvents.getSize() != 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or member do not have permission to manage roles the help menu with an explanation will be sent
        else if(MessagesFormat.hasPermission(Permission.MANAGE_ROLES))
            MessagesFormat.sendHelp();

            //Else it will unmute all possible users
        else {

            for(Member user: MessageEvents.getGuild().getMembersWithRoles(MessageEvents.getGuild().getRolesByName("mute", true))) {
                try {
                    MessageEvents.getGuild().removeRoleFromMember(user, MessageEvents.getGuild().getRolesByName("mute", true).get(0)).queue();
                    MessagesFormat.send(MessagesFormat.createEmbed(Color.BLUE, user.getUser().getName() + " has been unmuted"));
                }catch(HierarchyException e) {
                    MessagesFormat.sendHelp("Can't unmute " + user.getUser().getName() + ", I have equal or lower hierchy than the person");
                }catch(IndexOutOfBoundsException ex) {
                    MessagesFormat.sendHelp("Could not find role.");
                }
            }
        }
    }
}

//@DopeBot user [@Member] [@Member]
class Users implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message is not 3 words or from a guild the help menu with an example will be sent
        if(MessageEvents.getSize() == 2 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the member didn't mention someone else the help menu will be sent
        else if(MessageEvents.getMembers().size() == 1)
            MessagesFormat.sendHelp();

            //Else the mentioned member's information will be sent
        else {
            for(Member user: MessageEvents.getMembers().subList(1, MessageEvents.getMembers().size())) {
                EmbedBuilder embed = new EmbedBuilder();
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd-yyyy");
                embed.setColor(Color.BLUE);
                try {
                    if(!user.getUser().getAvatarUrl().equals(null)) {
                        embed.setThumbnail(user.getUser().getAvatarUrl());
                    }
                }catch(Exception e) {
                    embed.setThumbnail(user.getUser().getDefaultAvatarUrl());
                }
                String roles;
                if (!user.getRoles().isEmpty()){
                    Role tempRole = (Role) user.getRoles().get(0);
                    roles = tempRole.getName();
                    for (int i = 1; i < user.getRoles().size(); i++){
                        tempRole = (Role) user.getRoles().get(i);
                        roles = roles + ", " + tempRole.getName();
                    }
                }else{
                    roles = "No Roles";
                }
                String activity = "";
                try{
                    List<Activity> game = user.getActivities();
                    for(Activity x : game) {
                        activity += x.getName() + " "; /////////RichPresence(Destiny 2 / 438122941302046720) is for playing      Activity(POP HITS) is for music
                    }
                }catch (NullPointerException exx){
                    activity = "No Game Being Played";
                }
                embed.setTitle("Information on " + user.getUser().getName());
                embed.setDescription(user.getUser().getName() + " joined " + MessageEvents.getEvent().getGuild().getName()  + " on " + user.getTimeJoined().format(fmt));
                embed.addField("Activity:", user.getActivities().size() == 0 ? "No Activity" : activity, true);
                embed.addField("Status: ", user.getOnlineStatus().toString(), true);
                embed.addField("Roles: ", roles, true);
                embed.addField("Nickname: ", user.getNickname() == null ? "No Nickname" : MessageEvents.getUser().getNickname(), true);
                MessagesFormat.send(embed.build());
            }
        }
    }
}

//**@DopeBot** volume [number]
class Volume implements BotActions {

    @Override
    public void execute() throws SQLException {

        //If the message has more than 3 words or was not from a guild the help menu with an example will be sent
        if(MessageEvents.getSize() > 3 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or user does not have permission to manage the server the help menu will be sent
        else if(MessagesFormat.hasPermission(Permission.PRIORITY_SPEAKER))
            MessagesFormat.sendHelp();

            //Else If the message is only two words it will send information on the current volume
        else if (MessageEvents.getSize() == 2)
            MessagesFormat.send(MessagesFormat.createEmbed(Color.RED, "Current player volume: **" + MessageEvents.player.getVolume() + "**"));

            //Else it will try to change the volume of the player
        else {
            try {
                MessageEvents.player.setVolume(Math.max(10, Math.min(100, Integer.parseInt(MessageEvents.getPostCommand()))));
                MessagesFormat.send(MessagesFormat.createEmbed(Color.RED, "Player volume changed from **" + MessageEvents.player.getVolume() + "** to **" +
                        Math.max(10, Math.min(100, Integer.parseInt(MessageEvents.getPostCommand()))) + "**"));
            }catch (NumberFormatException e) {
                MessagesFormat.sendHelp("**" + MessageEvents.getPostCommand() + "** is not a valid integer. (10 - 100)");
            }
        }
    }
}

//**@DopeBot** warn [@Member]
class Warn implements BotActions {

    public void timeout(String number, String strikes) throws SQLException {
        SQLiteDataSource.setWarnings(number);
        MessagesFormat.send("Feel the heat! " + MessageEvents.getUsername() + " has received their " + strikes + " strike! :fire:");
        MessageEvents.getGuild().addRoleToMember(MessageEvents.getUser(), MessageEvents.getGuild().getRolesByName("soft ban", true).get(0)).queue();
        MessagesFormat.send((MessageEvents.getUsername() +" has been set to softban for 10 minutes."));
        new java.util.Timer().schedule(new java.util.TimerTask() { //runs after x amount of seconds

            @Override
            public void run() {
                MessagesFormat.send(("Softban removed for " + MessageEvents.getUsername() + "."));
                MessageEvents.getGuild().removeRoleFromMember(MessageEvents.getUser(), MessageEvents.getGuild().getRolesByName("soft ban", true).get(0)).complete(); //
            }
        }, 600000L);
    }

    @Override
    public void execute() throws SQLException{

        //If the message does not have 3 words or was not from a guild the help menu with an example will be sent
        if(MessageEvents.getSize() != 3 || !MessageEvents.getEvent().isFromGuild())
            MessagesFormat.sendHelp();

            //Else If the bot or user do not have permission to manage roles or didn't mention 2 people the help menu will be sent with an explanation
        else if(MessageEvents.getMembers().size() != 2 || MessagesFormat.hasPermission(Permission.MANAGE_ROLES))
            MessagesFormat.sendHelp();

            //Else it will use a switch to see how many warnings it has and execute their corresponding procedure
        else {
            switch(SQLiteDataSource.getWarnings()) {
                case "0":
                    timeout("1", "1st");
                    break;
                case "1":
                    timeout("2", "2nd");
                    break;
                case "2":
                    timeout("3", "3rd");
                    break;
                case "3":
                    SQLiteDataSource.updateBanned(MessageEvents.getUsername(), "true");
                    SQLiteDataSource.setWarnings("0");
                    MessageEvents.getBotCommands().get("ban").execute();
                    break;
                default:
                    break;
            }
        }
    }
}

//**@DopeBot** about
class About implements BotActions{

    @Override
    public void execute() throws SQLException {

        //If the message have 2 words the menu about the bot will be sent
        if(MessageEvents.getSize() == 2)
            MessagesFormat.send(new EmbedBuilder().setTitle("Hello! " + MessageEvents.getEvent().getMessage().getAuthor().getName()).setColor(Color.BLUE)
                    .setFooter(SQLiteDataSource.getHelp("footer1"), SQLiteDataSource.getHelp("footer2")).addField("Need some help?", "Join our [Discord Server](https://discord.gg/nWWBYJ9)", true)
                    .setDescription("I'm **@DopeBot.** Created by **Jaime 💎!**\nBuilt using [Java 14](https://docs.oracle.com/en/java/javase/14/) and [JDA 4](https://github.com/DV8FromTheWorld/JDA/wiki)").build());
    }
}

//**@DopeBot** calc [expression]
class Calc implements BotActions{

    @Override
    public void execute() throws SQLException {

        //If the message only has 2 words the help menu with an example will be sent
        if(MessageEvents.getSize() == 2)
            MessagesFormat.sendHelp();

            //Else it will send the result of the operation
        else {
            try {
                URL calcUrl = new URL("http://api.mathjs.org/v4/?expr=" + MessagesFormat.getSentence(2).replace(" ", "%20").replace("+", "%2B").replace("/", "%2F").replace("^", "%5E") + "&precision=3");
                BufferedReader bf = new BufferedReader(new InputStreamReader(calcUrl.openConnection().getInputStream()));
                MessagesFormat.send(MessagesFormat.createEmbed(Color.LIGHT_GRAY, "Result for " + MessagesFormat.getSentence(2) + ":\n\n" + bf.readLine()));
            }catch (Exception e){
                MessagesFormat.sendHelp("Something went wrong when calculating " + MessagesFormat.getSentence(2));
            }
        }
    }
}
