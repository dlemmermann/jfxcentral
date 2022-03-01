package com.dlsc.jfxcentral.views.detail;

import com.dlsc.jfxcentral.data.DataRepository;
import com.dlsc.jfxcentral.data.ImageManager;
import com.dlsc.jfxcentral.data.model.Blog;
import com.dlsc.jfxcentral.data.model.Company;
import com.dlsc.jfxcentral.data.model.Person;
import com.dlsc.jfxcentral.data.model.Post;
import com.dlsc.jfxcentral.panels.SectionPane;
import com.dlsc.jfxcentral.util.EmptySelectionModel;
import com.dlsc.jfxcentral.views.AdvancedListView;
import com.dlsc.jfxcentral.views.PhotoView;
import com.dlsc.jfxcentral.views.RootPane;
import com.dlsc.jfxcentral.views.View;
import com.dlsc.jfxcentral.views.detail.cells.DetailPostCell;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BlogsDetailView extends ModelObjectDetailView<Blog> {

    private VBox content = new VBox();

    private final ObservableList<Post> posts = FXCollections.observableArrayList();

    public BlogsDetailView(RootPane rootPane) {
        super(rootPane, View.BLOGS);

        getStyleClass().add("blogs-detail-view");

        VBox.setVgrow(content, Priority.ALWAYS);

        createTitleBox();
        createPostsBox();

        setContent(content);

        selectedItemProperty().addListener(it -> {
            posts.clear();

            if (loadPostsThread != null) {
                loadPostsThread.cancel();
            }

            Blog blog = getSelectedItem();
            if (blog != null) {
                loadPostsThread = new LoadPostsThread(blog);
                loadPostsThread.start();
            }
        });
    }

    private LoadPostsThread loadPostsThread;

    private final class LoadPostsThread extends Thread {
        private final Blog blog;
        private boolean running = true;

        public LoadPostsThread(Blog blog) {
            this.blog = blog;
            setName("Load Posts Thread");
            setDaemon(true);
        }

        @Override
        public void run() {
            if (running) {
                List<Post> result = DataRepository.getInstance().loadPosts(blog);
                Platform.runLater(() -> {
                    if (running) {
                        posts.setAll(result);
                    }
                });
            }
        }

        public void cancel() {
            running = false;
        }
    }

    protected boolean isUsingMasterView() {
        return true;
    }

    private void createPostsBox() {
        SectionPane sectionPane = new SectionPane();
        sectionPane.setTitle("Posts");
        sectionPane.subtitleProperty().bind(Bindings.createStringBinding(() -> getSelectedItem() != null ? "List of current posts on " + getSelectedItem().getName() : "", selectedItemProperty()));

        FilteredList<Post> filteredPosts = new FilteredList<>(posts);
        filteredPosts.predicateProperty().bind(Bindings.createObjectBinding(() -> post -> getSelectedItem() == null || post.getBlog().getId().equals(getSelectedItem().getId()), selectedItemProperty()));

        SortedList<Post> sortedPosts = new SortedList<>(filteredPosts);
        sortedPosts.setComparator(Comparator.comparing(Post::getDate).reversed());

        AdvancedListView<Post> listView = new AdvancedListView<>();
        Label placeholder = new Label("Loading posts ...");
        placeholder.getStyleClass().add("placeholder");
        listView.setPlaceholder(placeholder);
        VBox.setVgrow(listView, Priority.ALWAYS);

        listView.getListView().setSelectionModel(new EmptySelectionModel<>());
        listView.setPaging(true);
        listView.setVisibleRowCount(20);

        listView.setCellFactory(view -> {
            DetailPostCell cell = new DetailPostCell(getRootPane());
            cell.blogProperty().bind(selectedItemProperty());
            return cell;
        });

        listView.setItems(sortedPosts);

        sectionPane.getNodes().add(listView);

        VBox.setVgrow(sectionPane, Priority.ALWAYS);

        content.getChildren().add(sectionPane);
    }

    @Override
    protected void createTitleBox() {
        PhotoView photoView = new PhotoView();
        photoView.visibleProperty().bind(photoView.photoProperty().isNotNull());
        photoView.managedProperty().bind(photoView.photoProperty().isNotNull());
        photoView.setEditable(false);
        selectedItemProperty().addListener(it -> {
            Blog blog = getSelectedItem();
            photoView.photoProperty().unbind();
            photoView.setPhoto(null);
            if (blog != null) {
                String companyId = blog.getCompanyId();
                if (StringUtils.isNotBlank(companyId)) {
                    Optional<Company> companyById = DataRepository.getInstance().getCompanyById(companyId);
                    if (companyById.isPresent()) {
                        photoView.photoProperty().bind(ImageManager.getInstance().companyImageProperty(companyById.get()));
                    }
                } else {
                    List<String> personIds = blog.getPersonIds();
                    if (!personIds.isEmpty()) {
                        Optional<Person> personById = DataRepository.getInstance().getPersonById(personIds.get(0));
                        if (personById.isPresent()) {
                            photoView.photoProperty().bind(ImageManager.getInstance().personImageProperty(personById.get()));
                        }
                    }
                }
            }
        });

        Label nameLabel = new Label();
        nameLabel.getStyleClass().addAll("header1", "title-label");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.textProperty().bind(Bindings.createStringBinding(() -> getSelectedItem() != null ? getSelectedItem().getName() : "", selectedItemProperty()));
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label descriptionLabel = new Label();
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);
        descriptionLabel.getStyleClass().add("subtitle-label");
        descriptionLabel.textProperty().bind(Bindings.createStringBinding(() -> getSelectedItem() != null ? getSelectedItem().getSummary() : "", selectedItemProperty()));
        HBox.setHgrow(descriptionLabel, Priority.ALWAYS);

        HBox linksBox = new HBox();
        linksBox.getStyleClass().add("social-box");

        VBox vBox = new VBox(nameLabel, descriptionLabel, linksBox);
        vBox.getStyleClass().add("vbox");
        vBox.setFillWidth(true);
        HBox.setHgrow(vBox, Priority.ALWAYS);

        HBox titleBox = new HBox(vBox, photoView);
        titleBox.getStyleClass().add("hbox");

        SectionPane sectionPane = new SectionPane(titleBox);
        sectionPane.getStyleClass().add("title-section");

        sectionPane.visibleProperty().bind(selectedItemProperty().isNotNull());
        sectionPane.managedProperty().bind(selectedItemProperty().isNotNull());

        content.getChildren().addAll(sectionPane);
    }
}
