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
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

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
    @FXML private StackPane diceCube;

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

            // ── ANIMATION: fade-in the whole board on start ──
            animateBoardEntrance();

            logAction("Match Initiated. Layout generated successfully.");
            postAlertNotification("Game Started! Welcome.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════
    //  NEW ANIMATIONS
    // ══════════════════════════════════════════════════════

    /** Fade the board in from transparent on game start */
    private void animateBoardEntrance() {
        if (boardGrid == null) return;
        boardGrid.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(800), boardGrid);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /** Flash a label's text color to highlight an energy change */
    private void animateEnergyChange(Label energyLabel, boolean gained) {
        if (energyLabel == null) return;
        Color flashColor = gained ? Color.LIMEGREEN : Color.RED;
        String originalStyle = energyLabel.getStyle();

        energyLabel.setTextFill(flashColor);

        // Scale pop effect
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), energyLabel);
        scale.setFromX(1.0); scale.setFromY(1.0);
        scale.setToX(1.4);   scale.setToY(1.4);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);

        // Fade back to original color after
        PauseTransition pause = new PauseTransition(Duration.millis(500));
        pause.setOnFinished(e -> energyLabel.setStyle(originalStyle));

        new SequentialTransition(scale, pause).play();
    }

    /** Red shake on the status label when a FREEZE skip happens */
    private void animateFreezeSkip(Label target) {
        if (target == null) return;
        String originalStyle = target.getStyle();
        target.setStyle(originalStyle + "; -fx-text-fill: #00bcd4; -fx-font-weight: bold;");

        TranslateTransition shake = new TranslateTransition(Duration.millis(60), target);
        shake.setFromX(0); shake.setByX(10);
        shake.setAutoReverse(true);
        shake.setCycleCount(6);

        shake.setOnFinished(e -> target.setStyle(originalStyle));
        shake.play();
    }

    /** Yellow glow pulse on shield block */
    private void animateShieldBlock(Label statusLbl) {
        if (statusLbl == null) return;
        String originalStyle = statusLbl.getStyle();
        statusLbl.setStyle(originalStyle + "; -fx-text-fill: gold; -fx-font-weight: bold;");

        ScaleTransition pulse = new ScaleTransition(Duration.millis(300), statusLbl);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.3);   pulse.setToY(1.3);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(4);
        pulse.setOnFinished(e -> statusLbl.setStyle(originalStyle));
        pulse.play();
    }

    /** Spin + glow the card label when a card is drawn */
    private void animateCardDrawn() {
        if (cardLabel == null) return;

        RotateTransition spin = new RotateTransition(Duration.millis(400), cardLabel);
        spin.setFromAngle(-8); spin.setToAngle(8);
        spin.setAutoReverse(true);
        spin.setCycleCount(4);

        FadeTransition flash = new FadeTransition(Duration.millis(200), cardLabel);
        flash.setFromValue(0.2); flash.setToValue(1.0);
        flash.setCycleCount(3);
        flash.setAutoReverse(true);

        new ParallelTransition(spin, flash).play();
    }

    /** Purple flash on the role label when confusion is applied */
    private void animateConfusion(Label roleLabel) {
        if (roleLabel == null) return;
        String originalStyle = roleLabel.getStyle();
        roleLabel.setStyle(originalStyle + "; -fx-text-fill: #ce93d8;");

        FadeTransition blink = new FadeTransition(Duration.millis(150), roleLabel);
        blink.setFromValue(1.0); blink.setToValue(0.2);
        blink.setAutoReverse(true);
        blink.setCycleCount(6);
        blink.setOnFinished(e -> roleLabel.setStyle(originalStyle));
        blink.play();
    }

    /** Highlight a cell on the board when a monster lands on it */
    private void animateCellLanding(int index) {
        // Find the StackPane in the board grid matching this index
        int[] pos = indexToRowCol(index);
        int visualRow = (Constants.BOARD_ROWS - 1) - pos[0];
        int col = pos[1];

        boardGrid.getChildren().stream()
            .filter(n -> {
                Integer r = GridPane.getRowIndex(n);
                Integer c = GridPane.getColumnIndex(n);
                return (r != null && r == visualRow) && (c != null && c == col);
            })
            .findFirst()
            .ifPresent(node -> {
                ScaleTransition pop = new ScaleTransition(Duration.millis(200), node);
                pop.setFromX(1.0); pop.setFromY(1.0);
                pop.setToX(1.15); pop.setToY(1.15);
                pop.setAutoReverse(true);
                pop.setCycleCount(2);
                pop.play();
            });
    }

    /** Bounce the dice cube when rolling */
    private void animateDiceBounce() {
        if (diceCube == null) return;
        TranslateTransition bounce = new TranslateTransition(Duration.millis(80), diceCube);
        bounce.setFromY(0); bounce.setToY(-8);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(6);
        bounce.play();
    }

    /** Slide + fade the turn label when turn switches */
    private void animateTurnSwitch() {
        if (turnLabel == null) return;
        FadeTransition fade = new FadeTransition(Duration.millis(300), turnLabel);
        fade.setFromValue(0); fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), turnLabel);
        slide.setFromX(-20); slide.setToX(0);

        new ParallelTransition(fade, slide).play();
    }

    // ══════════════════════════════════════════════════════
    //  EXISTING METHODS (with animation hooks added)
    // ══════════════════════════════════════════════════════

    private void logAction(String clearMessage) {
        if (logFeedArea != null) {
            logFeedArea.appendText(clearMessage + "\n");
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

        Label indexLabel = new Label(String.valueOf(index + 1));
        indexLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11; -fx-font-weight: bold;");
        StackPane.setAlignment(indexLabel, Pos.TOP_LEFT);
        indexLabel.setPadding(new Insets(3));

        String imgName = "normal_bg.png";
        String secondaryText = "";
        Color textColor = Color.WHITE;

        if (data instanceof DoorCell) {
            DoorCell door = (DoorCell) data;
            imgName = door.getRole() == Role.LAUGHER ? "door_bg.png" : "door_bg2.png";
            secondaryText = "DR: +" + door.getEnergy();
            if (door.isActivated()) {
                cell.setStyle("-fx-border-color: #ff5722; -fx-background-color: rgba(244,67,54,0.3); -fx-opacity: 0.55;");
                secondaryText = "[USED]";
                textColor = Color.LIGHTGRAY;
            } else {
                textColor = Color.GOLD;
            }
        } else if (data instanceof MonsterCell) {
            MonsterCell mc = (MonsterCell) data;
            imgName = "m_bg.png";
            secondaryText = mc.getCellMonster() != null ? mc.getCellMonster().getName() : "Ally";
            textColor = Color.DEEPPINK;
        } else if (data instanceof ConveyorBelt) {
            imgName = "conveyor_bg.png";
            secondaryText = "CONV";
            textColor = Color.AQUA;
        } else if (data instanceof ContaminationSock) {
            imgName = "sock_bg.png";
            secondaryText = "SOCK";
            textColor = Color.LIGHTPINK;
        } else if (data instanceof CardCell) {
            imgName = "card_bg.png";
            secondaryText = "CARD";
            textColor = Color.MEDIUMPURPLE;
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

    @FXML
    private void handlePlayerTurn() {
        if (isAnimating || game.getCurrent() != game.getPlayer() || game.getWinner() != null) return;
        runTurnSequence();
    }

    private void executeTwoStageAnimation(Monster activeMonster, int start, int intermediate, int finalDest) {
        Circle targetToken = (activeMonster == game.getPlayer()) ? pToken : oToken;

        double startX = calculateAbsoluteX(start);
        double startY = calculateAbsoluteY(start);
        double interX = calculateAbsoluteX(intermediate);
        double interY = calculateAbsoluteY(intermediate);
        double finalX = calculateAbsoluteX(finalDest);
        double finalY = calculateAbsoluteY(finalDest);

        int pIndex = (activeMonster == game.getPlayer()) ? finalDest : game.getPlayer().getPosition();
        int oIndex = (activeMonster == game.getOpponent()) ? finalDest : game.getOpponent().getPosition();

        if (pIndex == oIndex) {
            if (activeMonster == game.getPlayer()) finalX -= 12;
            else finalX += 12;
        }

        targetToken.setTranslateX(startX);
        targetToken.setTranslateY(startY);

        TranslateTransition phase1 = new TranslateTransition(Duration.millis(400), targetToken);
        phase1.setFromX(startX); phase1.setFromY(startY);

        if (intermediate != finalDest) {
            phase1.setToX(interX); phase1.setToY(interY);

            TranslateTransition phase2 = new TranslateTransition(Duration.millis(400), targetToken);
            phase2.setFromX(interX); phase2.setFromY(interY);
            phase2.setToX(finalX);  phase2.setToY(finalY);

            SequentialTransition totalMovement = new SequentialTransition(
                phase1, new PauseTransition(Duration.millis(100)), phase2
            );
            totalMovement.setOnFinished(e -> {
                // ── ANIMATION: cell landing pop ──
                animateCellLanding(finalDest);
                finishTurnSequence();
            });
            totalMovement.play();
        } else {
            phase1.setToX(finalX); phase1.setToY(finalY);
            phase1.setOnFinished(e -> {
                animateCellLanding(finalDest);
                finishTurnSequence();
            });
            phase1.play();
        }
    }

    private void finishTurnSequence() {
        Platform.runLater(() -> {
            setupBoard();
            updateUI();
            placeTokens();
            isAnimating = false;

            // ── ANIMATION: turn switch slide ──
            animateTurnSwitch();

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

        // ── ANIMATION: dice cube bounce ──
        animateDiceBounce();

        Timeline diceAnim = new Timeline(new KeyFrame(Duration.millis(80), e -> {
            diceLabel.setText(String.valueOf((int)(Math.random() * 6) + 1));
        }));
        diceAnim.setCycleCount(10);

        diceAnim.setOnFinished(e -> {
            Monster activeMonster = game.getCurrent();
            int startPos = activeMonster.getPosition();
            int initialDeckSize = Board.getCards().size();

            int prePlayerEnergy = game.getPlayer().getEnergy();
            int preOpponentEnergy = game.getOpponent().getEnergy();

            boolean wasFrozen = activeMonster.isFrozen();
            if (wasFrozen) {
                postAlertNotification(activeMonster.getName() + " is frozen! Turn skipped.");
                logAction(activeMonster.getName() + " is frozen! Skipping turn.");
                // ── ANIMATION: freeze shake ──
                animateFreezeSkip(statusLabel);
            }

            try {
                int visuallyRolledFace = Integer.parseInt(diceLabel.getText());
                int intermediatePos = (startPos + visuallyRolledFace) % Constants.BOARD_SIZE;

                game.playTurn();

                int finalPos = activeMonster.getPosition();

                Cell landedCell = game.getBoard().getBoardCells()
                    [indexToRowCol(finalPos)[0]][indexToRowCol(finalPos)[1]];
                String cellTypeName = landedCell.getClass().getSimpleName().replace("Cell", "");
                if (landedCell instanceof DoorCell)
                    cellTypeName = ((DoorCell) landedCell).getRole() + "_DOOR";
                if (cellTypeName.isEmpty()) cellTypeName = "NORMAL";

                logAction(activeMonster.getName() + " rolled a " + visuallyRolledFace
                    + " and landed on " + cellTypeName.toUpperCase() + "!");

                // ── ANIMATION: energy change flash ──
                int deltaPlayer   = game.getPlayer().getEnergy()   - prePlayerEnergy;
                int deltaOpponent = game.getOpponent().getEnergy() - preOpponentEnergy;

                if (deltaPlayer != 0) {
                    logAction("  -> " + game.getPlayer().getName() + " got " + deltaPlayer + " energy!");
                    animateEnergyChange(pEnergy, deltaPlayer > 0);
                }
                if (deltaOpponent != 0) {
                    logAction("  -> " + game.getOpponent().getName() + " got " + deltaOpponent + " energy!");
                    animateEnergyChange(oEnergy, deltaOpponent > 0);
                }

                // ── ANIMATION: shield block ──
                // If energy was supposed to drop but didn't due to shield, flag it
                if (deltaPlayer == 0 && game.getPlayer().isShielded()) {
                    logAction("  -> " + game.getPlayer().getName() + "'s Shield blocked damage! 🛡️");
                    animateShieldBlock(pStatus);
                }
                if (deltaOpponent == 0 && game.getOpponent().isShielded()) {
                    logAction("  -> " + game.getOpponent().getName() + "'s Shield blocked damage! 🛡️");
                    animateShieldBlock(oStatus);
                }

                // ── ANIMATION: card drawn ──
                if (Board.getCards().size() != initialDeckSize && !Board.getCards().isEmpty()) {
                    Card drawn = Board.getCards().get(0);
                    cardLabel.setText("[" + drawn.getName() + "]: " + drawn.getDescription());
                    logAction("  -> Card Activated: [" + drawn.getName() + "]");
                    animateCardDrawn();
                }

                // ── ANIMATION: confusion role swap ──
                if (game.getPlayer().isConfused())   animateConfusion(pCurr);
                if (game.getOpponent().isConfused())  animateConfusion(oCurr);

                executeTwoStageAnimation(activeMonster, startPos, intermediatePos, finalPos);

            } catch (InvalidMoveException ex) {
                postAlertNotification("Prohibited Move: " + ex.getMessage());
                isAnimating = false;
                updateUI();
                if (game.getCurrent() == game.getOpponent() && game.getWinner() != null)
                    triggerOpponentAI();
            }
        });
        diceAnim.play();
    }

    private void triggerOpponentAI() {
        turnLabel.setText("Turn: OPPONENT");
        animateTurnSwitch();

        PauseTransition thinking = new PauseTransition(Duration.seconds(1.2));
        thinking.setOnFinished(e -> {
            if (game.getOpponent().getEnergy() >= Constants.POWERUP_COST && Math.random() < 0.3) {
                try {
                    game.usePowerup();
                    postAlertNotification("Opponent activated Power-up!");
                    logAction(game.getOpponent().getName() + " used their power-up!");
                    updateUI();
                } catch (Exception ex) {}
            }

            isAnimating = true;
            Monster opponent = game.getOpponent();
            int startPos = opponent.getPosition();
            int initialDeckSize = Board.getCards().size();

            int prePlayerEnergy   = game.getPlayer().getEnergy();
            int preOpponentEnergy = game.getOpponent().getEnergy();

            try {
                int aiRollSim = (int)(Math.random() * 6) + 1;
                diceLabel.setText(String.valueOf(aiRollSim));
                animateDiceBounce();

                int intermediatePos = (startPos + aiRollSim) % Constants.BOARD_SIZE;
                game.playTurn();
                int finalPos = opponent.getPosition();

                Cell landedCell = game.getBoard().getBoardCells()
                    [indexToRowCol(finalPos)[0]][indexToRowCol(finalPos)[1]];
                String cellTypeName = landedCell.getClass().getSimpleName().replace("Cell", "");
                if (landedCell instanceof DoorCell)
                    cellTypeName = ((DoorCell) landedCell).getRole() + "_DOOR";
                if (cellTypeName.isEmpty()) cellTypeName = "NORMAL";

                logAction(opponent.getName() + " rolled a " + aiRollSim
                    + " and landed on " + cellTypeName.toUpperCase() + "!");

                int deltaPlayer   = game.getPlayer().getEnergy()   - prePlayerEnergy;
                int deltaOpponent = game.getOpponent().getEnergy() - preOpponentEnergy;

                if (deltaPlayer != 0) {
                    logAction("  -> " + game.getPlayer().getName() + " got " + deltaPlayer + " energy!");
                    animateEnergyChange(pEnergy, deltaPlayer > 0);
                }
                if (deltaOpponent != 0) {
                    logAction("  -> " + game.getOpponent().getName() + " got " + deltaOpponent + " energy!");
                    animateEnergyChange(oEnergy, deltaOpponent > 0);
                }

                if (deltaPlayer == 0 && game.getPlayer().isShielded()) {
                    logAction("  -> " + game.getPlayer().getName() + "'s Shield blocked damage! 🛡️");
                    animateShieldBlock(pStatus);
                }
                if (deltaOpponent == 0 && game.getOpponent().isShielded()) {
                    logAction("  -> " + game.getOpponent().getName() + "'s Shield blocked damage! 🛡️");
                    animateShieldBlock(oStatus);
                }

                if (Board.getCards().size() != initialDeckSize && !Board.getCards().isEmpty()) {
                    Card drawn = Board.getCards().get(0);
                    cardLabel.setText("[" + drawn.getName() + "]: " + drawn.getDescription());
                    logAction("  -> Card Activated: [" + drawn.getName() + "]");
                    animateCardDrawn();
                }

                if (game.getPlayer().isConfused())   animateConfusion(pCurr);
                if (game.getOpponent().isConfused())  animateConfusion(oCurr);

                executeTwoStageAnimation(opponent, startPos, intermediatePos, finalPos);

            } catch (InvalidMoveException ex) {
                logAction("AI invalid move: " + ex.getMessage() + ". Re-rolling...");
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
        statusLabel.setText("🏆 " + winner.getName() + " WINS!");
        logAction("🏆 GAME OVER! WINNER: " + winner.getName());
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
            Scene scene = new Scene(root);

            // ── ANIMATION: fade scene transition to GameOver ──
            root.setOpacity(0);
            stage.setScene(scene);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(600), root);
            fadeIn.setFromValue(0); fadeIn.setToValue(1);
            fadeIn.play();

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
            pt.setOnFinished(e -> placeTokens());
            pt.play();

        } catch (Exception e) {
            postAlertNotification("Denied: " + e.getMessage());
        }
    }

    private void updateUI() {
        Monster p = game.getPlayer();
        if (pName != null) pName.setText(p.getName().toUpperCase());
        pPos.setText("Position: " + (p.getPosition() + 1));
        pOrig.setText("Original: " + p.getOriginalRole());
        pCurr.setText("Role: " + p.getRole() + (p.isConfused() ? " [CONFUSED]" : ""));
        pType.setText("Type: " + p.getClass().getSimpleName());
        pEnergy.setText("Energy: " + p.getEnergy());
        pStatus.setText("Status: " + buildStatusString(p));

        Monster o = game.getOpponent();
        if (oName != null) oName.setText(o.getName().toUpperCase());
        oPos.setText("Position: " + (o.getPosition() + 1));
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
        if (m.isShielded()) effects.add("Shielded 🛡️");
        if (m.isFrozen())   effects.add("Frozen ❄️");
        if (m.isConfused()) effects.add("Confused 🌀 (" + m.getConfusionTurns() + " Turns)");
        return effects.isEmpty() ? "Normal" : String.join(", ", effects);
    }

    private void playDiceSound() {
        try {
            URL res = getClass().getResource("/gui/dice_roll.mp3");
            if (res != null) new AudioClip(res.toExternalForm()).play();
        } catch (Exception e) {}
    }
}
