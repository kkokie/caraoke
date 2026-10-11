package com.caraoke.block;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BlockId implements Serializable {

    @Column(name = "blocker_id")
    private Long blockerId;

    @Column(name = "blocked_id")
    private Long blockedId;

    protected BlockId() { }

    public BlockId(long blockerId, long blockedId) {
        this.blockerId = blockerId;
        this.blockedId = blockedId;
    }

    public Long getBlockerId() { return blockerId; }
    public Long getBlockedId() { return blockedId; }

    @Override
    public boolean equals(Object o) {
        return o instanceof BlockId b && Objects.equals(blockerId, b.blockerId) && Objects.equals(blockedId, b.blockedId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockerId, blockedId);
    }
}
