package com.archy.geneus;

import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GenealogyApp extends javafx.application.Application {


    protected static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    private List<Person> people = new ArrayList<>();


    @Override
    public void start(Stage primaryStage) {

        var person1 = new Person("1", "John Doe");
        person1.setBirthDate(LocalDate.of(1980, 1, 1));

        var spouse = new Person("2", "Jane Doe");
        spouse.setBirthDate(LocalDate.of(1985, 5, 15));

        person1.addMarriage(new Marriage(spouse));
        spouse.addMarriage(new Marriage(person1));

        person1.getMarriages().get(0).setStartDate(LocalDate.of(2000, 6, 20));
        person1.getMarriages().get(0).setStartArea("New York");
        person1.getMarriages().get(0).setStartCountry("USA");
        spouse.getMarriages().get(0).setStartDate(LocalDate.of(2000, 6, 20));
        spouse.getMarriages().get(0).setStartArea("New York");
        spouse.getMarriages().get(0).setStartCountry("USA");

        person1.getMarriages().get(0).setEndDate(LocalDate.of(2010, 6, 20));
        person1.getMarriages().get(0).setEndArea("Los Angeles");
        person1.getMarriages().get(0).setEndCountry("USA");
        spouse.getMarriages().get(0).setEndDate(LocalDate.of(2010, 6, 20));
        spouse.getMarriages().get(0).setEndArea("Los Angeles");
        spouse.getMarriages().get(0).setEndCountry("USA");


        var child1 = new Person("3", "Alice Doe");
        child1.setBirthDate(LocalDate.of(2005, 3, 10));
        child1.setParent1(person1);
        child1.setParent2(spouse);
        person1.addChild(child1);
        spouse.addChild(child1);

        var child2 = new Person("4", "Bob Doe");
        child2.setBirthDate(LocalDate.of(2008, 7, 20));
        child2.setParent1(person1);
        child2.setParent2(spouse);
        person1.addChild(child2);
        spouse.addChild(child2);


        var child3 = new Person("5", "Charlie Doe");
        child3.setBirthDate(LocalDate.of(2010, 11, 30));
        child3.setParent1(person1);
        child3.setParent2(spouse);
        person1.addChild(child3);
        spouse.addChild(child3);



        var mum = new Person("6", "Mary Doe");
        mum.setBirthDate(LocalDate.of(1950, 2, 2));
        var dad = new Person("7", "Robert Doe");
        dad.setBirthDate(LocalDate.of(1948, 3, 3));
        person1.setParent1(mum);
        person1.setParent2(dad);
        mum.addChild(person1);
        dad.addChild(person1);

        var bro = new Person("8", "David Doe");
        bro.setBirthDate(LocalDate.of(1975, 4, 4));
        bro.setParent1(mum);
        bro.setParent2(dad);
        mum.addChild(bro);
        dad.addChild(bro);

        people.add(person1);
        people.add(spouse);
        people.add(child1);
        people.add(child2);
        people.add(child3);
        people.add(mum);
        people.add(dad);
        people.add(bro);


        Pane tree = new TreeBuilder(person1).build();
        ScrollPane scrollPane = new ScrollPane(tree);
        scrollPane.setPannable(true);

        Scene scene = new Scene(scrollPane, 1000, 700);

        FamilyTreeRW.saveFamilyTree(people, "family_tree.xml");

        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("Geneus - Orthogonal View");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}