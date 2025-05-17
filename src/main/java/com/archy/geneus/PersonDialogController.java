package com.archy.geneus;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.List;

public class PersonDialogController {

    @FXML private TextField nameField;
    @FXML private DatePicker birthDatePicker, deathDatePicker;
    @FXML private ComboBox<Person> parent1Combo, parent2Combo;

    private List<Person> allPeople;
    private Person editingPerson;

    public void setData(List<Person> allPeople, Person editingPerson) {

        this.allPeople = allPeople;
        this.editingPerson = editingPerson;

        parent1Combo.getItems().clear();
        parent2Combo.getItems().clear();

        parent1Combo.getItems().add(null);
        parent2Combo.getItems().add(null);

        for (Person p : allPeople) {
            if (editingPerson == null || !p.equals(editingPerson)) {
                parent1Combo.getItems().add(p);
                parent2Combo.getItems().add(p);
            }
        }

        Callback<ListView<Person>, ListCell<Person>> personCellFactory = lv -> new ListCell<>() {
            @Override
            protected void updateItem(Person person, boolean empty) {
                super.updateItem(person, empty);
                if (empty || person == null) {
                    setText("(none)");
                } else {
                    String birth = person.getBirthDate() != null ?
                            String.valueOf(person.getBirthDate().getYear()) : "?";
                    setText(person.getDisplayName() + " (" + birth + ")");
                }
            }
        };

        parent1Combo.setCellFactory(personCellFactory);
        parent1Combo.setButtonCell(personCellFactory.call(null));
        parent2Combo.setCellFactory(personCellFactory);
        parent2Combo.setButtonCell(personCellFactory.call(null));

        if (editingPerson != null) {
            nameField.setText(editingPerson.getDisplayName());
            birthDatePicker.setValue(editingPerson.getBirthDate());
            deathDatePicker.setValue(editingPerson.getDeathDate());
            parent1Combo.setValue(editingPerson.getParent1());
            parent2Combo.setValue(editingPerson.getParent2());
        }
    }


    public Person getResult() {

        String name = nameField.getText().trim();
        LocalDate birth = birthDatePicker.getValue();
        LocalDate death = deathDatePicker.getValue();
        Person p1 = parent1Combo.getValue();
        Person p2 = parent2Combo.getValue();

        if (name.isEmpty()) return null;

        if (birth != null && death != null && death.isBefore(birth)) {
            showError("Death before birth!");
            return null;
        }

        if (p1 != null && p2 != null && p1.equals(p2)) {
            showError("Parents must be different!");
            return null;
        }

        Person result = editingPerson == null
                ? new Person(generateUniqueId(allPeople), name)
                : editingPerson;

        result.setBirthDate(birth);
        result.setDeathDate(death);
        result.setParent1(p1);
        result.setParent2(p2);
        return result;
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private String generateUniqueId(List<Person> allPeople) {
        int max = allPeople.stream().mapToInt(p -> {
            try { return Integer.parseInt(p.getId()); }
            catch (Exception e) { return 0; }
        }).max().orElse(0);
        return String.valueOf(max + 1);
    }
}
