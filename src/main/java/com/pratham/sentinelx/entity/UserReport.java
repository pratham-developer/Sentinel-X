package com.pratham.sentinelx.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "user_reports", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"reporter_number", "reported_number"}) // One vote per user
})
public class UserReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_number", nullable = false)
    private String reporterNumber; // The Victim

    @Column(name = "reported_number", nullable = false)
    private String reportedNumber; // The Scammer

    private String reason; // "Scam", "Spam", "Abuse"

    private LocalDateTime timestamp;

    public UserReport(String reporter, String reported, String reason) {
        this.reporterNumber = reporter;
        this.reportedNumber = reported;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }
}