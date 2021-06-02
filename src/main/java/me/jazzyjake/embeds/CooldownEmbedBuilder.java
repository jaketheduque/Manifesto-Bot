package me.jazzyjake.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class CooldownEmbedBuilder extends EmbedBuilder {
    public CooldownEmbedBuilder(int secondsLeft) {
        this.setTitle("Please wait!");
        this.setDescription("Bot is on cooldown for **" + secondsLeft + "** more seconds");
        this.setColor(Color.RED);
    }

    public MessageEmbed build() {
        return super.build();
    }
}
