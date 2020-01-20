package thestoreforcarscharging.enums;

import thestoreforcarscharging.dto.ChargingSessionDTO;
import thestoreforcarscharging.dto.StatsCounterRepresentationDTO;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Objects.isNull;
import static thestoreforcarscharging.enums.StatusEnum.*;

/**
 * It`s a trade-safe stats counter which have only one instance
 *
 * @author <a href="mailto:1arxemond1@gmail.com">Yuri Glushenkov</a>
 */
public enum StatsCounter {
    INSTANCE;

    private final LinkedList<ChargingSessionDTO>
            total = new LinkedList<>(),
            started = new LinkedList<>(),
            stopped = new LinkedList<>();

    private static final AtomicReference<StatsCounter> statsCounterDTO = new AtomicReference<>(INSTANCE);

    private static final long MINUS_MINUTES = 1L;

    /**
     * The logic for cleaning queues
     * @param queue - an queue for clean
     * @param nowMinusNTime - (curTime - {@link thestoreforcarscharging.enums.StatsCounter#MINUS_MINUTES})
     * @param se - is a queue (the first arg) IN_PROGRESS or FINISHED
     */
    private void clean(final LinkedList<ChargingSessionDTO> queue, final LocalDateTime nowMinusNTime, final StatusEnum se) {
        ChargingSessionDTO cs = queue.peekFirst();

        while (true) {

            if (isNull(cs)
                    ||
                    (se.equals(FINISHED) ? cs.getStoppedAt() : cs.getStartedAt()).compareTo(nowMinusNTime) >= 0
            )
                break;
            else {
                queue.removeFirst();

                cs = queue.peekFirst();
            }
        }
    }

    /**
     * Clean all queues and left items in them less then required interval {@link StatsCounter#MINUS_MINUTES}
     *
     * @return StatsCounterRepresentationDTO - an obj of representation
     */
    private synchronized StatsCounterRepresentationDTO cleaner() {
        final LocalDateTime nowMinusNTime = LocalDateTime.now().minusMinutes(MINUS_MINUTES);

        clean(total, nowMinusNTime, IN_PROGRESS);
        clean(started, nowMinusNTime, IN_PROGRESS);
        clean(stopped, nowMinusNTime, FINISHED);

        return new StatsCounterRepresentationDTO(total.size(), started.size(), stopped.size());
    }

    /**
     * A method for add to total&started queues
     *
     * @param cs
     */
    public void incCurrent(final ChargingSessionDTO cs) {
        statsCounterDTO.getAndUpdate(inst -> {
            cleaner();

            total.add(cs);
            started.add(cs);

            return inst;
        });
    }

    /**
     * A method for add to the stopped queue
     *
     * @param cs
     */
    public void incFinished(final ChargingSessionDTO cs) {
        statsCounterDTO.getAndUpdate(inst -> {
            cleaner();

            stopped.add(cs);

            return inst;
        });
    }

    public long getTotal() {
        return total.size();
    }

    public long getStarted() {
        return started.size();
    }


    public long getStopped() {
        return stopped.size();
    }

    @Override
    public String toString() {
        return "StatsCounterDTO{" +
                "current=" + total.size() +
                ", inProgress=" + started.size() +
                ", finished=" + stopped.size() +
                '}';
    }

    /**
     * Fetch an obj of representation to the response
     *
     * @return - the representation onj
     * 1) totalCount – total number of charging session updates for the last minute
     * 2) startedCount – total number of started charging sessions for the last minute
     * 3) stoppedCount – total number of stopped charging sessions for the last minute
     */
    public StatsCounterRepresentationDTO getRepresentation() {
        return cleaner();
    }
}
