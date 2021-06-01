package me.jazzyjake.listeners;

import me.jazzyjake.main.ManifestoBotMain;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.*;

public class CommandListener extends ListenerAdapter {
    private static final Message.MentionType[] ALL_MENTION_TYPES = {Message.MentionType.CHANNEL, Message.MentionType.EMOTE, Message.MentionType.EVERYONE, Message.MentionType.HERE, Message.MentionType.ROLE, Message.MentionType.USER};

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] params = event.getMessage().getContentStripped().split(" ");

        // Checks to see if the message starts with !
        if (event.getMessage().getContentStripped().startsWith("!")) {
            switch (params[0]) {
                // TODO Add the reaction delete feature and 1 minute cool-down
                case "!randommanifesto":
                    // Gets a connection to the database
                    try (Connection conn = DriverManager.getConnection(ManifestoBotMain.DERBY_PROTOCOL + ManifestoBotMain.DERBY_NAME)) {
                        // Retrieves a random manifesto
                        try (PreparedStatement randomManifesto = conn.prepareStatement("SELECT * FROM manifestos ORDER BY RANDOM() {LIMIT 1}")) {
                            try (ResultSet result = randomManifesto.executeQuery()) {
                                result.next();

                                MessageEmbed embed = new EmbedBuilder()
                                        .setTitle("Random Manifesto:")
                                        .setDescription(result.getString("manifesto"))
                                        .addField("Added to manifesto list by:", result.getString("manifestoer"), false)
                                        .setColor(Color.GRAY)
                                        .build();
                                Message randomManifestoMessage = new MessageBuilder()
                                        .denyMentions(ALL_MENTION_TYPES)
                                        .setEmbed(embed)
                                        .build();

                                event.getChannel().sendMessage(randomManifestoMessage).queue();
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
            }
        }
    }
}
