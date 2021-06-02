package me.jazzyjake.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class ManifestoBlacklistEmbedBuilder extends EmbedBuilder {
    public ManifestoBlacklistEmbedBuilder(String manifesto) {
        this.setTitle("Manifesto Not Added!");
        this.setDescription("`" + manifesto + "` is on manifesto blacklist");
        this.setColor(Color.RED);
    }
}
