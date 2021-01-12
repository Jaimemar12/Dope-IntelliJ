import java.awt.Color;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.TextChannel;


public class PlayerManager {
    public static PlayerManager INSTANCE = null;
    private static Map<Long, GuildMusicManager> musicManagers;
    private static AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        musicManagers = new HashMap<>();
        audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    public static GuildMusicManager getMusicManager(net.dv8tion.jda.api.entities.Guild guild) {
        long guildId = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if(musicManager == null) {
            musicManager = new GuildMusicManager(audioPlayerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public void loadAndPlay(TextChannel channel, String trackUrl) {
        GuildMusicManager musicManager = getMusicManager(channel.getGuild());

        audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("ADDING TO QUEUE: ");
                try {
                    embed.setFooter(SQLiteDataSource.getHelp("footer1"), SQLiteDataSource.getHelp("footer2"));
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                embed.setColor(Color.RED);
                embed.setDescription(track.getInfo().author + ": " + track.getInfo().title);
                channel.sendMessage(embed.build()).queue();
                play(musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().remove(0);
                }
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("ADDING TO QUEUE: ");
                try {
                    embed.setFooter(SQLiteDataSource.getHelp("footer1"), SQLiteDataSource.getHelp("footer2"));
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                embed.setColor(Color.RED);
                embed.setDescription(firstTrack.getInfo().author + ": " + firstTrack.getInfo().title);
                channel.sendMessage(embed.build()).queue();

                play(musicManager, firstTrack);

                playlist.getTracks().forEach(musicManager.scheduler::queue);
            }

            @Override
            public void noMatches() {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("ADDING TO QUEUE: ");
                try {
                    embed.setFooter(SQLiteDataSource.getHelp("footer1"), SQLiteDataSource.getHelp("footer2"));
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                embed.setColor(Color.RED);
                embed.setDescription("Nothing found by " + trackUrl);
                channel.sendMessage(embed.build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("ADDING TO QUEUE: ");
                try {
                    embed.setFooter(SQLiteDataSource.getHelp("footer1"), SQLiteDataSource.getHelp("footer2"));
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                embed.setColor(Color.RED);
                embed.setDescription("Could not play: " + exception.getMessage());
                channel.sendMessage(embed.build()).queue();
            }
        });
    }

    public void play(GuildMusicManager musicManager, AudioTrack track) {
        musicManager.scheduler.queue(track);
    }

    public static PlayerManager getInstance() {

        if(INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }

        return INSTANCE;
    }
}

class AudioPlayerSendHandler implements AudioSendHandler {
    private AudioPlayer audioPlayer = null;
    private ByteBuffer buffer = null;
    private  MutableAudioFrame frame = null;

    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.buffer = ByteBuffer.allocate(1024);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        // TODO Auto-generated method stub
        return this.audioPlayer.provide(this.frame);
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        // TODO Auto-generated method stub
        return this.buffer.flip();
    }

    @Override
    public boolean isOpus() {
        // TODO Auto-generated method stub
        return true;
    }

}

class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }

    public BlockingQueue<AudioTrack> getQueue(){

        return queue;
    }

    public void nextTrack() {
        player.startTrack(queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }
}

class GuildMusicManager {
    public AudioPlayer audioPlayer;
    public TrackScheduler scheduler;
    public AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager manager) {
        audioPlayer = manager.createPlayer();
        scheduler = new TrackScheduler(audioPlayer);
        audioPlayer.addListener(scheduler);
        sendHandler = new AudioPlayerSendHandler(audioPlayer);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(audioPlayer);
    }
}