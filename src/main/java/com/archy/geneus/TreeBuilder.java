package com.archy.geneus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

public class TreeBuilder {

    private final Person root;

    public TreeBuilder(Person root) {
        this.root = root;
    }

    public Pane build() {

        Pane pane = new Pane();

        PersonNode rootNode = new PersonNode(root);
        rootNode.setLayoutX(200);
        rootNode.setLayoutY(150);
        pane.getChildren().add(rootNode);

        List<Person> parents = root.getParents();
        if (parents.size() == 2) {
            buildTwoParentTree(pane, rootNode, parents);
        } else if (parents.size() == 1) {
            buildSingleParentTree(pane, rootNode, parents.get(0));
        }

        buildPartnerTree(pane, rootNode);

        adjustPaneLayout(pane);

        return pane;
    }

    private void buildTwoParentTree(Pane pane, PersonNode rootNode, List<Person> parents) {
        Person father = parents.get(0);
        Person mother = parents.get(1);

        PersonNode fatherNode = new PersonNode(father);
        PersonNode motherNode = new PersonNode(mother);

        double rootX = rootNode.getLayoutX();
        double rootY = rootNode.getLayoutY();

        double parentY = rootY - 150;
        double parentSpacing = 160;

        double centerX = rootX + PersonNode.WIDTH / 2;
        double startX = centerX - parentSpacing / 2 - PersonNode.WIDTH;

        fatherNode.setLayoutX(startX);
        fatherNode.setLayoutY(parentY);
        motherNode.setLayoutX(startX + parentSpacing + PersonNode.WIDTH);
        motherNode.setLayoutY(parentY);

        pane.getChildren().addAll(fatherNode, motherNode);

        Line partnerLine = createLine(fatherNode.getRightAnchor(), motherNode.getLeftAnchor());
        pane.getChildren().add(partnerLine);

        double midX = (fatherNode.getRightAnchor().getX() + motherNode.getLeftAnchor().getX()) / 2;
        double forkY = rootY - 30;
        Line downLine = new Line(midX, partnerLine.getStartY(), midX, forkY);
        pane.getChildren().add(downLine);

        buildSiblingsTree(pane, rootNode, father, mother, midX, forkY, rootY);
    }

    private void buildSingleParentTree(Pane pane, PersonNode rootNode, Person soleParent) {
        PersonNode parentNode = new PersonNode(soleParent);

        double rootX = rootNode.getLayoutX();
        double rootY = rootNode.getLayoutY();

        double parentX = rootX;
        double parentY = rootY - 150;

        parentNode.setLayoutX(parentX);
        parentNode.setLayoutY(parentY);
        pane.getChildren().add(parentNode);

        Line line = createLine(parentNode.getBottomAnchor(), rootNode.getTopAnchor());
        pane.getChildren().add(line);
    }

    private void buildSiblingsTree(Pane pane, PersonNode rootNode, Person father, Person mother, double midX, double forkY, double rootY) {
        List<Person> siblings = father.getSharedDescendantsWith(mother)
                .stream()
                .filter(p -> !p.equals(root))
                .toList();

        if (!siblings.isEmpty()) {
            List<Person> all = new ArrayList<>(siblings);
            all.add(root);
            all.sort(Comparator.comparing(Person::getBirthDate));

            int rootIndex = all.indexOf(root);
            double siblingSpacing = 160;
            double totalWidth = (all.size() - 1) * siblingSpacing;
            double siblingStartX = midX - totalWidth / 2;

            for (int i = 0; i < all.size(); i++) {
                Person person = all.get(i);
                if (person.equals(root)) continue;

                PersonNode siblingNode = new PersonNode(person);
                double x = siblingStartX + i * siblingSpacing - PersonNode.WIDTH / 2;
                siblingNode.setLayoutX(x);
                siblingNode.setLayoutY(rootY);
                pane.getChildren().add(siblingNode);

                Line line = createLine(new Point2D(midX, forkY), siblingNode.getTopAnchor());
                pane.getChildren().add(line);
            }

            double newRootX = siblingStartX + rootIndex * siblingSpacing - PersonNode.WIDTH / 2;
            rootNode.setLayoutX(newRootX);
        }

        Line rootLine = createLine(new Point2D(midX, forkY), rootNode.getTopAnchor());
        pane.getChildren().add(rootLine);
    }

    private void buildPartnerTree(Pane pane, PersonNode rootNode) {
        List<Person> partners = root.getPartners();
        double baseSpacing = 200;
        double rootCenterX = rootNode.getLayoutX() + PersonNode.WIDTH / 2;
        double currentX = rootCenterX;

        for (int i = 0; i < partners.size(); i++) {
            Person partner = partners.get(i);
            double spacing = baseSpacing;

            final Label[] marriageLabel = {null};
            var marriage = partner.getMarriageWith(root);

            if (marriage != null && marriage.getStartDate() != null) {
                String dateStr = marriage.getStartDate().format(GenealogyApp.dateFormatter);
                String locationStr = "";

                if (marriage.getStartArea() != null && marriage.getStartCountry() != null) {
                    locationStr = " " + marriage.getStartArea() + ", " + marriage.getStartCountry();
                }

                marriageLabel[0] = new Label("♥ " + dateStr + locationStr);
                marriageLabel[0].getStyleClass().add("marriage-label");
            }

            currentX += (i * spacing) + PersonNode.WIDTH + (marriageLabel == null ? 0 : 0); // offset jen pokud chceš větší mezeru

            var partnerNode = new PersonNode(partner);
            partnerNode.setLayoutX(currentX);
            partnerNode.setLayoutY(rootNode.getLayoutY()); // ve stejné výšce jako root
            pane.getChildren().add(partnerNode);

            Line coupleLine = createLine(rootNode.getRightAnchor(), partnerNode.getLeftAnchor());
            pane.getChildren().add(coupleLine);

            if (marriageLabel != null) {
                pane.getChildren().add(marriageLabel[0]);

                Platform.runLater(() -> {
                    double rootRight = rootNode.getRightAnchor().getX();
                    double partnerLeft = partnerNode.getLeftAnchor().getX();
                    double centerX = (rootRight + partnerLeft) / 2;

                    double labelWidth = marriageLabel[0].getWidth();
                    double labelHeight = marriageLabel[0].getHeight();

                    marriageLabel[0].setLayoutX(centerX - labelWidth / 2);
                    marriageLabel[0].setLayoutY(rootNode.getLayoutY() - labelHeight); // nad partnery
                });
            }

            // Build children tree
            buildDescendantsTree(pane, rootNode, partnerNode, root.getSharedDescendantsWith(partner));
        }
    }


    private void buildDescendantsTree(Pane pane, PersonNode rootNode, PersonNode partnerNode, List<Person> descendants) {
        if (!descendants.isEmpty()) {
            double childY = 300;
            double childSpacing = 160;

            Platform.runLater(() -> {
                Point2D rootAnchor = rootNode.getRightAnchor();
                Point2D partnerAnchor = partnerNode.getLeftAnchor();
                double midX = (rootAnchor.getX() + partnerAnchor.getX()) / 2;
                double midY = rootAnchor.getY();
                double connectionY = midY + 50;

                // svislá čára mezi partnery a potomky
                Line verticalLine = new Line(midX, midY, midX, connectionY);
                pane.getChildren().add(verticalLine);

                double totalWidth = (descendants.size() - 1) * childSpacing;
                double startX = midX - totalWidth / 2;

                // vodorovná čára mezi dětmi
                if (descendants.size() > 1) {
                    double leftX = startX;
                    double rightX = startX + (descendants.size() - 1) * childSpacing;
                    Line horizontalLine = new Line(leftX, connectionY, rightX, connectionY);
                    pane.getChildren().add(horizontalLine);
                }

                for (int i = 0; i < descendants.size(); i++) {
                    Person child = descendants.get(i);
                    PersonNode childNode = new PersonNode(child);

                    double x = startX + i * childSpacing - PersonNode.WIDTH / 2;
                    childNode.setLayoutX(x);
                    childNode.setLayoutY(childY);
                    pane.getChildren().add(childNode);

                    double childCenterX = x + PersonNode.WIDTH / 2;
                    Line connector = new Line(childCenterX, connectionY, childCenterX, childY);
                    pane.getChildren().add(connector);
                }
            });
        }
    }


    private void adjustPaneLayout(Pane pane) {
        Platform.runLater(() -> {
            double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
            double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

            for (var node : pane.getChildren()) {
                if (node instanceof PersonNode p) {
                    minX = Math.min(minX, p.getLayoutX());
                    maxX = Math.max(maxX, p.getLayoutX() + PersonNode.WIDTH);
                    minY = Math.min(minY, p.getLayoutY());
                    maxY = Math.max(maxY, p.getLayoutY() + PersonNode.HEIGHT);
                } else if (node instanceof Label label) {
                    // Adjust for marriage labels
                    minX = Math.min(minX, label.getLayoutX());
                    maxX = Math.max(maxX, label.getLayoutX() + label.getWidth());
                    minY = Math.min(minY, label.getLayoutY());
                    maxY = Math.max(maxY, label.getLayoutY() + label.getHeight());
                } else if (node instanceof Line l) {
                    // Adjust for lines
                    minX = Math.min(minX, Math.min(l.getStartX(), l.getEndX()));
                    maxX = Math.max(maxX, Math.max(l.getStartX(), l.getEndX()));
                    minY = Math.min(minY, Math.min(l.getStartY(), l.getEndY()));
                    maxY = Math.max(maxY, Math.max(l.getStartY(), l.getEndY()));
                }
            }

            double contentWidth = maxX - minX;
            double contentHeight = maxY - minY;

            // Centering and offsetting
            double offsetX = 100 - minX;
            double offsetY = 100 - minY;

            for (var node : pane.getChildren()) {
                if (node instanceof PersonNode p) {
                    p.setLayoutX(p.getLayoutX() + offsetX);
                    p.setLayoutY(p.getLayoutY() + offsetY);
                } else if (node instanceof Line l) {
                    l.setStartX(l.getStartX() + offsetX);
                    l.setEndX(l.getEndX() + offsetX);
                    l.setStartY(l.getStartY() + offsetY);
                    l.setEndY(l.getEndY() + offsetY);
                } else if (node instanceof Label label) {
                    label.setLayoutX(label.getLayoutX() + offsetX);
                    label.setLayoutY(label.getLayoutY() + offsetY);
                }
            }

            pane.setPrefSize(contentWidth + 200, contentHeight + 200);
        });
    }


    private Line createLine(Point2D start, Point2D end) {
        Line line = new Line(start.getX(), start.getY(), end.getX(), end.getY());
        line.getStyleClass().add("relation-line"); // Add a CSS class for styling
        return line;
    }
}
