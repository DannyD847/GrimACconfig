package dev.danny.grimconfig.gui;

import dev.danny.grimconfig.ChecksService;
import dev.danny.grimconfig.GrimConfigPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

public final class MenuListener implements Listener {

    private final GrimConfigPlugin plugin;

    public MenuListener(GrimConfigPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof CheckMenu.Holder holder)) return;
        event.setCancelled(true);
        if (event.getClickedInventory() != top) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getSlot();
        switch (slot) {
            case CheckMenu.SLOT_PREV -> {
                click(player);
                CheckMenu.open(plugin, player, holder.page() - 1);
                return;
            }
            case CheckMenu.SLOT_NEXT -> {
                click(player);
                CheckMenu.open(plugin, player, holder.page() + 1);
                return;
            }
            case CheckMenu.SLOT_ALL_ON -> {
                plugin.checks().setAll(true);
                click(player);
                CheckMenu.open(plugin, player, holder.page());
                return;
            }
            case CheckMenu.SLOT_ALL_OFF -> {
                plugin.checks().setAll(false);
                click(player);
                CheckMenu.open(plugin, player, holder.page());
                return;
            }
            default -> { }
        }

        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        String name = meta.getPersistentDataContainer()
                .get(CheckMenu.checkKey(plugin), PersistentDataType.STRING);
        if (name == null) return;

        Optional<ChecksService.CheckEntry> toggled = plugin.checks().toggle(name);
        if (toggled.isEmpty()) return;

        top.setItem(slot, CheckMenu.checkItem(plugin, toggled.get()));
        top.setItem(CheckMenu.SLOT_INFO, CheckMenu.infoItem(plugin.checks()));
        click(player);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof CheckMenu.Holder) {
            event.setCancelled(true);
        }
    }

    private void click(Player player) {
        player.playSound(player.getLocation(), "minecraft:ui.button.click", 0.5f, 1.0f);
    }
}
