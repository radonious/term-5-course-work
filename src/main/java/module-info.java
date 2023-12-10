module com.course.term5cw {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    requires com.fasterxml.jackson.databind;

    requires io.github.javadiffutils;

    opens com.course.term5cw.Controller to javafx.fxml;

    exports com.course.term5cw.Controller;
    exports com.course.term5cw.View;
    exports com.course.term5cw.Model;
    exports com.course.term5cw.Common;
}