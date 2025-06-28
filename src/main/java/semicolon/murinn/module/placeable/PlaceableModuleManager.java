// Tryed Pakuri: https://github.com/Nexo-MC/CustomBlockData

package semicolon.murinn.module.placeable;

import semicolon.murinn.module.util.PDCUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

public class PlaceableModuleManager {
    public static void registerModule(PlaceableModule module, Location location, List<Location> barrierLocations, UUID displayUUID) {
        saveModuleData(location, module, barrierLocations, displayUUID);

        for (Location barrierLoc : barrierLocations) {
            Block barrierBlock = barrierLoc.getBlock();
            if (barrierBlock.getType() == Material.BARRIER) {
                saveBarrierReference(barrierBlock, location);
            }
        }
    }

    public static PlaceableModule getModuleFromBarrier(Block block) {
        if (block.getType() != Material.BARRIER) return null;

        Location moduleLoc = getModuleLocation(block);
        if (moduleLoc == null) return null;

        return getModuleAtLocation(moduleLoc);
    }

    public static Location getModuleLocation(Block barrierBlock) {
        if (barrierBlock.getType() != Material.BARRIER) return null;

        PersistentDataContainer blockPdc = getBlockPdc(barrierBlock);
        if (!blockPdc.has(PDCUtil.PARENT_MODULE, PersistentDataType.STRING)) return null;

        String parentKey = blockPdc.get(PDCUtil.PARENT_MODULE, PersistentDataType.STRING);
        return PDCUtil.parseLocationKey(parentKey);
    }

    public static PlaceableModule getModuleAtLocation(Location location) {
        PersistentDataContainer moduleData = getModuleData(location);
        if (moduleData == null) return null;

        String moduleTypeName = moduleData.get(PDCUtil.MODULE_TYPE, PersistentDataType.STRING);
        if (moduleTypeName == null) return null;

        return PlaceableModule.valueOf(moduleTypeName);
    }

    public static List<Location> getBarrierLocations(Location location) {
        PersistentDataContainer moduleData = getModuleData(location);
        if (moduleData == null) return Collections.emptyList();

        String barriersStr = moduleData.get(PDCUtil.BARRIER_LOCATIONS, PersistentDataType.STRING);
        return deserializeBarrierLocations(barriersStr, location.getWorld());
    }

    private static void saveModuleData(Location location, PlaceableModule module, List<Location> barrierLocations, UUID displayUUID) {
        PersistentDataContainer moduleMap = PDCUtil.getModuleMap(location.getChunk());

        PersistentDataContainer moduleData = moduleMap.getAdapterContext().newPersistentDataContainer();
        moduleData.set(PDCUtil.MODULE_TYPE, PersistentDataType.STRING, module.name());

        String barriers = serializeBarrierLocations(barrierLocations);
        moduleData.set(PDCUtil.BARRIER_LOCATIONS, PersistentDataType.STRING, barriers);
        moduleData.set(PDCUtil.DISPLAY_UUID, PersistentDataType.STRING, displayUUID.toString());

        NamespacedKey locationKey = PDCUtil.forLocation(location);
        moduleMap.set(locationKey, PersistentDataType.TAG_CONTAINER, moduleData);

        location.getChunk().getPersistentDataContainer().set(PDCUtil.CHUNK_DATA, PersistentDataType.TAG_CONTAINER, moduleMap);
    }

    private static void saveBarrierReference(Block barrierBlock, Location moduleLocation) {
        PersistentDataContainer blockPdc = getBlockPdc(barrierBlock);
        blockPdc.set(PDCUtil.PARENT_MODULE, PersistentDataType.STRING, PDCUtil.getLocationKey(moduleLocation));
        saveBlockPdc(barrierBlock, blockPdc);
    }

    public static void clearBarrierData(Block barrierBlock) {
        PersistentDataContainer blockPdc = getBlockPdc(barrierBlock);
        blockPdc.remove(PDCUtil.PARENT_MODULE);
        saveBlockPdc(barrierBlock, blockPdc);
    }

    public static void removeModuleData(Location location) {
        PersistentDataContainer moduleMap = PDCUtil.getModuleMap(location.getChunk());

        NamespacedKey locationKey = PDCUtil.forLocation(location);
        moduleMap.remove(locationKey);

        location.getChunk().getPersistentDataContainer().set(PDCUtil.CHUNK_DATA, PersistentDataType.TAG_CONTAINER, moduleMap);
    }

    public static PersistentDataContainer getModuleData(Location location) {
        if (location == null) return null;

        PersistentDataContainer moduleMap = PDCUtil.getModuleMap(location.getChunk());

        NamespacedKey locationKey = PDCUtil.forLocation(location);
        if (moduleMap.has(locationKey, PersistentDataType.TAG_CONTAINER)) {
            return moduleMap.get(locationKey, PersistentDataType.TAG_CONTAINER);
        }

        return null;
    }

    public static UUID getDisplayUUID(Location location) {
        PersistentDataContainer moduleData = getModuleData(location);
        if (moduleData == null || !moduleData.has(PDCUtil.DISPLAY_UUID, PersistentDataType.STRING)) {
            return null;
        }

        String uuidString = moduleData.get(PDCUtil.DISPLAY_UUID, PersistentDataType.STRING);
        if (uuidString != null) {
            return UUID.fromString(uuidString);
        }
        return null;
    }

    private static String serializeBarrierLocations(List<Location> locations) {
        return locations.stream()
                .map(loc -> loc.getBlockX() + "_" + loc.getBlockY() + "_" + loc.getBlockZ())
                .collect(Collectors.joining(";"));
    }

    private static List<Location> deserializeBarrierLocations(String data, World world) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyList();
        }

        List<Location> locations = new ArrayList<>();
        String[] barrierLocStrings = data.split(";");

        for (String barrierLocStr : barrierLocStrings) {
            String[] coords = barrierLocStr.split("_");
            if (coords.length < 3) continue;

            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            int z = Integer.parseInt(coords[2]);
            locations.add(new Location(world, x, y, z));
        }

        return locations;
    }

    private static PersistentDataContainer getBlockPdc(Block block) {
        if (block.getState() instanceof TileState) {
            return ((TileState) block.getState()).getPersistentDataContainer();
        }

        PersistentDataContainer chunkPdc = block.getChunk().getPersistentDataContainer();

        if (!chunkPdc.has(PDCUtil.CHUNK_DATA, PersistentDataType.TAG_CONTAINER)) {
            chunkPdc.set(PDCUtil.CHUNK_DATA, PersistentDataType.TAG_CONTAINER,
                    chunkPdc.getAdapterContext().newPersistentDataContainer());
        }

        PersistentDataContainer blockMap = chunkPdc.get(PDCUtil.CHUNK_DATA, PersistentDataType.TAG_CONTAINER);

        NamespacedKey blockKey = PDCUtil.forLocation(block.getLocation());

        if (blockMap.has(blockKey, PersistentDataType.TAG_CONTAINER)) {
            return blockMap.get(blockKey, PersistentDataType.TAG_CONTAINER);
        } else {
            return blockMap.getAdapterContext().newPersistentDataContainer();
        }
    }

    private static void saveBlockPdc(Block block, PersistentDataContainer pdc) {
        if (block.getState() instanceof TileState state) {
            PersistentDataContainer blockPdc = state.getPersistentDataContainer();

            for (NamespacedKey key : pdc.getKeys()) {
                if (pdc.has(key, PersistentDataType.STRING)) {
                    blockPdc.set(key, PersistentDataType.STRING, pdc.get(key, PersistentDataType.STRING));
                }
            }

            state.update();
            return;
        }

        PersistentDataContainer chunkPdc = block.getChunk().getPersistentDataContainer();

        if (!chunkPdc.has(PDCUtil.CHUNK_DATA, PersistentDataType.TAG_CONTAINER)) {
            chunkPdc.set(PDCUtil.CHUNK_DATA, PersistentDataType.TAG_CONTAINER,
                    chunkPdc.getAdapterContext().newPersistentDataContainer());
        }

        PersistentDataContainer blockMap = chunkPdc.get(PDCUtil.CHUNK_DATA, PersistentDataType.TAG_CONTAINER);
        NamespacedKey blockKey = PDCUtil.forLocation(block.getLocation());

        if (pdc.isEmpty()) {
            if (blockMap != null) {
                blockMap.remove(blockKey);
            }
        } else {
            if (blockMap != null) {
                blockMap.set(blockKey, PersistentDataType.TAG_CONTAINER, pdc);
            }
        }

        if (blockMap != null) {
            chunkPdc.set(PDCUtil.CHUNK_DATA, PersistentDataType.TAG_CONTAINER, blockMap);
        }
    }
}