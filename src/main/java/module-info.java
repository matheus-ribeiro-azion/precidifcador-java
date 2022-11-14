module com.precificador.precificador {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;
    requires java.net.http;

    opens com.precificador.precificador to javafx.fxml;
    exports com.precificador.precificador;
}