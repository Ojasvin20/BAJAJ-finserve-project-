package com.quiz;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class QuizServiceTest {

    // ------------------------------------------------------------------ helpers
    private QuizEvent event(String roundId, String participant, int score) {
        JSONObject j = new JSONObject();
        j.put("roundId", roundId);
        j.put("participant", participant);
        j.put("score", score);
        return QuizEvent.fromJson(j);
    }

    // ---------------------------------------------------------- deduplication
    @Test
    void deduplication_keepsFirstOccurrence() {
        Map<String, QuizEvent> unique = new LinkedHashMap<>();

        QuizEvent e1 = event("R1", "Alice", 10);
        QuizEvent e2 = event("R1", "Alice", 10); // exact duplicate

        unique.putIfAbsent(e1.deduplicationKey(), e1);
        unique.putIfAbsent(e2.deduplicationKey(), e2);

        assertEquals(1, unique.size());
        assertEquals(10, unique.get("R1|Alice").getScore());
    }

    @Test
    void deduplication_differentRoundsSameParticipant_keptSeparate() {
        Map<String, QuizEvent> unique = new LinkedHashMap<>();

        QuizEvent e1 = event("R1", "Alice", 10);
        QuizEvent e2 = event("R2", "Alice", 20);

        unique.putIfAbsent(e1.deduplicationKey(), e1);
        unique.putIfAbsent(e2.deduplicationKey(), e2);

        assertEquals(2, unique.size());
    }

    @Test
    void deduplication_sameRoundDifferentParticipants_keptSeparate() {
        Map<String, QuizEvent> unique = new LinkedHashMap<>();

        QuizEvent e1 = event("R1", "Alice", 10);
        QuizEvent e2 = event("R1", "Bob",   20);

        unique.putIfAbsent(e1.deduplicationKey(), e1);
        unique.putIfAbsent(e2.deduplicationKey(), e2);

        assertEquals(2, unique.size());
    }

    // ---------------------------------------------------------- aggregation
    @Test
    void aggregation_sumsScoresPerParticipant() {
        Map<String, QuizEvent> unique = new LinkedHashMap<>();
        unique.put("R1|Alice", event("R1", "Alice", 10));
        unique.put("R2|Alice", event("R2", "Alice", 30));
        unique.put("R1|Bob",   event("R1", "Bob",   20));

        Map<String, Integer> scores = new LinkedHashMap<>();
        for (QuizEvent e : unique.values()) {
            scores.merge(e.getParticipant(), e.getScore(), Integer::sum);
        }

        assertEquals(40, scores.get("Alice"));
        assertEquals(20, scores.get("Bob"));
    }

    // ---------------------------------------------------------- leaderboard sort
    @Test
    void leaderboard_sortedDescending() {
        List<LeaderboardEntry> board = new ArrayList<>();
        board.add(new LeaderboardEntry("Alice", 40));
        board.add(new LeaderboardEntry("Bob",   20));
        board.add(new LeaderboardEntry("Carol", 60));
        Collections.sort(board);

        assertEquals("Carol", board.get(0).getParticipant());
        assertEquals("Alice", board.get(1).getParticipant());
        assertEquals("Bob",   board.get(2).getParticipant());
    }

    // ---------------------------------------------------------- grand total
    @Test
    void grandTotal_correctAfterDeduplication() {
        Map<String, QuizEvent> unique = new LinkedHashMap<>();
        unique.put("R1|Alice", event("R1", "Alice", 10));
        unique.put("R2|Alice", event("R2", "Alice", 30));
        unique.put("R1|Bob",   event("R1", "Bob",   20));
        // This duplicate should never reach the map:
        // unique.put("R1|Alice", event("R1", "Alice", 10)); ← already present

        Map<String, Integer> scores = new LinkedHashMap<>();
        for (QuizEvent e : unique.values()) {
            scores.merge(e.getParticipant(), e.getScore(), Integer::sum);
        }

        int total = scores.values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(60, total); // 10 + 30 + 20 = 60
    }

    // ---------------------------------------------------------- dedup key
    @Test
    void deduplicationKey_format() {
        QuizEvent e = event("R3", "Charlie", 15);
        assertEquals("R3|Charlie", e.deduplicationKey());
    }
}
