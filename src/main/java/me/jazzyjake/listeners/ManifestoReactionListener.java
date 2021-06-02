package me.jazzyjake.listeners;

import me.jazzyjake.embeds.ExceptionEmbedBuilder;
import me.jazzyjake.main.ManifestoBotMain;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ManifestoReactionListener extends ListenerAdapter {
    private static final Logger log = LogManager.getLogger(ManifestoReactionListener.class);

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        // Checks if the emoji is the garbage can emoji (Unicode: U+1F5D1)
        if (event.getReactionEmote().isEmoji() && event.getReactionEmote().getAsReactionCode().equals("\uD83D\uDDD1")) {
            // Gets the message
            Message eventMessage = event.retrieveMessage().complete();

            // Checks if the message author is Manifesto Bot
            if (eventMessage.getAuthor().equals(ManifestoBotMain.getJDA().getSelfUser())) {
                // Checks if the message is 1. An embed 2. Has the title "Retrieved Manifesto:"
                if (!eventMessage.getEmbeds().isEmpty() && eventMessage.getEmbeds().get(0).getTitle().equals("Retrieved Manifesto:")) {
                    // Checks if the garbage can reaction count is over DELETE_REACTION_LIMIT
                    if (eventMessage.getReactions().get(0).getCount() > ManifestoBotMain.DELETE_REACTION_LIMIT) {
                        // Gets the content of the message (Used to remove the manifesto from the database)
                        String manifesto = eventMessage.getEmbeds().get(0).getDescription();

                        // Removes the ` (code block annotation) from the beginning and end
                        manifesto = manifesto.substring(1, manifesto.length() - 1);

                        // Deletes the message
                        eventMessage.delete().queue();

                        // Deletes the manifesto from the database
                        try (Connection con = DriverManager.getConnection(ManifestoBotMain.DATABASE_URL)) {
                            try (PreparedStatement ps = con.prepareStatement("DELETE FROM manifestos WHERE manifesto=?")) {
                                ps.setString(1, manifesto);

                                ps.executeUpdate();

                                log.info("Deleted manifesto from database: \"{}\"", manifesto.length() > 50 ? manifesto.substring(0, 50) + "..." : manifesto);
                            }
                        } catch (SQLException e) {
                            event.getChannel().sendMessage(new ExceptionEmbedBuilder(e).build());
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}

/**
 * @deprecated Look at this utter piece of shit
 *
 * Last touched: 5/31/2021
 * // This code is shit...but it's the only way
 * // sigh...
 * // Gets a stream of MessageReaction from the message and filters the stream down to (hopefully) a single result using two checks
 * // 1. Is the reaction an emoji? 2. Is the reaction the garbage can (Unicode: U+1F5D1)
 * Stream<MessageReaction> messageReactionStream = event.retrieveMessage().complete().getReactions().stream();
 * MessageReaction deletionEmoji = messageReactionStream.filter(r -> r.getReactionEmote().isEmoji() && r.getReactionEmote().getEmoji().equals("U+1F5D1")).findFirst().get();
 */
