package com.pratham.sentinelx.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatsProjection {
    private Long freq;
    private Double avgDur;
    private Double avgDist;
    private Long circleDiv;     // Matches COUNT(DISTINCT c.receiverCircle)
    private Long unqContacts;   // Matches COUNT(DISTINCT c.receiverNumber)

    // Constructor matching JPQL order EXACTLY
    public StatsProjection(Long freq, Double avgDur, Double avgDist, Long circleDiv, Long unqContacts) {
        this.freq = freq != null ? freq : 0L;
        this.avgDur = avgDur != null ? avgDur : 0.0;
        this.avgDist = avgDist != null ? avgDist : 0.0;
        this.circleDiv = circleDiv != null ? circleDiv : 0L;
        this.unqContacts = unqContacts != null ? unqContacts : 0L;
    }
}