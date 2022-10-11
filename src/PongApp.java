import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class PongApp extends Application{
    Point2D size = new Point2D(900, 1200);
    final static double TAU = Math.PI * 2;
    Set<KeyCode> keysDown = new HashSet<>();

    int key(KeyCode k) {
        return keysDown.contains(k) ? 1 : 0;
    }

    public void start(Stage stage) throws Exception {
        Group gRoot = new Group();
        Scene scene = new Scene(gRoot, size.getX(), size.getY());

        stage.setScene(scene);
        stage.setTitle("pong");
        scene.setFill(Color.WHITE);

        Label fpsLabel = new Label();
        fpsLabel.setTranslateX(2);
        fpsLabel.setTextFill(Color.RED);

        // Score Label
        Label scoreLabel = new Label();
        scoreLabel.setTranslateX(scene.getWidth()/2);
        scoreLabel.setTranslateY(scene.getHeight()/3);
        scoreLabel.setTextFill(Color.BLUE);
        scoreLabel.setFont(Font.font(40));

        //Wall Object
        Rectangle bounds = new Rectangle(0, 0, size.getX(), size.getY());
        bounds.setVisible(false);
        gRoot.getChildren().add(bounds);

        // BAT Object
        Rectangle bat = new Rectangle(100.0f, scene.getHeight()-20, 200, 20);
        bat.setFill(Color.BLUE);
        bat.setStroke(Color.BLUE);
        bat.setStrokeWidth(3);

        // BALL Object

        Rectangle ball = new Rectangle(scene.getWidth()/2, scene.getHeight()/2, 50, 50);
        ball.setFill(Color.BLUE);
        ball.setStroke(Color.BLUE);
        ball.setStrokeWidth(3);

        Group gGame = new Group();
        Group gBat = new Group(bat);
        Group gBall = new Group(ball);

        gGame.getChildren().addAll(gBat, gBall);
//40:26
        gRoot.getChildren().addAll(gGame, fpsLabel, scoreLabel);

        /*
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent event) {
                keysDown.add(event.getCode());
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent event) {
                keysDown.remove(event.getCode());
            }
        });
        */

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
              if (keysDown.contains(event.getCode())) {
                  keysDown.remove(event.getCode());
              } else {
                  keysDown.add(event.getCode());
              }
            }
        });

        scene.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bat.setX(event.getX() - (bat.getWidth()/2));
            }
        });


        /* SETUP */
        AnimationTimer loop = new AnimationTimer() {
            double old = -1;
            double elapsedTime = 0;
            int score = 0;

            double horzBall = 0;
            double vertBall = 6;

            double horzBat = 0;
            double oldBatPos = bat.getX();


            public void handle(long nano) {
                if (old < 0) old = nano;
                double delta = (nano - old) / 1e9; // Divide by 1e9 to put time into Nanosecs

                old = nano;
                elapsedTime += delta;

                // Make this dynamic not just a number
                // Just for testing
                // Divide Delta by Framerate?
                horzBat = (bat.getX()-oldBatPos)/delta/144;
                oldBatPos = bat.getX();

                System.out.println(horzBat);

                fpsLabel.setText(String.format("%.2f %.2f", 1 / delta, elapsedTime)); // 1/delta = frames per sec
                scoreLabel.setText(Integer.toString(score));
                /* GAME LOOP */

                //System.out.println(keysDown);

                if (keysDown.contains(KeyCode.I)) {
                    fpsLabel.setVisible(true);
                } else {
                    fpsLabel.setVisible(false);
                }

                int direction;
                if (keysDown.contains(KeyCode.D)) {
                    direction = 1;
                }

                /* BALL Movement */
                Point2D pBall = new Point2D(ball.getX(), ball.getY());

                //System.out.println((pBall.add(vBall)).getY());

                ball.setX(pBall.add(horzBall, vertBall).getX());
                ball.setY(pBall.add(horzBall, vertBall).getY());

                if (leavingBounds(ball, bounds) == 0) {
                    System.out.println("GAME OVER");
//                    ball.getTransforms().clear();
                    ball.setX(Math.random() * (scene.getWidth() - 100));
                    ball.setY(Math.random() * (50));
                    horzBall = 0;
                    vertBall = 6;
                    score = 0;
                }
                if (leavingBounds(ball, bounds) == 1) {
                    score+=1;
                    vertBall = vertBall *-1;
                }
                if (leavingBounds(ball, bounds) == 2 ) {
                    score+=1;
                    horzBall = horzBall *-1;
                }
                if (leavingBounds(ball, bounds) == 3) {
                    score+=1;
                    horzBall = horzBall *-1;
                }

                // If the ball hits the bat
                if (!ball.intersect(ball, bat).getBoundsInLocal().isEmpty()) {
                    System.out.println("HIT");
                    score+=1;
                    Point2D hitVelocity = new Point2D(horzBall, vertBall).add(horzBat, 0);
                    horzBall = hitVelocity.getX();
                    vertBall = hitVelocity.getY() *-1;

                    //vBall = Point2D(vBall.getX(), vBall.get)
                    //vertBall[0] = vertBall[0] *-1;
//                    ball.getTransforms().addAll(
//                            new Translate(vBall.getX(), vBall.getY())
//                            new Rotate(Math.toDegrees(theta))
//                    );

                }


                //System.out.println(ball.intersects(bat.getBoundsInLocal()));
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
            if (ball.getY() > bounds.getHeight()) {
                return 0; // Bottom
            }
        }
        return -1;
    }

//    public Point2D vecAngle(double angle, double mag) {
//        return new Point2D(Math.cos(angle), Math.sin(angle)).multiply(mag);
//    }
//
//    public double rand(double min, double max) {
//        return Math.random() * (max - min) + min;
//    }

//    public void update(double delta, Point2D p, Point2D v, double theta, double omega, Rectangle transform) {
//        p = p.add(v.multiply(delta));
//        theta = (theta + omega * delta) % TAU;
//
//        transform.getTransforms().clear();
//        transform.getTransforms().addAll(
//                new Translate(p.getX(), p.getY()),
//                new Rotate(Math.toDegrees(theta))
//        );
//    }

    public static void main(String[] args) {
        launch(args);
    }
}

