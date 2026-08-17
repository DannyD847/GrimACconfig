package dev.danny.grimconfig.gui;

import dev.danny.grimconfig.ChecksService;
import dev.danny.grimconfig.GrimConfigPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** Paginated chest GUI listing every known Grim check; click to toggle. */
public final class CheckMenu {

    public static final int SIZE = 54;
    public static final int CHECKS_PER_PAGE = 45;
    public static final int SLOT_PREV = 45;
    public static final int SLOT_ALL_ON = 47;
    public static final int SLOT_INFO = 49;
    public static final int SLOT_ALL_OFF = 51;
    public static final int SLOT_NEXT = 53;

    private CheckMenu() {}

    public static NamespacedKey checkKey(GrimConfigPlugin plugin) {
        return new NamespacedKey(plugin, "check");
    }

    public static final class Holder implements InventoryHolder {
        final int page;
        final List<String> slotNames; // index = slot, value = check display name
        Inventory inventory;

        Holder(int page, List<String> slotNames) {
            this.page = page;
            this.slotNames = slotNames;
        }

        public int page() { return page; }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }

    public static void open(GrimConfigPlugin plugin, Player player, int page) {
        plugin.discoverOnline();
        ChecksService checks = plugin.checks();
        List<ChecksService.CheckEntry> all = checks.all();

        int pages = Math.max(1, (all.size() + CHECKS_PER_PAGE - 1) / CHECKS_PER_PAGE);
        page = Math.max(0, Math.min(page, pages - 1));

        int from = page * CHECKS_PER_PAGE;
        int to = Math.min(from + CHECKS_PER_PAGE, all.size());
        List<ChecksService.CheckEntry> shown = all.subList(from, to);

        List<String> slotNames = new ArrayList<>(shown.size());
        for (ChecksService.CheckEntry entry : shown) {
            slotNames.add(entry.display());
        }

        Holder holder = new Holder(page, slotNames);
        Inventory inventory = Bukkit.createInventory(holder, SIZE,
                Component.text("Grim Checks  ·  page " + (page + 1) + "/" + pages, NamedTextColor.DARK_GRAY));
        holder.inventory = inventory;

        for (int i = 0; i < shown.size(); i++) {
            inventory.setItem(i, checkItem(plugin, shown.get(i)));
        }

        if (page > 0) {
            inventory.setItem(SLOT_PREV, navItem(Material.ARROW, "◀ Previous page"));
        }
        if (page < pages - 1) {
            inventory.setItem(SLOT_NEXT, navItem(Material.ARROW, "Next page ▶"));
        }
        inventory.setItem(SLOT_ALL_ON, bulkItem(Material.LIME_STAINED_GLASS_PANE,
                "Enable ALL checks", NamedTextColor.GREEN));
        inventory.setItem(SLOT_ALL_OFF, bulkItem(Material.RED_STAINED_GLASS_PANE,
                "Disable ALL checks", NamedTextColor.RED));
        inventory.setItem(SLOT_INFO, infoItem(checks));

        player.openInventory(inventory);
    }

    public static ItemStack checkItem(GrimConfigPlugin plugin, ChecksService.CheckEntry entry) {
        boolean enabled = entry.enabled();
        ItemStack item = new ItemStack(enabled ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plain(Component.text(entry.display(),
                enabled ? NamedTextColor.GREEN : NamedTextColor.RED)));

        List<Component> lore = new ArrayList<>();
        lore.add(plain(enabled
                ? Component.text("Enabled", NamedTextColor.GREEN)
                : Component.text("Disabled", NamedTextColor.RED)));
        if (!entry.description().isEmpty()) {
            lore.add(plain(Component.text(entry.description(), NamedTextColor.GRAY)));
        }
        if (entry.experimental()) {
            lore.add(plain(Component.text("Experimental check", NamedTextColor.GOLD)));
        }
        lore.add(plain(Component.text(enabled ? "Click to disable" : "Click to enable",
                NamedTextColor.YELLOW)));
        meta.lore(lore);

        meta.getPersistentDataContainer().set(checkKey(plugin), PersistentDataType.STRING, entry.display());
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack infoItem(ChecksService checks) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plain(Component.text("GrimConfig", NamedTextColor.AQUA)));
        int enabled = checks.enabledCount();
        int total = checks.size();
        meta.lore(List.of(
                plain(Component.text(enabled + " enabled", NamedTextColor.GREEN)
                        .append(Component.text("  ·  ", NamedTextColor.DARK_GRAY))
                        .append(Component.text((total - enabled) + " disabled", NamedTextColor.RED))),
                plain(Component.text("Changes apply instantly and", NamedTextColor.GRAY)),
                plain(Component.text("are saved to config.yml", NamedTextColor.GRAY))));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack navItem(Material material, String label) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plain(Component.text(label, NamedTextColor.YELLOW)));
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack bulkItem(Material material, String label, NamedTextColor color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(plain(Component.text(label, color)));
        meta.lore(List.of(plain(Component.text("Applies to every known check", NamedTextColor.GRAY))));
        item.setItemMeta(meta);
        return item;
    }

    private static Component plain(Component component) {
        return component.decoration(TextDecoration.ITALIC, false);
    }
}
