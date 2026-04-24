package com.quiz;

import org.json.JSONObject;

/**
 * A single entry on the leaderboard — one participant and their total score.
 */
public class LeaderboardEntry implements Comparable<LeaderboardEntry> {

    private final String participant;
    private final int totalScore;

    public LeaderboardEntry(String participant, int totalScore) {
        this.participant = participant;
        this.totalScore  = totalScore;
    }

    public String getParticipant() { return participant; }
    public int    getTotalScore()  { return totalScore; }

    /** Descending order by totalScore */
    @Override
    public int compareTo(LeaderboardEntry other) {
        return Integer.compare(other.totalScore, this.totalScore);
    }

    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        obj.put("participant", participant);
        obj.put("totalScore", totalScore);
        return obj;
    }

    @Override
    public String toString() {
        return String.format("%-20s %d", participant, totalScore);
    }
}
