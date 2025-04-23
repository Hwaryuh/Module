package kr.murinn.module.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.structure.Mirror;
import org.bukkit.entity.Player;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;

import java.util.Random;

public class StructureUtil {
    public static void placeStructureAtCorner(Player player, String structureName, Direction direction) {
        Chunk currentChunk = player.getLocation().getChunk();
        World world = player.getWorld();
        int currentX = currentChunk.getX();
        int currentZ = currentChunk.getZ();
        int playerY = player.getLocation().getBlockY();

        Location corner;

        switch (direction) {
            case NORTH:
                corner = new Location(world, (currentX * 16 + 0.5), playerY, (currentZ * 16 + 16.5));
                break;
            case EAST:
                corner = new Location(world, (currentX * 16 - 0.5), playerY, (currentZ * 16 + 0.5));
                break;
            case WEST:
                corner = new Location(world, (currentX * 16 + 16.5), playerY, (currentZ * 16 + 15.5));
                break;
            case SOUTH:
                corner = new Location(world, (currentX * 16 + 15.5), playerY, (currentZ * 16 - 0.5));
                break;
            default:
                return;
        }

        StructureManager structureManager = player.getServer().getStructureManager();
        NamespacedKey key = new NamespacedKey("minecraft", structureName);
        Structure structure = structureManager.loadStructure(key);

        if (structure == null) {
            player.sendMessage(structureName + "을(를) 찾을 수 없습니다.");
            return;
        }

        structure.place(corner, true, direction.getRotation(), Mirror.NONE, 0, 1.0f, new Random());
    }
}