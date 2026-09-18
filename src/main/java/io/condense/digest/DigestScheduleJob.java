package io.condense.digest;

import io.condense.subscriber.Cadence;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DigestScheduleJob {

    private final DigestDispatchService dispatchService;
    private final Clock clock;

    public DigestScheduleJob(DigestDispatchService dispatchService, Clock clock) {
        this.dispatchService = dispatchService;
        this.clock = clock;
    }

    @Scheduled(cron = "${condense.schedule.daily-digest-cron}", zone = "America/New_York")
    public void sendDailyDigests() {
        dispatchService.dispatch(Cadence.DAILY, LocalDate.now(clock));
        dispatchService.dispatch(Cadence.PER_FILING, LocalDate.now(clock));
    }

    @Scheduled(cron = "${condense.schedule.weekly-digest-cron}", zone = "America/New_York")
    public void sendWeeklyDigests() {
        dispatchService.dispatch(Cadence.WEEKLY, LocalDate.now(clock));
    }
}
