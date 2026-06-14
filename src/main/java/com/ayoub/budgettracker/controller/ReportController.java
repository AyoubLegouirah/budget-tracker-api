package com.ayoub.budgettracker.controller;

import com.ayoub.budgettracker.entity.User;
import com.ayoub.budgettracker.service.PdfReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final PdfReportService pdfReportService;

    @GetMapping("/monthly")
    public ResponseEntity<byte[]> monthly(
            @RequestParam(required = false) String month,
            @AuthenticationPrincipal User user) {
        YearMonth ym = month != null ? YearMonth.parse(month) : YearMonth.now();
        try {
            byte[] pdf = pdfReportService.generateMonthlyReport(user.getId(), ym);
            String filename = "bilan-" + ym + ".pdf";
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .body(pdf);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la generation du rapport PDF", e);
        }
    }
}
