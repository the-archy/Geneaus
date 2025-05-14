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

    private static final double INITIAL_X = 200;
    private static final double INITIAL_Y = 150;
    private static final double SIBLING_SPACING = 160;
    private static final double PARTNER_SPACING = 200;
    private static final double CHILD_SPACING = 150;
    private static final double MARRIAGE_LABEL_OFFSET_MULT = -1.5;
    private static final double DIVORCE_LABEL_OFFSET_MULT = -2.75;
    private static final double PANE_PADDING = 100;

    public TreeBuilder(Person root) {
        this.root = root;
    }

    public Pane build() {
        Pane pane = new Pane();

        PersonNode rootNode = new PersonNode(root);
        rootNode.setLayoutX(INITIAL_X);
        rootNode.setLayoutY(INITIAL_Y);
        pane.getChildren().add(rootNode);

        List<Person> parents = root.getParents();
        if (!parents.isEmpty()) {
            buildParentalTree(pane, rootNode, parents);
        }

        buildPartnerTree(pane, rootNode);
        adjustPaneLayout(pane);
        return pane;
    }

    private void buildParentalTree(Pane pane, PersonNode childNode, List<Person> parents) {

        if (parents.size() == 1) {
            Person soleParent = parents.get(0);
            PersonNode parentNode = new PersonNode(soleParent);

            double childX = childNode.getLayoutX();
            double childY = childNode.getLayoutY();
            double parentY = childY - PARTNER_SPACING;

            parentNode.setLayoutX(childX);
            parentNode.setLayoutY(parentY);
            pane.getChildren().add(parentNode);
            return;
        }

        Person parent1 = parents.get(0);
        Person parent2 = parents.get(1);

        PersonNode parentNode1 = new PersonNode(parent1);
        PersonNode parentNode2 = new PersonNode(parent2);

        double childX = childNode.getLayoutX();
        double childY = childNode.getLayoutY();
        double parentY = childY - PARTNER_SPACING;

        double centerX = childX + PersonNode.WIDTH / 2;
        double startX = centerX - PARTNER_SPACING;

        parentNode1.setLayoutX(startX);
        parentNode1.setLayoutY(parentY);
        parentNode2.setLayoutX(startX + PARTNER_SPACING);
        parentNode2.setLayoutY(parentY);

        pane.getChildren().addAll(parentNode1, parentNode2);

        Marriage marriage = parent1.getMarriageWith(parent2);
        boolean hasCommonChild = parent1.getSharedDescendantsWith(parent2).contains(root);

        if (marriage != null || hasCommonChild) {
            Line coupleLine = new Line(parentNode1.getRightAnchor().getX(), parentNode1.getRightAnchor().getY(), parentNode2.getLeftAnchor().getX(), parentNode2.getLeftAnchor().getY());
            pane.getChildren().add(coupleLine);

            double midX = (parentNode1.getRightAnchor().getX() + parentNode2.getLeftAnchor().getX()) / 2;
            double forkY = childY - 30;

            Line downLine = new Line(midX, coupleLine.getStartY(), midX, forkY);
            pane.getChildren().add(downLine);

            Label marriageLabel = createMarriageLabel(marriage);
            Label divorceLabel = createDivorceLabel(marriage);

            if (marriageLabel != null) {
                pane.getChildren().add(marriageLabel);
                positionLabel(marriageLabel, MARRIAGE_LABEL_OFFSET_MULT, parentNode1, parentNode2);
            }

            if (divorceLabel != null) {
                pane.getChildren().add(divorceLabel);
                positionLabel(divorceLabel, DIVORCE_LABEL_OFFSET_MULT, parentNode1, parentNode2);
            }

            List<Person> siblings = parent1.getSharedDescendantsWith(parent2);
            buildSharedDescendantsFork(pane, midX, forkY, childY, siblings, childNode);
        }
    }

    private void buildPartnerTree(Pane pane, PersonNode rootNode) {
        List<Person> partners = root.getPartners();
        double baseSpacing = PARTNER_SPACING;
        double rootCenterX = rootNode.getLayoutX() + PersonNode.WIDTH / 2;
        double currentX = rootCenterX;

        for (int i = 0; i < partners.size(); i++) {
            Person partner = partners.get(i);

            // Create marriage and divorce labels first to calculate space needed
            var marriage = partner.getMarriageWith(root);
            Label marriageLabel = createMarriageLabel(marriage);
            Label divorceLabel = createDivorceLabel(marriage);

            // Calculate needed spacing (more space if we have labels)
            double spacingNeeded = baseSpacing;
            if (marriageLabel != null || divorceLabel != null) {
                spacingNeeded += 60; // Add extra space for labels
            }

            currentX += spacingNeeded;

            // Create partner node with calculated position
            var partnerNode = new PersonNode(partner);
            partnerNode.setLayoutX(currentX);
            partnerNode.setLayoutY(rootNode.getLayoutY());
            pane.getChildren().add(partnerNode);

            // Draw connection with proper styling
            Line coupleLine = new Line(
                rootNode.getRightAnchor().getX(),
                rootNode.getRightAnchor().getY(),
                partnerNode.getLeftAnchor().getX(),
                partnerNode.getLeftAnchor().getY()
            );
            coupleLine.getStyleClass().add("relation-line");
            pane.getChildren().add(coupleLine);

            // Position labels ON the line instead of above it
            if (marriageLabel != null) {
                pane.getChildren().add(marriageLabel);
                marriageLabel.getStyleClass().add("on-line-label");

                Platform.runLater(() -> {
                    double midX = (rootNode.getRightAnchor().getX() + partnerNode.getLeftAnchor().getX()) / 2;
                    marriageLabel.setLayoutX(midX - marriageLabel.getWidth() / 2);
                    // Position directly on the line
                    marriageLabel.setLayoutY(rootNode.getRightAnchor().getY() - marriageLabel.getHeight() / 2);
                });
            }

            if (divorceLabel != null) {
                pane.getChildren().add(divorceLabel);
                divorceLabel.getStyleClass().add("on-line-label");

                Platform.runLater(() -> {
                    double midX = (rootNode.getRightAnchor().getX() + partnerNode.getLeftAnchor().getX()) / 2;
                    // Offset the divorce label to avoid overlap with marriage label
                    divorceLabel.setLayoutX(midX + 20);
                    // Position directly on the line
                    divorceLabel.setLayoutY(rootNode.getRightAnchor().getY() - divorceLabel.getHeight() / 2);
                });
            }

            buildDescendantsTree(pane, rootNode, partnerNode, root.getSharedDescendantsWith(partner));
        }
    }

    private void buildDescendantsTree(Pane pane, PersonNode rootNode, PersonNode partnerNode, List<Person> descendants) {
        if (descendants.isEmpty()) return;

        // Calculate midpoint between partners
        double midX = (rootNode.getRightAnchor().getX() + partnerNode.getLeftAnchor().getX()) / 2;
        double parentY = rootNode.getLayoutY() + PersonNode.HEIGHT;
        double childY = parentY + CHILD_SPACING;

        // Connection point from parent line
        double horizontalLineY = childY - 30;

        // Draw vertical line down from parent midpoint to the horizontal line
        Line verticalLine = new Line(midX, parentY, midX, horizontalLineY);
        verticalLine.getStyleClass().add("relation-line");
        pane.getChildren().add(verticalLine);

        // Calculate improved spacing for children
        double childTotalWidth = descendants.size() * PersonNode.WIDTH +
                              (descendants.size() - 1) * (CHILD_SPACING - PersonNode.WIDTH);
        double startX = midX - childTotalWidth / 2;

        // Draw horizontal connection line above children
        if (descendants.size() > 0) {
            double leftMostX = startX + PersonNode.WIDTH/2;
            double rightMostX = startX + (descendants.size() - 1) * CHILD_SPACING + PersonNode.WIDTH/2;

            Line horizontalLine = new Line(leftMostX, horizontalLineY, rightMostX, horizontalLineY);
            horizontalLine.getStyleClass().add("relation-line");
            pane.getChildren().add(horizontalLine);
        }

        // Position all children with proper spacing and vertical connections
        for (int i = 0; i < descendants.size(); i++) {
            Person child = descendants.get(i);
            PersonNode childNode = new PersonNode(child);

            double x = startX + i * CHILD_SPACING;
            childNode.setLayoutX(x);
            childNode.setLayoutY(childY);
            pane.getChildren().add(childNode);

            // Draw vertical connection from horizontal line to child
            double childCenterX = x + PersonNode.WIDTH / 2;
            Line childLine = new Line(childCenterX, horizontalLineY, childCenterX, childY);
            childLine.getStyleClass().add("relation-line");
            pane.getChildren().add(childLine);
        }
    }

    private Label createMarriageLabel(Marriage marriage) {
        if (marriage == null || marriage.getStartDate() == null) return null;

        String dateStr = marriage.getStartDate().format(GenealogyApp.dateFormatter);
        String locationStr = (marriage.getStartArea() != null && marriage.getStartCountry() != null)
                ? " " + marriage.getStartArea() + ", " + marriage.getStartCountry()
                : "";

        Label label = new Label("♥ " + dateStr + locationStr);
        label.getStyleClass().add("marriage-label");
        return label;
    }

    private Label createDivorceLabel(Marriage marriage) {
        if (marriage == null || marriage.getEndDate() == null) return null;

        String dateStr = marriage.getEndDate().format(GenealogyApp.dateFormatter);
        String locationStr = (marriage.getEndArea() != null && marriage.getEndCountry() != null)
                ? " " + marriage.getEndArea() + ", " + marriage.getEndCountry()
                : "";

        Label label = new Label("✗ " + dateStr + locationStr);
        label.getStyleClass().add("divorce-label");
        return label;
    }

    private void positionLabel(Label label, double verticalMultiplier, PersonNode rootNode, PersonNode partnerNode) {
        Platform.runLater(() -> {
            double rootRight = rootNode.getRightAnchor().getX();
            double partnerLeft = partnerNode.getLeftAnchor().getX();
            double centerX = (rootRight + partnerLeft) / 2;

            double labelWidth = label.getWidth();
            double labelHeight = label.getHeight();

            label.setLayoutX(centerX - labelWidth / 2);
            label.setLayoutY(rootNode.getLayoutY() + verticalMultiplier * labelHeight);
        });
    }

    private void buildSharedDescendantsFork(Pane pane, double midX, double forkY, double nodeY, List<Person> people, PersonNode referenceNode) {
        if (people.isEmpty()) return;

        List<Person> sorted = new ArrayList<>(people);
        sorted.sort(Comparator.comparing(Person::getBirthDate));

        // Find the reference person in the sorted list
        int rootIndex = -1;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).equals(root)) {
                rootIndex = i;
                break;
            }
        }

        double totalWidth = (sorted.size() - 1) * SIBLING_SPACING;
        double startX = midX - totalWidth / 2;

        // Only draw the horizontal line if there are multiple siblings
        if (sorted.size() > 1) {
            Line horizontal = new Line(startX, forkY, startX + totalWidth, forkY);
            horizontal.getStyleClass().add("relation-line");
            pane.getChildren().add(horizontal);
        }

        // Draw vertical lines and nodes for all siblings EXCEPT root
        for (int i = 0; i < sorted.size(); i++) {
            Person p = sorted.get(i);
            // Skip the root person (John) - we already have a node for him
            if (p.equals(root)) continue;

            PersonNode node = new PersonNode(p);
            double x = startX + i * SIBLING_SPACING - PersonNode.WIDTH / 2;
            node.setLayoutX(x);
            node.setLayoutY(nodeY);
            pane.getChildren().add(node);

            double centerX = x + PersonNode.WIDTH / 2;
            Line down = new Line(centerX, forkY, centerX, nodeY);
            down.getStyleClass().add("relation-line");
            pane.getChildren().add(down);
        }

        // Position the reference node (John) correctly
        if (referenceNode != null && rootIndex >= 0) {
            double rootX = startX + rootIndex * SIBLING_SPACING - PersonNode.WIDTH / 2;
            referenceNode.setLayoutX(rootX);

            // Draw a single direct line from fork to reference's top anchor
            Line lineToRef = new Line(midX, forkY, referenceNode.getTopAnchor().getX(), referenceNode.getTopAnchor().getY());
            lineToRef.getStyleClass().add("relation-line");
            pane.getChildren().add(lineToRef);
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
                    minX = Math.min(minX, label.getLayoutX());
                    maxX = Math.max(maxX, label.getLayoutX() + label.getWidth());
                    minY = Math.min(minY, label.getLayoutY());
                    maxY = Math.max(maxY, label.getLayoutY() + label.getHeight());
                } else if (node instanceof Line l) {
                    minX = Math.min(minX, Math.min(l.getStartX(), l.getEndX()));
                    maxX = Math.max(maxX, Math.max(l.getStartX(), l.getEndX()));
                    minY = Math.min(minY, Math.min(l.getStartY(), l.getEndY()));
                    maxY = Math.max(maxY, Math.max(l.getStartY(), l.getEndY()));
                }
            }

            double contentWidth = maxX - minX;
            double contentHeight = maxY - minY;

            double offsetX = PANE_PADDING - minX;
            double offsetY = PANE_PADDING - minY;

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

}
