package gui;

import game.engine.Game;
import game.engine.Role;
import game.engine.Board;
import game.engine.cells.*;
import game.engine.monsters.Monster;
import game.engine.cards.Card;
import game.engine.exceptions.InvalidMoveException;
import game.engine.Constants;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class GameController {

    @FXML private GridPane boardGrid;
    @FXML private Pane tokenLayer;
    @FXML private Label diceLabel;
    @FXML private Label statusLabel;
    @FXML private Label turnLabel;
    @FXML private Label cardLabel;
    @FXML private TextArea logFeedArea;

    @FXML private Label pPos, pOrig, pCurr, pType, pEnergy, pName, pStatus;
    @FXML private Label oPos, oOrig, oCurr, oType, oEnergy, oName, oStatus;
    @FXML private Button powerBtn;

    private Game game;
    private Circle pToken;
    private Circle oToken;
    private boolean isAnimating = false;
    private Timeline alertAutoClearTimeline;

    private final double CELL_SIZE = 65.0;
    private final double CELL_GAP = 2.0;

    public void initialize(Role selectedRole) {
        try {
            this.game = new Game(selectedRole);

            pToken = new Circle(13, Color.BLUEVIOLET);
            oToken = new Circle(13, Color.CRIMSON);
            pToken.setStroke(Color.WHITE);
            pToken.setStrokeWidth(2.5);
            oToken.setStroke(Color.WHITE);
            oToken.setStrokeWidth(2.5);

            tokenLayer.getChildren().addAll(pToken, oToken);

            setupBoard();
            updateUI();
            placeTokens();

            logAction("Match Initiated. Layout generated successfully.");
            postAlertNotification("Game Started! Welcome.");

            Platform.runLater(() -> {
                if (boardGrid.getScene() != null) {
                    boardGrid.getScene().addEventHandler(KeyEvent.KEY_PRESSED, this::handleCheatingKeys);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCheatingKeys(KeyEvent event) {
        if (isAnimating || game.getWinner() != null) return;
        
        if (event.getCode() == KeyCode.W) {
            Monster p = game.getPlayer();
            p.setPosition(99);
            p.setEnergy(1000);
            logAction("[CHEAT] Player teleported to Cell 99 and energy set to 1000 for win demonstration.");
            postAlertNotification("Cheated: Instawin activated!");
            setupBoard();
            updateUI();
            placeTokens();
            if (game.getWinner() != null) {
                handleGameOver(game.getWinner());
            }
        } else if (event.getCode() == KeyCode.E) {
            Monster p = game.getPlayer();
            p.setEnergy(p.getEnergy() + 500);
            logAction("[CHEAT] Player energy boosted by +500.");
            postAlertNotification("Cheated: +500 Energy");
            updateUI();
            if (game.getWinner() != null) {
                handleGameOver(game.getWinner());
            }
        }
    }

    private void logAction(String message) {
        if (logFeedArea != null) {
            logFeedArea.appendText(message + "\n");
            logFeedArea.setScrollTop(Double.MAX_VALUE);
        }
    }

    private void postAlertNotification(String text) {
        statusLabel.setText(text);
        if (alertAutoClearTimeline != null) alertAutoClearTimeline.stop();
        alertAutoClearTimeline = new Timeline(new KeyFrame(Duration.seconds(3.0), event -> {
            statusLabel.setText("Ready to Dash!");
        }));
        alertAutoClearTimeline.setCycleCount(1);
        alertAutoClearTimeline.play();
    }

    private void setupBoard() {
        boardGrid.getChildren().clear();
        Cell[][] cells = game.getBoard().getBoardCells();

        for (int i = 0; i < Constants.BOARD_SIZE; i++) {
            int[] pos = indexToRowCol(i);
            int engineRow = pos[0];
            int engineCol = pos[1];
            int visualRow = (Constants.BOARD_ROWS - 1) - engineRow;
            boardGrid.add(createVisualCell(i, cells[engineRow][engineCol]), engineCol, visualRow);
        }
    }

    private StackPane createVisualCell(int index, Cell data) {
        StackPane cell = new StackPane();
        cell.setPrefSize(CELL_SIZE, CELL_SIZE);
        cell.setMinSize(CELL_SIZE, CELL_SIZE);
        cell.setMaxSize(CELL_SIZE, CELL_SIZE);
        cell.setStyle("-fx-border-color: rgba(255,255,255,0.15); -fx-background-color: rgba(0,0,0,0.25);");

        Label indexLabel = new Label(String.valueOf(index));
        indexLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11; -fx-font-weight: bold;");
        StackPane.setAlignment(indexLabel, Pos.TOP_LEFT);
        indexLabel.setPadding(new Insets(3));

        String imgName = "normal_bg.png";
        String secondaryText = "";
        Color textColor = Color.WHITE;

        if (containsElement(Constants.MONSTER_CELL_INDICES, index)) {
            imgName = "m_bg.png";
            if (data instanceof MonsterCell && ((MonsterCell) data).getCellMonster() != null) {
                secondaryText = ((MonsterCell) data).getCellMonster().getName();
            } else {
                secondaryText = "MONSTER";
            }
            textColor = Color.DEEPPINK;
        } else if (containsElement(Constants.CONVEYOR_CELL_INDICES, index)) {
            imgName = "conveyor_bg.png";
            secondaryText = "CONV";
            textColor = Color.AQUA;
        } else if (containsElement(Constants.SOCK_CELL_INDICES, index)) {
            imgName = "sock_bg.png";
            secondaryText = "SOCK";
            textColor = Color.LIGHTPINK;
        } else if (containsElement(Constants.CARD_CELL_INDICES, index)) {
            imgName = "card_bg.png";
            secondaryText = "CARD";
            textColor = Color.MEDIUMPURPLE;
        } else if (data instanceof DoorCell) {
            DoorCell door = (DoorCell) data;
            imgName = (door.getRole() == Role.LAUGHER) ? "door_bg.png" : "door_bg2.png";
            secondaryText = "DR: +" + door.getEnergy();
            if (door.isActivated()) {
                cell.setStyle("-fx-border-color: #ff5722; -fx-background-color: rgba(244,67,54,0.3); -fx-opacity: 0.55;");
                secondaryText = "[USED]";
                textColor = Color.LIGHTGRAY;
            } else {
                textColor = Color.GOLD;
            }
        }

        URL url = getClass().getResource("/gui/" + imgName);
        if (url != null) {
            ImageView bg = new ImageView(new Image(url.toExternalForm()));
            bg.setFitWidth(CELL_SIZE - 2);
            bg.setFitHeight(CELL_SIZE - 2);
            cell.getChildren().add(bg);
        }

        cell.getChildren().add(indexLabel);

        if (!secondaryText.isEmpty()) {
            Label subInfo = new Label(secondaryText);
            subInfo.setStyle("-fx-font-size: 9; -fx-font-weight: bold;");
            subInfo.setTextFill(textColor);
            StackPane.setAlignment(subInfo, Pos.BOTTOM_CENTER);
            subInfo.setPadding(new Insets(2));
            cell.getChildren().add(subInfo);
        }

        return cell;
    }

    private boolean containsElement(int[] arr, int target) {
        for (int val : arr) if (val == target) return true;
        return false;
    }

    @FXML
    private void handlePlayerTurn() {
        if (isAnimating || game.getCurrent() != game.getPlayer() || game.getWinner() != null) return;
        runTurnSequence();
    }

    private void executeTwoStageAnimation(Monster activeMonster, int start, int intermediate, int finalDest) {
        Circle targetToken = (activeMonster == game.getPlayer()) ? pToken : oToken;

        // Calculate direct coordinates from start to final destination
        double startX = calculateAbsoluteX(start);
        double startY = calculateAbsoluteY(start);
        double finalX = calculateAbsoluteX(finalDest);
        double finalY = calculateAbsoluteY(finalDest);

        int pIndex = (activeMonster == game.getPlayer()) ? finalDest : game.getPlayer().getPosition();
        int oIndex = (activeMonster == game.getOpponent()) ? finalDest : game.getOpponent().getPosition();

        // Prevent token overlap if they occupy the same final cell
        if (pIndex == oIndex) {
            if (activeMonster == game.getPlayer()) finalX -= 12;
            else finalX += 12;
        }

        // Set the token to its starting position explicitly before moving
        targetToken.setTranslateX(startX);
        targetToken.setTranslateY(startY);

        // Create a single, smooth transition directly to the final destination
        TranslateTransition directMovement = new TranslateTransition(Duration.millis(500), targetToken);
        directMovement.setFromX(startX);
        directMovement.setFromY(startY);
        directMovement.setToX(finalX);
        directMovement.setToY(finalY);

        // Clean up and proceed to next turn when the direct animation finishes
        directMovement.setOnFinished(ev -> finishTurnSequence());
        directMovement.play();
    }

    private void finishTurnSequence() {
        Platform.runLater(() -> {
            setupBoard();
            updateUI();
            placeTokens();
            isAnimating = false;

            if (game.getWinner() != null) {
                handleGameOver(game.getWinner());
            } else if (game.getCurrent() == game.getOpponent()) {
                triggerOpponentAI();
            }
        });
    }

    private void runTurnSequence() {
        isAnimating = true;
        playDiceSound();

        Timeline diceAnim = new Timeline(new KeyFrame(Duration.millis(80), e -> {
            diceLabel.setText(String.valueOf((int)(Math.random() * 6) + 1));
        }));
        diceAnim.setCycleCount(10);

        diceAnim.setOnFinished(e -> {
            Monster activeMonster = game.getCurrent();
            int prePlayerEnergy = game.getPlayer().getEnergy();
            int preOpponentEnergy = game.getOpponent().getEnergy();

            try {
                game.playTurn();
                
                if (game.isTurnWasFrozen()) {
                    logAction(activeMonster.getName() + " was FROZEN! Turn skipped.");
                    postAlertNotification(activeMonster.getName() + " is frozen! Turn skipped.");
                    finishTurnSequence();
                    return;
                }

                int actualRoll = game.getLastRoll();
                diceLabel.setText(String.valueOf(actualRoll));
                
                int startPos = game.getTurnStartPos();
                int intermediatePos = game.getTurnIntermediatePos();
                int finalPos = activeMonster.getPosition();

                Cell landedCell = game.getBoard().getBoardCells()
                        [indexToRowCol(finalPos)[0]][indexToRowCol(finalPos)[1]];
                String cellTypeName;
                if (containsElement(Constants.MONSTER_CELL_INDICES, finalPos))       cellTypeName = "MONSTER";
                else if (containsElement(Constants.CONVEYOR_CELL_INDICES, finalPos)) cellTypeName = "CONVEYOR";
                else if (containsElement(Constants.SOCK_CELL_INDICES, finalPos))     cellTypeName = "SOCK";
                else if (containsElement(Constants.CARD_CELL_INDICES, finalPos))     cellTypeName = "CARD";
                else if (landedCell instanceof DoorCell)
                    cellTypeName = ((DoorCell) landedCell).getRole() + "_DOOR";
                else cellTypeName = "NORMAL";

                logAction(activeMonster.getName() + " rolled a " + actualRoll
                        + " and moved to cell " + finalPos + " (" + cellTypeName + ")");

                int deltaPlayer   = game.getPlayer().getEnergy()   - prePlayerEnergy;
                int deltaOpponent = game.getOpponent().getEnergy() - preOpponentEnergy;
                if (deltaPlayer   != 0) logAction("  -> " + game.getPlayer().getName()   + " energy: " + (deltaPlayer   > 0 ? "+" : "") + deltaPlayer);
                if (deltaOpponent != 0) logAction("  -> " + game.getOpponent().getName() + " energy: " + (deltaOpponent > 0 ? "+" : "") + deltaOpponent);

                Card drawn = game.getTurnDrawnCard();
                if (drawn != null) {
                    cardLabel.setText("[" + drawn.getName() + "]: " + drawn.getDescription());
                    logAction("  -> Card drawn: [" + drawn.getName() + "] - " + drawn.getDescription());
                }

                executeTwoStageAnimation(activeMonster, startPos, intermediatePos, finalPos);

            } catch (InvalidMoveException ex) {
                postAlertNotification("Invalid Move: " + ex.getMessage());
                logAction("  -> Invalid move: " + ex.getMessage());
                isAnimating = false;
                updateUI();
            }
        });
        diceAnim.play();
    }

    private void triggerOpponentAI() {
        turnLabel.setText("Turn: OPPONENT");
        PauseTransition thinking = new PauseTransition(Duration.seconds(1.2));
        thinking.setOnFinished(e -> {
            if (game.getOpponent().getEnergy() >= Constants.POWERUP_COST && Math.random() < 0.3) {
                try {
                    game.usePowerup();
                    postAlertNotification("Opponent activated Power-up!");
                    logAction(game.getOpponent().getName() + " used their power-up!");
                    updateUI();
                } catch (Exception ex) { }
            }

            isAnimating = true;
            Monster opponent = game.getOpponent();
            int prePlayerEnergy   = game.getPlayer().getEnergy();
            int preOpponentEnergy = game.getOpponent().getEnergy();

            try {
                game.playTurn();
                
                if (game.isTurnWasFrozen()) {
                    logAction(opponent.getName() + " was FROZEN! Turn skipped.");
                    postAlertNotification(opponent.getName() + " is frozen! Turn skipped.");
                    finishTurnSequence();
                    return;
                }

                int actualRoll = game.getLastRoll();
                diceLabel.setText(String.valueOf(actualRoll));
                
                int startPos = game.getTurnStartPos();
                int intermediatePos = game.getTurnIntermediatePos();
                int finalPos = opponent.getPosition();

                Cell landedCell = game.getBoard().getBoardCells()
                        [indexToRowCol(finalPos)[0]][indexToRowCol(finalPos)[1]];
                String cellTypeName;
                if (containsElement(Constants.MONSTER_CELL_INDICES, finalPos))       cellTypeName = "MONSTER";
                else if (containsElement(Constants.CONVEYOR_CELL_INDICES, finalPos)) cellTypeName = "CONVEYOR";
                else if (containsElement(Constants.SOCK_CELL_INDICES, finalPos))     cellTypeName = "SOCK";
                else if (containsElement(Constants.CARD_CELL_INDICES, finalPos))     cellTypeName = "CARD";
                else if (landedCell instanceof DoorCell)
                    cellTypeName = ((DoorCell) landedCell).getRole() + "_DOOR";
                else cellTypeName = "NORMAL";

                logAction(opponent.getName() + " rolled a " + actualRoll
                        + " and moved to cell " + finalPos + " (" + cellTypeName + ")");

                int deltaPlayer   = game.getPlayer().getEnergy()   - prePlayerEnergy;
                int deltaOpponent = game.getOpponent().getEnergy() - preOpponentEnergy;
                if (deltaPlayer   != 0) logAction("  -> " + game.getPlayer().getName()   + " energy: " + (deltaPlayer   > 0 ? "+" : "") + deltaPlayer);
                if (deltaOpponent != 0) logAction("  -> " + game.getOpponent().getName() + " energy: " + (deltaOpponent > 0 ? "+" : "") + deltaOpponent);

                Card drawn = game.getTurnDrawnCard();
                if (drawn != null) {
                    cardLabel.setText("[" + drawn.getName() + "]: " + drawn.getDescription());
                    logAction("  -> Card drawn: [" + drawn.getName() + "] - " + drawn.getDescription());
                }

                executeTwoStageAnimation(opponent, startPos, intermediatePos, finalPos);

            } catch (InvalidMoveException ex) {
                logAction("Opponent invalid move: " + ex.getMessage() + " - retrying...");
                isAnimating = false;
                updateUI();
                if (game.getWinner() == null) triggerOpponentAI();
            } catch (Exception ex) {
                isAnimating = false;
                updateUI();
            }
        });
        thinking.play();
    }

    private void placeTokens() {
        if (game == null || game.getPlayer() == null || game.getOpponent() == null) return;
        int pIndex = game.getPlayer().getPosition();
        int oIndex = game.getOpponent().getPosition();
        double pX = calculateAbsoluteX(pIndex), pY = calculateAbsoluteY(pIndex);
        double oX = calculateAbsoluteX(oIndex), oY = calculateAbsoluteY(oIndex);
        if (pIndex == oIndex) {
            pToken.setTranslateX(pX - 12);
            oToken.setTranslateX(oX + 12);
        } else {
            pToken.setTranslateX(pX);
            oToken.setTranslateX(oX);
        }
        pToken.setTranslateY(pY);
        oToken.setTranslateY(oY);
    }

    private double calculateAbsoluteX(int index) {
        int col = indexToRowCol(index)[1];
        return (col * (CELL_SIZE + CELL_GAP)) + (CELL_SIZE / 2.0);
    }

    private double calculateAbsoluteY(int index) {
        int row = indexToRowCol(index)[0];
        int visualRow = (Constants.BOARD_ROWS - 1) - row;
        return (visualRow * (CELL_SIZE + CELL_GAP)) + (CELL_SIZE / 2.0);
    }

    private int[] indexToRowCol(int index) {
        int cols = Constants.BOARD_COLS;
        int row = index / cols;
        int col = index % cols;
        if (row % 2 == 1) col = cols - 1 - col;
        return new int[]{row, col};
    }

    private void handleGameOver(Monster winner) {
        statusLabel.setText("[WIN] " + winner.getName() + " WINS!");
        logAction("[WIN] GAME OVER! WINNER: " + winner.getName());
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> showGameOver(winner));
        delay.play();
    }

    @FXML
    private void handleExit() { showGameOver(null); }

    private void showGameOver(Monster winner) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/GameOver.fxml"));
            Parent root = loader.load();
            GameOverController controller = loader.getController();
            if (winner != null) {
                controller.setWinnerDetails(
                    winner.getName() + " (" + winner.getOriginalRole() + ")\n" +
                    "Player Final Energy: "   + game.getPlayer().getEnergy()   + "\n" +
                    "Opponent Final Energy: " + game.getOpponent().getEnergy());
            } else {
                controller.setWinnerDetails("No Winner");
            }
            Stage stage = (Stage) boardGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handlePowerup() {
        if (isAnimating || game.getWinner() != null) return;
        try {
            game.usePowerup();
            postAlertNotification("Power-up activated successfully!");
            logAction(game.getPlayer().getName() + " activated Power-Up.");
            updateUI();

            double targetPX = calculateAbsoluteX(game.getPlayer().getPosition());
            double targetPY = calculateAbsoluteY(game.getPlayer().getPosition());
            double targetOX = calculateAbsoluteX(game.getOpponent().getPosition());
            double targetOY = calculateAbsoluteY(game.getOpponent().getPosition());

            if (game.getPlayer().getPosition() == game.getOpponent().getPosition()) {
                targetPX -= 12; targetOX += 12;
            }

            TranslateTransition ttP = new TranslateTransition(Duration.millis(400), pToken);
            ttP.setFromX(pToken.getTranslateX()); ttP.setFromY(pToken.getTranslateY());
            ttP.setToX(targetPX); ttP.setToY(targetPY);

            TranslateTransition ttO = new TranslateTransition(Duration.millis(400), oToken);
            ttO.setFromX(oToken.getTranslateX()); ttO.setFromY(oToken.getTranslateY());
            ttO.setToX(targetOX); ttO.setToY(targetOY);

            ParallelTransition pt = new ParallelTransition(ttP, ttO);
            pt.setOnFinished(ev -> placeTokens());
            pt.play();

        } catch (Exception e) {
            postAlertNotification("Denied: " + e.getMessage());
        }
    }

    private void updateUI() {
        Monster p = game.getPlayer();
        if (pName != null) pName.setText(p.getName().toUpperCase());
        pPos.setText("Position: " + p.getPosition());
        pOrig.setText("Original: " + p.getOriginalRole());
        pCurr.setText("Role: " + p.getRole() + (p.isConfused() ? " [CONFUSED]" : ""));
        pType.setText("Type: " + p.getClass().getSimpleName());
        pEnergy.setText("Energy: " + p.getEnergy());
        pStatus.setText("Status: " + buildStatusString(p));

        Monster o = game.getOpponent();
        if (oName != null) oName.setText(o.getName().toUpperCase());
        oPos.setText("Position: " + o.getPosition());
        oOrig.setText("Original: " + o.getOriginalRole());
        oCurr.setText("Role: " + o.getRole() + (o.isConfused() ? " [CONFUSED]" : ""));
        oType.setText("Type: " + o.getClass().getSimpleName());
        oEnergy.setText("Energy: " + o.getEnergy());
        oStatus.setText("Status: " + buildStatusString(o));

        if (game.getWinner() == null)
            turnLabel.setText(game.getCurrent() == p ? "Turn: PLAYER" : "Turn: OPPONENT");
    }

    private String buildStatusString(Monster m) {
        ArrayList<String> effects = new ArrayList<>();
        if (m.isShielded()) effects.add("Shielded [SHIELD]");
        if (m.isFrozen())   effects.add("Frozen [FROZEN]");
        if (m.isConfused()) effects.add("Confused [CONFUSED] (" + m.getConfusionTurns() + " turns)");
        return effects.isEmpty() ? "Normal" : String.join(", ", effects);
    }

    private void playDiceSound() {
        try {
            URL res = getClass().getResource("/gui/dice_roll.mp3");
            if (res != null) new AudioClip(res.toExternalForm()).play();
        } catch (Exception e) { }
    }
}
