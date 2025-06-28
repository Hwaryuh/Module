package semicolon.murinn.module.util;

import semicolon.murinn.module.Main;
import semicolon.murinn.module.build.BuildResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.structure.Structure;

import java.util.Random;

public class BuildUtil {
    public static BuildResult check(Location location, String structureName, int chunksToOccupy, int direction) {
        Chunk baseChunk = location.getChunk();

        // 기본 청크 체크
        if (hasStructure(baseChunk)) return BuildResult.ALREADY_EXISTS;

        // 추가 청크들 체크
        for (int i = 1; i < chunksToOccupy; i++) {
            int dx = 0, dz = 0;
            if (direction == 0) dz = i;      // 앞쪽
            else if (direction == 1) dz = -i; // 뒤쪽
            else if (direction == 2) dx = i;  // 오른쪽 (수정: -i -> i)
            else if (direction == 3) dx = -i; // 왼쪽 (수정: i -> -i)

            Chunk additionalChunk = baseChunk.getWorld().getChunkAt(
                    baseChunk.getX() + dx, baseChunk.getZ() + dz);

            if (hasStructure(additionalChunk)) return BuildResult.ALREADY_EXISTS;
        }

        NamespacedKey key = new NamespacedKey("minecraft", structureName);
        Structure structure = Bukkit.getServer().getStructureManager().loadStructure(key);
        if (structure == null) return BuildResult.STRUCTURE_NOT_FOUND;

        return BuildResult.SUCCESS;
    }

    public static void placeStructure(Location location, String structureName, StructureRotation rotation, int chunksToOccupy, int direction) {
        NamespacedKey key = new NamespacedKey("minecraft", structureName);
        Structure structure = Bukkit.getServer().getStructureManager().loadStructure(key);

        if (structure != null) {
            float[] integrity = { 0.1f, 0.5f, 1.0f };
            float[] pitch = { 0.4f, 1.0f, 1.8f };

            new BukkitRunnable() {
                int progress = 0;

                @Override
                public void run() {
                    if (progress >= integrity.length) {
                        saveStructureData(location.getChunk(), chunksToOccupy, direction);
                        this.cancel();
                        return;
                    }

                    boolean skipEntity = (progress < integrity.length - 1);
                    structure.place(location, !skipEntity, rotation, Mirror.NONE, 0, integrity[progress], new Random());
                    location.getWorld().playSound(SpatialUtil.getCenterLocation(location, 1), "semicolon.build_module_progress", 2.0f, pitch[progress]);
                    progress ++;
                }
            }.runTaskTimer(Main.plugin(), 25L, 25L);
        }
    }

    public static void resetPlayerChunk(Player player) {
        Chunk chunk = player.getChunk();

        if (hasStructure(chunk)) {
            removeStructureData(chunk);
            player.sendActionBar(Component.text("해당 청크의 데이터가 초기화되었습니다.", NamedTextColor.GREEN));
        } else {
            player.sendActionBar(Component.text("이 청크에는 구조물이 없습니다.", NamedTextColor.YELLOW));
        }
    }

    public static boolean hasStructure(Chunk chunk) {
        PersistentDataContainer chunkPdc = chunk.getPersistentDataContainer();
        return chunkPdc.has(PDCUtil.CHUNK_HAS_STRUCTURE, PersistentDataType.BOOLEAN) &&
                Boolean.TRUE.equals(chunkPdc.get(PDCUtil.CHUNK_HAS_STRUCTURE, PersistentDataType.BOOLEAN));
    }

    public static void saveStructureData(Chunk baseChunk, int chunksToOccupy, int direction) {
        // 기본 청크 점유
        PersistentDataContainer chunkPdc = baseChunk.getPersistentDataContainer();
        chunkPdc.set(PDCUtil.CHUNK_HAS_STRUCTURE, PersistentDataType.BOOLEAN, true);

        // 기본 청크 로그
        Main.plugin().getLogger().info("구조물 점유 청크: [" + baseChunk.getX() + ", " + baseChunk.getZ() + "]");

        // 추가 청크들 점유
        for (int i = 1; i < chunksToOccupy; i++) {
            int dx = 0, dz = 0;
            if (direction == 0) dz = i;      // 앞쪽
            else if (direction == 1) dz = -i; // 뒤쪽
            else if (direction == 2) dx = i;  // 오른쪽 (수정: -i -> i)
            else if (direction == 3) dx = -i; // 왼쪽 (수정: i -> -i)

            Chunk additionalChunk = baseChunk.getWorld().getChunkAt(
                    baseChunk.getX() + dx, baseChunk.getZ() + dz);

            PersistentDataContainer additionalPdc = additionalChunk.getPersistentDataContainer();
            additionalPdc.set(PDCUtil.CHUNK_HAS_STRUCTURE, PersistentDataType.BOOLEAN, true);

            // 추가 청크 로그
            Main.plugin().getLogger().info("구조물 점유 청크: [" + additionalChunk.getX() + ", " + additionalChunk.getZ() + "]");
        }
    }

    public static void removeStructureData(Chunk chunk) {
        PersistentDataContainer chunkPdc = chunk.getPersistentDataContainer();
        chunkPdc.remove(PDCUtil.CHUNK_HAS_STRUCTURE);
    }
}