package me.jazzyjake.listeners;

import me.jazzyjake.main.ManifestoBotMain;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.sql.*;
import java.util.jar.Manifest;

public class ManifestoListener extends ListenerAdapter {
    private static final Logger log = LogManager.getLogger(ManifestoListener.class);
    private static final MessageEmbed NO_SELF_MANIFESTO_EMBED = new EmbedBuilder()
            .setTitle("Manifesto Not Added!")
            .setDescription("Self-manifestos not allowed")
            .setColor(Color.RED)
            .build();
    private static final MessageEmbed BOT_MANIFESTO_EMBED = new EmbedBuilder()
            .setTitle("Manifesto Not Added!")
            .setDescription("Manifestos from bots not allowed")
            .setColor(Color.RED)
            .build();
    private static final MessageEmbed DUPLICATE_MANIFESTO_EMBED = new EmbedBuilder()
            .setTitle("Manifesto Not Added!")
            .setDescription("Duplicate manifesto")
            .setColor(Color.RED)
            .build();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // If the message equals !manifesto
        if (event.getMessage().getContentRaw().equals("!manifesto")) {
            // Gets the message before the !manifesto message
            MessageHistory history = event.getChannel().getHistoryBefore(event.getMessageId(), 1).complete();
            Message manifesto = history.getRetrievedHistory().get(0);

            // TODO Add check to prevent manifestos matching a blacklist
            if (event.getAuthor().getId().equals(manifesto.getAuthor().getId())) {
                /*
                 * If the manifesto author id is the same as the manifestoer(?) id
                 */

                // Private message the NO_SELF_MANIFESTO_EMBED to the user
                PrivateChannel privateChannel = event.getAuthor().openPrivateChannel().complete();
                privateChannel.sendMessage(NO_SELF_MANIFESTO_EMBED).queue();
            } else if (manifesto.getAuthor().isBot()) {
                /*
                 * If the manifesto is from a bot
                 */

                // Private message the BOT_MANIFESTO_EMBED to the user
                PrivateChannel privateChannel = event.getAuthor().openPrivateChannel().complete();
                privateChannel.sendMessage(BOT_MANIFESTO_EMBED).queue();
            } else {
                /*
                 * If the manifesto is valid
                 */

                String manifestoContent = manifesto.getContentRaw();

                // Create a connection to the derby database
                try (Connection conn = DriverManager.getConnection(ManifestoBotMain.DERBY_PROTOCOL + ManifestoBotMain.DERBY_NAME)) {
                    // Check for duplicate manifestos
                    try (PreparedStatement dupeCheck = conn.prepareStatement("SELECT COUNT(*) AS DupeCount FROM manifestos WHERE manifesto=?")) {
                        // Set manifesto parameter
                        dupeCheck.setString(1, manifestoContent);

                        // Check dupes count
                        try (ResultSet dupes = dupeCheck.executeQuery()) {
                            dupes.next();

                            // If duplicates were found
                            if (dupes.getInt("DupeCount") > 0) {
                                // Private message the DUPLICATE_MANIFESTO_EMBED to the user
                                PrivateChannel privateChannel = event.getAuthor().openPrivateChannel().complete();
                                privateChannel.sendMessage(DUPLICATE_MANIFESTO_EMBED).queue();
                                return;
                            }
                        }
                    }

                    // Prepare insert query
                    try (PreparedStatement insertManifesto = conn.prepareStatement("INSERT INTO manifestos VALUES (DEFAULT, ?, ?)")) {
                        // Set parameters
                        insertManifesto.setString(1, manifestoContent);
                        insertManifesto.setString(2, event.getAuthor().getAsTag());

                        // Execute query
                        insertManifesto.executeUpdate();
                    }

                    // Logs the manifesto addition
                    log.info("Manifesto added to database by {}: {}", event.getAuthor().getAsTag(), manifestoContent);
                } catch (SQLException e) {
                    // TODO Log exception here(?)
                    e.printStackTrace();
                }
            }
        }
    }
}
