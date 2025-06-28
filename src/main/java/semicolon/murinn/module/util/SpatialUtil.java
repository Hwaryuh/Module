package semicolon.murinn.module.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import semicolon.murinn.module.Main;

public class SpatialUtil {
    public static Location getCenterLocation(Player player, int yOffset) {
        int x = player.getChunk().getX() * 16 + 8;
        int z = player.getChunk().getZ() * 16 + 8;
        int y = (int) Math.round(player.getY()) + yOffset;

        return new Location(player.getWorld(), x, y, z);
    }

    public static Location getCenterLocation(Chunk chunk, Player player, int yOffset) {
        int x = chunk.getX() * 16 + 8;
        int z = chunk.getZ() * 16 + 8;
        int y = (int) Math.round(player.getY()) + yOffset;

        return new Location(chunk.getWorld(), x, y + 0.5, z);
    }

    public static Location getCenterLocation(Location location, int yOffset) {
        int x = location.getChunk().getX() * 16 + 8;
        int z = location.getChunk().getZ() * 16 + 8;
        int y = (int) Math.round(location.getY()) + yOffset;

        return new Location(location.getWorld(), x, y + 0.5, z);
    }

    public static Chunk getRelativeChunk(Player player, int direction) {
        Chunk chunk = player.getChunk();

        float yaw = player.getLocation().getYaw();
        if (yaw < 0) yaw += 360;

        float yawAngle = 0;
        if (direction == 1) yawAngle = 180;
        else if (direction == 2) yawAngle = 270;
        else if (direction == 3) yawAngle = 90;

        float result = (yaw + yawAngle) % 360;
        if (result < 0) result += 360;

        int dx = 0, dz = 0;
        if (result >= 315 || result < 45) dz = 1;      // 남 (≈ 0°)
        else if (result >= 45 && result < 135) dx = -1; // 서 (≈ 90°)
        else if (result >= 135 && result < 225) dz = -1;// 북 (≈ 180°)
        else if (result >= 225 && result < 315) dx = 1; // 동 (≈ 270°)

        return player.getWorld().getChunkAt(chunk.getX() + dx, chunk.getZ() + dz);
    }

    public static int getIndicatorDirection(Player player, ItemDisplay indicator) {
        Chunk playerChunk = player.getChunk();
        Chunk indicatorChunk = indicator.getChunk();

        int dir;
        int dx = indicatorChunk.getX() - playerChunk.getX();
        int dz = indicatorChunk.getZ() - playerChunk.getZ();

        if (Math.abs(dx) > Math.abs(dz)) {
            if (dx > 0) dir = 2;
            else dir = 3;
        } else {
            if (dz > 0) dir = 0;
            else dir = 1;
        }
        return dir;
    }

    public static float getRotationForDirection(int direction, float observeYaw) {
        float rot = 0;
        boolean is90 = Math.abs(Math.abs(observeYaw) - 90.0f) < 0.1f;

        if (is90) {
            switch (direction) {
                case 0 -> rot = observeYaw > 0 ? -90 : 90;
                case 1 -> rot = observeYaw > 0 ? 90 : -90;
                case 2 -> rot = observeYaw > 0 ? 0 : 180;
                case 3 -> rot = observeYaw > 0 ? 180 : 0;
            }
        } else {
            switch (direction) {
                case 0 -> rot = 0;
                case 1 -> rot = 180;
                case 2 -> rot = 90;
                case 3 -> rot = -90;
            }

            rot = (observeYaw + rot) % 360;
        }

        if (rot > 180) rot -= 360;
        else if (rot < -180) rot += 360;

        return rot;
    }

    public static StructureRotation getStructureRotation(Player player, ItemDisplay indicator) {
        Location playerLoc = player.getLocation();
        Location indicatorLoc = indicator.getLocation();

        double dx = indicatorLoc.getX() - playerLoc.getX();
        double dz = indicatorLoc.getZ() - playerLoc.getZ();
        double angle = Math.atan2(dx, dz);
        double degrees = Math.toDegrees(angle);

        if (degrees < 0) degrees += 360;

        int degrees2 = (int) Math.round(degrees / 90) * 90;
        degrees2 %= 360;

        return switch (degrees2) {
            case 90 -> StructureRotation.COUNTERCLOCKWISE_90;
            case 180 -> StructureRotation.CLOCKWISE_180;
            case 270 -> StructureRotation.CLOCKWISE_90;
            default -> StructureRotation.NONE;
        };
    }
}