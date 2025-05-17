package com.archy.geneus;

import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public final class FamilyTreeRW {

    public static List<Person> loadFamilyTree(String filePath) throws Exception {

        List<Person> people = new ArrayList<>();

        var dbf = DocumentBuilderFactory.newInstance();
        var db = dbf.newDocumentBuilder();
        var doc = db.parse(new File(filePath));
        doc.getDocumentElement().normalize();

        var personsWrap = (Element) doc.getElementsByTagName("persons").item(0);
        var persons = personsWrap.getElementsByTagName("person");

        for (int i = 0; i < persons.getLength(); i++) {

            var personEl = (Element) persons.item(i);
            String id = personEl.getAttribute("id");
            String name = personEl.getAttribute("name");
            String birthDateStr = personEl.getAttribute("birthDate");
            String deathDateStr = personEl.getAttribute("deathDate");

            Person person = new Person(id, name);
            if (!birthDateStr.isEmpty()) person.setBirthDate(LocalDate.parse(birthDateStr));
            if (!deathDateStr.isEmpty()) person.setDeathDate(LocalDate.parse(deathDateStr));


            people.add(person);
        }

        var marriagesWrap = (Element) doc.getElementsByTagName("marriages").item(0);
        var marriages = marriagesWrap.getElementsByTagName("marriage");

        for (int i = 0; i < marriages.getLength(); i++) {

            var marriageEl = (Element) marriages.item(i);
            var spouse1Id = marriageEl.getAttribute("spouse1");
            var spouse2Id = marriageEl.getAttribute("spouse2");

            var spouse1 = Person.getPersonByID(people, spouse1Id);
            var spouse2 = Person.getPersonByID(people, spouse2Id);

            if (spouse1 != null && spouse2 != null) {

                var marriage1 = new Marriage(spouse2);
                var marriage2 = new Marriage(spouse1);

                marriage1.setStartDate(LocalDate.parse(marriageEl.getAttribute("startDate")));
                marriage2.setStartDate(LocalDate.parse(marriageEl.getAttribute("startDate")));
                marriage1.setStartArea(marriageEl.getAttribute("startArea"));
                marriage2.setStartArea(marriageEl.getAttribute("startArea"));
                marriage1.setStartCountry(marriageEl.getAttribute("startCountry"));
                marriage2.setStartCountry(marriageEl.getAttribute("startCountry"));
                marriage1.setEndDate(LocalDate.parse(marriageEl.getAttribute("endDate")));
                marriage2.setEndDate(LocalDate.parse(marriageEl.getAttribute("endDate")));
                marriage1.setEndArea(marriageEl.getAttribute("endArea"));
                marriage2.setEndArea(marriageEl.getAttribute("endArea"));
                marriage1.setEndCountry(marriageEl.getAttribute("endCountry"));
                marriage2.setEndCountry(marriageEl.getAttribute("endCountry"));

                spouse1.addMarriage(marriage1);
                spouse2.addMarriage(marriage2);

            }
        }

        for (int i = 0; i < persons.getLength(); i++) {

            var personEl = (Element) persons.item(i);
            var id = personEl.getAttribute("id");
            var person = Person.getPersonByID(people, id);

            if (person != null) {

                var children = personEl.getElementsByTagName("child");

                for (int j = 0; j < children.getLength(); j++) {

                    var childEl = (Element) children.item(j);

                    String childId = childEl.getAttribute("id");
                    var child = Person.getPersonByID(people, childId);

                    if (child != null)
                        person.addChild(child);

                }
            }
        }

        for (var person : people) {
            if (person.getParent1() != null)
                person.getParent1().addChild(person);

            if (person.getParent2() != null)
                person.getParent2().addChild(person);

        }

        return people;
    }

    public static void saveFamilyTree(List<Person> people, String filePath) throws Exception {

        var dbf = DocumentBuilderFactory.newInstance();
        var db = dbf.newDocumentBuilder();
        var doc = db.newDocument();

        var root = doc.createElement("familyTree");
        doc.appendChild(root);

        var personsWrap = doc.createElement("persons");
        root.appendChild(personsWrap);

        List<Element> marriageElements = new ArrayList<>();
        Set<String> seenMarriages = new HashSet<>();

        for (var person : people) {
            var personEl = doc.createElement("person");
            personEl.setAttribute("id", person.getId());

            if (person.getDisplayName() != null) {
                personEl.setAttribute("name", person.getDisplayName());
            }

            if (person.getBirthDate() != null) {
                personEl.setAttribute("birthDate", person.getBirthDate().toString());
            }

            if (person.getDeathDate() != null) {
                personEl.setAttribute("deathDate", person.getDeathDate().toString());
            }

            if (person.getParent1() != null) {
                personEl.setAttribute("parent1", person.getParent1().getId());
            }


            if (person.getParent2() != null) {
                personEl.setAttribute("parent2", person.getParent2().getId());
            }

            for (var child : new LinkedHashSet<>(person.getDescendants())) {
                Element childEl = doc.createElement("child");
                childEl.setAttribute("id", child.getId());
                personEl.appendChild(childEl);
            }

            personsWrap.appendChild(personEl);

            for (var m : person.getMarriages()) {

                String id1 = person.getId();
                String id2 = m.getSpouse().getId();
                String key = id1.compareTo(id2) < 0 ? id1 + '-' + id2 : id2 + '-' + id1;

                if (!seenMarriages.contains(key)) {

                    var marriageEl = doc.createElement("marriage");
                    marriageEl.setAttribute("spouse1", id1);
                    marriageEl.setAttribute("spouse2", id2);

                    if (m.getStartDate() != null)
                        marriageEl.setAttribute("startDate", m.getStartDate().toString());

                    if (m.getStartArea() != null)
                        marriageEl.setAttribute("startArea", m.getStartArea());

                    if (m.getStartCountry() != null)
                        marriageEl.setAttribute("startCountry", m.getStartCountry());

                    if (m.getEndDate() != null)
                        marriageEl.setAttribute("endDate", m.getEndDate().toString());

                    if (m.getEndArea() != null)
                        marriageEl.setAttribute("endArea", m.getEndArea());

                    if (m.getEndCountry() != null)
                        marriageEl.setAttribute("endCountry", m.getEndCountry());

                    marriageElements.add(marriageEl);
                    seenMarriages.add(key);
                }
            }
        }

        var marriageWrap = doc.createElement("marriages");
        root.appendChild(marriageWrap);
        for (Element marriageEl : marriageElements)
            marriageWrap.appendChild(marriageEl);


        var tf = TransformerFactory.newInstance();
        var transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        var source = new DOMSource(doc);
        var result = new StreamResult(new File(filePath));
        transformer.transform(source, result);

        System.out.println("Family tree saved to " + filePath);
    }
}
