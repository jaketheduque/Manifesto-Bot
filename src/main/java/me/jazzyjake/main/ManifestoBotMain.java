package me.jazzyjake.main;


import me.jazzyjake.listeners.CommandListener;
import me.jazzyjake.listeners.ManifestoListener;
import me.jazzyjake.listeners.ManifestoReactionListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ManifestoBotMain {
    // application.properties constants
    public static final ResourceBundle properties = ResourceBundle.getBundle("application");
    public static final int DELETE_REACTION_LIMIT = Integer.parseInt(properties.getString("delete-reaction-limit"));
    public static final String[] MANIFESTO_BLACKLIST = properties.getString("manifesto-blacklist").split(",");
    private static final String TOKEN = properties.getString("token");

    // Postgres database constants
    public static final String DATABASE_URL = System.getenv("JDBC_DATABASE_URL");

//    Apache Derby database constants
//    public static final String DERBY_NAME = "ManifestoDB";
//    public static final String DERBY_PROTOCOL = "jdbc:derby:";

    private static JDA jda;
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private static final Logger log = LogManager.getLogger(ManifestoBotMain.class);

    public static void main(String[] args) {
        // Creates the JDA bot and builds it
        try {
            jda = JDABuilder
                    .createDefault(TOKEN)
                    .addEventListeners(new ManifestoListener(), new CommandListener(), new ManifestoReactionListener())
                    .build();

            jda.awaitReady();

            log.info("Current manifesto blacklist: {}", Arrays.toString(MANIFESTO_BLACKLIST));

            // Switches bot presence between showing !manifestohelp (help command) and the manifesto website
            executorService.scheduleAtFixedRate(() -> {
                if (jda.getPresence().getActivity() == null || jda.getPresence().getActivity().getType() == Activity.ActivityType.WATCHING) {
                    jda.getPresence().setPresence(Activity.listening("!manifestohelp"), false);
                } else {
                    // TODO Change this to actual website
                    jda.getPresence().setPresence(Activity.watching("jazzyjake.ddns.net"), false);
                }
            }, 0, 1, TimeUnit.MINUTES);

        } catch (LoginException | InterruptedException e) {
            log.error("Startup exception occurred!");
            e.printStackTrace();
        }
    }

    public static JDA getJDA() {
        return jda;
    }
}
