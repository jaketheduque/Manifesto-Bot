package me.jazzyjake.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class ExceptionEmbedBuilder extends EmbedBuilder {
    public ExceptionEmbedBuilder(Exception e) {
        this.setTitle("Error Encountered!");

        StringBuilder sb = new StringBuilder();
        sb.append("Exception: ");
        sb.append(e.getClass().getSimpleName());
        sb.append("\n");
        sb.append("Message: ");
        sb.append(e.getMessage());

        this.setDescription(sb.toString());
        this.setColor(Color.RED);
    }

    public MessageEmbed build() {
        return super.build();
    }
}
