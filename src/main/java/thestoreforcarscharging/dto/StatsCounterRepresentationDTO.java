package thestoreforcarscharging.dto;

public class StatsCounterRepresentationDTO {
    private int totalCount;
    private int startedCount;
    private int stoppedCount;

    public StatsCounterRepresentationDTO() {

    }

    public StatsCounterRepresentationDTO(final int total, final int start, final int stop) {
        this.totalCount = total;
        this.startedCount = start;
        this.stoppedCount = stop;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public long getStartedCount() {
        return startedCount;
    }

    public long getStoppedCount() {
        return stoppedCount;
    }

    @Override
    public String toString() {
        return "StatsCounterRepresentationDTO{" +
                "totalCount=" + totalCount +
                ", startedCount=" + startedCount +
                ", stoppedCount=" + stoppedCount +
                '}';
    }
}
