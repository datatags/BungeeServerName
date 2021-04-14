package me.datatags.bungeeservername;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class BungeeServerName extends JavaPlugin implements PluginMessageListener, Listener {
	private static final String BUNGEE_CHANNEL = "BungeeCord";
	private String serverName;
	private boolean usingCache = true;
	public void onEnable() {
		saveDefaultConfig();
		getConfig().addDefault("serverName", "");
		serverName = getConfig().getString("serverName");
		getServer().getMessenger().registerIncomingPluginChannel(this, BUNGEE_CHANNEL, this);
		getServer().getMessenger().registerOutgoingPluginChannel(this, BUNGEE_CHANNEL);
		getServer().getPluginManager().registerEvents(this, this);
		new BungeeServerNamePlaceholder(this).register();
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if (!usingCache) return;
		new BukkitRunnable() {
			@Override
			public void run() {
				sendServerMessage(e.getPlayer());
			}
		}.runTaskLater(this, 1);
	}
	private void sendServerMessage(Player player) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("GetServer");
		player.sendPluginMessage(BungeeServerName.this, BUNGEE_CHANNEL, out.toByteArray());
	}
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] data) {
		if (!usingCache) return;
		if (!channel.equals("BungeeCord")) return;
		ByteArrayDataInput in = ByteStreams.newDataInput(data);
		String subchannel = in.readUTF();
		if (!subchannel.equals("GetServer")) return;
		serverName = in.readUTF();
		getConfig().set("serverName", serverName);
		saveConfig();
		usingCache = false;
	}
	public String getServerName() {
		return serverName;
	}
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("servername")) {
			sender.sendMessage(ChatColor.YELLOW + "Updated since server start: " + (usingCache ? ChatColor.RED + "no" : ChatColor.GREEN + "yes") + ChatColor.YELLOW + ", current value: '" + ChatColor.AQUA + serverName + ChatColor.YELLOW + "'");
			return true;
		}
		// cmd is updateservername
		Player player;
		usingCache = true;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
			if (player == null) {
				sender.sendMessage(ChatColor.RED + "No players online, will update on next login.");
				return true;
			}
		}
		sendServerMessage(player);
		sender.sendMessage(ChatColor.GREEN + "Updating now, use /servername for status");
		return true;
	}
}
