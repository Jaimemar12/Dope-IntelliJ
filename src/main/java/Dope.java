import java.sql.SQLException;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Dope {
    public static JDA jda;
    public static String prefix = "<@!709436765693542450>"; //@DopeBot computer
    public static String prefixM = "<@709436765693542450>"; //@DopeBot mobile

    public static void main(String[] args) throws LoginException, SQLException {

        jda = JDABuilder.createDefault("NzA5NDM2NzY1NjkzNTQyNDUw.Xrl4eQ.P-ddjnmzPAo2I99eoBd9IgeHckA").setEnabledIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES
                , GatewayIntent.GUILD_EMOJIS, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGES).build();
        jda.getPresence().setStatus(OnlineStatus.ONLINE);
        jda.getPresence().setActivity(Activity.playing("@DopeBot help"));
        jda.addEventListener(new Formats());
        jda.addEventListener(new GuildMemberJoin());
        jda.addEventListener(new GuildEvent());
        jda.addEventListener(new GuildMemberLeave());
        jda.addEventListener(new MessageEvents());
    }
}