package fr.minemobs.discordbot.PufferTeamMain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import fr.minemobs.discordbot.PufferTeamMain.commands.*;
import fr.minemobs.discordbot.PufferTeamMain.listener.Listener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PufferTeamMain {

    private static Logger LOGGER = LoggerFactory.getLogger(PufferTeamMain.class);
    private static JDA jda;
    private static CommandClientBuilder client;

    private static Gson gson = new GsonBuilder().serializeNulls().create();

    public static void main(String[] args) {
        try{
            if(args[0].toLowerCase().contains("DISCORD_TOKEN=".toLowerCase()) &&
                    args[1].toLowerCase().contains("MC_PASSWORD=".toLowerCase()) &&
                    args[2].toLowerCase().contains("MC_IP=".toLowerCase())){
                String token = args[0].replace("DISCORD_TOKEN=", "");
                String mcPassword = args[1].replace("MC_PASSWORD=", "");
                String mcIp = args[2].replace("MC_IP=","");
                launchDiscordBot(token, mcPassword, mcIp);
            }
        }catch (ArrayIndexOutOfBoundsException e){
            LOGGER.error("Veuillez relancer le bot avec l'arguement : DISCORD_TOKEN=");
        }
    }

    private static void launchDiscordBot(String token, String mcPassword, String mcIp) {
        try{
            File config = new File("config.txt");
            if(!config.exists()) config.createNewFile();
            List<String> list = Files.readAllLines(Paths.get(config.getName()));

            String ownerId = list.get(0);
            String[] coOwnersId = list.get(1).split(",");

            EventWaiter waiter = new EventWaiter();

            client = new CommandClientBuilder()
                    .setOwnerId(ownerId)
                    .setEmojis("\uD83D\uDE03", "\uD83D\uDE2E", "\uD83D\uDE26")
                    .setPrefix(Infos.PREFIX.getS())
                    .addCommands(
                            new SetStatusCmd(),
                            new PingCommand(),
                            new SearchRepo(),
                            new SuggestionCommand(),
                            new EvalCommand()
                    )
                    .setHelpWord("help")
                    .useHelpBuilder(true)
                    .setActivity(Activity.watching("Minemobs developing"))
                    .setStatus(OnlineStatus.DO_NOT_DISTURB);

            for (String s : coOwnersId) {
                client.setCoOwnerIds(coOwnersId);
            }

            if(testRCON(mcIp, mcPassword)){
                client.addCommand(new McCommand(mcPassword, mcIp));
            }

            jda = JDABuilder.createDefault(token).enableIntents(GatewayIntent.GUILD_MEMBERS).build();
            jda.addEventListener(new Listener());
            jda.addEventListener(waiter, client.build());
        }catch (LoginException | IOException e){
            e.printStackTrace();
        }
    }

    private static boolean testRCON(String ip, String mdp){
        boolean success = true;
        try {
            new Rcon(ip, 25575, mdp.getBytes());
        } catch (IOException | AuthenticationException e) {
            success = false;
        }
        return success;
    }

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public static Gson getGson() {
        return gson;
    }

    public static JDA getJda() {
        return jda;
    }

    public static CommandClientBuilder getClient() {
        return client;
    }
}
