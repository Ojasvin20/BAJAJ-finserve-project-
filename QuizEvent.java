package com.quiz;

import org.json.JSONObject;

/**
 * Represents a single quiz event (round score for a participant).
 * The deduplication key is (roundId + participant).
 */
public class QuizEvent {

    private final String roundId;
    private final String participant;
    private final int score;

    public QuizEvent(String roundId, String participant, int score) {
        this.roundId = roundId;
        this.participant = participant;
        this.score = score;
    }

    public static QuizEvent fromJson(JSONObject json) {
        return new QuizEvent(
            json.getString("roundId"),
            json.getString("participant"),
            json.getInt("score")
        );
    }

    /**
     * Unique key used for deduplication: roundId + "|" + participant
     */
    public String deduplicationKey() {
        return roundId + "|" + participant;
    }

    public String getRoundId()     { return roundId; }
    public String getParticipant() { return participant; }
    public int    getScore()       { return score; }

    @Override
    public String toString() {
        return String.format("QuizEvent{roundId='%s', participant='%s', score=%d}", roundId, participant, score);
    }
}
