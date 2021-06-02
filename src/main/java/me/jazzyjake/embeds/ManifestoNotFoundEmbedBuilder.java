package me.jazzyjake.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class ManifestoNotFoundEmbedBuilder extends EmbedBuilder {
    public ManifestoNotFoundEmbedBuilder(int id) {
        this.setTitle("Manifesto Not Found!");
        this.setDescription("Manifesto with id `" + id + "` was not found");
        this.setColor(Color.RED);
    }

    public MessageEmbed build() {
        return super.build();
    }
}
