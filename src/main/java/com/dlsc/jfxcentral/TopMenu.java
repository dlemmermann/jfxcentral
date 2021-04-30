package com.dlsc.jfxcentral;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.material.Material;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

class TopMenu extends VBox {

    public TopMenu(RootPane rootPane) {
        getStyleClass().add("top-menu");

        setAlignment(Pos.TOP_CENTER);
        setFillWidth(true);

        ImageView imageView = new ImageView();
        imageView.fitWidthProperty().bind(Bindings.createDoubleBinding(() -> isExpanded() ? 100d : 50d, expandedProperty()));
        imageView.setOnMouseClicked(evt -> setExpanded(!isExpanded()));
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("duke");

        ToggleButton homeButton = createButton("Home", new FontIcon(Material.HOME));
        ToggleButton peopleButton = createButton("People", new FontIcon(Material.PERSON));
        ToggleButton blogsButton = createButton("Blogs", new FontIcon(Material.DESCRIPTION));
        ToggleButton booksButton = createButton("Books", new FontIcon(Material.BOOK));
        ToggleButton tutorialsButton = createButton("Tutorials", new FontIcon(Material.SCHOOL));
        ToggleButton libsButton = createButton("Libraries", new FontIcon(Material.BOOK));
        ToggleButton openJfxButton = createButton("OpenJFX", new FontIcon(Material.STAR));

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(homeButton, peopleButton, blogsButton, booksButton, tutorialsButton, libsButton, openJfxButton);
        toggleGroup.selectToggle(homeButton);

        Region spacer = new Region();
        spacer.getStyleClass().add("spacer");
        spacer.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(homeButton, peopleButton, blogsButton, booksButton, tutorialsButton, libsButton, openJfxButton);

        view.addListener(it -> {
            switch (getView()) {
                case HOME:
                    toggleGroup.selectToggle(homeButton);
                    break;
                case OPENJFX:
                    toggleGroup.selectToggle(openJfxButton);
                    break;
                case PEOPLE:
                    toggleGroup.selectToggle(peopleButton);
                    break;
                case LEARN:
                    toggleGroup.selectToggle(tutorialsButton);
                    break;
                case LIBS:
                    toggleGroup.selectToggle(libsButton);
                    break;
                case BLOGS:
                    toggleGroup.selectToggle(blogsButton);
                    break;
                case BOOKS:
                    toggleGroup.selectToggle(booksButton);
                    break;
            }
        });

        toggleGroup.selectedToggleProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                toggleGroup.selectToggle(oldSelection);
                return;
            }

            if (newSelection == homeButton) {
                setView(View.HOME);
            } else if (newSelection == openJfxButton) {
                setView(View.OPENJFX);
            } else if (newSelection == peopleButton) {
                setView(View.PEOPLE);
            } else if (newSelection == tutorialsButton) {
                setView(View.LEARN);
            } else if (newSelection == libsButton) {
                setView(View.LIBS);
            } else if (newSelection == blogsButton) {
                setView(View.BLOGS);
            } else if (newSelection == booksButton) {
                setView(View.BOOKS);
            }
        });

        expandedProperty().addListener(it -> updateExpandedPseudoClass());
        updateExpandedPseudoClass();
    }

    private void updateExpandedPseudoClass() {
        pseudoClassStateChanged(PseudoClass.getPseudoClass("expanded"), isExpanded());
    }

    private ToggleButton createButton(String name, FontIcon icon) {
        ToggleButton button = new ToggleButton(name);
        button.contentDisplayProperty().bind(Bindings.createObjectBinding(() -> isExpanded() ? ContentDisplay.LEFT : ContentDisplay.GRAPHIC_ONLY, expandedProperty()));
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setGraphic(icon);
        button.setOnAction(evt -> setExpanded(false));
        return button;
    }

    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded", true);

    public boolean isExpanded() {
        return expanded.get();
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
    }

    private ObjectProperty<View> view = new SimpleObjectProperty<>(this, "view", View.HOME);

    public View getView() {
        return view.get();
    }

    public ObjectProperty<View> viewProperty() {
        return view;
    }

    public void setView(View view) {
        this.view.set(view);
    }
}