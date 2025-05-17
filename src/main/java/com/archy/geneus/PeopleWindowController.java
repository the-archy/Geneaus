package com.archy.geneus;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PeopleWindowController {

    @FXML private TableView<Person> peopleTable;
    @FXML private TableColumn<Person, String> idCol, nameCol, birthCol, deathCol, parent1Col, parent2Col;
    @FXML private ScrollPane treeScroll;
    @FXML private MenuItem loadMenuItem;
    @FXML private MenuItem saveMenuItem;

    private ObservableList<Person> people;
    private Person rootPerson;

    @FXML
    public void initialize() {
        loadMenuItem.setOnAction(e -> onLoad());
        saveMenuItem.setOnAction(e -> onSave());
        idCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getId()));
        nameCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDisplayName()));
        birthCol.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getBirthDate() != null ?
            cd.getValue().getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""
        ));
        deathCol.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getDeathDate() != null ?
            cd.getValue().getDeathDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""
        ));
        parent1Col.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getParent1() != null ?
            cd.getValue().getParent1().getDisplayName() : ""
        ));
        parent2Col.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getParent2() != null ?
            cd.getValue().getParent2().getDisplayName() : ""
        ));


        try {
            people = FXCollections.observableArrayList(
                FamilyTreeRW.loadFamilyTree("family_tree.xml")
            );

        } catch (Exception e) {
            e.printStackTrace();
            people = FXCollections.observableArrayList();
        }
        peopleTable.setItems(people);

        // Default root (first person)
        if (!people.isEmpty()) {
            rootPerson = people.get(0);
            peopleTable.getSelectionModel().select(rootPerson);
        }

        peopleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            rootPerson = newVal;
            redrawTree();
        });

        redrawTree();
    }

    private void redrawTree() {
        if (rootPerson != null) {
            Pane tree = new TreeBuilder(rootPerson).build();
            treeScroll.setContent(tree);
        }
    }

    private Person showPersonDialog(Person toEdit) {
        try {
            var loader = new FXMLLoader(getClass().getResource("/PersonDialog.fxml"));
            DialogPane dialogPane = loader.load();
            PersonDialogController controller = loader.getController();
            controller.setData(people, toEdit);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(toEdit == null ? "Add Person" : "Edit Person");
            dialog.initOwner(peopleTable.getScene().getWindow());

            ButtonType okButton = dialogPane.getButtonTypes().stream()
                .filter(b -> b.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                .findFirst().orElse(null);

            dialog.setResultConverter(bt -> bt);
            var result = dialog.showAndWait();

            if (result.isPresent() && result.get() == okButton)
                return controller.getResult();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    @FXML
    private void onAdd() {

        Person newPerson = showPersonDialog(null);

        if (newPerson != null) {
            people.add(newPerson);
            peopleTable.getSelectionModel().select(newPerson);
            saveAndRedraw();
        }
    }

    @FXML
    private void onEdit() {

        Person selected = peopleTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            Person updated = showPersonDialog(selected);
            if (updated != null) {
                peopleTable.refresh();
                saveAndRedraw();
            }
        }
    }

    @FXML
    private void onDelete() {

        Person selected = peopleTable.getSelectionModel().getSelectedItem();

        if (selected != null) {

            var alert = new Alert(Alert.AlertType.CONFIRMATION);

            alert.setTitle("Delete Person");
            alert.setHeaderText("Do you really want to delete this person?");
            alert.setContentText("Name: " + selected.getDisplayName());
            alert.initOwner(peopleTable.getScene().getWindow());

            var result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                removePersonEverywhere(selected);
                people.remove(selected);
                saveAndRedraw();
            }
        }
    }

    private void removePersonEverywhere(Person p) {

        if (p.getParent1() != null)
            p.getParent1().getDescendants().remove(p);

        if (p.getParent2() != null)
            p.getParent2().getDescendants().remove(p);


        for (Person other : people) {

            if (other.getParent1() != null && other.getParent1().equals(p))
                other.setParent1(null);

            if (other.getParent2() != null && other.getParent2().equals(p))
                other.setParent2(null);


            other.getDescendants().remove(p);

            List<Marriage> toRemove = new ArrayList<>();

            for (Marriage m : other.getMarriages())
                if (m.getSpouse().equals(p))
                    toRemove.add(m);

            other.getMarriages().removeAll(toRemove);
        }

        p.setParent1(null);
        p.setParent2(null);
    }



    private void onLoad() {

        var fc = new FileChooser();
        fc.setTitle("Open Family Tree File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));
        File file = fc.showOpenDialog(peopleTable.getScene().getWindow());

        if (file != null) {
            try {
                people = FXCollections.observableArrayList(FamilyTreeRW.loadFamilyTree(file.getAbsolutePath()));

                peopleTable.setItems(people);

                if (!people.isEmpty()) {
                    rootPerson = people.get(0);
                    peopleTable.getSelectionModel().select(rootPerson);
                }

                redrawTree();
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Failed to load file:\n" + ex.getMessage());
            }
        }
    }

    private void onSave() {

        var fc = new FileChooser();

        fc.setTitle("Save Family Tree File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML Files", "*.xml"));

        File file = fc.showSaveDialog(peopleTable.getScene().getWindow());

        if (file != null) {
            try {
                FamilyTreeRW.saveFamilyTree(people, file.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Failed to save file:\n" + ex.getMessage());
            }
        }
    }


    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private void saveAndRedraw() {
        try {
            FamilyTreeRW.saveFamilyTree(people, "family_tree.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
        redrawTree();
    }

    @FXML
    private void onExit() {
        var alert = new Alert(Alert.AlertType.CONFIRMATION, "Really exit Geneus?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Exit");
        alert.setTitle("Exit");
        alert.initOwner(peopleTable.getScene().getWindow());
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            var stage = (Stage) peopleTable.getScene().getWindow();
            stage.close();
        }
    }

}
