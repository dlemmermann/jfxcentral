package com.dlsc.jfxcentral.views;

import com.dlsc.gemsfx.DialogPane;
import com.dlsc.gemsfx.FilterView;
import com.dlsc.jfxcentral.AdvancedListView;
import com.dlsc.jfxcentral.DataRepository;
import com.dlsc.jfxcentral.ImageManager;
import com.dlsc.jfxcentral.RootPane;
import com.dlsc.jfxcentral.model.Person;
import com.dlsc.jfxcentral.model.Video;
import com.dlsc.jfxcentral.panels.SectionPaneWithFilterView;
import com.dlsc.jfxcentral.util.Util;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

public class VideosView extends PageView {

    private final FilterView.FilterGroup<Video> typeGroup = new FilterView.FilterGroup<>("Type");
    private final FilterView.FilterGroup<Video> eventGroup = new FilterView.FilterGroup<>("Event");
    private final FilterView.FilterGroup<Video> speakerGroup = new FilterView.FilterGroup<>("Speaker");
    private final FilterView.FilterGroup<Video> platformGroup = new FilterView.FilterGroup<>("Platform");
    private final FilterView.FilterGroup<Video> domainGroup = new FilterView.FilterGroup<>("Domain");

    public VideosView(RootPane rootPane) {
        super(rootPane);

        SectionPaneWithFilterView sectionPane = new SectionPaneWithFilterView();
        sectionPane.setTitle("Videos");
        sectionPane.setEnableAutoSubtitle(true);

        FilterView<Video> filterView = sectionPane.getFilterView();
        filterView.setItems(DataRepository.getInstance().videosProperty());
        filterView.getFilterGroups().setAll(typeGroup, eventGroup, speakerGroup, platformGroup, domainGroup);
        filterView.setTextFilterProvider(text -> video -> {
            if (video.getTitle().toLowerCase().contains(text)) {
                return true;
            }
            if (video.getDescription().toLowerCase().contains(text)) {
                return true;
            }
            return false;
        });

        AdvancedListView<Video> listView = new AdvancedListView<>();
        listView.setMaxHeight(Double.MAX_VALUE);
        listView.setMinWidth(Region.USE_PREF_SIZE);
        listView.setCellFactory(view -> new VideoCell());
        listView.itemsProperty().bind(filterView.filteredItemsProperty());
        listView.getSelectionModel().selectedItemProperty().addListener(it -> setVideo(listView.getSelectionModel().getSelectedItem()));
        VBox.setVgrow(listView, Priority.ALWAYS);
        sectionPane.getNodes().add(listView);

        setContent(sectionPane);

        DataRepository.getInstance().videosProperty().addListener((Observable it) -> updateFilters());

        updateFilters();
    }

    private void updateFilters() {
        eventGroup.getFilters().clear();
        typeGroup.getFilters().clear();
        domainGroup.getFilters().clear();
        speakerGroup.getFilters().clear();
        platformGroup.getFilters().clear();

        updateEventGroup();
        updateDomainGroup();
        updateTypeGroup();
        updatePlatformGroup();
        updateSpeakersGroup();
    }

    private void updateSpeakersGroup() {
        List<String> speakersList = new ArrayList<>();

        DataRepository.getInstance().getVideos().forEach(video -> {
            List<String> personIds = video.getPersonIds();
            for (String id : personIds) {
                if (!speakersList.contains(id.trim())) {
                    speakersList.add(id.trim());
                }
            }
        });

        speakersList.forEach(item -> {
            Optional<Person> personById = DataRepository.getInstance().getPersonById(item);
            if (personById.isPresent()) {
                speakerGroup.getFilters().add(new FilterView.Filter<>(personById.get().getName()) {
                    @Override
                    public boolean test(Video video) {
                        return video.getPersonIds().contains(item);
                    }
                });
            }
        });
    }

    private void updateEventGroup() {
        List<String> itemList = new ArrayList<>();
        DataRepository.getInstance().getVideos().forEach(video -> {
            String event = video.getEvent();
            if (StringUtils.isNotBlank(event)) {
                StringTokenizer st = new StringTokenizer(event, ",");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken().trim();
                    if (!itemList.contains(token)) {
                        itemList.add(token);
                    }
                }
            }
        });

        itemList.forEach(item -> eventGroup.getFilters().add(new FilterView.Filter<>(item) {
            @Override
            public boolean test(Video video) {
                return StringUtils.containsIgnoreCase(video.getEvent(), item);
            }
        }));
    }

    private void updateDomainGroup() {
        List<String> itemList = new ArrayList<>();
        DataRepository.getInstance().getVideos().forEach(video -> {
            String domain = video.getDomain();
            if (StringUtils.isNotBlank(domain)) {
                StringTokenizer st = new StringTokenizer(domain, ",");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken().trim();
                    if (!itemList.contains(token)) {
                        itemList.add(token);
                    }
                }
            }
        });

        itemList.forEach(item -> domainGroup.getFilters().add(new FilterView.Filter<>(item) {
            @Override
            public boolean test(Video video) {
                return StringUtils.containsIgnoreCase(video.getDomain(), item);
            }
        }));
    }

    private void updatePlatformGroup() {
        List<String> itemList = new ArrayList<>();
        DataRepository.getInstance().getVideos().forEach(video -> {
            String platform = video.getPlatform();
            if (StringUtils.isNotBlank(platform)) {
                StringTokenizer st = new StringTokenizer(platform, ",");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken().trim();
                    if (!itemList.contains(token)) {
                        itemList.add(token);
                    }
                }
            }
        });

        itemList.forEach(item -> platformGroup.getFilters().add(new FilterView.Filter<>(item) {
            @Override
            public boolean test(Video video) {
                return StringUtils.containsIgnoreCase(video.getPlatform(), item);
            }
        }));
    }

    private void updateTypeGroup() {
        List<String> itemList = new ArrayList<>();
        DataRepository.getInstance().getVideos().forEach(video -> {
            String type = video.getType();
            if (StringUtils.isNotBlank(type)) {
                StringTokenizer st = new StringTokenizer(type, ",");
                while (st.hasMoreTokens()) {
                    String token = st.nextToken().trim();
                    if (!itemList.contains(token)) {
                        itemList.add(token);
                    }
                }
            }
        });

        itemList.forEach(item -> typeGroup.getFilters().add(new FilterView.Filter<>(item) {
            @Override
            public boolean test(Video video) {
                return StringUtils.containsIgnoreCase(video.getType(), item);
            }
        }));
    }

    private final ObjectProperty<Video> video = new SimpleObjectProperty<>(this, "video");

    public Video getVideo() {
        return video.get();
    }

    public ObjectProperty<Video> videoProperty() {
        return video;
    }

    public void setVideo(Video video) {
        this.video.set(video);
    }

    private void showVideo(Video video) {
        WebView webView = new WebView();
        webView.getEngine().load("https://www.youtube.com/embed/" + video.getId());
        getRootPane().getDialogPane().showNode(DialogPane.Type.BLANK, video.getTitle(), webView, true);
        webView.sceneProperty().addListener(it -> {
            if (webView.getScene() == null) {
                System.out.println("Unloading");
                webView.getEngine().loadContent("empty");
            }
        });
    }

    class VideoCell extends ListCell<Video> {

        private final Label titleLabel = new Label();
        private final Label descriptionLabel = new Label();
        private final ImageView thumbnailView = new ImageView();
        private final Button playButton = new Button("Play");
        private final Button playOnYouTubeButton = new Button("YouTube");

        public VideoCell() {
            getStyleClass().add("video-cell");

            playButton.setGraphic(new FontIcon(MaterialDesign.MDI_PLAY));
            playButton.setOnAction(evt -> showVideo(getItem()));

            playOnYouTubeButton.setGraphic(new FontIcon(MaterialDesign.MDI_YOUTUBE_PLAY));
            playOnYouTubeButton.setOnAction(evt -> Util.browse("https://youtu.be/" + getItem().getId()));

            titleLabel.getStyleClass().add("title-label");
            titleLabel.setWrapText(true);
            titleLabel.setMinHeight(Region.USE_PREF_SIZE);

            descriptionLabel.getStyleClass().add("description-label");
            descriptionLabel.setWrapText(true);
            descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);

            thumbnailView.setPreserveRatio(true);
            thumbnailView.setFitWidth(320);

            StackPane coverImageWrapper = new StackPane(thumbnailView);
            StackPane.setAlignment(thumbnailView, Pos.TOP_LEFT);

            HBox buttonBox = new HBox(10, playButton, playOnYouTubeButton);

            GridPane.setRowSpan(coverImageWrapper, 3);

            GridPane gridPane = new GridPane();
            gridPane.getStyleClass().add("grid-pane");
            gridPane.setPrefWidth(0);
            gridPane.setMinHeight(Region.USE_PREF_SIZE);
            gridPane.setMinSize(0, 0);
            gridPane.add(coverImageWrapper, 0, 0);
            gridPane.add(titleLabel, 1, 0);
            gridPane.add(descriptionLabel, 1, 1);
            gridPane.add(buttonBox, 1, 2);

            RowConstraints row1 = new RowConstraints();
            RowConstraints row2 = new RowConstraints();
            RowConstraints row3 = new RowConstraints();

            row1.setValignment(VPos.TOP);
            row2.setValignment(VPos.TOP);
            row3.setValignment(VPos.BOTTOM);

            gridPane.getRowConstraints().setAll(row1, row2);

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(gridPane);

            setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2) {
                    showVideo(getItem());
                }
            });
        }

        @Override
        protected void updateItem(Video video, boolean empty) {
            super.updateItem(video, empty);

            if (!empty && video != null) {
                titleLabel.setText(video.getTitle());
                descriptionLabel.setText(video.getDescription());
                thumbnailView.setVisible(true);
                thumbnailView.setManaged(true);
                thumbnailView.imageProperty().bind(ImageManager.getInstance().youTubeImageProperty(video));
            } else {
                titleLabel.setText("");
                descriptionLabel.setText("");
                thumbnailView.imageProperty().unbind();
                thumbnailView.setVisible(false);
                thumbnailView.setManaged(false);
            }
        }
    }
}