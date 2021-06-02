package me.jazzyjake.listeners;

import me.jazzyjake.embeds.*;
import me.jazzyjake.main.ManifestoBotMain;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CommandListener extends ListenerAdapter {
    private static final Message.MentionType[] ALL_MENTION_TYPES = {Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.EVERYONE, Message.MentionType.HERE, Message.MentionType.ROLE, Message.MentionType.USER};
    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    private static boolean onHold = false;
    private static LocalDateTime holdEndTime;

    // Used to store 1) The command usage 2) The parameters and a description of each parameter
    private static final HashMap<String, String> commandUsages = new HashMap<>();
    private static final HashMap<String, Map<String, String>> commandParameters = new HashMap<>();

    static {
        commandUsages.put("!getmanifesto", "!getmanifesto <id>");
        commandParameters.put("!getmanifesto", Map.of("id", "The id of the manifesto wanted"));
    }

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

                                    MessageEmbed embed = new ManifestoEmbedBuilder(result.getString("manifesto"), result.getString("manifestoer")).build();
                                    Message randomManifestoMessage = new MessageBuilder()
                                            .denyMentions(ALL_MENTION_TYPES)
                                            .setEmbed(embed)
                                            .build();

                                    // Sends the manifesto and adds the garbage can reaction
                                    event.getChannel().sendMessage(randomManifestoMessage)
                                            .flatMap(m -> m.addReaction("\uD83D\uDDD1"))
                                            .queue();

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
                        MessageEmbed cooldownEmbed = new CooldownEmbedBuilder(secondsLeft).build();

                        event.getAuthor().openPrivateChannel()
                                .flatMap(p -> p.sendMessage(cooldownEmbed))
                                .queue();
                    }
                    return;
                case "!getmanifesto":
                    if (params.length < 2) {
                        event.getChannel().sendMessage(new InvalidUsageEmbedBuilder(commandUsages.get("!getmanifesto"), commandParameters.get("!getmanifesto")).build()).queue();
                    } else {
                        if (!onHold) {
                            // Gets a connection to the database
                            try (Connection conn = DriverManager.getConnection(ManifestoBotMain.DERBY_PROTOCOL + ManifestoBotMain.DERBY_NAME)) {
                                // Retrieves the requested manifesto
                                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM manifestos WHERE id=?")) {
                                    // Checks if the 2nd parameter is a valid number
                                    try {
                                        ps.setInt(1, Integer.parseInt(params[1]));
                                    } catch (NumberFormatException e) {
                                        event.getChannel().sendMessage(new ExceptionEmbedBuilder(e).build()).queue();
                                        break;
                                    }

                                    try (ResultSet result = ps.executeQuery()) {
                                        if (result.next()) {
                                            // Creates an embed with the manifesto and then a message with the embed as the content
                                            MessageEmbed embed = new ManifestoEmbedBuilder(result.getString("manifesto"), result.getString("manifestoer")).build();
                                            Message randomManifestoMessage = new MessageBuilder()
                                                    .denyMentions(ALL_MENTION_TYPES)
                                                    .setEmbed(embed)
                                                    .build();

                                            // Sends the manifesto and adds the garbage can reaction
                                            event.getChannel().sendMessage(randomManifestoMessage)
                                                    .flatMap(m -> m.addReaction("\uD83D\uDDD1"))
                                                    .queue();

                                            startMinuteCooldown();
                                        } else {
                                            /*
                                             * If no manifesto with the given embed was found
                                             */

                                            MessageEmbed embed = new ManifestoNotFoundEmbedBuilder(Integer.parseInt(params[1])).build();
                                            event.getChannel().sendMessage(embed).queue();
                                        }
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
                            MessageEmbed cooldownEmbed = new CooldownEmbedBuilder(secondsLeft).build();

                            event.getAuthor().openPrivateChannel()
                                    .flatMap(p -> p.sendMessage(cooldownEmbed))
                                    .queue();
                        }
                    }
                    break;
                case "!manifestohelp":
                    // Gets all commands from commandUsages
                    Set<String> commands = commandUsages.keySet();

                    ArrayList<MessageEmbed.Field> fields = new ArrayList<>();

                    // Creates a field for each command
                    for (String command : commands) {
                        String properUsage = commandUsages.get(command);
                        Map<String, String> parameters = commandParameters.get(command);

                        StringBuilder value = new StringBuilder();
                        value.append("Proper Usage:\n");
                        value.append("`" + properUsage + "`\n\n");

                        value.append("Parameters:\n");
                        parameters.forEach((p, d) -> value.append("`" + p + "`: " + d));

                        fields.add(new MessageEmbed.Field(command, value.toString(), false));
                    }

                    EmbedBuilder helpBuilder = new EmbedBuilder()
                            .setTitle("Manifesto Bot Commands")
                            .setColor(Color.GRAY);

                    // Adds each one of the command fields to helpBuilder
                    fields.forEach(helpBuilder::addField);

                    event.getChannel().sendMessage(helpBuilder.build()).queue();
                    break;
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
