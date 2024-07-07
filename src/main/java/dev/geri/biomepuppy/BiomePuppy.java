package dev.geri.biomepuppy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BiomePuppy extends JavaPlugin implements Listener {

    private final List<Material> validFoods = new ArrayList<>();
    private final HashMap<Wolf.Variant, List<Biome>> variants = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();

        this.validFoods.clear();
        for (String raw : config.getStringList("valid-foods")) {
            try {
                this.validFoods.add(Material.valueOf(raw));
            } catch (IllegalArgumentException e) {
                this.getLogger().warning("Invalid food: " + raw);
            }
        }

        this.variants.clear();
        for (String rawVariant : config.getConfigurationSection("variants").getKeys(false)) {
            try {
                Wolf.Variant variant = Registry.WOLF_VARIANT.get(NamespacedKey.minecraft(rawVariant.toLowerCase()));
                ArrayList<Biome> biomes = new ArrayList<>();
                for (String rawBiome : config.getStringList("variants." + rawVariant)) {
                    try {
                        biomes.add(Biome.valueOf(rawBiome));
                    } catch (IllegalArgumentException e) {
                        this.getLogger().warning("Invalid biome: " + rawBiome);
                    }
                }
                this.variants.put(variant, biomes);
            } catch (Exception exception) {
                this.getLogger().warning("Invalid variant: " + rawVariant);
            }
        }
    }

    @EventHandler
    public void onEntityBreed(EntityBreedEvent event) {
        if (!(event.getEntity() instanceof Wolf puppy)) return;
        if (event.getBredWith() == null || !this.validFoods.contains(event.getBredWith().getType())) return;
        for (Map.Entry<Wolf.Variant, List<Biome>> entry : this.variants.entrySet()) {
            if (entry.getValue().contains(puppy.getLocation().getBlock().getBiome())) {
                puppy.setVariant(entry.getKey());
                puppy.setTamed(false);
                return;
            }
        }
    }

}
