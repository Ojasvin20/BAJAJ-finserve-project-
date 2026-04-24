package com.quiz;

public class Main {
    public static void main(String[] args) throws Exception {
        String regNo = args.length > 0 ? args[0] : "2024CS101";
        System.out.println("=== Quiz Leaderboard System ===");
        System.out.println("Registration Number: " + regNo);
        System.out.println("================================\n");

        QuizService service = new QuizService(regNo);
        service.run();
    }
}
