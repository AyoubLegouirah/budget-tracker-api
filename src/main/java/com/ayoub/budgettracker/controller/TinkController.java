package com.ayoub.budgettracker.controller;

import com.ayoub.budgettracker.entity.User;
import com.ayoub.budgettracker.service.TinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tink")
@RequiredArgsConstructor
public class TinkController {

    private final TinkService tinkService;

    /**
     * Returns the Tink Link URL the Angular app must redirect the user to.
     * Requires authentication.
     */
    @GetMapping("/connect")
    public ResponseEntity<Map<String, String>> connect(@AuthenticationPrincipal User user) {
        String tinkUrl = tinkService.generateConnectUrl(user);
        return ResponseEntity.ok(Map.of("url", tinkUrl));
    }

    /**
     * Called by Angular after Tink Link redirects to http://localhost:4200/tink/callback?code=xxx.
     * Angular reads the code from the URL and POSTs it here with the user's JWT.
     * Returns the number of imported transactions.
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Integer>> importTransactions(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User user) {
        String code = body.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        int imported = tinkService.importFromCode(code, user);
        return ResponseEntity.ok(Map.of("imported", imported));
    }
}
