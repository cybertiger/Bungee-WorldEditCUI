/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cyberiantiger.minecraft.fixworldeditcui;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

/**
 *
 * @author antony
 */
public class Main extends Plugin implements Listener {
    private static final String CHANNEL = "WECUI";
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final byte[] V3_CLIENT_RESET = "s|cuboid".getBytes(CHARSET);
    private static final byte[] V3_CLIENT_INIT = "v|3".getBytes(CHARSET);


    private final Map<UUID,byte[]> cuiInit = new HashMap<UUID,byte[]>();

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        getProxy().registerChannel(CHANNEL);
        getProxy().getPluginManager().registerListener(this, this);
    }

    @Override
    public void onDisable() {
        getProxy().unregisterChannel(CHANNEL);
    }


    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if ((e.getSender() instanceof ProxiedPlayer) && CHANNEL.equals(e.getTag())) {
            ProxiedPlayer sender = (ProxiedPlayer) e.getSender();
            cuiInit.put(sender.getUniqueId(), e.getData());
        }
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent e) {
        ProxiedPlayer player = e.getPlayer();
        Server server = e.getServer();
        byte[] initString = cuiInit.get(player.getUniqueId());
        if (initString != null) {
            // Hack to work around the server not initializing
            // the client correctly.
            if (Arrays.equals(V3_CLIENT_INIT, initString)) {
                player.sendData(CHANNEL, V3_CLIENT_RESET);
            }
            server.sendData(CHANNEL, initString);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent e) {
        cuiInit.remove(e.getPlayer().getUniqueId());
    }
}
