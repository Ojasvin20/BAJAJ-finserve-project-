# Quiz Leaderboard System

SRM Internship Assignment — Java backend that polls a quiz validator API, deduplicates events, aggregates scores, and submits a final leaderboard.

---

## Problem Summary

A quiz validator API exposes scores across 10 polls. The same event data may appear in multiple polls (simulating duplicate delivery in distributed systems). The goal is to:

1. Poll the API **10 times** (poll indices 0–9) with a **mandatory 5-second delay** between calls.
2. **Deduplicate** events using the composite key `(roundId + participant)`.
3. **Aggregate** total scores per participant.
4. Generate a **leaderboard** sorted by `totalScore` (descending).
5. **Submit** the leaderboard **exactly once**.

---

## Project Structure

```
quiz-leaderboard/
├── pom.xml
└── src/
    ├── main/java/com/quiz/
    │   ├── Main.java              ← Entry point
    │   ├── QuizService.java       ← Core orchestration logic
    │   ├── ApiClient.java         ← HTTP calls (poll + submit)
    │   ├── PollResponse.java      ← Model: API poll response
    │   ├── QuizEvent.java         ← Model: single round score event
    │   └── LeaderboardEntry.java  ← Model: participant + total score
    └── test/java/com/quiz/
        └── QuizServiceTest.java   ← Unit tests for dedup + aggregation
```

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 11+ |
| Maven | 3.6+ |

---

## Build

```bash
mvn clean package
```

This produces `target/quiz-leaderboard.jar` (fat JAR with all dependencies).

---

## Run

```bash
java -jar target/quiz-leaderboard.jar <YOUR_REG_NO>
```

**Example:**
```bash
java -jar target/quiz-leaderboard.jar 2024CS101
```

The program will:
- Poll the API 10 times (one every 5 seconds → ~45 seconds total)
- Print accepted and duplicate events in real time
- Print the final leaderboard
- Submit the result and print the validator response

---

## Run Tests

```bash
mvn test
```

Tests cover:
- Deduplication: exact duplicate → ignored
- Deduplication: same participant different rounds → kept
- Deduplication: same round different participants → kept
- Score aggregation across rounds
- Leaderboard sorted descending
- Grand total calculation after deduplication

---

## How Deduplication Works

Each event carries a `roundId` and `participant`. The composite key `"roundId|participant"` uniquely identifies a score entry. A `LinkedHashMap` is used so the **first occurrence wins** and subsequent identical keys are ignored.

```
Poll 0 → R1|Alice=10   ✅ accepted
Poll 3 → R1|Alice=10   ❌ duplicate → skipped
Poll 0 → R1|Bob=20     ✅ accepted
Poll 2 → R2|Alice=15   ✅ accepted (different round)
```

Final scores: Alice = 25, Bob = 20

---

## API Reference

**Base URL:** `https://devapigw.vidalhealthtpa.com/srm-quiz-task`

### GET `/quiz/messages`

| Param | Description |
|-------|-------------|
| `regNo` | Your registration number |
| `poll` | Poll index (0–9) |

### POST `/quiz/submit`

```json
{
  "regNo": "2024CS101",
  "leaderboard": [
    { "participant": "Alice", "totalScore": 100 },
    { "participant": "Bob",   "totalScore": 80  }
  ]
}
```

---

## Dependencies

| Library | Purpose |
|---------|---------|
| `org.json` 20240303 | Lightweight JSON parsing (no framework overhead) |
| JUnit Jupiter 5.10 | Unit testing |

No Spring, no Jackson, no external HTTP client — just clean Java 11 with the standard `HttpURLConnection`.

---

## Author

Built for the Bajaj Finserv Health / SRM Internship Java Qualifier Assignment.
