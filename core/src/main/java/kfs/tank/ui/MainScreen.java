package kfs.tank.ui;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import kfs.tank.KfsConst;
import kfs.tank.KfsMain;
import kfs.tank.ScoreClient;

public class MainScreen extends BaseScreen {

    private Label hiScoreLabel;

    public MainScreen(KfsMain game) {
        super(game, false);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);

        ScrollPane scrollPane = new ScrollPane(table, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setFillParent(true);
        stage.addActor(scrollPane);

        // Title
        Label.LabelStyle titleStyle = new Label.LabelStyle(fontBig, Color.LIME);
        Label title = new Label("KUBASAURUS", titleStyle);
        table.add(title).padBottom(5).row();

        Label.LabelStyle subTitleStyle = new Label.LabelStyle(fontMiddle, Color.YELLOW);
        table.add(new Label("T A N K", subTitleStyle)).padBottom(10).row();

        Label.LabelStyle subStyle = new Label.LabelStyle(fontSmall, Color.GRAY);
        table.add(new Label("Dino-tank izometricka akce", subStyle)).padBottom(30).row();

        // Hi-score
        Label.LabelStyle hiStyle = new Label.LabelStyle(fontSmall, Color.GOLD);
        hiScoreLabel = new Label("HI-SCORE: ...", hiStyle);
        table.add(hiScoreLabel).padBottom(20).row();

        TextButton.TextButtonStyle buttonStyle = getTextButtonStyle(fontBig, Color.WHITE);

        TextButton playButton = new TextButton("Play", buttonStyle);
        playButton.getColor().a = KfsConst.BUTTON_TRANSPARENCY;
        TextButton leaderboardButton = new TextButton("Leaderboard", buttonStyle);
        leaderboardButton.getColor().a = KfsConst.BUTTON_TRANSPARENCY;
        TextButton musicButton = new TextButton("Music", buttonStyle);
        musicButton.getColor().a = KfsConst.BUTTON_TRANSPARENCY;

        float buttonWidth = 400f;
        float buttonHeight = 80f;

        table.defaults().width(buttonWidth).height(buttonHeight).pad(12f);

        table.add(playButton).row();
        table.add(leaderboardButton).row();
        table.add(musicButton).row();

        if (Gdx.app.getType() != Application.ApplicationType.WebGL) {
            TextButton quitButton = new TextButton("Quit", buttonStyle);
            quitButton.getColor().a = KfsConst.BUTTON_TRANSPARENCY;
            table.add(quitButton).row();

            quitButton.addListener(e -> {
                if (quitButton.isPressed()) Gdx.app.exit();
                return false;
            });
        }

        playButton.addListener(e -> {
            if (playButton.isPressed()) {
                game.setScreen(new GameScreen(game));
            }
            return false;
        });

        leaderboardButton.addListener(e -> {
            if (leaderboardButton.isPressed()) game.setScreen(new LeaderboardScreen(game));
            return false;
        });

        musicButton.addListener(e -> {
            if (musicButton.isPressed()) {
                if (game.music.isPlaying()) {
                    game.music.stop();
                } else {
                    game.music.play();
                }
            }
            return false;
        });

        stage.setScrollFocus(scrollPane);

        // Load hi-score async
        ScoreClient.getTopScores(1, new ScoreClient.TopScoresCallback() {
            @Override
            public void onSuccess(java.util.List<ScoreClient.ScoreEntry> scores) {
                if (!scores.isEmpty()) {
                    hiScoreLabel.setText("HI-SCORE: " + scores.get(0).score);
                } else {
                    hiScoreLabel.setText("HI-SCORE: ---");
                }
            }

            @Override
            public void onError(String message) {
                hiScoreLabel.setText("HI-SCORE: ---");
            }
        });
    }
}
