package thestoreforcarscharging.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import thestoreforcarscharging.enums.StatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

import static thestoreforcarscharging.enums.StatusEnum.*;

public class ChargingSessionDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final UUID id;
    private String stationId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final LocalDateTime startedAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private volatile LocalDateTime stoppedAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private volatile StatusEnum status;

    public ChargingSessionDTO() {
        this.id = UUID.randomUUID();
        this.startedAt = LocalDateTime.now();
        this.status = IN_PROGRESS;
    }

    public UUID getId() {
        return id;
    }

    public String getStationId() {
        return stationId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public synchronized LocalDateTime getStoppedAt() {
        return stoppedAt;
    }

    /**
     * Change the status to FINISHED and stoppedAt for current time under the synchronized semantics
     */
    public synchronized void changeStatusToFinish() {
        status = FINISHED;
        stoppedAt = LocalDateTime.now();
    }

    public synchronized StatusEnum getStatus() {
        return status;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    @Override
    public String toString() {
        return "ChargingSessionDTO{" +
                "id=" + id +
                ", stationId='" + stationId + '\'' +
                ", startedAt=" + startedAt +
                ", stoppedAt=" + stoppedAt +
                ", status=" + status +
                '}';
    }
}