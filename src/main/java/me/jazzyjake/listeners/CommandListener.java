package me.jazzyjake.listeners;

import me.jazzyjake.embeds.ExceptionEmbedBuilder;
import me.jazzyjake.main.ManifestoBotMain;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CommandListener extends ListenerAdapter {
    private static final Message.MentionType[] ALL_MENTION_TYPES = {Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.EVERYONE, Message.MentionType.HERE, Message.MentionType.ROLE, Message.MentionType.USER};
    private static boolean onHold = false;
    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private static LocalDateTime holdEndTime;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] params = event.getMessage().getContentStripped().split(" ");

        // Checks to see if the message starts with !
        if (event.getMessage().getContentStripped().startsWith("!")) {
            switch (params[0]) {
                case "!randommanifesto":
                    if (!onHold) {
                        // Gets a connection to the database
                        try (Connection conn = DriverManager.getConnection(ManifestoBotMain.DERBY_PROTOCOL + ManifestoBotMain.DERBY_NAME)) {
                            // Retrieves a random manifesto
                            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM manifestos ORDER BY RANDOM() {LIMIT 1}")) {
                                try (ResultSet result = ps.executeQuery()) {
                                    result.next();

                                    // Creates an embed with the manifesto and then a message with the embed as the content
                                    MessageEmbed embed = new EmbedBuilder()
                                            .setTitle("Retrieved Manifesto:")
                                            .setDescription(result.getString("manifesto"))
                                            .addField("Added to manifesto list by:", result.getString("manifestoer"), false)
                                            .setColor(Color.GRAY)
                                            .build();
                                    Message randomManifestoMessage = new MessageBuilder()
                                            .denyMentions(ALL_MENTION_TYPES)
                                            .setEmbed(embed)
                                            .build();

                                    Message sentManifesto = event.getChannel().sendMessage(randomManifestoMessage).complete();

                                    // Adds the garbage can reaction to the sent manifesto
                                    sentManifesto.addReaction("\uD83D\uDDD1").queue();

                                    // Begins the minute cool-down
                                    startMinuteCooldown();
                                }
                            }
                        } catch (SQLException e) {
                            event.getChannel().sendMessage(new ExceptionEmbedBuilder(e).build());
                            e.printStackTrace();
                        }
                    } else {
                        LocalDateTime now = LocalDateTime.now();

                        // Finds the seconds left on the cool-down
                        int minuteDifference = holdEndTime.getMinute() - now.getMinute();
                        int secondDifference = holdEndTime.getSecond() - now.getSecond();
                        int secondsLeft = (minuteDifference * 60) + secondDifference;

                        // Creates an embed with the amount of seconds left
                        MessageEmbed cooldownEmbed = new EmbedBuilder()
                                .setTitle("Please wait!")
                                .setDescription("Bot is on cooldown for **" + Integer.toString(secondsLeft) + "** more seconds")
                                .setColor(Color.RED)
                                .build();

                        PrivateChannel privateChannel = event.getAuthor().openPrivateChannel().complete();
                        privateChannel.sendMessage(cooldownEmbed).queue();
                    }
                    return;
                // TODO Implement !getmanifesto command using id
                case "!getmanifesto":
            }
        }
    }

    private static void startMinuteCooldown() {
        // Sets holdEndTime to one minute in the future
        holdEndTime = LocalDateTime.now().plusMinutes(1);
        ManifestoBotMain.getJDA().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);

        onHold = true;
        scheduledExecutor.schedule(() -> {
            onHold = false;
            holdEndTime = null;
        }, 1, TimeUnit.MINUTES);

        ManifestoBotMain.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
    }
}
