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
    private LocalDate deathDate;

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

    public void setDeathDate(LocalDate dd) { this.deathDate = dd; }
    public LocalDate getDeathDate() { return deathDate; }

    public List<Person> getDescendants() { return descendants; }

    public List<Person> getParents() {
        return Stream.of(parent1, parent2)
                     .filter(Objects::nonNull)
                     .collect(Collectors.toList());
    }

    public void addMarriage(Marriage m) { marriages.add(m); }

    public List<Marriage> getMarriages() { return marriages; }

    public Marriage getMarriageWith(Person partner) {
        return marriages.stream()
                        .filter(m -> m.getSpouse().equals(partner))
                        .findFirst()
                        .orElse(null);
    }

    public List<Person> getPartners() {
        return marriages.stream()
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
            ).distinct().toList();
    }

    public static Person getPersonByID(List<Person> people, String id) {
        return people.stream()
                     .filter(p -> p.getId().equals(id))
                     .findFirst()
                     .orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        return id.equals(((Person) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Person{" +
            "id='" + id + '\'' +
            ", displayName='" + displayName + '\'' +
            ", birthDate=" + birthDate +
            ", deathDate=" + deathDate +
            ", parent1=" + (parent1 != null ? parent1.getDisplayName() + " (ID: " + parent1.getId() + ")" : null) +
            ", parent2=" + (parent2 != null ? parent2.getDisplayName() + " (ID: " + parent2.getId() + ")" : null) +
            ", marriages=" + marriages.stream()
            .map(m -> m.getSpouse().getDisplayName() + " (ID: " + m.getSpouse().getId() + ")")
            .collect(Collectors.joining(", ", "[", "]")) +
            ", descendants=" + descendants.stream()
            .map(d -> d.getDisplayName() + " (ID: " + d.getId() + ")")
            .collect(Collectors.joining(", ", "[", "]")) +
            '}';
    }
}
