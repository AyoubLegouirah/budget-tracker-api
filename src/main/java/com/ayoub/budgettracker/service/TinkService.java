package com.ayoub.budgettracker.service;

import com.ayoub.budgettracker.dto.tink.*;
import com.ayoub.budgettracker.entity.Account;
import com.ayoub.budgettracker.entity.Category;
import com.ayoub.budgettracker.entity.Transaction;
import com.ayoub.budgettracker.entity.User;
import com.ayoub.budgettracker.repository.AccountRepository;
import com.ayoub.budgettracker.repository.CategoryRepository;
import com.ayoub.budgettracker.repository.TransactionRepository;
import com.ayoub.budgettracker.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TinkService {

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Value("${tink.client-id}")
    private String clientId;

    @Value("${tink.client-secret}")
    private String clientSecret;

    @Value("${tink.redirect-uri}")
    private String redirectUri;

    @Value("${tink.api-url}")
    private String apiUrl;

    @Value("${tink.link-url}")
    private String linkUrl;

    @Value("${tink.market:SE}")
    private String market;

    @Value("${tink.locale:en_US}")
    private String locale;

    private RestClient tinkApiClient;

    @PostConstruct
    private void init() {
        tinkApiClient = RestClient.builder().baseUrl(apiUrl).build();
    }

    /**
     * Generates the Tink Link URL the user must visit to connect their bank.
     *
     * First connection (tinkUserId == null): no authorization_code — Tink creates the user
     * automatically and returns the user_id in the callback code's JWT.
     *
     * Reconnection (tinkUserId != null): authorization_code authenticates the existing Tink
     * user so the new bank connection is added to the same user.
     */
    public String generateConnectUrl(User user) {
        log.info("Tink Link URL (no auth_code) for user={}", user.getId());
        return UriComponentsBuilder
                .fromUriString(linkUrl + "/1.0/transactions/connect-accounts/")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("market", market)
                .queryParam("locale", locale)
                .toUriString();
    }

    /**
     * Exchanges the Tink callback code for a user token, then fetches and imports transactions.
     * Called from the authenticated POST /api/tink/import endpoint (Angular sends the code + JWT).
     * On first connection, extracts the Tink user_id from the JWT and persists it.
     * Returns the number of newly imported transactions.
     */
    @Transactional
    public int importFromCode(String code, User user) {
        String userToken = exchangeCodeForUserToken(code);

        if (user.getTinkUserId() == null) {
            String tinkUserId = extractTinkUserIdFromJwt(userToken);
            user.setTinkUserId(tinkUserId);
            userRepository.save(user);
            log.info("Saved Tink user_id={} for app user={}", tinkUserId, user.getId());
        }

        List<TinkTransactionItem> tinkTransactions = fetchAllTransactions(userToken);
        return importTransactions(tinkTransactions, user);
    }

    // ─── Private: Tink API calls ────────────────────────────────────────────

    private String exchangeCodeForUserToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);
        body.add("grant_type", "authorization_code");

        TinkTokenResponse response = tinkApiClient.post()
                .uri("/api/v1/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(TinkTokenResponse.class);

        if (response == null || response.getAccessToken() == null) {
            throw new RuntimeException("Impossible d'échanger le code contre un token Tink");
        }
        return response.getAccessToken();
    }

    private String extractTinkUserIdFromJwt(String jwtToken) {
        String[] parts = jwtToken.split("\\.");
        if (parts.length < 2) throw new RuntimeException("Token JWT Tink invalide");
        int pad = parts[1].length() % 4;
        String padded = pad == 0 ? parts[1] : parts[1] + "=".repeat(4 - pad);
        String payload = new String(
                java.util.Base64.getUrlDecoder().decode(padded),
                java.nio.charset.StandardCharsets.UTF_8);
        // sub format: "tink://auth/user/{userId}"
        String marker = "\"sub\":\"tink://auth/user/";
        int start = payload.indexOf(marker);
        if (start < 0) throw new RuntimeException("Claim 'sub' introuvable dans le JWT Tink");
        int valueStart = start + marker.length();
        int valueEnd = payload.indexOf('"', valueStart);
        return payload.substring(valueStart, valueEnd);
    }

    private List<TinkTransactionItem> fetchAllTransactions(String userToken) {
        List<TinkTransactionItem> all = new ArrayList<>();
        String pageToken = null;

        do {
            String uri = pageToken != null
                    ? "/data/v2/transactions?pageToken=" + pageToken
                    : "/data/v2/transactions";

            TinkTransactionPage page = tinkApiClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + userToken)
                    .retrieve()
                    .body(TinkTransactionPage.class);

            if (page != null && page.getTransactions() != null) {
                all.addAll(page.getTransactions());
            }
            pageToken = (page != null && page.getNextPageToken() != null && !page.getNextPageToken().isEmpty())
                    ? page.getNextPageToken()
                    : null;
        } while (pageToken != null);

        return all;
    }

    // ─── Private: import logic ───────────────────────────────────────────────

    private int importTransactions(List<TinkTransactionItem> tinkTransactions, User user) {
        List<Account> accounts = accountRepository.findByUserId(user.getId());
        if (accounts.isEmpty()) {
            throw new RuntimeException("Aucun compte trouvé pour cet utilisateur. Créez d'abord un compte.");
        }
        Account defaultAccount = accounts.get(0);

        int count = 0;
        for (TinkTransactionItem tinkTx : tinkTransactions) {
            if (tinkTx.getId() == null) continue;
            if (transactionRepository.findByTinkId(tinkTx.getId()).isPresent()) continue;

            BigDecimal amount = computeAmount(tinkTx.getAmount());
            String type = amount.signum() >= 0 ? "INCOME" : "EXPENSE";

            String categoryName = resolveCategoryName(tinkTx);
            Category category = categoryRepository
                    .findByNameIgnoreCaseAndUserId(categoryName, user.getId())
                    .orElseGet(() -> categoryRepository.save(
                            Category.builder().name(categoryName).user(user).build()
                    ));

            Transaction tx = Transaction.builder()
                    .tinkId(tinkTx.getId())
                    .amount(amount.abs())
                    .type(type)
                    .date(resolveDate(tinkTx.getDates()))
                    .description(resolveDescription(tinkTx))
                    .account(defaultAccount)
                    .category(category)
                    .user(user)
                    .build();

            transactionRepository.save(tx);
            count++;
        }

        log.info("Tink import: {} nouvelles transactions pour l'utilisateur {}", count, user.getId());
        return count;
    }

    private BigDecimal computeAmount(TinkTransactionItem.TinkAmount amount) {
        if (amount == null || amount.getValue() == null) return BigDecimal.ZERO;
        return BigDecimal.valueOf(amount.getValue().getUnscaledValue())
                .movePointLeft(amount.getValue().getScale());
    }

    private String resolveCategoryName(TinkTransactionItem tx) {
        if (tx.getCategories() != null
                && tx.getCategories().getPfm() != null
                && tx.getCategories().getPfm().getName() != null) {
            return tx.getCategories().getPfm().getName();
        }
        return "Divers";
    }

    private LocalDate resolveDate(TinkTransactionItem.TinkDates dates) {
        if (dates != null && dates.getBooked() != null && !dates.getBooked().isEmpty()) {
            return LocalDate.parse(dates.getBooked());
        }
        return LocalDate.now();
    }

    private String resolveDescription(TinkTransactionItem tx) {
        if (tx.getDescriptions() != null) {
            if (tx.getDescriptions().getDisplay() != null) return tx.getDescriptions().getDisplay();
            if (tx.getDescriptions().getOriginal() != null) return tx.getDescriptions().getOriginal();
        }
        return "Import Tink";
    }
}
