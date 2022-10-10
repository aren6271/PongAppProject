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

            // Make ball fall on spawn
        double bAngle = 0;
        double bOmega = 0;
        Point2D bv = PhysicsObject.vecAngle(Math.PI + bAngle + PhysicsObject.rand(-0.2,0.2), PhysicsObject.rand(50, 100));

        Translate translate = new Translate();


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
            public void handle(long nano) {
                if (old < 0) old = nano;
                double delta = (nano - old) / 1e9; // Divide by 1e9 to put time into Nanosecs

                old = nano;
                elapsedTime += delta;


                fpsLabel.setText(String.format("%.2f %.2f", 1 / delta, elapsedTime)); // 1/delta = frames per sec
                scoreLabel.setText(Integer.toString(score));
                /* GAME LOOP */

                System.out.println(keysDown);

                if (keysDown.contains(KeyCode.I)) {
                    fpsLabel.setVisible(true);
                } else {
                    fpsLabel.setVisible(false);
                }

                int direction;
                if (keysDown.contains(KeyCode.D)) {
                    direction = 1;
                }

                /* Bat Movement */



                //bat.update(delta, 0, key(KeyCode.D));

                //double rot = 4 * (key(KeyCode.D) - key(KeyCode.A));
                //ball.update(delta, rot, key(KeyCode.W));

                /* BALL Movement */
                translate.setY(5);
                ball.getTransforms().addAll(translate);

                if (leavingBounds(ball, bounds)) {
                    System.out.println("GAME OVER");
                    ball.setX(Math.random() * (scene.getWidth() - 50) + 50);
                    ball.getTransforms().clear();
                }

                // If the ball hits the bat
//                if (ball.intersect(ball, bat).getBoundsInLocal().isEmpty())

                //System.out.println(ball.intersects(bat.getBoundsInLocal()));
            }
        };

        loop.start();

        stage.show();
    }

    public boolean leavingBounds(Rectangle ball, Rectangle bounds) {
        if (!ball.intersect(ball, bounds).getBoundsInLocal().isEmpty()) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

abstract class PhysicsObject {
    Point2D p, v;
    double theta, omega; //angle and velocity of object
    Group root, transform;
    boolean alive;

    final static double TAU = Math.PI * 2;

    public PhysicsObject(Group parent, Point2D p0, Point2D v0, double theta0, double omega0) {
        root = new Group();
        transform = new Group();
        root.getChildren().add(transform);
        parent.getChildren().add(root);

        p = p0;
        v = v0;
        theta = theta0;
        omega = omega0;
        alive = true;
    }

    public void update(double delta) {
        p = p.add(v.multiply(delta));
        theta = (theta + omega * delta) % TAU;

        transform.getTransforms().clear();
        transform.getTransforms().addAll(
                new Translate(p.getX(), p.getY()), //transform then rotate (order matters)
                new Rotate(Math.toDegrees(theta))
        );
    }

    public void destroy(Group parent) {
        parent.getChildren().remove(root);
        alive = false;
    }

    abstract Shape getShapeBounds();

    public boolean intersects(PhysicsObject po) {
        return alive && po.alive &&
                !Shape.intersect(getShapeBounds(), po.getShapeBounds())
                .getBoundsInLocal().isEmpty(); // Checks if the 2 objects are colliding
    }

    static Point2D vecAngle(double angle, double mag) {
        return new Point2D(Math.cos(angle), Math.sin(angle)).multiply(mag);
    }

    static double rand(double min, double max) {
        return Math.random() * (max - min) + min;
    }
}

//class Ball extends PhysicsObject {
//    double thrust = 150;
//    Polygon pgon;
//
//    public Ball(Group parent, Point2D p) {
//        super(parent, p, Point2D.ZERO, 0, 0);
//
//        pgon = new Polygon(0.7, 0.7, 0.7, -0.7, -0.7, -0.7, -0.7, 0.7);
//
//        transform.getChildren().add(pgon);
//
//        pgon.setStroke(Color.rgb(255, 0, 0));
//        pgon.setStrokeWidth(0.1);
//        pgon.getTransforms().add(new Scale(30,30));
//
//        update(0,0,0);
//    }
//
//    public void update(double delta, double omega, double throttle) {
//        if (throttle != 0) {
//            Point2D acc = vecAngle(theta, thrust * throttle);
//            v = v.add(acc.multiply(delta));
//        } else {
//            v = v.multiply(1 - 0.2 * delta);
//        }
//
//        this.omega = omega;
//        super.update(delta);
//    }
//
//    public Shape getShapeBounds() { return pgon; }
//}
//
//class Bat extends PhysicsObject {
//    double thrust = 150;
//    Rectangle rect;
//
//    public Bat(Group parent, Point2D p) {
//        super(parent, p, Point2D.ZERO, 0, 0);
//
//        rect = new Rectangle(100, 10, Color.RED);
//        rect.setStroke(Color.RED);
//        rect.setStrokeWidth(3);
//
//        transform.getChildren().add(rect);
//
//        update(0);
//    }
//
//    public void update(double delta, double omega, double throttle) {
//        // Able to move the bat?
//        if (throttle != 0) {
//            Point2D acc = vecAngle(theta, thrust * throttle);
//            v = v.add(acc.multiply(delta));
//        } else {
//            v = v.multiply(1 - 0.2 * delta);
//        }
//        this.omega = omega;
//        super.update(delta);
//    }
//    public Shape getShapeBounds() { return rect; }
//}