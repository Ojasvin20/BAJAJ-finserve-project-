package com.quiz;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the API response from GET /quiz/messages
 */
public class PollResponse {

    private final String regNo;
    private final String setId;
    private final int pollIndex;
    private final List<QuizEvent> events;

    public PollResponse(String regNo, String setId, int pollIndex, List<QuizEvent> events) {
        this.regNo     = regNo;
        this.setId     = setId;
        this.pollIndex = pollIndex;
        this.events    = events;
    }

    public static PollResponse fromJson(JSONObject json) {
        List<QuizEvent> events = new ArrayList<>();
        JSONArray arr = json.getJSONArray("events");
        for (int i = 0; i < arr.length(); i++) {
            events.add(QuizEvent.fromJson(arr.getJSONObject(i)));
        }
        return new PollResponse(
            json.getString("regNo"),
            json.getString("setId"),
            json.getInt("pollIndex"),
            events
        );
    }

    public String getRegNo()      { return regNo; }
    public String getSetId()      { return setId; }
    public int    getPollIndex()  { return pollIndex; }
    public List<QuizEvent> getEvents() { return events; }
}
