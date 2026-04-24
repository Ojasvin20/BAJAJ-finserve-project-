package com.quiz;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Handles all HTTP communication with the quiz validator API.
 */
public class ApiClient {

    private static final String BASE_URL   = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";
    private static final int    TIMEOUT_MS = 15_000;

    private final String regNo;

    public ApiClient(String regNo) {
        this.regNo = regNo;
    }

    // ------------------------------------------------------------------ poll
    /**
     * Calls GET /quiz/messages?regNo={regNo}&poll={pollIndex}
     */
    public PollResponse poll(int pollIndex) throws IOException {
        String urlStr = BASE_URL + "/quiz/messages?regNo=" + regNo + "&poll=" + pollIndex;
        System.out.println("[POLL " + pollIndex + "] GET " + urlStr);

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("Accept", "application/json");

        int status = conn.getResponseCode();
        String body = readStream(conn, status);

        if (status != 200) {
            throw new IOException("Poll " + pollIndex + " failed with HTTP " + status + ": " + body);
        }

        JSONObject json = new JSONObject(body);
        PollResponse response = PollResponse.fromJson(json);
        System.out.println("       setId=" + response.getSetId()
                           + "  events=" + response.getEvents().size());
        return response;
    }

    // --------------------------------------------------------------- submit
    /**
     * Calls POST /quiz/submit with the final leaderboard.
     * Returns the raw JSON response body.
     */
    public JSONObject submit(List<LeaderboardEntry> leaderboard) throws IOException {
        String urlStr = BASE_URL + "/quiz/submit";
        System.out.println("\n[SUBMIT] POST " + urlStr);

        // Build request body
        JSONArray arr = new JSONArray();
        for (LeaderboardEntry entry : leaderboard) {
            arr.put(entry.toJson());
        }
        JSONObject payload = new JSONObject();
        payload.put("regNo", regNo);
        payload.put("leaderboard", arr);

        byte[] bodyBytes = payload.toString().getBytes(StandardCharsets.UTF_8);

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(bodyBytes);
        }

        int status = conn.getResponseCode();
        String body = readStream(conn, status);

        System.out.println("[SUBMIT] HTTP " + status + " → " + body);
        return new JSONObject(body);
    }

    // --------------------------------------------------------- helper
    private static String readStream(HttpURLConnection conn, int status) throws IOException {
        try (Scanner scanner = new Scanner(
                status >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}
