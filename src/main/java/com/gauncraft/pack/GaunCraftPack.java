package com.gauncraft.pack;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.util.Properties;

public final class GaunCraftPack extends JavaPlugin implements Listener, CommandExecutor {

    private String url;
    private byte[] hash;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadPack();
        getServer().getPluginManager().registerEvents(this, this);
        var cmd = getCommand("gcpack");
        if (cmd != null) cmd.setExecutor(this);
    }

    private void loadPack() {
        reloadConfig();
        Properties built = new Properties();
        try (InputStream in = getResource("pack.properties")) {
            if (in != null) built.load(in);
        } catch (Exception e) {
            getLogger().warning("pack.properties okunamadi: " + e.getMessage());
        }

        String u = getConfig().getString("url", "").trim();
        String s = getConfig().getString("sha1", "").trim();
        if (u.isEmpty()) u = built.getProperty("url", "").trim();
        if (s.isEmpty()) s = built.getProperty("sha1", "").trim();

        url = null;
        hash = null;
        if (u.isEmpty() || !s.matches("[0-9a-fA-F]{40}")) {
            getLogger().warning("Pack adresi ya da sha1 gecersiz. Pack gonderilmeyecek.");
            return;
        }
        url = u;
        hash = hex(s);
        getLogger().info("Pack hazir: " + url + " (sha1 " + s + ")");
    }

    private static byte[] hex(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        }
        return b;
    }

    private Component text(String path) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(getConfig().getString(path, ""));
    }

    private void sendPack(Player p) {
        if (url == null) return;
        p.setResourcePack(url, hash, text("prompt"), getConfig().getBoolean("force", false));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        long delay = Math.max(1, getConfig().getLong("send-delay-ticks", 20));
        Player p = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (p.isOnline()) sendPack(p);
        }, delay);
    }

    @EventHandler
    public void onStatus(PlayerResourcePackStatusEvent e) {
        if (e.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            e.getPlayer().sendMessage(text("messages.failed"));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                loadPack();
                sender.sendMessage(text("messages.reloaded"));
            }
            case "send" -> {
                if (url == null) {
                    sender.sendMessage(text("messages.no-pack"));
                    return true;
                }
                Player target = args.length > 1 ? Bukkit.getPlayerExact(args[1])
                        : (sender instanceof Player pl ? pl : null);
                if (target == null) {
                    sender.sendMessage(text("messages.not-found"));
                    return true;
                }
                sendPack(target);
                String msg = getConfig().getString("messages.sent", "").replace("%player%", target.getName());
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(msg));
            }
            default -> {
                return false;
            }
        }
        return true;
    }
}
