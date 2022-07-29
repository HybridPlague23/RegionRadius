package me.hybridplague.regionradius;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;


public class RegionRadius extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
	}
	
	
	public Set<ProtectedRegion> checkForRegions(World world, double x, double y, double z) {
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager manager = container.get(BukkitAdapter.adapt(world));
		
		BlockVector3 bv = BlockVector3.at(x, y, z);
		
		ApplicableRegionSet ars = manager.getApplicableRegions(bv);
		
		if (ars.getRegions().isEmpty()) {
			return null;
		}
		return ars.getRegions();
	}
	
	public String getRegionName(World world, double x, double y, double z) {
		String name = "";
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager manager = container.get(BukkitAdapter.adapt(world));
		
		BlockVector3 bv = BlockVector3.at(x, y, z);
		
		ApplicableRegionSet ars = manager.getApplicableRegions(bv);
		
		for (ProtectedRegion rg : ars) {
			name = rg.getId();
			return name;
		}
		return null;
	}
	
	
	@SuppressWarnings("deprecation")
	public void check(Player p, Location loc) {
		int radius = 20;
		
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager manager = container.get(BukkitAdapter.adapt(loc.getWorld()));
		
		List<String> names = new ArrayList<String>();
		List<BlockVector3> namesLocation = new ArrayList<BlockVector3>();
		
		for (int x = loc.getBlockX() - radius; x <= loc.getBlockX() + radius; x++) {
			for (int z = loc.getBlockZ() - radius; z <= loc.getBlockZ() + radius; z++) {
				//Block block = loc.getWorld().getBlockAt(x, y, z);
				
				/*if (block.getType() == Material.AIR) {
					block.setType(Material.STONE);
				}*/
				
				int y = loc.getWorld().getHighestBlockYAt(x, z) + 1;
				
				if (checkForRegions(loc.getWorld(), x, y, z) != null) {
					
					
					
					BlockVector3 bv = BlockVector3.at(x, y, z);
					
					ApplicableRegionSet ars = manager.getApplicableRegions(bv);
					
					for (ProtectedRegion rg : ars) {
						String name = rg.getId();
						if (!names.contains(name)) {
							names.add(name);
							namesLocation.add(bv);
						}
					}
				}
			}
		}
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&lRegions Found:"));
		
		if (names.isEmpty()) {
			p.sendMessage(ChatColor.GRAY + "No regions.");
			return;
		}
		int i = 0;
		for (String n : names) {
			BlockVector3 b = (BlockVector3) namesLocation.toArray()[i];
			
			net.md_5.bungee.api.chat.TextComponent message = new net.md_5.bungee.api.chat.TextComponent(ChatColor.translateAlternateColorCodes('&', "&e" + n + "&7: &f" + b.getX() + " " + b.getY() + " " + b.getZ() + " &7&o(click to teleport)"));
			message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tppos " + b.getX() + " " + b.getY() + " " + b.getZ()));
			message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GRAY + "Click to teleport to the region!").create()));
			p.spigot().sendMessage(message);
				
			//BlockVector3 b = BlockVector3.at(manager.getRegion(n).getMaximumPoint().getX(), loc.getWorld().getHighestBlockYAt(manager.getRegion(n).getMaximumPoint().getX(), manager.getRegion(n).getMaximumPoint().getZ()) + 1, manager.getRegion(n).getMaximumPoint().getZ());
			//TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&e" + n + "&7: &f" + b.getX() + " " + b.getY() + " " + b.getZ() + " &7&o(click to teleport)"));
			i++;
		}
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (!p.hasPermission("businesscraft.helper")) {
			return;
		}
		
		if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			ItemStack item = new ItemStack(p.getInventory().getItemInMainHand());
			ItemMeta meta = item.getItemMeta();
			
			if (e.getHand() == EquipmentSlot.OFF_HAND) return;
			
			if (item.getType() == Material.WOODEN_SHOVEL
					&& meta.getDisplayName().equals(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Radius Checker")) {
				e.setCancelled(true);
				Block block = e.getClickedBlock();
				check(p, block.getLocation());
				return;
			}
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("radiuschecker")) {
			if (!(sender instanceof Player))
				return false;
			Player p = (Player) sender;
			

			if (!p.hasPermission("businesscraft.mod")) {
				return false;
			}
			
			ItemStack item = new ItemStack(Material.WOODEN_SHOVEL);
			ItemMeta meta = item.getItemMeta();
			List<String> lore = new ArrayList<String>();
			
			lore.add("");
			lore.add(ChatColor.GRAY + "Right click a block to check for");
			lore.add(ChatColor.GRAY + "regions within a 20 block radius of it.");
			meta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Radius Checker");
			meta.setLore(lore);
			item.setItemMeta(meta);
			
			p.getInventory().addItem(item);
			p.sendMessage(ChatColor.GREEN + "You have been given a RadiusChecker");
			return true;
		}
		
		
		if (label.equalsIgnoreCase("checkradius")) {
			if (!(sender instanceof Player))
				return false;
			Player p = (Player) sender;

			if (!p.hasPermission("businesscraft.mod")) {
				return false;
			}

			check(p, p.getLocation());
			
			return true;
		}
		return false;
	}
	
}
