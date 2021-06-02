package me.jazzyjake.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.Map;

public class InvalidUsageEmbedBuilder extends EmbedBuilder {
    public InvalidUsageEmbedBuilder(String properUsage, Map<String, String> parameterDescriptions) {
        this.setTitle("Invalid Command Usage!");

        StringBuilder sb = new StringBuilder();
        sb.append("Proper Usage: \n");
        sb.append("`" + properUsage + "`\n\n");

        sb.append("Parameters:\n");
        if (!(parameterDescriptions == null)) {
            // Appends each parameter and its description in the following format: `parameter`: description
            parameterDescriptions.forEach((p, d) -> sb.append("`" + p + "`: " + d + "\n"));
        } else {
            sb.append("None");
        }

        this.setDescription(sb.toString());
        this.setColor(Color.RED);
    }
}
