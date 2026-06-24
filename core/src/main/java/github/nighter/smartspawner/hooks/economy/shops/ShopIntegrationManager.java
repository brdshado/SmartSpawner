package github.nighter.smartspawner.hooks.economy.shops;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.hooks.economy.shops.providers.ShopProvider;
import github.nighter.smartspawner.hooks.economy.shops.providers.economyshopgui.EconomyShopGUIProvider;
import github.nighter.smartspawner.hooks.economy.shops.providers.economyshopgui.ESGUICompatibilityHandler;
import github.nighter.smartspawner.hooks.economy.shops.providers.excellentshop.ExcellentShopProvider;
import github.nighter.smartspawner.hooks.economy.shops.providers.shopguiplus.ShopGuiPlusProvider;
import github.nighter.smartspawner.hooks.economy.shops.providers.shopguiplus.SpawnerHook;
import github.nighter.smartspawner.hooks.economy.shops.providers.zshop.ZShopProvider;
import github.nighter.smartspawner.hooks.economy.shops.providers.bharatmcshop.BharatMCShopProvider;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ShopIntegrationManager {
    private final SmartSpawner plugin;
    private ShopProvider activeProvider;
    private final List<ShopProvider> availableProviders = new ArrayList<>();
    private SpawnerHook spawnerHook = null;
    private ESGUICompatibilityHandler esguiCompatibilityHandler = null;

    public void initialize() {
        availableProviders.clear();
        activeProvider = null;

        detectAndRegisterActiveProviders();
        selectActiveProvider();
    }

    private void detectAndRegisterActiveProviders() {
        // Check configuration for preferred plugin first
        String configuredShop = plugin.getConfig().getString("custom_economy.shop_integration.preferred_plugin", "auto");
        boolean autoDetect = "auto".equalsIgnoreCase(configuredShop);

        // If a specific shop is configured, only try to load that one
        if (!autoDetect) {
            if (tryRegisterSpecificProvider(configuredShop)) {
                plugin.getLogger().info("Successfully loaded preferred shop plugin: " + configuredShop);
                return;
            } else {
                plugin.getLogger().warning("Preferred shop plugin '" + configuredShop + "' could not be loaded.");
                plugin.getLogger().info("Available plugins: " + String.join(", ", 
                    java.util.Arrays.stream(plugin.getServer().getPluginManager().getPlugins())
                        .map(org.bukkit.plugin.Plugin::getName)
                        .toArray(String[]::new)));
                plugin.getLogger().info("Falling back to auto-detection...");
            }
        }

        registerProviderIfAvailable("EconomyShopGUI", () -> {
            EconomyShopGUIProvider provider = new EconomyShopGUIProvider(plugin);

            // Initialize PluginCompatibilityHandler after creating the provider and only if it's null
            if (provider.isAvailable() && esguiCompatibilityHandler == null) {
                try {
                    esguiCompatibilityHandler = new ESGUICompatibilityHandler(plugin);
                    plugin.getServer().getPluginManager().registerEvents(esguiCompatibilityHandler, plugin);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to register ESGUICompatibilityHandler: " + e.getMessage());
                }
            }

            return provider;
        });


        // Only try ShopGUIPlus if the plugin is actually present and enabled
        if (isPluginAvailable("ShopGUIPlus")) {
            registerProviderIfAvailable("ShopGUIPlus", () -> {
                // Register the spawner hook event listener before creating the provider
                if (spawnerHook == null) {
                    try {
                        spawnerHook = new SpawnerHook(plugin);
                        plugin.getServer().getPluginManager().registerEvents(spawnerHook, plugin);
                    } catch (Exception e) {
                        plugin.debug("Failed to register SpawnerHook: " + e.getMessage());
                        throw e; // Re-throw to prevent provider registration
                    }
                }
                return new ShopGuiPlusProvider(plugin);
            });
        }

        // registerProviderIfAvailable("ZShop", () -> new ZShopProvider(plugin));
        registerProviderIfAvailable("ExcellentShop", () -> new ExcellentShopProvider(plugin));
        registerProviderIfAvailable("BharatMC-Shop", () -> new BharatMCShopProvider(plugin));
    }

    private boolean tryRegisterSpecificProvider(String providerName) {
        try {
            switch (providerName.toLowerCase()) {
                case "economyshopgui":
                    if (isPluginAvailable("EconomyShopGUI")) {
                        registerProviderIfAvailable("EconomyShopGUI", () -> {
                            EconomyShopGUIProvider provider = new EconomyShopGUIProvider(plugin);

                            // Initialize PluginCompatibilityHandler after creating the provider and only if it's null
                            if (provider.isAvailable() && esguiCompatibilityHandler == null) {
                                try {
                                    esguiCompatibilityHandler = new ESGUICompatibilityHandler(plugin);
                                    plugin.getServer().getPluginManager().registerEvents(esguiCompatibilityHandler, plugin);
                                } catch (Exception e) {
                                    plugin.getLogger().warning("Failed to register ESGUICompatibilityHandler: " + e.getMessage());
                                }
                            }

                            return provider;
                        });
                        return !availableProviders.isEmpty();
                    }
                    break;
                case "shopguiplus":
                    if (isPluginAvailable("ShopGUIPlus")) {
                        registerProviderIfAvailable("ShopGUIPlus", () -> {
                            if (spawnerHook == null) {
                                spawnerHook = new SpawnerHook(plugin);
                                plugin.getServer().getPluginManager().registerEvents(spawnerHook, plugin);
                                plugin.debug("Registered SpawnerHook event listener for ShopGUIPlus");
                            }
                            return new ShopGuiPlusProvider(plugin);
                        });
                        return !availableProviders.isEmpty();
                    }
                    break;
                case "zshop":
                    if (isPluginAvailable("ZShop")) {
                        registerProviderIfAvailable("ZShop", () -> new ZShopProvider(plugin));
                        return !availableProviders.isEmpty();
                    }
                    break;
                case "excellentshop":
                    if (isPluginAvailable("ExcellentShop")) {
                        registerProviderIfAvailable("ExcellentShop", () -> new ExcellentShopProvider(plugin));
                        return !availableProviders.isEmpty();
                    }
                    break;
                case "bharatmc-shop":
                case "bharatmc-sell":
                case "bharatmcshop":
                case "bharatmcsell":
                    if (isPluginAvailable("BharatMC-Shop")) {
                        registerProviderIfAvailable("BharatMC-Shop", () -> new BharatMCShopProvider(plugin));
                        return !availableProviders.isEmpty();
                    }
                    if (isPluginAvailable("BharatMC-Sell")) {
                        registerProviderIfAvailable("BharatMC-Sell", () -> new BharatMCShopProvider(plugin));
                        return !availableProviders.isEmpty();
                    }
                    break;
            }
        } catch (Throwable t) {
            plugin.getLogger().log(Level.SEVERE, "CRITICAL error while loading specific shop provider: " + providerName, t);
        }
        return false;
    }

    private boolean isPluginAvailable(String pluginName) {
        // First try direct lookup
        Plugin targetPlugin = plugin.getServer().getPluginManager().getPlugin(pluginName);
        
        // If not found, try case-insensitive search
        if (targetPlugin == null) {
            for (Plugin p : plugin.getServer().getPluginManager().getPlugins()) {
                if (p.getName().equalsIgnoreCase(pluginName)) {
                    targetPlugin = p;
                    break;
                }
            }
        }

        return true;
    }

    private void registerProviderIfAvailable(String providerName, Supplier<ShopProvider> providerSupplier) {
        // If we already have an active provider and we're in single-provider mode, skip
        if (!availableProviders.isEmpty()) {
            plugin.debug("Skipping " + providerName + " registration - already have active provider: " +
                    availableProviders.getFirst().getPluginName());
            return;
        }

        try {
            ShopProvider provider = providerSupplier.get();
            if (provider.isAvailable()) {
                availableProviders.add(provider);
            }
        } catch (NoClassDefFoundError e) {
            plugin.debug("Shop provider " + providerName + " classes not found (plugin not installed): " + e.getMessage());
        } catch (Throwable t) {
            plugin.getLogger().log(Level.SEVERE, "CRITICAL: Error initializing shop integrations", t);
        }
    }

    private void selectActiveProvider() {
        if (availableProviders.isEmpty()) {
            plugin.getLogger().info("No compatible shop plugins found. Shop integration is disabled.");
            return;
        }

        // Since we now ensure only one provider is registered, just use the first (and only) one
        activeProvider = availableProviders.getFirst();
        plugin.getLogger().info("Auto-detected & successfully hook into shop plugin: " + activeProvider.getPluginName());
    }

    public double getPrice(Material material) {
        if (activeProvider == null || material == null) {
            return 0.0;
        }

        try {
            return activeProvider.getSellPrice(material);
        } catch (Exception e) {
            plugin.debug("Error getting price for " + material + " from " + activeProvider.getPluginName() + ": " + e.getMessage());
            return 0.0;
        }
    }

    public String getActiveShopPlugin() {
        return activeProvider != null ? activeProvider.getPluginName() : "None";
    }

    public boolean hasActiveProvider() {
        return activeProvider != null;
    }

    public void cleanup() {
        availableProviders.clear();
        activeProvider = null;
        if (spawnerHook != null) {
            spawnerHook.unregister();
            spawnerHook = null;
        }
        if (esguiCompatibilityHandler != null) {
            esguiCompatibilityHandler = null;
        }
    }
}