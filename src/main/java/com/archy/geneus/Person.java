package com.archy.geneus;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Person implements Comparable<Person> {

    @Override
    public int compareTo(Person o) {
        return Comparator.comparing(Person::getDisplayName)
                         .thenComparing(Person::getBirthDate)
                         .compare(this, o);
    }

    private String id;
    private String displayName;

    private LocalDate birthDate;

    private Person parent1, parent2;

    private List<Person> descendants = new ArrayList<>();
    private List<Marriage> marriages = new ArrayList<>();

    public Person(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId()           { return id;           }
    public String getDisplayName()  { return displayName;  }
    public LocalDate getBirthDate() { return birthDate;    }
    public void setBirthDate(LocalDate bd) { this.birthDate = bd; }

    public void setParent1(Person p) { this.parent1 = p; }
    public void setParent2(Person p) { this.parent2 = p; }

    public Person getParent1() { return parent1; }
    public Person getParent2() { return parent2; }

    public void addChild(Person child) {
        descendants.add(child);
        if (child.parent1 == null) child.parent1 = this;
        else if (child.parent2 == null) child.parent2 = this;
    }

    public List<Person> getDescendants() { return Collections.unmodifiableList(descendants); }

    public List<Person> getParents() {
        return Stream.of(parent1, parent2)
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList());
    }

    public void addMarriage(Marriage m) { marriages.add(m); }

    public List<Marriage> getMarriages() { return Collections.unmodifiableList(marriages); }

    public Marriage getMarriageWith(Person partner) {
        return marriages.stream()
                        .filter(m -> m.getSpouse().equals(partner))
                        .findFirst()
                        .orElse(null);
    }

    public List<Person> getPartners() {
        return marriages.stream()
                        .filter(Marriage::isActive)
                        .map(Marriage::getSpouse)
                        .collect(Collectors.toList());
    }

    public List<Person> getSiblings() {
        return getParents().stream()
            .flatMap(p -> p.getDescendants().stream())
            .filter(c -> !c.equals(this))
            .distinct()
            .collect(Collectors.toList());
    }

    public List<Person> getSharedDescendantsWith(Person partner) {
        return getDescendants().stream()
            .filter(child ->
                (Objects.equals(child.getParent1(), this) && Objects.equals(child.getParent2(), partner)) ||
                (Objects.equals(child.getParent2(), this) && Objects.equals(child.getParent1(), partner))
            ).toList();
    }
}
