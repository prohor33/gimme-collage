package crystal.tech.gimmecollage.utility;

import java.util.List;

/**
 * Created by Dmitry on 05.02.2015.
 */
public class ComplexImageItem {
    private String mImage;
    private String mThumbnail;
    private boolean mSelected = false;

    public ComplexImageItem() {}

    public void setImage(String image) { mImage = image; }
    public void setThumbnail(String thumbnail) { mThumbnail = thumbnail; }
    public void setSelected(boolean selected) { mSelected = selected; }

    public String getImage() { return mImage; }
    public String getThumbnail() { return mThumbnail; }
    public boolean getSelected() { return mSelected; }

    public void toggle() {
        mSelected = !mSelected;
    }

    public static String[] GenerateImagesArray(List<ComplexImageItem> list) {
        String[] array = new String[list.size()];
        for(int i = 0; i < list.size(); i++) {
            array[i] = list.get(i).mImage;
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
            array[i] = list.get(i).mThumbnail;
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
            array[i] = list.get(i).mSelected;
        }
        return array;
    }

    public static void RestoreSelectedFromArray(List<ComplexImageItem> list, boolean[] array) {
        for(int i = 0; i < array.length; i++) {
            list.get(i).setSelected(array[i]);
        }
    }

}
