package kfs.tank.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import kfs.tank.KfsMain;
import kfs.tank.ScoreClient;

import java.util.List;

public class LeaderboardScreen extends BaseScreen {

    private static final Color GOLD = new Color(1f, 0.84f, 0f, 1f);
    private static final Color SILVER = Color.LIGHT_GRAY;
    private static final Color BRONZE = Color.ORANGE;

    private final Table table;

    public LeaderboardScreen(KfsMain game) {
        super(game, true);

        table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        showLoading();
        loadScores();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    private void showLoading() {
        table.clear();
        addTitle();
        Label.LabelStyle style = new Label.LabelStyle(fontMiddle, Color.CYAN);
        table.add(new Label("LOADING...", style)).padBottom(60).row();
    }

    private void showScores(List<ScoreClient.ScoreEntry> scores) {
        table.clear();
        addTitle();

        if (scores.isEmpty()) {
            Label.LabelStyle style = new Label.LabelStyle(fontSmall, Color.GRAY);
            table.add(new Label("NO SCORES YET", style)).padBottom(40).row();
        } else {
            for (int i = 0; i < scores.size(); i++) {
                ScoreClient.ScoreEntry entry = scores.get(i);
                int rank = i + 1;
                Color color;
                if (rank == 1) color = GOLD;
                else if (rank == 2) color = SILVER;
                else if (rank == 3) color = BRONZE;
                else color = Color.WHITE;

                String line = String.format("%2d. %-10s %6d", rank, entry.playerName, entry.score);
                Label.LabelStyle style = new Label.LabelStyle(fontSmall, color);
                table.add(new Label(line, style)).padBottom(6).row();
            }
        }

        table.add().padBottom(30).row();
        addButtons();
    }

    private void showError() {
        table.clear();
        addTitle();

        Label.LabelStyle style = new Label.LabelStyle(fontMiddle, Color.RED);
        table.add(new Label("OFFLINE", style)).padBottom(40).row();

        addButtons();
    }

    private void addTitle() {
        Label.LabelStyle titleStyle = new Label.LabelStyle(fontBig, GOLD);
        table.add(new Label("TOP 10", titleStyle)).padBottom(30).row();
    }

    private void addButtons() {
        TextButton playButton = new TextButton("PLAY", getTextButtonStyle(fontMiddle, Color.WHITE));
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
            }
        });
        table.add(playButton).width(300).height(60).padBottom(20).row();

        TextButton menuButton = new TextButton("MENU", getTextButtonStyle(fontMiddle, Color.WHITE));
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainScreen(game));
            }
        });
        table.add(menuButton).width(250).height(60).padBottom(20).row();
    }

    private void loadScores() {
        ScoreClient.getTopScores(10, new ScoreClient.TopScoresCallback() {
            @Override
            public void onSuccess(List<ScoreClient.ScoreEntry> scores) {
                showScores(scores);
            }

            @Override
            public void onError(String message) {
                showError();
            }
        });
    }
}
