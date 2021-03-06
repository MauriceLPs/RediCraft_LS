package at.mlps.rc.cmd;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import at.mlps.rc.api.MojangAPI;
import at.mlps.rc.api.Prefix;
import at.mlps.rc.main.LanguageHandler;
import at.mlps.rc.main.Main;
import at.mlps.rc.mysql.lb.MySQL;

public class Maintenance implements CommandExecutor, Listener{
	
	File whitelist = new File("plugins/RCLS/whitelist.yml");

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			Bukkit.getConsoleSender().sendMessage(Main.consolesend);
		}else {
			Player p = (Player)sender;
			MojangAPI mapi = new MojangAPI();
			if(cmd.getName().equalsIgnoreCase("whitelist")) {
				if(args.length == 0) {
					p.sendMessage(Prefix.prefix("main") + "§7Usage: /whitelist <add|remove> <Name>");
				}else if(args.length == 2) {
					if(p.hasPermission("mlps.whitelist")) {
						String uuid = mapi.getUUIDfromName(args[1]);
						if(args[0].equalsIgnoreCase("add")) {
							if(uuid.equalsIgnoreCase("errored")) {
								p.sendMessage("§7========[§aAdduser§7]========");
								p.sendMessage("§7User: §a" + args[1]);
								p.sendMessage("§7Reason: §cPlayer couldn't be found. Please check the name again.");
							}else {
								HashMap<String, Object> hm = new HashMap<>();
								hm.put("uuid", uuid);
								try {
									if(Main.mysql.isInDatabase("redicore_userwhitelist", hm)) {
										PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE redicore_userwhitelist SET admin = ?, enabled = ? WHERE uuid = ?");
										ps.setString(1, p.getUniqueId().toString());
										ps.setBoolean(2, true);
										ps.setString(3, uuid);
										ps.executeUpdate();
										p.sendMessage("§7========[§bWhitelist§7]========");
										p.sendMessage("§7User: §a" + args[1] + " §7/§a " + uuid);
										p.sendMessage("§7Player is now allowed to join.");
									}else {
										hm.put("admin", p.getUniqueId().toString());
										hm.put("enabled", true);
										Main.mysql.insertInto("redicore_userwhitelist", hm);
										p.sendMessage("§7========[§bWhitelist§7]========");
										p.sendMessage("§7User: §a" + args[1] + " §7/§a " + uuid);
										p.sendMessage("§7Player has been added and is now able to join.");
									}
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}else if(args[0].equalsIgnoreCase("remove")) {
							HashMap<String, Object> hm = new HashMap<>();
							hm.put("uuid", uuid);
							try {
								if(Main.mysql.isInDatabase("redicore_userwhitelist", hm)) {
									PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE redicore_userwhitelist SET admin = ?, enabled = ? WHERE uuid = ?");
									ps.setString(1, p.getUniqueId().toString());
									ps.setBoolean(2, false);
									ps.setString(3, uuid);
									ps.executeUpdate();
									p.sendMessage("§7========[§bWhitelist§7]========");
									p.sendMessage("§7User: §a" + args[1] + " §7/§a " + uuid);
									p.sendMessage("§7Player is now disallowed to join.");
									ps.close();
								}else {
									hm.put("admin", p.getUniqueId().toString());
									hm.put("enabled", false);
									Main.mysql.insertInto("redicore_userwhitelist", hm);
									p.sendMessage("§7========[§bWhitelist§7]========");
									p.sendMessage("§7User: §a" + args[1] + " §7/§a " + uuid);
									p.sendMessage("§7Player has been added but is §cnot §7able to join.");
								}
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}else {
							p.sendMessage(Prefix.prefix("main") + "§7Usage: /whitelist <add|remove> <Name>");
						}
					}
				}else {
					p.sendMessage(Prefix.prefix("main") + "§7Usage: /whitelist <add|remove> <Name>");
				}
			}
			if(cmd.getName().equalsIgnoreCase("userlist")) {
				if(args.length == 0) {
					p.sendMessage(Prefix.prefix("main") + "§7Usage: /userlist <add|remove|changereason|check|listall> <Name> <Message>");
				}else if(args.length >= 0) {
					SimpleDateFormat date = new SimpleDateFormat("dd/MM/yy-HH:mm:ss");
				    String sdate = date.format(new Date());
					if(p.hasPermission("mlps.whitelisting")) {
						if(args[0].equalsIgnoreCase("add")) {
							String name = args[1].toLowerCase();
							StringBuilder sb = new StringBuilder();
							for(int i = 2; i < args.length; i++) {
								sb.append(args[i]);
								sb.append(" ");
							}
							String msg = sb.toString();
							String uuid = mapi.getUUIDfromName(name);
							if(uuid.equalsIgnoreCase("errored")) {
								p.sendMessage("§7========[§aAdduser§7]========");
								p.sendMessage("§7User: §a" + name);
								p.sendMessage("§7Reason: §cPlayer couldn't be found. Please check the name again.");
							}else {
								setStatus(name, uuid, p.getName(), sdate, msg, System.currentTimeMillis(), true);
								p.sendMessage("§7========[§aAdduser§7]========");
								p.sendMessage("§7User: §a" + name + " §7/§a " + uuid);
								p.sendMessage("§7Reason: §a" + msg);
							}
						}else if(args[0].equalsIgnoreCase("remove")) {
							String name = args[1].toLowerCase();
							StringBuilder sb = new StringBuilder();
							for(int i = 2; i < args.length; i++) {
								sb.append(args[i]);
								sb.append(" ");
							}
							String msg = sb.toString();
							String uuid = mapi.getUUIDfromName(name);
							if(uuid.equalsIgnoreCase("errored")) {
								p.sendMessage("§7========[§cRemoveuser§7]========");
								p.sendMessage("§7User: §a" + name);
								p.sendMessage("§7Reason: §cPlayer couldn't be found. Please check the name again.");
							}else {
								setStatus(name, uuid, p.getName(), sdate, msg, System.currentTimeMillis(), false);
								p.sendMessage("§7========[§cRemoveuser§7]========");
								p.sendMessage("§7User: §a" + name + " §7/§a " + uuid);
								p.sendMessage("§7Reason: §a" + msg);
							}
						}else if(args[0].equalsIgnoreCase("check")) {
							String name = args[1].toLowerCase();
							String uuid = "";
							String msg = "§aDefault Reason.";
							String admin = "§cAlex";
							String datifo = "dd/MM/yy - HH:mm:ss";
							boolean boo = false;
							try {
								PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM redicore_whitelist WHERE name = ?");
								ps.setString(1, name);
								ResultSet rs = ps.executeQuery();
								rs.next();
								uuid = rs.getString("uuid");
								msg = rs.getString("reason");
								admin = rs.getString("admin");
								datifo = rs.getString("timest");
								boo = rs.getBoolean("allowed");
								rs.close();
								ps.close();
							}catch (SQLException e) { }
							p.sendMessage("§7========[§2Checkuser§7]========");
							p.sendMessage("§7User: §a" + name + " §7/§a " + uuid);
							p.sendMessage("§7Reason: §a" + msg);
							p.sendMessage("§7Admin: §a" + admin);
							p.sendMessage("§7Date & Time: §a" + datifo);
							if(boo == true) {
								p.sendMessage("§7Can Join: §ayes");
							}else {
								p.sendMessage("§7Can Join: §cno");
							}
						}else if(args[0].equalsIgnoreCase("changereason")) {
							String name = args[1].toLowerCase();
							StringBuilder sb = new StringBuilder();
							for(int i = 2; i < args.length; i++) {
								sb.append(args[i]);
								sb.append(" ");
							}
							String msg = sb.toString();
							String uuid = mapi.getUUIDfromName(name);
							if(uuid.equalsIgnoreCase("errored")) {
								p.sendMessage("§7========[§eChangereason§7]========");
								p.sendMessage("§7User: §a" + name);
								p.sendMessage("§7Reason: §cPlayer couldn't be found. Please check the name again.");
							}else {
								changeStatus(uuid, name, p.getName(), msg);
								p.sendMessage("§7========[§eChangereason§7]========");
								p.sendMessage("§7User: §a" + name + " §7/§a " + uuid);
								p.sendMessage("§7Reason: §a" + msg);
							}
						}else if(args[0].equalsIgnoreCase("listall")) {
							try {
								PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM redicore_whitelist");
								ResultSet rs = ps.executeQuery();
								p.sendMessage("§7========[§9Listall§7]========");
								while(rs.next()) {
									boolean allow = rs.getBoolean("allowed");
									if(allow == true) {
										p.sendMessage("§7ID: §2" + rs.getInt("id") + " §7| Player: §a" + rs.getString("name") + " §7| Whitelisted: §a" + allow);
									}else {
										p.sendMessage("§7ID: §2" + rs.getInt("id") + " §7| Player: §a" + rs.getString("name") + " §7| Whitelisted: §c" + allow);
									}
								}
								rs.close();
								ps.close();
							}catch (SQLException e) {
								p.sendMessage("§cError while performing this command.");
							}
						}else {
							p.sendMessage(Prefix.prefix("main") + "§7Usage: /userlist <add|remove|changereason|check|listall> <Name> <Message>");
						}
					}else {
						LanguageHandler.sendMSGReady(p, "noPermission");
					}
				}
			}else if(cmd.getName().equalsIgnoreCase("maintenance")) {
				if(!whitelist.exists()) {
					try { whitelist.createNewFile(); }catch (IOException e) { e.printStackTrace(); }
				}
				if(args.length == 0) {
					p.sendMessage(Prefix.prefix("main") + "§7Usage: /maintenance <on|off|status> <(if on)Message>");
				}else if(args.length >= 1) {
					if(p.hasPermission("mlps.maintenance")) {
						YamlConfiguration cfg = YamlConfiguration.loadConfiguration(whitelist);
						StringBuilder sb = new StringBuilder();
						for(int i = 1; i < args.length; i++) {
							sb.append(args[i]);
							sb.append(" ");
						}
						String msg = sb.toString();
						if(args[0].equalsIgnoreCase("on")) {
							if(msg.isEmpty()) {
								p.sendMessage(Prefix.prefix("main") + "§cPlease give at least one Word for Reason.");
							}else {
								cfg.set("Whitelist.Maintenance.Message", msg);
								cfg.set("Whitelist.Maintenance.Boolean", true);
								cfg.set("Whitelist.Maintenance.activatedFrom", p.getName());
								try {
									cfg.save(whitelist);
									p.sendMessage(Prefix.prefix("main") + "§7You activated the maintenance - state.");
									p.sendMessage("§7Message: §9" + msg);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}else if(args[0].equalsIgnoreCase("off")) {
							cfg.set("Whitelist.Maintenance.Boolean", false);
							try {
								cfg.save(whitelist);
								p.sendMessage(Prefix.prefix("main") + "§7You deactivated the maintenance - state.");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}else if(args[0].equalsIgnoreCase("status")) {
							String cfgmsg = cfg.getString("Whitelist.Maintenance.Message");
							boolean boo = cfg.getBoolean("Whitelist.Maintenance.Boolean");
							String admin = cfg.getString("Whitelist.Maintenance.deactivatedFrom", p.getName());
							if(boo == true) {
								p.sendMessage(Prefix.prefix("main") + "§7Status of Maintenance: §cactive");
							}else if(boo == false) {
								p.sendMessage(Prefix.prefix("main") + "§7Status of Maintenance: §ainactive");
							}
							p.sendMessage(Prefix.prefix("main") + "§7Last Maintenance-Message: §f" + cfgmsg);
							p.sendMessage(Prefix.prefix("main") + "§7Admin: " + admin);
						}
					}else {
						LanguageHandler.sendMSGReady(p, "noPermission");
					}
				}
			}
		}
		return true;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onLogin(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		String uuid = p.getUniqueId().toString().replace("-", "");
		try {
			HashMap<String, Object> hm = new HashMap<>();
			hm.put("uuid", p.getUniqueId().toString().replace("-", ""));
			if(Main.mysql.isInDatabase("redicore_userwhitelist", hm)) {
				PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM redicore_userwhitelist WHERE uuid = ?");
				ps.setString(1, p.getUniqueId().toString().replace("-", ""));
				ResultSet rs = ps.executeQuery();
				rs.next();
				if(rs.getBoolean("enabled")) {
					e.allow();
				}else {
					if(p.hasPermission("mlps.isTeam") || getStatus(uuid) == true) {
						e.allow();
					}else {
						e.disallow(Result.KICK_OTHER, "\n§aRedi§cCraft\n \n§7Hey " + e.getPlayer().getName() + ",\n§7thank you for joining our server,\n§7but it seems you are not allowed to join our server.\n \nIf you want to play on our server, you have to fill out our Google Forms.\nURL: https://forms.gle/2mvyoJ8DGqeBP2fH7 \n \nYou want to know more about it?\nJoin our Discord Server now: https://discord.gg/sHDF9WR");
					}
				}
			}else {
				if(p.hasPermission("mlps.isTeam") || getStatus(uuid) == true) {
					e.allow();
				}else {
					e.disallow(Result.KICK_OTHER, "\n§aRedi§cCraft\n \n§7Hey " + e.getPlayer().getName() + ",\n§7thank you for joining our server,\n§7but it seems you are not allowed to join our server.\n \nIf you want to play on our server, you have to fill out our Google Forms.\nURL: https://forms.gle/2mvyoJ8DGqeBP2fH7 \n \nYou want to know more about it?\nJoin our Discord Server now: https://discord.gg/sHDF9WR");
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	private void setStatus(String name, String uuid, String admin, String timest, String reason, long timets, boolean allowed) {
		try {
			HashMap<String, Object> hm = new HashMap<>();
			hm.put("uuid", uuid);
			if(Main.mysql.isInDatabase("redicore_whitelist", hm)) {
				PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE redicore_whitelist SET name = ?, allowed = ?, reason = ?, timest = ?, timets = ?, admin = ? WHERE uuid = ?");
				ps.setString(1, name);
				ps.setBoolean(2, allowed);
				ps.setString(3, reason);
				ps.setString(4, timest);
				ps.setLong(5, timets);
				ps.setString(6, admin);
				ps.setString(7, uuid);
				ps.executeUpdate();
				ps.close();
			}else {
				hm.put("name", name);
				hm.put("allowed", allowed);
				hm.put("reason", reason);
				hm.put("admin", admin);
				hm.put("timest", timest);
				hm.put("timets", timets);
				Main.mysql.insertInto("redicore_whitelist", hm);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void changeStatus(String uuid, String name, String admin, String reason) {
		try {
			HashMap<String, Object> hm = new HashMap<>();
			hm.put("uuid", uuid);
			if(Main.mysql.isInDatabase("redicore_whitelist", hm)) {
				PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE redicore_whitelist SET name = ?, reason = ?, admin = ? WHERE uuid = ?");
				ps.setString(1, name);
				ps.setString(2, reason);
				ps.setString(3, admin);
				ps.setString(4, uuid);
				ps.executeUpdate();
				ps.close();
			}
		}catch (SQLException e) {}
	}
	
	private boolean getStatus(String uuid) {
		boolean boo = false;
		HashMap<String, Object> hm = new HashMap<>();
		hm.put("uuid", uuid);
		try {
			if(Main.mysql.isInDatabase("redicore_whitelist", hm)) {
				PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT * FROM redicore_whitelist WHERE uuid = ?");
				ps.setString(1, uuid);
				ResultSet rs = ps.executeQuery();
				rs.next();
				boo = rs.getBoolean("allowed");
				rs.close();
				ps.close();
			}
		}catch (SQLException e) {}
		return boo;
	}
}