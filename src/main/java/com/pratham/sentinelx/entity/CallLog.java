package com.pratham.sentinelx.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "call_history_logs", indexes = {
        @Index(name = "idx_caller", columnList = "caller_number")
})
public class CallLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caller_number", nullable = false)
    private String callerNumber;

    @Column(name = "receiver_circle")
    private String receiverCircle;

    @Column(name = "receiver_number")
    private String receiverNumber;

    @Column(name = "distance_km")
    private Integer distanceKm;

    private Integer duration;

    private LocalDateTime timestamp;
}