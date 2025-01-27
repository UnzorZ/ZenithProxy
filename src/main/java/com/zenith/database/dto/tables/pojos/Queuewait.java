/*
 * This file is generated by jOOQ.
 */
package com.zenith.database.dto.tables.pojos;


import java.io.Serializable;
import java.time.OffsetDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Queuewait implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Integer id;
    private final String playerName;
    private final Boolean prio;
    private final OffsetDateTime startQueueTime;
    private final OffsetDateTime endQueueTime;
    private final Long queueTime;
    private final Integer initialQueueLen;

    public Queuewait(Queuewait value) {
        this.id = value.id;
        this.playerName = value.playerName;
        this.prio = value.prio;
        this.startQueueTime = value.startQueueTime;
        this.endQueueTime = value.endQueueTime;
        this.queueTime = value.queueTime;
        this.initialQueueLen = value.initialQueueLen;
    }

    public Queuewait(
            Integer id,
            String playerName,
            Boolean prio,
            OffsetDateTime startQueueTime,
            OffsetDateTime endQueueTime,
            Long queueTime,
            Integer initialQueueLen
    ) {
        this.id = id;
        this.playerName = playerName;
        this.prio = prio;
        this.startQueueTime = startQueueTime;
        this.endQueueTime = endQueueTime;
        this.queueTime = queueTime;
        this.initialQueueLen = initialQueueLen;
    }

    /**
     * Getter for <code>public.queuewait.id</code>.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Getter for <code>public.queuewait.player_name</code>.
     */
    public String getPlayerName() {
        return this.playerName;
    }

    /**
     * Getter for <code>public.queuewait.prio</code>.
     */
    public Boolean getPrio() {
        return this.prio;
    }

    /**
     * Getter for <code>public.queuewait.start_queue_time</code>.
     */
    public OffsetDateTime getStartQueueTime() {
        return this.startQueueTime;
    }

    /**
     * Getter for <code>public.queuewait.end_queue_time</code>.
     */
    public OffsetDateTime getEndQueueTime() {
        return this.endQueueTime;
    }

    /**
     * Getter for <code>public.queuewait.queue_time</code>.
     */
    public Long getQueueTime() {
        return this.queueTime;
    }

    /**
     * Getter for <code>public.queuewait.initial_queue_len</code>.
     */
    public Integer getInitialQueueLen() {
        return this.initialQueueLen;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Queuewait (");

        sb.append(id);
        sb.append(", ").append(playerName);
        sb.append(", ").append(prio);
        sb.append(", ").append(startQueueTime);
        sb.append(", ").append(endQueueTime);
        sb.append(", ").append(queueTime);
        sb.append(", ").append(initialQueueLen);

        sb.append(")");
        return sb.toString();
    }
}
