package com.pratham.sentinelx.repository;

import com.pratham.sentinelx.entity.CallLog;
import com.pratham.sentinelx.projection.StatsProjection;
import org.springframework.data.jpa.repository.JpaRepository; // Correct Import
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Correct Import
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CallLogRepository extends JpaRepository<CallLog, Long> {

    // Added: "AND c.timestamp > :startTime"
    @Query("""
        SELECT new com.pratham.sentinelx.projection.StatsProjection(
            COUNT(c), 
            AVG(c.duration), 
            AVG(c.distanceKm), 
            COUNT(DISTINCT c.receiverCircle),
            COUNT(DISTINCT c.receiverNumber)
        )
        FROM CallLog c 
        WHERE c.callerNumber = :phoneNumber 
        AND c.timestamp > :startTime
    """)
    StatsProjection getCallerStats(
            @Param("phoneNumber") String phoneNumber,
            @Param("startTime") LocalDateTime startTime // <--- New Param
    );
}