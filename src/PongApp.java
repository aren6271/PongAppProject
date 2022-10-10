import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
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
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Group gRoot = new Group();
        Scene scene = new Scene(gRoot, size.getX(), size.getY());

        stage.setScene(scene);
        stage.setTitle("pong");
        scene.setFill(Color.WHITE);

        Label fpsLabel = new Label();
        fpsLabel.setTranslateX(2);
        fpsLabel.setTextFill(Color.RED);

        gRoot.getChildren().add(fpsLabel);

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
        /* SETUP */
        AnimationTimer loop = new AnimationTimer() {
            double old = -1;
            double elapsedTime = 0;
            public void handle(long nano) {
                if (old < 0) old = nano;
                double delta = (nano - old) / 1e9; // Divide by 1e9 to put time into Nanosecs

                old = nano;
                elapsedTime += delta;

                fpsLabel.setText(String.format("%.2f %.2f", 1 / delta, elapsedTime)); // 1/delta = frames per sec

                /* GAME LOOP */

                System.out.println(keysDown);
            }
        };

        loop.start();

        stage.show();
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

class Ball extends PhysicsObject {
    double thrust = 150;
    Polygon pgon;

    public Ball(Group parent, Point2D p) {
        super(parent, p, Point2D.ZERO, 0, 0);

        pgon = new Polygon(0.7, 0, -0.7, -0.4, -0.7, -0.4);

        transform.getChildren().add(pgon);

        pgon.setStroke(Color.rgb(196, 237, 253));
        pgon.setStrokeWidth(0.1);
        pgon.getTransforms().add(new Scale(30,30));
    }

    public void update(double delta, double omega, double throttle) {
        if (throttle != 0) {
            Point2D acc = vecAngle(theta, thrust * throttle);
            v = v.add(acc.multiply(delta));
        } else {
            v = v.multiply(1 - 0.2 * delta);
        }

        this.omega = omega;
        super.update(delta);
    }

    public Shape getShapeBounds() { return pgon; }
}