package zabi.minecraft.extraalchemy.entitydata;

import java.util.Objects;

import net.minecraft.entity.player.PlayerEntity;

public interface PlayerProperties {
    boolean isMagnetismEnabled();

    void setMagnetismEnabled(boolean magnetismActive);

    int calculateXPDue(float xp);

    static PlayerProperties of(PlayerEntity player) {
        if (Objects.nonNull(player)) {
            return (PlayerProperties) (Object) player;
        } else {
            throw new NullPointerException("PlayerProperties can't be read from null players");
        }
    }
}
