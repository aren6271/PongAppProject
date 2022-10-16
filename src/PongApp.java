import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javax.sound.midi.*;

public class PongApp extends Application{
    Point2D size = new Point2D(Screen.getPrimary().getBounds().getWidth()*0.30, Screen.getPrimary().getBounds().getHeight()*0.85);
    final static double SPEED_INCREMENT = 0.15;

    public void start(Stage stage) throws Exception {
        Group gRoot = new Group();
        Scene scene = new Scene(gRoot, size.getX(), size.getY());

        stage.setScene(scene);
        stage.setTitle("pong");
        scene.setFill(Color.WHITE);
        scene.setCursor(Cursor.NONE);

        Label fpsLabel = new Label();
        fpsLabel.setTranslateX(2);
        fpsLabel.setTextFill(Color.RED);
        fpsLabel.setFont(Font.font(25));

        // Score Label
        Label scoreLabel = new Label();
        scoreLabel.setTranslateX(scene.getWidth()/2);
        scoreLabel.setTranslateY(scene.getHeight()/3);
        scoreLabel.setTextFill(Color.BLUE);
        scoreLabel.setFont(Font.font(40));

        //WALL Object
        Rectangle bounds = new Rectangle(0, 0, size.getX(), size.getY());
        bounds.setVisible(false);
        gRoot.getChildren().add(bounds);

        // BAT Object
        Rectangle bat = new Rectangle(scene.getWidth()/2, scene.getHeight()-20, scene.getWidth() * 0.35, 30);
        bat.setFill(Color.BLUE);
        bat.setStroke(Color.BLUE);
        bat.setStrokeWidth(3);

        // BALL Object
        Rectangle ball = new Rectangle(scene.getWidth()/2, scene.getHeight()/2, 50, 50);
        ball.setFill(Color.BLUE);
        ball.setStroke(Color.BLUE);
        ball.setStrokeWidth(3);

        BinkBonkSound sound = new BinkBonkSound();

        Group gGame = new Group();
        Group gBat = new Group(bat);
        Group gBall = new Group(ball);

        gGame.getChildren().addAll(gBat, gBall);
        gRoot.getChildren().addAll(gGame, fpsLabel, scoreLabel);

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.S) {
                    sound.toggleSound();
                } else if (event.getCode() == KeyCode.I) {
                    fpsLabel.setVisible(!fpsLabel.isVisible());
                }
            }
        });
        scene.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bat.setX(event.getX() - (bat.getWidth()/2));
                bat.setY(scene.getHeight()-bat.getHeight());
            }
        });
        scene.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bat.setFill(Color.RED);
                bat.setStroke(Color.RED);
            }
        });
        scene.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bat.setFill(Color.BLUE);
                bat.setStroke(Color.BLUE);
            }
        });

        /* SETUP */
        AnimationTimer loop = new AnimationTimer() {
            double old = -1;
            double elapsedTime = 0;
            double frames = -1;
            double frameTimes = -1;
            double avgFrames = -1;
            double avgFrameTimes = -1;
            int framesIndex = 0;

            int score = 0;

            double horzBall = 0;
            double vertBall = 6;

            double horzBat = 0;
            double oldBatPos = bat.getX();
            double offset = 0;

            boolean inBat = false;
            boolean inTop = false;
            boolean inBot = false;
            boolean inLeft = false;
            boolean inRight = false;

            public void handle(long nano) {
                /* GAME INFORMATION */
                if (old < 0) old = nano;
                double delta = (nano - old) / 1e9; // Divide by 1e9 to put time into seconds

                old = nano;
                elapsedTime += delta;

                if (avgFrames < 0) avgFrames = 1/delta;
                if (frames < 0) {
                    frames = 1/delta;
                } else {
                    frames = frames + 1/delta;
                }

                if (avgFrameTimes < 0) avgFrameTimes = delta*1000;
                if (frameTimes < 0) {
                    frameTimes = delta*1000;
                } else {
                    frameTimes = frameTimes + (delta*1000);
                }

                framesIndex += 1;

                if (framesIndex == 25) {
                    avgFrames = frames/framesIndex;
                    frames = -1;

                    avgFrameTimes = frameTimes/framesIndex;
                    frameTimes = -1;

                    framesIndex = 0;
                }

                fpsLabel.setText(String.format("%.2f FPS (avg) FT = %.2f (ms avg), GT = %.2f (s)", avgFrames, avgFrameTimes, elapsedTime)); // 1/delta = frames per sec
                scoreLabel.setText(Integer.toString(score));

                /* GAME LOOP */

                /* BALL MECHANICS */
                Point2D pBall = new Point2D(ball.getX(), ball.getY());
                ball.setX(pBall.add(horzBall, vertBall).getX());
                ball.setY(pBall.add(horzBall, vertBall).getY());
                ball.setRotate(ball.getRotate()+offset);

                /* BAT MECHANICS */
                horzBat = (bat.getX()-oldBatPos);
                oldBatPos = bat.getX();

                if (score > 50 && bat.getWidth() > 2*ball.getWidth()) {
                    bat.setWidth(bat.getWidth()-0.1);
                }

                /* WALL COLLISION MECHANICS */

                if (leavingBounds(ball, bounds) == 0) {
                    if (!inBot) {
                        ball.setX(Math.random() * (scene.getWidth() - (2*ball.getWidth())));
                        ball.setY(Math.random() * (scene.getHeight()*0.1));
                        horzBall = 0;
                        vertBall = 6;
                        ball.setRotate(0);
                        offset = 0;
                        score = 0;
                        bat.setWidth(scene.getWidth() * 0.35);
                        inBot = true;
                    }
                } else {
                    inBot = false;
                }

                if (leavingBounds(ball, bounds) == 1) {
                    //Top
                    if (!inTop) {
                        sound.play(false);
                        score += 1;
                        if (vertBall > -60 && vertBall < 60) {
                            vertBall = (vertBall - SPEED_INCREMENT) * -1;
                        } else {
                            vertBall = vertBall * -1;
                        }
                        inTop = true;
                    }
                } else {
                    inTop = false;
                }

                if (leavingBounds(ball, bounds) == 2) {
                    //Right
                    if (!inRight) {
                        sound.play(false);
                        score += 1;
                        horzBall = horzBall * -1;
                        if (vertBall > -60 && vertBall < 60) {
                            if (vertBall < 0) {
                                vertBall = vertBall - SPEED_INCREMENT;
                            } else {
                                vertBall = vertBall + SPEED_INCREMENT;
                            }
                        }
                        inRight = true;
                    }
                } else {
                        inRight = false;
                }

                if (leavingBounds(ball, bounds) == 3) {
                    //Left
                    if (!inLeft) {
                        sound.play(false);
                        score += 1;
                        horzBall = horzBall * -1;
                        if (vertBall > -60 && vertBall < 60) {
                            if (vertBall < 0) {
                                vertBall = vertBall - SPEED_INCREMENT;
                            } else {
                                vertBall = vertBall + SPEED_INCREMENT;
                            }
                        }
                        inLeft = true;
                    }
                } else {
                    inLeft = false;
                }

                /* BAT COLLISION MECHANICS */
                if (!ball.intersect(ball, bat).getBoundsInLocal().isEmpty()) {
                    if (!inBat) {
                        sound.play(true);
                        score += 1;
                        horzBall = horzBall + (horzBat * 0.25);
                        if (vertBall > -60 && vertBall < 60) {
                            vertBall = (vertBall + SPEED_INCREMENT) * -1;
                        } else {
                            vertBall = vertBall * -1;
                        }
                        offset = horzBall * 0.5;
                        inBat = true;
                    }
                } else {
                    inBat = false;
                }
            }
        };
        loop.start();
        stage.show();
    }

    public int leavingBounds(Rectangle ball, Rectangle bounds) {
        if (!ball.intersect(ball, bounds).getBoundsInLocal().isEmpty()) {
            // ENUM This
            if (ball.getY() < 0 ) {
                return 1; // Top
            } else if (ball.getX() + ball.getWidth() > bounds.getWidth()) {
                return 2; // Right
            } else if (ball.getX() < 0) {
                return 3; // Left
            }
        } else {
            return 0; // Bottom / Out of Bounds
        }
        return -1;
    }

class BinkBonkSound {

    // magic numbers that are not common knowledge unless one
    // has studied the GM2 standard and the midi sound system
    //
    // The initials GM mean General Midi. This GM standard
    // provides for a set of common sounds that respond
    // to midi messages in a common way.
    //
    // MIDI is a standard for the encoding and transmission
    // of musical sound meta-information, e.g., play this
    // note on this instrument at this level and this pitch
    // for this long.
    //
    private static final int MAX_PITCH_BEND = 16383;
    private static final int MIN_PITCH_BEND = 0;
    private static final int REVERB_LEVEL_CONTROLLER = 91;
    private static final int MIN_REVERB_LEVEL = 0;
    private static final int MAX_REVERB_LEVEL = 127;
    private static final int DRUM_MIDI_CHANNEL = 9;
    private static final int CLAVES_NOTE = 76;
    private static final int NORMAL_VELOCITY = 100;
    private static final int MAX_VELOCITY = 127;

    Instrument[] instrument;
    MidiChannel[] midiChannels;
    boolean playSound;

    public BinkBonkSound(){
        playSound=true;
        try{
            Synthesizer gmSynthesizer = MidiSystem.getSynthesizer();
            gmSynthesizer.open();
            instrument = gmSynthesizer.getDefaultSoundbank().getInstruments();
            midiChannels = gmSynthesizer.getChannels();

        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    // This method has more comments than would typically be needed for
    // programmers using the Java sound system libraries. This is because
    // most students will not have exposure to the specifics of midi and
    // the general midi sound system. For example, drums are on channel
    // 10 and this cannot be changed. The GM2 standard defines much of
    // the detail that I have chosen to use static constants to encode.
    //
    // The use of midi to play sounds allows us to avoid using external
    // media, e.g., wav files, to play sounds in the game.
    //
    void play(boolean hiPitch){
        if(playSound) {

            // Midi pitch bend is required to play a single drum note
            // at different pitches. The high and low pongs are two
            // octaves apart. As you recall from high school physics,
            // each additional octave doubles the frequency.
            //
            midiChannels[DRUM_MIDI_CHANNEL]
                    .setPitchBend(hiPitch ? MAX_PITCH_BEND : MIN_PITCH_BEND);

            // Turn the reverb send fully off. Drum sounds play until they
            // decay completely. Reverb extends the audible decay and,
            // from a gameplay point of view, is distracting.
            //
            midiChannels[DRUM_MIDI_CHANNEL]
                    .controlChange(REVERB_LEVEL_CONTROLLER, MIN_REVERB_LEVEL);

            // Play the claves on the drum channel at a "normal" volume
            //
            midiChannels[DRUM_MIDI_CHANNEL]
                    .noteOn(CLAVES_NOTE, NORMAL_VELOCITY);
        }
    }

    public void toggleSound() {
        playSound = !playSound;
    }
}

    public static void main(String[] args) {
        launch(args);
    }
}

