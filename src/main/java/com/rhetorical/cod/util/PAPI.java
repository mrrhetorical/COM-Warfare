package com.rhetorical.cod.util;

import com.rhetorical.cod.ComWarfare;
import com.rhetorical.cod.progression.CreditManager;
import com.rhetorical.cod.progression.ProgressionManager;
import com.rhetorical.cod.progression.StatHandler;
import com.rhetorical.cod.streaks.KillStreakManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

/**
 * This class will be registered through the register-method in the
 * plugins onEnable-method.
 */
public class PAPI extends PlaceholderExpansion {

    private ComWarfare plugin;

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin
     *        The instance of our plugin.
     */
    public PAPI(ComWarfare plugin){
        this.plugin = plugin;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>The identifier has to be lowercase and can't contain _ or %
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier(){
        return "cod";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    /**
     * This is the method called when a placeholder with our identifier
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A {@link org.bukkit.entity.Player Player}.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier){

        if(player == null){
            return "";
        }

        // %cod_kills%
        if(identifier.equals("kills")){
            return "" + StatHandler.getKills(player.getName());
        }

        // %cod_deaths%
        if(identifier.equals("deaths")){
            return "" + StatHandler.getDeaths(player.getName());
        }

        // %cod_experience%
        if(identifier.equals("experience")){
            return "" + StatHandler.getExperience(player.getName());
        }

        // %cod_level%
        if(identifier.equals("level")){
            return "" + ProgressionManager.getInstance().getLevel(player);
        }

        // %cod_prestige_level%
        if(identifier.equals("prestige_level")){
            return "" + ProgressionManager.getInstance().getPrestigeLevel(player);
        }

        // %cod_credits%
        if(identifier.equals("credits")){
            return "" + CreditManager.getCredits(player);
        }

        // Load killstreaks for player if they aren't already
        if (KillStreakManager.getInstance().playerKillstreaks.get(player) == null) {
            KillStreakManager.getInstance().loadStreaks(player);
        }

        // %cod_killstreak_1%
        if(identifier.equals("killstreak_1")){
            return KillStreakManager.getInstance().playerKillstreaks.get(player)[0].toString();
        }

        // %cod_killstreak_2%
        if(identifier.equals("killstreak_2")){
            return KillStreakManager.getInstance().playerKillstreaks.get(player)[1].toString();
        }

        // %cod_killstreak_3%
        if(identifier.equals("killstreak_3")){
            return KillStreakManager.getInstance().playerKillstreaks.get(player)[2].toString();
        }

        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%)
        // was provided
        return null;
    }
}