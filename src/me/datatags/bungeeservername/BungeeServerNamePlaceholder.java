package me.datatags.bungeeservername;

import org.bukkit.OfflinePlayer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class BungeeServerNamePlaceholder extends PlaceholderExpansion {
	private final BungeeServerName main;
	public BungeeServerNamePlaceholder(BungeeServerName main) {
		this.main = main;
	}

	@Override
	public String getAuthor() {
		return main.getDescription().getAuthors().get(0);
	}

	@Override
	public String getIdentifier() {
		return main.getDescription().getName();
	}

	@Override
	public String getVersion() {
		return main.getDescription().getVersion();
	}
	
	@Override
	public boolean persist() {
		return true;
	}
	
	@Override
	public boolean canRegister() {
		return true;
	}
	
	@Override
	public String onRequest(OfflinePlayer player, String identifier) {
		if (!identifier.equals("name")) return null;
		return main.getServerName();
	}
}
