package net.coreprotect.database.logger;

import java.sql.PreparedStatement;
import java.util.Locale;

import net.coreprotect.CoreProtect;
import net.coreprotect.config.Config;
import net.coreprotect.event.CoreProtectPreLogEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.coreprotect.config.ConfigHandler;
import net.coreprotect.utility.Util;

public class ContainerBreakLogger {

    private ContainerBreakLogger() {
        throw new IllegalStateException("Database class");
    }

    public static void log(PreparedStatement preparedStmt, int batchCount, String player, Location l, Material type, ItemStack[] oldInventory) {
        try {
            CoreProtectPreLogEvent event = new CoreProtectPreLogEvent(player, l);
            if (Config.getGlobal().API_ENABLED && !Bukkit.isPrimaryThread()) {
                CoreProtect.getInstance().getServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
                return;
            }
            Util.mergeItems(type, oldInventory);
            ContainerLogger.logTransaction(preparedStmt, batchCount, player, type, null, oldInventory, 0, l);
            String loggingContainerId = player.toLowerCase(Locale.ROOT) + "." + l.getBlockX() + "." + l.getBlockY() + "." + l.getBlockZ();

            // If there was a pending chest transaction, it would have already been processed.
            if (ConfigHandler.forceContainer.get(loggingContainerId) != null) {
                ConfigHandler.forceContainer.remove(loggingContainerId);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
