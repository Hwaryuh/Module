package semicolon.murinn.module.util;

import semicolon.murinn.module.Main;
import semicolon.murinn.module.placeable.PlaceableModuleManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PDCUtil {
    public static final NamespacedKey SEMICOLON_ITEM = new NamespacedKey(Main.plugin(), "semicolon_item");
    public static final NamespacedKey MODULE_TYPE = new NamespacedKey(Main.plugin(), "module_type");
    public static final NamespacedKey BARRIER_LOCATIONS = new NamespacedKey(Main.plugin(), "barrier_locations");
    public static final NamespacedKey DISPLAY_UUID = new NamespacedKey(Main.plugin(), "display_uuid");
    public static final NamespacedKey PARENT_MODULE = new NamespacedKey(Main.plugin(), "parent_module_loc");
    public static final NamespacedKey CHUNK_DATA = new NamespacedKey(Main.plugin(), "chunk_module_data");
    public static final NamespacedKey CHUNK_HAS_STRUCTURE = new NamespacedKey(Main.plugin(), "chunk_has_structure");
    public static final NamespacedKey MODULE_STATE = new NamespacedKey(Main.plugin(), "module_state");
    public static final NamespacedKey MODULE_PROCESS_RESULT = new NamespacedKey(Main.plugin(), "module_process_result");
    public static final NamespacedKey MODULE_COMPLETION_TIME = new NamespacedKey(Main.plugin(), "module_completion_time");
    public static final NamespacedKey GRINDER_UPGRADE_LEVEL = new NamespacedKey(Main.plugin(), "grinder_upgrade_level");
    public static final NamespacedKey FURNACE_UPGRADE_LEVEL = new NamespacedKey(Main.plugin(), "furnace_upgrade_level");

    public static NamespacedKey forLocation(Location location) {
        String key = "loc_" + getLocationKey(location);
        return new NamespacedKey(Main.plugin(), key);
    }

    public static String getLocationKey(Location location) {
        return location.getWorld().getName() + "_" +
                location.getBlockX() + "_" +
                location.getBlockY() + "_" +
                location.getBlockZ();
    }

    public static Location parseLocationKey(String key) {
        if (key == null) return null;

        String[] parts = key.split("_");
        if (parts.length < 4) return null;

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;

        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);
        return new Location(world, x, y, z);
    }

    public static PersistentDataContainer getModuleMap(Chunk chunk) {
        PersistentDataContainer chunkPdc = chunk.getPersistentDataContainer();

        if (chunkPdc.has(CHUNK_DATA, PersistentDataType.TAG_CONTAINER)) {
            return chunkPdc.get(CHUNK_DATA, PersistentDataType.TAG_CONTAINER);
        } else {
            return chunkPdc.getAdapterContext().newPersistentDataContainer();
        }
    }

    public static void saveModulePdc(Location moduleLocation, PersistentDataContainer pdc) {
        NamespacedKey locationKey = forLocation(moduleLocation);
        PersistentDataContainer moduleMap = getModuleMap(moduleLocation.getChunk());
        moduleMap.set(locationKey, PersistentDataType.TAG_CONTAINER, pdc);
        moduleLocation.getChunk().getPersistentDataContainer().set(
                CHUNK_DATA, PersistentDataType.TAG_CONTAINER, moduleMap);
    }

    public static PersistentDataContainer getModulePdc(Location moduleLocation) {
        return PlaceableModuleManager.getModuleData(moduleLocation);
    }
}