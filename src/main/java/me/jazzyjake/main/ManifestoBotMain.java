package me.jazzyjake.main;


import me.jazzyjake.listeners.CommandListener;
import me.jazzyjake.listeners.ManifestoListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ManifestoBotMain {
    // application.properties constants
    public static final ResourceBundle properties = ResourceBundle.getBundle("application");
    public static final int DELETE_REACTION_LIMIT = Integer.parseInt(properties.getString("delete-reaction-limit"));
    private static final String TOKEN = properties.getString("token");

    // Manifesto database constants
    public static final String DERBY_NAME = "ManifestoDB";
    public static final String DERBY_PROTOCOL = "jdbc:derby:";

    private static JDA jda;
    private static final Logger log = LogManager.getLogger(ManifestoBotMain.class);

    public static void main(String[] args) {
        // Creates the JDA bot and builds it
        try {
            jda = JDABuilder
                    .createDefault(TOKEN)
                    .addEventListeners(new ManifestoListener(), new CommandListener())
                    .build();
        } catch (LoginException e) {
            log.error("Login exception occurred!");
            e.printStackTrace();
        }
    }

    public static JDA getJDA() {
        return jda;
    }
}
