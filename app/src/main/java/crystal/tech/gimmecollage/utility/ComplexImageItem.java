package crystal.tech.gimmecollage.utility;

import java.util.List;

/**
 * Created by Dmitry on 05.02.2015.
 */
public class ComplexImageItem {
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    private String image;
    private String thumbnail;
    private long id;
    private boolean selected;

    public ComplexImageItem() {}

    public void toggle() {
        this.selected = !this.selected;
    }

    public static String[] GenerateImagesArray(List<ComplexImageItem> list) {
        String[] array = new String[list.size()];
        for(int i = 0; i < list.size(); i++) {
            array[i] = list.get(i).image;
        }
        return array;
    }

    public static void RestoreImagesFromArray(List<ComplexImageItem> list, String[] array) {
        for(int i = 0; i < array.length; i++) {
            list.get(i).setImage(array[i]);
        }
    }

    public static String[] GenerateThumbnailsArray(List<ComplexImageItem> list) {
        String[] array = new String[list.size()];
        for(int i = 0; i < list.size(); i++) {
            array[i] = list.get(i).thumbnail;
        }
        return array;
    }

    public static void RestoreThumbnailsFromArray(List<ComplexImageItem> list, String[] array) {
        for(int i = 0; i < array.length; i++) {
            list.get(i).setThumbnail(array[i]);
        }
    }

    public static boolean[] GenerateSelectedArray(List<ComplexImageItem> list) {
        boolean[] array = new boolean[list.size()];
        for(int i = 0; i < list.size(); i++) {
            array[i] = list.get(i).selected;
        }
        return array;
    }

    public static void RestoreSelectedFromArray(List<ComplexImageItem> list, boolean[] array) {
        for(int i = 0; i < array.length; i++) {
            list.get(i).setSelected(array[i]);
        }
    }

}
