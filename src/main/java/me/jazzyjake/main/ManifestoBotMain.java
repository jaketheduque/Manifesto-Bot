package me.jazzyjake.main;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.security.auth.login.LoginException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ManifestoBotMain {
    public static final ResourceBundle properties = ResourceBundle.getBundle("application");
    public static final int DELETE_REACTION_LIMIT = Integer.parseInt(properties.getString("delete-reaction-limit"));
    private static final String TOKEN = properties.getString("token");

    private static JDA jda;

    private static final Logger log = LogManager.getLogger(ManifestoBotMain.class);
    
    public static void main(String[] args) {
        try {
            jda = JDABuilder.createDefault(TOKEN).build();
        } catch (LoginException e) {
            log.error("Login exception occurred!");
            e.printStackTrace();
        }
    }

    public static JDA getJDA() {
        return jda;
    }
}
