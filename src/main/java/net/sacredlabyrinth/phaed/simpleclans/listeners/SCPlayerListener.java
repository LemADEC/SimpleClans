package net.sacredlabyrinth.phaed.simpleclans.listeners;

import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.Helper;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.Iterator;

/**
 * @author phaed
 */
public class SCPlayerListener implements Listener
{
    private SimpleClans plugin;

    /**
     *
     */
    public SCPlayerListener()
    {
        plugin = SimpleClans.getInstance();
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        Player player = event.getPlayer();

        if (player == null)
        {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(player.getLocation().getWorld().getName()))
        {
            return;
        }

        if (event.getMessage().length() == 0)
        {
            return;
        }

        String[] split = event.getMessage().substring(1).split(" ");

        if (split.length == 0)
        {
            return;
        }

        String command = split[0];

        if (plugin.getSettingsManager().isTagBasedClanChat() && plugin.getClanManager().isClan(command))
        {
            if (!plugin.getSettingsManager().getClanChatEnable())
            {
                return;
            }

            ClanPlayer cp = plugin.getClanManager().getClanPlayer(player);

            if (cp == null)
            {
                return;
            }

            if (cp.getTag().equalsIgnoreCase(command))
            {
                event.setCancelled(true);

                if (split.length > 1)
                {
                    plugin.getClanManager().processClanChat(player, cp.getTag(), Helper.toMessage(Helper.removeFirst(split)));
                }
            }
        }
        if (command.equals("."))
        {
            if (!plugin.getSettingsManager().getClanChatEnable())
            {
                return;
            }

            ClanPlayer cp = plugin.getClanManager().getClanPlayer(player);

            if (cp == null)
            {
                return;
            }

            event.setCancelled(true);

            if (split.length > 1)
            {
                plugin.getClanManager().processClanChat(player, cp.getTag(), Helper.toMessage(Helper.removeFirst(split)));
            }
        }
        else if (command.equalsIgnoreCase(plugin.getSettingsManager().getCommandAlly()))
        {
            if (!plugin.getSettingsManager().isAllyChatEnable())
            {
                return;
            }

            event.setCancelled(true);

            if (split.length > 1)
            {
                plugin.getClanManager().processAllyChat(player, Helper.toMessage(Helper.removeFirst(split)));
            }
        }
        else if (command.equalsIgnoreCase(plugin.getSettingsManager().getCommandGlobal()))
        {
            event.setCancelled(true);

            if (split.length > 1)
            {
                plugin.getClanManager().processGlobalChat(player, Helper.toMessage(Helper.removeFirst(split)));
            }
        }
        else if (command.equalsIgnoreCase(plugin.getSettingsManager().getCommandClan()))
        {
            event.setCancelled(true);
            plugin.getCommandManager().processClan(player, Helper.removeFirst(split));
        }
        else if (command.equalsIgnoreCase(plugin.getSettingsManager().getCommandAccept()))
        {
            event.setCancelled(true);
            plugin.getCommandManager().processAccept(player);
        }
        else if (command.equalsIgnoreCase(plugin.getSettingsManager().getCommandDeny()))
        {
            event.setCancelled(true);
            plugin.getCommandManager().processDeny(player);
        }
        else if (command.equalsIgnoreCase(plugin.getSettingsManager().getCommandMore()))
        {
            event.setCancelled(true);
            plugin.getCommandManager().processMore(player);
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (plugin.getSettingsManager().isBlacklistedWorld(event.getPlayer().getLocation().getWorld().getName()))
        {
            return;
        }

        if (event.getPlayer() == null)
        {
            return;
        }

        String message = event.getMessage();
        ClanPlayer cp = plugin.getClanManager().getClanPlayer(event.getPlayer());

        if (cp != null)
        {
            if (cp.getChannel().equals(ClanPlayer.Channel.CLAN))
            {
                plugin.getClanManager().processClanChat(event.getPlayer(), message);
                event.setCancelled(true);
            }
            else if (cp.getChannel().equals(ClanPlayer.Channel.ALLY))
            {
                plugin.getClanManager().processAllyChat(event.getPlayer(), message);
                event.setCancelled(true);
            }
        }

        if (!plugin.getPermissionsManager().has(event.getPlayer(), "simpleclans.mod.nohide"))
        {
            boolean isClanChat = event.getMessage().contains("" + ChatColor.RED + ChatColor.WHITE + ChatColor.RED + ChatColor.BLACK);
            boolean isAllyChat = event.getMessage().contains("" + ChatColor.AQUA + ChatColor.WHITE + ChatColor.AQUA + ChatColor.BLACK);

            for (Iterator iter = event.getRecipients().iterator(); iter.hasNext(); )
            {
                Player player = (Player) iter.next();

                ClanPlayer rcp = plugin.getClanManager().getClanPlayer(player);

                if (rcp != null)
                {
                    if (!rcp.isClanChat())
                    {
                        if (isClanChat)
                        {
                            iter.remove();
                            continue;
                        }
                    }

                    if (!rcp.isAllyChat())
                    {
                        if (isAllyChat)
                        {
                            iter.remove();
                            continue;
                        }
                    }

                    if (!rcp.isGlobalChat())
                    {
                        if (!isAllyChat && !isClanChat)
                        {
                            iter.remove();
                        }
                    }
                }
            }
        }

        if (plugin.getSettingsManager().isCompatMode())
        {
            if (cp != null && cp.isTagEnabled())
            {
                String tagLabel = cp.getClan().getTagLabel(cp.isLeader());

                Player player = event.getPlayer();

                if (player.getDisplayName().contains("{clan}"))
                {
                    player.setDisplayName(player.getDisplayName().replace("{clan}", tagLabel));
                }
                else if (event.getFormat().contains("{clan}"))
                {
                    event.setFormat(event.getFormat().replace("{clan}", tagLabel));
                }
                else
                {
                    String format = event.getFormat();
                    event.setFormat(tagLabel + format);
                }
            }
            else
            {
                event.setFormat(event.getFormat().replace("{clan}", ""));
                event.setFormat(event.getFormat().replace("tagLabel", ""));
            }
        }
        else
        {
            plugin.getClanManager().updateDisplayName(event.getPlayer());
        }
    }

    /**
     * @param event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        final Player player = event.getPlayer();

        if (SimpleClans.getInstance().getSettingsManager().isBlacklistedWorld(player.getLocation().getWorld().getName()))
        {
            return;
        }

        ClanPlayer cp;
        if (SimpleClans.getInstance().getSettingsManager().getUseBungeeCord())
        {
            cp = SimpleClans.getInstance().getClanManager().getClanPlayerJoinEvent(player);
        }
        else
        {
            cp = SimpleClans.getInstance().getClanManager().getClanPlayer(player);
        }

        if (cp == null)
        {
            return;
        }
        cp.setName(player.getName());
        SimpleClans.getInstance().getClanManager().updateLastSeen(player);
        SimpleClans.getInstance().getClanManager().updateDisplayName(player);
        if (SimpleClans.getInstance().hasUUID())
        {
            SimpleClans.getInstance().getSpoutPluginManager().processPlayer(cp.getUniqueId());
        } else
        {
            SimpleClans.getInstance().getSpoutPluginManager().processPlayer(cp.getName());
        }
        SimpleClans.getInstance().getPermissionsManager().addPlayerPermissions(cp);

        if (plugin.getSettingsManager().isBbShowOnLogin())
        {
                if (cp.isBbEnabled())
                {
                    cp.getClan().displayBb(player);
                }
        }

        SimpleClans.getInstance().getPermissionsManager().addClanPermissions(cp);

        if (event.getPlayer().isOp())
        {
            for (String message : SimpleClans.getInstance().getMessages())
            {
                event.getPlayer().sendMessage(ChatColor.YELLOW + message);
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event)
    {
        if (plugin.getSettingsManager().isBlacklistedWorld(event.getPlayer().getLocation().getWorld().getName()))
        {
            return;
        }

        if (plugin.getSettingsManager().isTeleportOnSpawn())
        {
            Player player = event.getPlayer();

            ClanPlayer cp = plugin.getClanManager().getClanPlayer(player);

            if (cp != null)
            {
                Location loc = cp.getClan().getHomeLocation();

                if (loc != null)
                {
                    event.setRespawnLocation(loc);
                }
            }
        }
    }

    /**
     * @param event
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (plugin.getSettingsManager().isBlacklistedWorld(event.getPlayer().getLocation().getWorld().getName()))
        {
            return;
        }

        ClanPlayer cp = plugin.getClanManager().getClanPlayer(event.getPlayer());

        SimpleClans.getInstance().getPermissionsManager().removeClanPlayerPermissions(cp);
        plugin.getClanManager().updateLastSeen(event.getPlayer());
        plugin.getRequestManager().endPendingRequest(event.getPlayer().getName());
    }

    /**
     * @param event
     */
    @EventHandler
    public void onPlayerKick(PlayerKickEvent event)
    {
        if (plugin.getSettingsManager().isBlacklistedWorld(event.getPlayer().getLocation().getWorld().getName()))
        {
            return;
        }

        plugin.getClanManager().updateLastSeen(event.getPlayer());
    }

    /**
     * @param event
     */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        if (event.isCancelled())
        {
            return;
        }

        if (plugin.getSettingsManager().isBlacklistedWorld(event.getPlayer().getLocation().getWorld().getName()))
        {
            return;
        }

        plugin.getSpoutPluginManager().processPlayer(event.getPlayer());
    }

    /**
     * @param event
     */
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event)
    {
        plugin.getSpoutPluginManager().processPlayer(event.getPlayer());
    }
}