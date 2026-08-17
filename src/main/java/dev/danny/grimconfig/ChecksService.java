package dev.danny.grimconfig;

import ac.grim.grimac.api.AbstractCheck;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the enabled/disabled state of every known Grim check and persists it
 * to config.yml. Reads happen from Grim's netty threads (flag events), writes
 * from the main thread (commands / GUI), so state lives in a ConcurrentHashMap
 * with volatile fields.
 */
public final class ChecksService {

    public static final class CheckEntry {
        private final String display;
        private volatile boolean enabled;
        private volatile String description = "";
        private volatile boolean experimental;

        CheckEntry(String display, boolean enabled) {
            this.display = display;
            this.enabled = enabled;
        }

        public String display() { return display; }
        public boolean enabled() { return enabled; }
        public String description() { return description; }
        public boolean experimental() { return experimental; }
    }

    private final GrimConfigPlugin plugin;
    private final Map<String, CheckEntry> entries = new ConcurrentHashMap<>();

    public ChecksService(GrimConfigPlugin plugin) {
        this.plugin = plugin;
    }

    static String norm(String name) {
        return name.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "");
    }

    /** Hot path — called from Grim's flag/prediction/command events (any thread). */
    public boolean shouldSuppress(AbstractCheck check) {
        String name = check.getCheckName();
        if (name == null || name.isEmpty()) return false;
        CheckEntry entry = entries.get(norm(name));
        return entry != null && !entry.enabled;
    }

    /** Main thread only. */
    public void load() {
        entries.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("checks");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            entries.put(norm(key), new CheckEntry(key, section.getBoolean(key, true)));
        }
    }

    /** Main thread only. */
    public void save() {
        Map<String, Boolean> out = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (CheckEntry entry : entries.values()) {
            out.put(entry.display, entry.enabled);
        }
        plugin.getConfig().createSection("checks", out);
        plugin.saveConfig();
    }

    /**
     * Registers checks discovered from a live GrimUser. Unknown checks are
     * added enabled; known checks pick up description/experimental metadata.
     * Main thread only. Returns true if any new check was added.
     */
    public boolean discover(Collection<? extends AbstractCheck> checks) {
        boolean added = false;
        for (AbstractCheck check : checks) {
            String name = check.getCheckName();
            if (name == null || name.isEmpty()) continue;
            String key = norm(name);
            CheckEntry entry = entries.get(key);
            if (entry == null) {
                entry = new CheckEntry(name, true);
                entries.put(key, entry);
                added = true;
            }
            String desc = check.getDescription();
            if (desc != null && !desc.isEmpty() && !"No description provided".equals(desc)) {
                entry.description = desc;
            }
            entry.experimental = check.isExperimental();
        }
        if (added) save();
        return added;
    }

    /** Finds a check by user input (case/space/underscore-insensitive). */
    public Optional<CheckEntry> find(String query) {
        return Optional.ofNullable(entries.get(norm(query)));
    }

    /** Sets the state of one check. Returns the entry, or empty if unknown. */
    public Optional<CheckEntry> setEnabled(String query, boolean enabled) {
        CheckEntry entry = entries.get(norm(query));
        if (entry == null) return Optional.empty();
        entry.enabled = enabled;
        save();
        return Optional.of(entry);
    }

    /** Toggles one check. Returns the entry (with its new state), or empty if unknown. */
    public Optional<CheckEntry> toggle(String query) {
        CheckEntry entry = entries.get(norm(query));
        if (entry == null) return Optional.empty();
        entry.enabled = !entry.enabled;
        save();
        return Optional.of(entry);
    }

    /** Sets every known check to the given state. Returns how many were changed. */
    public int setAll(boolean enabled) {
        int changed = 0;
        for (CheckEntry entry : entries.values()) {
            if (entry.enabled != enabled) {
                entry.enabled = enabled;
                changed++;
            }
        }
        if (changed > 0) save();
        return changed;
    }

    /** All known checks, sorted alphabetically by display name. */
    public List<CheckEntry> all() {
        List<CheckEntry> list = new ArrayList<>(entries.values());
        list.sort(Comparator.comparing(CheckEntry::display, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public int enabledCount() {
        int n = 0;
        for (CheckEntry entry : entries.values()) if (entry.enabled) n++;
        return n;
    }

    public int size() {
        return entries.size();
    }
}
