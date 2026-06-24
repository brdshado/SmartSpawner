package github.nighter.smartspawner.hooks.economy.shops.providers.bharatmcshop;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.hooks.economy.shops.providers.ShopProvider;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class BharatMCShopProvider implements ShopProvider {

    private final SmartSpawner plugin;
    private Plugin targetPlugin;
    private Method getPriceMethod;

    public BharatMCShopProvider(SmartSpawner plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        // Try both possible names
        targetPlugin = plugin.getServer().getPluginManager().getPlugin("BharatMC-Shop");
        if (targetPlugin == null) {
            targetPlugin = plugin.getServer().getPluginManager().getPlugin("BharatMC-Sell");
        }

        // Case-insensitive fallback
        if (targetPlugin == null) {
            for (Plugin p : plugin.getServer().getPluginManager().getPlugins()) {
                if (p.getName().equalsIgnoreCase("BharatMC-Shop") || p.getName().equalsIgnoreCase("BharatMC-Sell")) {
                    targetPlugin = p;
                    break;
                }
            }
        }

        if (targetPlugin != null) {
            try {
                // Use reflection to find getPrice(Material) method
                getPriceMethod = targetPlugin.getClass().getMethod("getPrice", Material.class);
                plugin.getLogger().info("Successfully hooked into " + targetPlugin.getName() + " via reflection!");
            } catch (Exception e) {
                plugin.getLogger().warning("Found " + targetPlugin.getName() + " but could not find getPrice(Material) method!");
            }
        }
    }

    @Override
    public String getPluginName() {
        return targetPlugin != null ? targetPlugin.getName() : "BharatMC-Shop";
    }

    @Override
    public boolean isAvailable() {
        return targetPlugin != null && targetPlugin.isEnabled() && getPriceMethod != null;
    }

    @Override
    public double getSellPrice(Material material) {
        if (!isAvailable()) return 0.0;
        try {
            Object result = getPriceMethod.invoke(targetPlugin, material);
            if (result instanceof Double) {
                return (Double) result;
            } else if (result instanceof Number) {
                return ((Number) result).doubleValue();
            }
        } catch (Exception e) {
            // Silently fail to avoid console spam during mass selling
        }
        return 0.0;
    }
}
