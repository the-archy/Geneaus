package com.archy.geneus;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.Group;
import javafx.scene.control.Label;

public class PersonNode extends Group {

    private final Person person;


    protected final static double WIDTH = 140;
    protected final static double HEIGHT = 60;


    public PersonNode(Person person) {
        this.person = person;

        var background = new Rectangle(WIDTH, HEIGHT);
        background.setArcWidth(10);
        background.setArcHeight(10);
        background.setFill(Color.WHITE);
        background.setStroke(Color.DARKGRAY);
        background.setStrokeWidth(2);

        var name = new Label(person.getDisplayName());
        name.setFont(Font.font(14));
        name.setLayoutX(10);
        name.setLayoutY(8);

        var date = new Label(
            person.getBirthDate() != null
                ? person.getBirthDate().format(GenealogyApp.dateFormatter)
                : ""
        );
        date.setFont(Font.font(12));
        date.setLayoutX(10);
        date.setLayoutY(28);

        getChildren().addAll(background, name, date);
    }

    public Person getPerson() { return person; }



    public Point2D getLeftAnchor() {
        Bounds b = getBoundsInParent();
        return new Point2D(b.getMinX(), b.getMinY() + b.getHeight() / 2);
    }

    public Point2D getRightAnchor() {
        Bounds b = getBoundsInParent();
        return new Point2D(b.getMaxX(), b.getMinY() + b.getHeight() / 2);
    }

    public Point2D getTopAnchor() {
        Bounds b = getBoundsInParent();
        return new Point2D(b.getMinX() + b.getWidth() / 2, b.getMinY());
    }

    public Point2D getBottomAnchor() {
        Bounds b = getBoundsInParent();
        return new Point2D(b.getMinX() + b.getWidth() / 2, b.getMaxY());
    }
}