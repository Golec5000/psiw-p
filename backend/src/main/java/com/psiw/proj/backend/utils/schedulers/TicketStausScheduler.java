package com.psiw.proj.backend.utils.schedulers;

import com.psiw.proj.backend.service.interfaces.TicketValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketStausScheduler {

    private final TicketValidationService ticketValidationService;

    @Async
    @Scheduled(cron = "0 */10 * * * *")
    public void schedule() {
        int count = ticketValidationService.updateTicketStatus();
        if (count > 0) log.info("Updated {} tickets status to VALID", count);
        else log.info("No tickets status updated to VALID");
    }
}
