module com.example.geneus {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;

    requires transitive javafx.graphics;


    opens com.archy.geneus to javafx.fxml;
    exports com.archy.geneus;
}