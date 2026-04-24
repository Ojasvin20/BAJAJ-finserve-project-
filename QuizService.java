package com.quiz;

import org.json.JSONObject;

import java.util.*;

/**
 * Orchestrates the full quiz leaderboard workflow:
 *  1. Poll the API 10 times (poll 0–9) with a 5-second delay between each call.
 *  2. Deduplicate events using (roundId + participant) as a composite key.
 *  3. Aggregate scores per participant.
 *  4. Build and print the leaderboard (sorted descending by totalScore).
 *  5. Submit the leaderboard exactly once.
 */
public class QuizService {

    private static final int    TOTAL_POLLS      = 10;
    private static final long   POLL_DELAY_MS    = 5_000L; // 5 seconds — mandatory

    private final String    regNo;
    private final ApiClient apiClient;

    public QuizService(String regNo) {
        this.regNo     = regNo;
        this.apiClient = new ApiClient(regNo);
    }

    public void run() throws Exception {

        // ----------------------------------------------------------------
        // Step 1 & 2 — Poll 10 times, collecting unique events
        // ----------------------------------------------------------------
        // Key: "roundId|participant"  →  QuizEvent (first occurrence wins)
        Map<String, QuizEvent> uniqueEvents = new LinkedHashMap<>();

        for (int poll = 0; poll < TOTAL_POLLS; poll++) {
            PollResponse response = apiClient.poll(poll);

            for (QuizEvent event : response.getEvents()) {
                String key = event.deduplicationKey();
                if (uniqueEvents.containsKey(key)) {
                    System.out.println("       [DUPLICATE SKIPPED] " + event);
                } else {
                    uniqueEvents.put(key, event);
                    System.out.println("       [ACCEPTED]          " + event);
                }
            }

            // Mandatory 5-second delay between polls (not after the last one)
            if (poll < TOTAL_POLLS - 1) {
                System.out.println("       Waiting 5 seconds...\n");
                Thread.sleep(POLL_DELAY_MS);
            }
        }

        // ----------------------------------------------------------------
        // Step 3 — Aggregate scores per participant
        // ----------------------------------------------------------------
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (QuizEvent event : uniqueEvents.values()) {
            scores.merge(event.getParticipant(), event.getScore(), Integer::sum);
        }

        // ----------------------------------------------------------------
        // Step 4 — Build leaderboard (sorted descending by totalScore)
        // ----------------------------------------------------------------
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            leaderboard.add(new LeaderboardEntry(entry.getKey(), entry.getValue()));
        }
        Collections.sort(leaderboard);

        int grandTotal = leaderboard.stream().mapToInt(LeaderboardEntry::getTotalScore).sum();

        printLeaderboard(leaderboard, grandTotal);

        // ----------------------------------------------------------------
        // Step 5 — Submit exactly once
        // ----------------------------------------------------------------
        JSONObject result = apiClient.submit(leaderboard);
        printResult(result);
    }

    // ---------------------------------------------------------------- helpers
    private void printLeaderboard(List<LeaderboardEntry> leaderboard, int grandTotal) {
        System.out.println("\n========== LEADERBOARD ==========");
        System.out.printf("%-5s %-20s %s%n", "Rank", "Participant", "Total Score");
        System.out.println("---------------------------------");
        int rank = 1;
        for (LeaderboardEntry e : leaderboard) {
            System.out.printf("%-5d %-20s %d%n", rank++, e.getParticipant(), e.getTotalScore());
        }
        System.out.println("---------------------------------");
        System.out.println("Grand Total: " + grandTotal);
        System.out.println("=================================\n");
    }

    private void printResult(JSONObject result) {
        System.out.println("\n========== SUBMISSION RESULT ==========");
        System.out.println("isCorrect      : " + result.optBoolean("isCorrect"));
        System.out.println("isIdempotent   : " + result.optBoolean("isIdempotent"));
        System.out.println("submittedTotal : " + result.optInt("submittedTotal"));
        System.out.println("expectedTotal  : " + result.optInt("expectedTotal"));
        System.out.println("message        : " + result.optString("message"));
        System.out.println("=======================================");
    }
}
