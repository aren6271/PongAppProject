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
class GameTimer {
    Label info;
    boolean gameTimerVisible;
    double frames, frameTimes;
    double avgFrames, avgFrameTimes;
    int framesIndex;
//    double
    public GameTimer(Group parent) {
        info = new Label();
        info.setTranslateX(2);
        info.setTextFill(Color.RED);
        gameTimerVisible = true;

        frames = -1;
        frameTimes = -1;
        double avgFrames = -1;
        double avgFrameTimes = -1;
        framesIndex = 0;

        parent.getChildren().add(info);
    }
    public void toggleGameTimer() {
        gameTimerVisible = !gameTimerVisible;
        info.setVisible(gameTimerVisible);
    }

    public double calculateAvgFrames(double delta) {
        return 144.0f;
    }

    public double calculateAvgFrameTimes() {
        return 7.0f;
    }

    public void updateDisplay(double elapsedTime) {
        info.setText(String.format("%.2f FPS (avg) FT = %.2f (ms avg), GT = %.2f (s)",
                                    calculateAvgFrames(2.2),
                                    calculateAvgFrameTimes(),
                                    elapsedTime));
    }
}
class ScoreDisplay{

}
abstract class PhysicsObject {
    Point2D position;
    double horizontalVelocity, verticalVelocity, height, width, offset;
    Group root, transform;

    public PhysicsObject(Group parent, Point2D p0, double horizontalV, double verticalV,  double width0, double height0) {
        root = new Group();
        transform = new Group();
        root.getChildren().add(transform);
        parent.getChildren().add(root);

        position = p0;
        horizontalVelocity = horizontalV;
        verticalVelocity = verticalV;
        width = width0;
        height = height0;
        offset = 0;
    }
    public void update(Rectangle object) {
        object.setX(position.getX());
        object.setY(position.getY());
        object.setRotate(object.getRotate()+offset);
    }

    abstract Rectangle getShapeBounds();

    public boolean intersects(PhysicsObject po) {
        return !Rectangle.intersect(getShapeBounds(), po.getShapeBounds())
                .getBoundsInLocal().isEmpty(); // Checks if the 2 objects are colliding
    }

}

class Ball extends PhysicsObject {
    Rectangle ball;
    public Ball(Group parent, Point2D p, double w, double h) {
        super(parent, p, 0, 6, w, h);

        ball = new Rectangle(p.getX(), p.getY(), width, height);
        ball.setFill(Color.BLUE);
        ball.setStroke(Color.BLUE);
        ball.setStrokeWidth(3);

        transform.getChildren().add(ball);
    }
    public void update() {
        Point2D newP = position.add(horizontalVelocity, verticalVelocity);
        this.position = newP;
        this.offset = horizontalVelocity * 0.5;

        super.update(ball);
    }
    public void reset(double sceneWidth, double sceneHeight) {
        Point2D resetP = new Point2D(Math.random() * (sceneWidth - (2*width)),
                Math.random() * (sceneHeight*0.1));
        this.position = resetP;

        this.horizontalVelocity = 0;
        this.verticalVelocity = 6;
        this.offset = 0;

        ball.setRotate(0);

        super.update(ball);
    }
    public Rectangle getShapeBounds() { return ball; }
}
class Bat extends PhysicsObject {
    Rectangle bat;
    double oldPosition;

    public Bat(Group parent, Point2D p, double w, double h) {
        super(parent, p, 0, 0, w, h);

        bat = new Rectangle(p.getX(), p.getY(), width, height);
        bat.setFill(Color.BLUE);
        bat.setStroke(Color.BLUE);
        bat.setStrokeWidth(3);

        this.oldPosition = p.getX();

        transform.getChildren().add(bat);
    }

    public void update(double mouseX, double sceneHeight) {
        Point2D newP = new Point2D(mouseX - (bat.getWidth()/2), sceneHeight-bat.getHeight());
        this.position = newP;

        super.update(bat);
    }

    public void calculateSpeed(double currX) {
        this.horizontalVelocity = currX-oldPosition;
        this.oldPosition = currX;

    }
    public void changeColor(boolean isActive) {
        if (isActive) {
            bat.setFill(Color.BLUE);
            bat.setStroke(Color.BLUE);
        } else {
            bat.setFill(Color.RED);
            bat.setStroke(Color.RED);
        }
    }
    public Rectangle getShapeBounds() { return bat; }
}

class Walls extends PhysicsObject {
    Rectangle walls;
    public Walls(Group parent, double w, double h) {
        super(parent, Point2D.ZERO, 0, 0, w, h);
        walls = new Rectangle(0,0, w, h );
    }

    public Rectangle getShapeBounds() { return walls; }
}

class Pong {
    //Game Model (Score update + Collision)
    //Change this later
    final static double SPEED_INCREMENT = 0.15;

    boolean inBat, inTop, inRight, inLeft, inBottom;
    Ball ball;
    Bat bat;
    Walls walls;
    public Pong(Ball ball0, Bat bat0, Walls walls0) {
        inBat = false;
        inTop = false;
        inRight = false;
        inLeft = false;
        inBottom = false;

        ball = ball0;
        bat = bat0;
        walls = walls0;
    }
    enum Sides {
        TOP,
        SIDE,
        //LEFT,
        BOTTOM
    }
    public void inCollision() {
        if (ball.intersects(bat)) {
            if (!inBat) {
                collideBat();
                inBat = true;
            }
        } else {
            inBat = false;
        }

        if (ball.intersects(walls)) {
            if (!inTop) {
                if (ball.position.getY() < 0) {
                    // Top
                    collideWall(Sides.TOP);
                    inTop = true;
                }
            } else {
                inTop = false;
            }

            if (!inRight) {
                if (ball.position.getX() + ball.width > walls.width) {
                    // Right
                    collideWall(Sides.SIDE);
                    inRight = true;
                }
            } else {
                inRight = false;
            }

            if (!inLeft) {
                if (ball.position.getX() < 0) {
                    // Left
                    collideWall(Sides.SIDE);
                    inLeft = true;
                }
            } else {
                inLeft = false;
            }

        } else {
            if (!inBottom) {
                collideWall(Sides.BOTTOM);
                inBottom = true;
            } else {
                inBottom = false;
            }
        }
    }
    public void collideBat() {
        //sound.play(true);
        //score += 1;

        // Change 7 from constant to avgFrameTimes
        ball.horizontalVelocity = ball.horizontalVelocity + (bat.horizontalVelocity / 7);
        if (ball.verticalVelocity > -60 && ball.verticalVelocity < 60) {
            ball.verticalVelocity = (ball.verticalVelocity + SPEED_INCREMENT) * -1;
        } else {
            ball.verticalVelocity = ball.verticalVelocity * -1;
        }
    }

    public void collideWall(Sides side) {
//        sound.play(false);
//        score += 1;

        switch(side) {
            case TOP:
                if (ball.verticalVelocity > -60 && ball.verticalVelocity < 60) {
                    ball.verticalVelocity = (ball.verticalVelocity - SPEED_INCREMENT) * -1;
                } else {
                    ball.verticalVelocity = ball.verticalVelocity * -1;
                }
                break;
            case SIDE:
                if (ball.verticalVelocity > -60 && ball.verticalVelocity < 60) {
                    ball.horizontalVelocity = ball.horizontalVelocity * -1;
                    if (ball.verticalVelocity < 0) {
                        ball.verticalVelocity = (ball.verticalVelocity - SPEED_INCREMENT);
                    } else {
                        ball.verticalVelocity = (ball.verticalVelocity + SPEED_INCREMENT);
                    }
                }
                break;
            case BOTTOM:
                ball.reset(walls.width, walls.height);
                bat.width = (walls.width * 0.35);
                break;
        }
    }
}

public class OPongApp extends Application{
    Point2D size = new Point2D(Screen.getPrimary().getBounds().getWidth()*0.40, Screen.getPrimary().getBounds().getHeight()*0.85);

    public void start(Stage stage) throws Exception {
        Group gRoot = new Group();
        Scene scene = new Scene(gRoot, size.getX(), size.getY());

        stage.setScene(scene);
        stage.setTitle("pong");
        scene.setFill(Color.WHITE);
        scene.setCursor(Cursor.NONE);

        Group gLabel = new Group();

        BinkBonkSound sound = new BinkBonkSound();
        GameTimer informationDisplay = new GameTimer(gLabel);

        Group gGame = new Group();
        Ball ball = new Ball(gGame, new Point2D(scene.getWidth()/2, scene.getHeight()/2), 50, 50);
        Bat bat = new Bat(gGame, new Point2D(scene.getWidth()/2, scene.getHeight()-20), scene.getWidth() * 0.35, 30);
        Walls walls = new Walls(gGame, scene.getWidth(), scene.getHeight());

        Pong gameModel = new Pong(ball, bat, walls);

        gRoot.getChildren().addAll(gGame, gLabel);

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.S) {
                    sound.toggleSound();
                } else if (event.getCode() == KeyCode.I) {
                    informationDisplay.toggleGameTimer();
                }
            }
        });
        scene.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bat.update(event.getX(), scene.getHeight());}
        });
        scene.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bat.changeColor(false);
            }
        });
        scene.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bat.changeColor(true);
            }
        });
        AnimationTimer loop = new AnimationTimer() {
            double old = -1;
            double elapsedTime = 0;
            public void handle(long nano) {
                if (old < 0) old = nano;
                double delta = (nano - old) / 1e9; // Divide by 1e9 to put time into Nanosecs

                old = nano;
                elapsedTime += delta;

                informationDisplay.updateDisplay(elapsedTime);
                ball.update();
                bat.calculateSpeed(bat.position.getX());
                gameModel.inCollision();
            }
        };

        loop.start();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
