package com.ayoub.budgettracker.scheduler;

import com.ayoub.budgettracker.entity.User;
import com.ayoub.budgettracker.repository.UserRepository;
import com.ayoub.budgettracker.service.TinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TinkSyncJob {

    private final TinkService tinkService;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 2 * * *")
    public void syncAllUsers() {
        List<User> users = userRepository.findByTinkUserIdIsNotNull();
        log.info("Tink nightly sync — {} utilisateur(s) connecté(s)", users.size());

        int totalImported = 0;
        for (User user : users) {
            try {
                int imported = tinkService.syncUserTransactions(user);
                log.info("  user={} ({}) → {} nouvelle(s) transaction(s)", user.getId(), user.getEmail(), imported);
                totalImported += imported;
            } catch (Exception e) {
                log.error("  user={} ({}) → échec sync: {}", user.getId(), user.getEmail(), e.getMessage());
            }
        }

        log.info("Tink nightly sync terminé — {} nouvelle(s) transaction(s) au total", totalImported);
    }
}
