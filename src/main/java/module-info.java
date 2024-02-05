module com.oxuegen.petrinet {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
//    requires validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;

    opens com.oxuegen.petrinet to javafx.fxml;
    exports com.oxuegen.petrinet;
}