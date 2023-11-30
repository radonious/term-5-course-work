module com.course.term5cw {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    exports com.course.term5cw.Controller;
    opens com.course.term5cw.Controller to javafx.fxml;

    exports com.course.term5cw.View;
}