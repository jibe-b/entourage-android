package social.entourage.android.guide;

import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.List;

import social.entourage.android.R;
import social.entourage.android.api.model.map.Category;
import social.entourage.android.api.model.map.Poi;

public class PoiRenderer extends DefaultClusterRenderer<Poi> {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private List<Category> categories;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public PoiRenderer(Context context, GoogleMap map, ClusterManager<Poi> clusterManager) {
        super(context, map, clusterManager);
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @Override
    protected void onBeforeClusterItemRendered(Poi poi, MarkerOptions markerOptions) {
        CategoryType categoryType = categoryForCategoryId(poi.getCategoryId());
        BitmapDescriptor poiIcon = BitmapDescriptorFactory.fromResource(categoryType.getResourceId());
        markerOptions.icon(poiIcon);
    }

    CategoryType categoryForCategoryId(int categoryId) {
        if (categories != null) {
            for (Category category: categories) {
                if (category.getId() == categoryId) {
                    return CategoryType.findCategoryTypeByName(category.getName());
                }
            }
        }
        return CategoryType.OTHER;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public enum CategoryType {
        INSERTION("Se réinsérer", 1, R.drawable.poi_category_1, Color.parseColor("#97d791")),
        SELF_CARE("S'occuper de soi", 2,  R.drawable.poi_category_2, Color.parseColor("#88c0ff")),
        ORIENTATION("S'orienter", 3,  R.drawable.poi_category_3, Color.parseColor("#bfbfb9")),
        WATER("Se rafraîchir", 4, R.drawable.poi_category_4, Color.parseColor("#3ad7ff")),
        MEDICAL("Se soigner", 5, R.drawable.poi_category_5, Color.parseColor("#ff9999")),
        HOUSING("Se loger", 6, R.drawable.poi_category_6, Color.parseColor("#caa7ea")),
        FOOD("Se nourrir", 7, R.drawable.poi_category_7, Color.parseColor("#ffc57f")),
        OTHER("Other", 0, 0, Color.parseColor("#000000"));

        private final String name;
        private final int categoryId;
        private final int resourceId;
        private final int color;

        CategoryType(String name, int categoryId, int resourceId, int color) {
            this.name = name;
            this.categoryId = categoryId;
            this.resourceId = resourceId;
            this.color = color;
        }

        public int getResourceId() {
            return resourceId;
        }

        public int getColor() {
            return color;
        }

        public String getName() {
            return name;
        }

        public static CategoryType findCategoryTypeByName(String name) {
            for(CategoryType categoryType : values()) {
                if (categoryType.name.equalsIgnoreCase(name)) {
                    return categoryType;
                }
            }
            return OTHER;
        }

        public static CategoryType findCategoryTypeById(int categoryId) {
            for(CategoryType categoryType : values()) {
                if (categoryType.categoryId == categoryId) {
                    return categoryType;
                }
            }
            return OTHER;
        }
    }
}
