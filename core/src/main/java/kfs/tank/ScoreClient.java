package kfs.tank;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ScoreClient {

    private static String BASE_URL;
    private static String GAME_ID;

    public static class ScoreEntry {
        public final String playerName;
        public final int score;

        public ScoreEntry(String playerName, int score) {
            this.playerName = playerName;
            this.score = score;
        }
    }

    public interface SubmitCallback {
        void onSuccess(long rank, int personalBest, boolean isNewRecord);
        void onError(String message);
    }

    public interface TopScoresCallback {
        void onSuccess(List<ScoreEntry> scores);
        void onError(String message);
    }

    private static void ensureConfig() {
        if (BASE_URL != null) return;
        try {
            String text = Gdx.files.internal("game.properties").readString();
            Properties props = new Properties();
            props.load(new StringReader(text));
            BASE_URL = props.getProperty("leaderboard.url", "http://localhost:8080");
            GAME_ID = props.getProperty("leaderboard.gameId", "kfs-tank");
        } catch (IOException e) {
            BASE_URL = "http://localhost:8080";
            GAME_ID = "kfs-tank";
        }
    }

    public static void submitScore(String playerName, int score, SubmitCallback callback) {
        ensureConfig();
        String json = "{\"gameId\":\"" + GAME_ID + "\","
                     + "\"playerName\":\"" + escapeJson(playerName) + "\","
                     + "\"score\":" + score + "}";

        Net.HttpRequest request = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.POST)
            .url(BASE_URL + "/api/scores")
            .header("Content-Type", "application/json")
            .content(json)
            .build();

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String body = httpResponse.getResultAsString();
                long rank = parseLong(body, "rank");
                int best = (int) parseLong(body, "personalBest");
                boolean isNew = body.contains("\"isNewRecord\":true");
                Gdx.app.postRunnable(() -> callback.onSuccess(rank, best, isNew));
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> callback.onError(t.getMessage()));
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> callback.onError("Cancelled"));
            }
        });
    }

    public static void getTopScores(int limit, TopScoresCallback callback) {
        ensureConfig();
        Net.HttpRequest request = new HttpRequestBuilder()
            .newRequest()
            .method(Net.HttpMethods.GET)
            .url(BASE_URL + "/api/scores/" + GAME_ID + "/top?limit=" + limit)
            .build();

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                String body = httpResponse.getResultAsString();
                List<ScoreEntry> scores = parseTopScores(body);
                Gdx.app.postRunnable(() -> callback.onSuccess(scores));
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> callback.onError(t.getMessage()));
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> callback.onError("Cancelled"));
            }
        });
    }

    static List<ScoreEntry> parseTopScores(String json) {
        List<ScoreEntry> result = new ArrayList<>();
        int idx = 0;
        while (true) {
            int objStart = json.indexOf('{', idx);
            if (objStart < 0) break;
            int objEnd = json.indexOf('}', objStart);
            if (objEnd < 0) break;
            String obj = json.substring(objStart, objEnd + 1);
            String name = parseString(obj, "playerName");
            int score = (int) parseLong(obj, "score");
            if (name != null) {
                result.add(new ScoreEntry(name, score));
            }
            idx = objEnd + 1;
        }
        return result;
    }

    private static long parseLong(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return 0;
        idx += search.length();
        StringBuilder sb = new StringBuilder();
        while (idx < json.length() && (Character.isDigit(json.charAt(idx)) || json.charAt(idx) == '-')) {
            sb.append(json.charAt(idx++));
        }
        return sb.length() > 0 ? Long.parseLong(sb.toString()) : 0;
    }

    private static String parseString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        idx += search.length();
        int end = json.indexOf('"', idx);
        if (end < 0) return null;
        return json.substring(idx, end);
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
