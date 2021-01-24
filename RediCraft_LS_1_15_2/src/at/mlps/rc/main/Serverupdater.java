package at.mlps.rc.main;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import at.mlps.rc.api.GetBukkitInfo;
import at.mlps.rc.mysql.lb.MySQL;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.MinecraftServer;

public class Serverupdater implements Listener{
	
	static int rfm = 0;
	
	public static void runUpdaters(int delay, int period) {
		new BukkitRunnable() {
			@Override
			public void run() {
				updateServer();
				updateWorlds();
				rfm++;
				if(rfm == 3) {
					rfm = 0;
					/*String pl = returnRadio1("https://api.laut.fm/station/redifm", "current_playlist", "name");
					String art = returnRadio1("https://api.laut.fm/station/redifm/current_song", "artist", "name");
					String tra = returnRadio("https://api.laut.fm/station/redifm/current_song", "title");
					String alb = returnRadio("https://api.laut.fm/station/redifm/current_song", "album");
					String listeners = returnRadioListeners();
					try {
						PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE redifm_current SET track = ?, artist = ?, album = ?, playlist = ?, current_listener = ?  WHERE id = ?");
						ps.setString(1, tra);
						ps.setString(2, art);
						ps.setString(3, alb);
						ps.setString(4, pl);
						ps.setString(5, listeners);
						ps.setInt(6, 1);
						ps.executeUpdate();
					} catch (SQLException e) {
						e.printStackTrace();
					}*/
				}
			}
		}.runTaskTimerAsynchronously(Main.instance, delay, period);
	}
	
	public static void updateWorlds() {
		String server = GetBukkitInfo.getServerName();
		for(World w : Bukkit.getWorlds()) {
			HashMap<String, Object> hm = new HashMap<>();
			hm.put("server", server);
			hm.put("world", w.getName());
			String weather = "";
			if(w.isThundering()) {
				weather = "thunder";
			}else if(w.hasStorm()) {
				weather = "rain";
			}else {
				weather = "clear";
			}
			String time = parseTimeWorld(w.getTime());
			int players = w.getPlayers().size();
			try {
				if(!Main.mysql.isInDatabase("redicore_worldsettings", hm)) {
					hm.put("weather", weather);
					hm.put("time", time);
					hm.put("players", players);
					Main.mysql.insertInto("redicore_worldsettings", hm);
				}else {
					PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE redicore_worldsettings SET weather = ?, time = ?, players = ? WHERE server = ? AND world = ?");
					ps.setString(1, weather);
					ps.setString(2, time);
					ps.setInt(3, players);
					ps.setString(4, server);
					ps.setString(5, w.getName());
					ps.executeUpdate();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings({ "resource", "deprecation" })
	public static void updateServer() {
		if(MySQL.isConnected()) {
			Runtime runtime = Runtime.getRuntime();
			long ramusage = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
			long ramtotal = runtime.totalMemory() / 1048576L;
			int players = Bukkit.getOnlinePlayers().size();
			int pmax = Bukkit.getMaxPlayers();
			Timestamp ts = new Timestamp(System.currentTimeMillis());
			long timestamp = ts.getTime();
			SimpleDateFormat time = new SimpleDateFormat("dd/MM/yy - HH:mm:ss");
		    String stime = time.format(new Date());
		    StringBuilder sb = new StringBuilder("");
		    for(double tps : MinecraftServer.getServer().recentTps) {
		    	sb.append(format(tps));
		    }
		    String tps = sb.substring(0, sb.length() - 1);
		    int code1 = random(0, 5000);
			int code2 = random(5001, 10000);
			String gcode1 = code1 + "-" + code2;
			int staffs = 0;
			for(Player all : Bukkit.getOnlinePlayers()) {
				if(isStaff(all)) {
					if(isLoggedin(all)) {
						staffs++;
					}
				}
			}
		    try {
		    	Main.mysql.update("UPDATE useless_testtable SET toupdate = '" + gcode1 + "' WHERE type = 'lobby';");
		    	PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE redicore_serverstats SET ramusage = ?, serverid = ?, currPlayers = ?, maxPlayers = ?, lastupdateTS = ?, lastupdateST = ?, ramavailable = ?, version = ?, tps = ?, currStaffmembers = ? WHERE servername = ?");
		    	ps.setInt(1, (int) ramusage);
				ps.setString(2, GetBukkitInfo.getServerId());
				ps.setInt(3, players);
				ps.setInt(4, pmax);
				ps.setInt(5, (int) timestamp);
				ps.setString(6, stime);
				ps.setInt(7, (int) ramtotal);
				ps.setString(8, "1.16.5");
				ps.setString(9, tps);
				ps.setInt(10, staffs);
				ps.setString(11, GetBukkitInfo.getServerName());
				ps.executeUpdate();
				ps.close();
				
		    }catch (SQLException e) { e.printStackTrace(); Bukkit.getConsoleSender().sendMessage("�cCan't update DB-Stats."); }
		}else {
			Bukkit.getConsoleSender().sendMessage("�cDB is not connected.");
		}
		Serverrestarter();
	}
	
	private static void Serverrestarter() {
		SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
	    String stime = time.format(new Date());
	    if(stime.equals("20:00:00")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "4 Stunden").replace("%time_en", "4 hours"));
	    	}
	    }else if(stime.equals("21:00:00")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "3 Stunden").replace("%time_en", "3 hours"));
	    	}
	    }else if(stime.equals("22:00:00")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "2 Stunden").replace("%time_en", "2 hours"));
	    	}
	    }else if(stime.equals("22:30:00")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "90 Minuten").replace("%time_en", "90 minutes"));
	    	}
	    }else if(stime.equals("23:00:00")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "60 Minuten").replace("%time_en", "60 minutes"));
	    	}
	    }else if(stime.equals("23:30:00")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "30 Minuten").replace("%time_en", "30 minutes"));
	    	}
	    }else if(stime.equals("23:45:00")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "15 Minuten").replace("%time_en", "15 minutes"));
	    	}
	    }else if(stime.equals("23:55:00")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "5 Minuten").replace("%time_en", "5 minutes"));
	    	}
	    }else if(stime.equals("23:56:00")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "4 Minuten").replace("%time_en", "4 minutes"));
	    	}
	    }else if(stime.equals("23:57:00")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "3 Minuten").replace("%time_en", "3 minutes"));
	    	}
	    }else if(stime.equals("23:58:00")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "2 Minuten").replace("%time_en", "2 minutes"));
	    	}
	    }else if(stime.equals("23:59:00")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "1 Minute").replace("%time_en", "1 minute"));
	    	}
	    }else if(stime.equals("23:59:50")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "10 Sekunden").replace("%time_en", "10 seconds"));
	    	}
	    }else if(stime.equals("23:59:51")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "9 Sekunden").replace("%time_en", "9 seconds"));
	    	}
	    }else if(stime.equals("23:59:52")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "8 Sekunden").replace("%time_en", "8 seconds"));
	    	}
	    }else if(stime.equals("23:59:53")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "7 Sekunden").replace("%time_en", "7 seconds"));
	    	}
	    }else if(stime.equals("23:59:54")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "6 Sekunden").replace("%time_en", "6 seconds"));
	    	}
	    }else if(stime.equals("23:59:55")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "5 Sekunden").replace("%time_en", "5 seconds"));
	    	}
	    }else if(stime.equals("23:59:56")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "4 Sekunden").replace("%time_en", "4 seconds"));
	    	}
	    }else if(stime.equals("23:59:57")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "3 Sekunden").replace("%time_en", "3 seconds"));
	    	}
	    }else if(stime.equals("23:59:58")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "2 Sekunden").replace("%time_en", "2 seconds"));
	    	}
	    }else if(stime.equals("23:59:59")) {
	    	for(Player all : Bukkit.getOnlinePlayers()) {
	    		all.sendMessage(LanguageHandler.returnStringReady(all, "restarter.time").replace("%time_de", "1 Sekunde").replace("%time_en", "1 second"));
	    	}
	    }else if(stime.equals("00:00:01")) {
	    	Bukkit.shutdown();
	    }
	}
	
	private static String format(double tps) {
		return String.valueOf((tps > 18.0 ? ChatColor.GREEN : (tps > 16.0 ? ChatColor.YELLOW : ChatColor.RED)).toString()) + (tps > 20.0 ? "*" : "") + Math.min((double)Math.round(tps * 100.0) / 100.0, 20.0);
	}
	
	private static boolean isStaff(Player p) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT isstaff FROM redicore_userstats WHERE uuid = ?");
			ps.setString(1, p.getUniqueId().toString().replace("-", ""));
			ResultSet rs = ps.executeQuery();
			rs.next();
			boolean staff = rs.getBoolean("isstaff");
			return staff;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean isLoggedin(Player p) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT loggedin FROM redicore_userstats WHERE uuid = ?");
			ps.setString(1, p.getUniqueId().toString().replace("-", ""));
			ResultSet rs = ps.executeQuery();
			rs.next();
			boolean staff = rs.getBoolean("loggedin");
			return staff;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static int getPlayTime(Player p) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("SELECT playtime FROM redicore_userstats WHERE uuid = ?");
			ps.setString(1, p.getUniqueId().toString().replace("-", ""));
			ResultSet rs = ps.executeQuery();
			rs.next();
			return rs.getInt("playtime");
		}catch (SQLException e) {
			return 0;
		}
	}
	
	public void setPlayTime(Player p, long playtime) {
		try {
			PreparedStatement ps = MySQL.getConnection().prepareStatement("UPDATE redicore_userstats SET playtime = ? WHERE uuid = ?");
			ps.setLong(1, playtime);
			ps.setString(2, p.getUniqueId().toString().replace("-", ""));
			ps.executeUpdate();
		}catch (SQLException e) {
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		File file = new File("plugins/RCLS/ptimecache.yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		Player p = e.getPlayer();
		String uuid = p.getUniqueId().toString().replace("-", "");
		long systime = (System.currentTimeMillis() / 1000);
		cfg.set(uuid, systime);
		try {
			cfg.save(file);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		String uuid = p.getUniqueId().toString().replace("-", "");
		File file = new File("plugins/RCLS/ptimecache.yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		long oldts = cfg.getLong(uuid);
		long newts = (System.currentTimeMillis() / 1000);
		long diffts = (newts - oldts);
		long newptime = (diffts + getPlayTime(p));
		setPlayTime(p, newptime);
	}
	
	static int random(int low, int max) {
		Random r = new Random();
		int number = r.nextInt(max);
		while(number < low) {
			number = r.nextInt(max);
		}
		return number;
	}
	
	/*private static String returnRadio(String uri, String node) {
		String s = "";
		StringBuilder content = new StringBuilder();
		try {
			URL url = new URL(uri);
			URLConnection urlc = url.openConnection();
			BufferedReader bR = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			String line;
			while ((line = bR.readLine()) != null) {
				content.append(line + "\n");
			}
			bR.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		String lortu = content.toString();
		JSONParser parser = new JSONParser();
		try {
			JSONObject jo = (JSONObject)parser.parse(lortu);
			if(jo.get(node) == null) {
				s = "None";
			}else {
				s = (String) jo.get(node);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	private static String returnRadio1(String uri, String node, String subnode) {
		String s = "";
		StringBuilder content = new StringBuilder();
		try {
			URL url = new URL(uri);
			URLConnection urlc = url.openConnection();
			BufferedReader bR = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			String line;
			while ((line = bR.readLine()) != null) {
				content.append(line + "\n");
			}
			bR.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		String lortu = content.toString();
		JSONParser parser = new JSONParser();
		try {
			JSONObject jo = (JSONObject)parser.parse(lortu);
			if(jo.get(node) == null) {
				s = "None";
			}else {
				JSONObject sub = (JSONObject) jo.get(node);
				s = (String) sub.get(subnode);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return s;
	}
	
	private static String returnRadioListeners() {
		String s = "";
		try {
			URL url = new URL("https://api.laut.fm/station/redifm/listeners");
			URLConnection urlc = url.openConnection();
			BufferedReader bR = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			s = bR.readLine();
			bR.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}*/
	
	private static String parseTimeWorld(long time) {
		long gameTime = time;
		long hours = gameTime / 1000 + 6;
		long minutes = (gameTime % 1000) * 60 / 1000;
		String ampm = "AM";
		if(hours >= 12) {
			hours -= 12; ampm = "PM";
		}
		if(hours >= 12) {
			hours -= 12; ampm = "AM";
		}
		if(hours == 0) hours = 12;
		String mm = "0" + minutes;
		mm = mm.substring(mm.length() - 2, mm.length());
		return hours + ":" + mm + " " + ampm;
	}
}