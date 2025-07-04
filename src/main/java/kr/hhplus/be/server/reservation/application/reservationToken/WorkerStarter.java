package kr.hhplus.be.server.reservation.application.reservationToken;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkerStarter {
    private final QueueActivationWorker worker;

    @EventListener(ApplicationReadyEvent.class)
    public void startWorker() {
        worker.startProcessing();
    }

}
