package kfs.tank.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import kfs.tank.KfsMain;
import kfs.tank.ScoreClient;

public class GameOverScreen extends BaseScreen {

    private final int score;
    private final boolean won;
    private final Table table;
    private static final String CHAR_SET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ";
    private final char[] nameChars = {' ', ' ', ' ', ' ', ' ', ' '};
    private int cursorPos = 0;
    private TextButton[] letterButtons;

    public GameOverScreen(KfsMain game, int score, boolean won) {
        super(game, true);
        this.score = score;
        this.won = won;

        table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        // Always show name entry (survival game - score always matters)
        if (score > 0) {
            showEnterName();
        } else {
            showLoseScreen();
        }
    }

    @Override
    public void show() {
        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(stage);
        mux.addProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (letterButtons == null) return false;
                char upper = Character.toUpperCase(character);
                if (CHAR_SET.indexOf(upper) >= 0 && cursorPos < nameChars.length) {
                    nameChars[cursorPos] = upper;
                    updateButton(cursorPos);
                    cursorPos = Math.min(cursorPos + 1, nameChars.length - 1);
                    updateCursorHighlight();
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (letterButtons == null) return false;
                if (keycode == Input.Keys.BACKSPACE) {
                    nameChars[cursorPos] = ' ';
                    updateButton(cursorPos);
                    cursorPos = Math.max(cursorPos - 1, 0);
                    updateCursorHighlight();
                    return true;
                }
                if (keycode == Input.Keys.LEFT) {
                    cursorPos = Math.max(cursorPos - 1, 0);
                    updateCursorHighlight();
                    return true;
                }
                if (keycode == Input.Keys.RIGHT) {
                    cursorPos = Math.min(cursorPos + 1, nameChars.length - 1);
                    updateCursorHighlight();
                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(mux);
    }

    private void updateButton(int pos) {
        if (letterButtons != null && pos >= 0 && pos < letterButtons.length && letterButtons[pos] != null) {
            letterButtons[pos].setText(nameChars[pos] == ' ' ? "_" : String.valueOf(nameChars[pos]));
        }
    }

    private void updateCursorHighlight() {
        if (letterButtons == null) return;
        for (int i = 0; i < letterButtons.length; i++) {
            if (letterButtons[i] == null) continue;
            letterButtons[i].setColor(i == cursorPos ? Color.CYAN : Color.WHITE);
        }
    }

    private void showEnterName() {
        table.clear();
        addHeader();

        Label.LabelStyle hintStyle = new Label.LabelStyle(fontSmall, Color.GRAY);
        table.add(new Label("TYPE OR TAP", hintStyle)).padBottom(15).row();

        letterButtons = new TextButton[nameChars.length];
        Table nameRow = new Table();
        for (int i = 0; i < nameChars.length; i++) {
            final int pos = i;
            TextButton letterBtn = new TextButton(
                nameChars[pos] == ' ' ? "_" : String.valueOf(nameChars[pos]),
                getTextButtonStyle(fontBig, Color.WHITE));
            letterBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    cursorPos = pos;
                    int idx = CHAR_SET.indexOf(nameChars[pos]);
                    nameChars[pos] = CHAR_SET.charAt((idx + 1) % CHAR_SET.length());
                    letterBtn.setText(nameChars[pos] == ' ' ? "_" : String.valueOf(nameChars[pos]));
                    updateCursorHighlight();
                }
            });
            letterButtons[pos] = letterBtn;
            nameRow.add(letterBtn).width(55).height(60).pad(3);
        }
        table.add(nameRow).padBottom(20).row();
        updateCursorHighlight();

        TextButton submitButton = new TextButton("SUBMIT", getTextButtonStyle(fontMiddle, Color.LIME));
        submitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String name = new String(nameChars).trim();
                if (name.isEmpty()) name = "AAA";
                showSubmitting();
                ScoreClient.submitScore(name, score, new ScoreClient.SubmitCallback() {
                    @Override
                    public void onSuccess(long rank, int personalBest, boolean isNewRecord) {
                        showResult(rank, personalBest, isNewRecord);
                    }

                    @Override
                    public void onError(String message) {
                        showError("NETWORK ERROR");
                    }
                });
            }
        });
        table.add(submitButton).width(300).height(60).padBottom(20).row();

        addBottomButtons();
    }

    private void showLoseScreen() {
        table.clear();

        Label.LabelStyle titleStyle = new Label.LabelStyle(fontBig, Color.RED);
        table.add(new Label("GAME OVER", titleStyle)).padBottom(20).row();

        Label.LabelStyle scoreStyle = new Label.LabelStyle(fontMiddle, Color.YELLOW);
        table.add(new Label("SCORE: " + score, scoreStyle)).padBottom(30).row();

        TextButton retryButton = new TextButton("TRY AGAIN", getTextButtonStyle(fontMiddle, Color.WHITE));
        retryButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
            }
        });
        table.add(retryButton).width(350).height(60).padBottom(20).row();

        TextButton menuButton = new TextButton("MENU", getTextButtonStyle(fontMiddle, Color.WHITE));
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainScreen(game));
            }
        });
        table.add(menuButton).width(250).height(60).padBottom(20).row();
    }

    private void showSubmitting() {
        table.clear();
        addHeader();
        Label.LabelStyle style = new Label.LabelStyle(fontMiddle, Color.CYAN);
        table.add(new Label("SUBMITTING...", style)).padBottom(60).row();
    }

    private void showResult(long rank, int personalBest, boolean isNewRecord) {
        table.clear();
        addHeader();

        if (isNewRecord) {
            Label.LabelStyle recordStyle = new Label.LabelStyle(fontMiddle, Color.LIME);
            table.add(new Label("#" + rank + " NEW RECORD!", recordStyle)).padBottom(10).row();
        } else {
            Label.LabelStyle rankStyle = new Label.LabelStyle(fontMiddle, Color.CYAN);
            table.add(new Label("RANK #" + rank, rankStyle)).padBottom(10).row();
        }

        Label.LabelStyle bestStyle = new Label.LabelStyle(fontSmall, Color.WHITE);
        table.add(new Label("BEST: " + personalBest, bestStyle)).padBottom(40).row();

        TextButton lbButton = new TextButton("LEADERBOARD", getTextButtonStyle(fontMiddle, Color.GOLD));
        lbButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LeaderboardScreen(game));
            }
        });
        table.add(lbButton).width(400).height(60).padBottom(20).row();

        addBottomButtons();
    }

    private void showError(String message) {
        table.clear();
        addHeader();

        Label.LabelStyle errStyle = new Label.LabelStyle(fontMiddle, Color.RED);
        table.add(new Label(message, errStyle)).padBottom(40).row();

        TextButton retryButton = new TextButton("RETRY", getTextButtonStyle(fontMiddle, Color.YELLOW));
        retryButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showEnterName();
            }
        });
        table.add(retryButton).width(250).height(60).padBottom(20).row();

        addBottomButtons();
    }

    private void addHeader() {
        Label.LabelStyle titleStyle = new Label.LabelStyle(fontBig, Color.RED);
        table.add(new Label("YOU SURVIVED", titleStyle)).padBottom(10).row();

        Label.LabelStyle waveStyle = new Label.LabelStyle(fontSmall, Color.CYAN);
        table.add(new Label("WAVES CLEARED", waveStyle)).padBottom(10).row();

        Label.LabelStyle scoreStyle = new Label.LabelStyle(fontMiddle, Color.YELLOW);
        table.add(new Label("SCORE: " + score, scoreStyle)).padBottom(30).row();
    }

    private void addBottomButtons() {
        TextButton retryButton = new TextButton("TRY AGAIN", getTextButtonStyle(fontMiddle, Color.WHITE));
        retryButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
            }
        });
        table.add(retryButton).width(350).height(60).padBottom(20).row();

        TextButton menuButton = new TextButton("MENU", getTextButtonStyle(fontMiddle, Color.WHITE));
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainScreen(game));
            }
        });
        table.add(menuButton).width(250).height(60).padBottom(20).row();
    }
}
