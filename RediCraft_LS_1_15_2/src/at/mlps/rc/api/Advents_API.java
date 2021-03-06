package at.mlps.rc.api;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Advents_API {
	
	static File file = new File("plugins/RCLS/advents.yml");
	
	public void setReward(Player p, int day) {
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		cfg.set("Advents." + p.getUniqueId().toString() + ".Day" + day, true);
		try {
			cfg.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasRewardUsed(Player p, int day){
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		if(cfg.contains("Advents." + p.getUniqueId().toString() + ".Day" + day)) {
			return true;
		}
		return false;
	}
	
	public boolean isAllowedDate(String date) {
		String formDate = date + "/12";
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
		String sdff = sdf.format(new Date(1608037200000L));
		if(formDate.equalsIgnoreCase(sdff)) {
			return true;
		}
		return false;
	}

}