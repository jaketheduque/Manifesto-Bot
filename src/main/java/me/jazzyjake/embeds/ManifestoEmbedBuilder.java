package me.jazzyjake.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class ManifestoEmbedBuilder extends EmbedBuilder {
    public ManifestoEmbedBuilder(String manifesto, String manifestoer) {
        this.setTitle("Retrieved Manifesto:");
        this.setDescription("`" + manifesto + "`");
        this.addField("Added to manifesto list by:", manifestoer, false);
        this.setColor(Color.GRAY);
    }
}
