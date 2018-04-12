package uk.co.davidbaxter.letmepass.presentation;

import uk.co.davidbaxter.letmepass.R;

public enum SortingCriteria {

    NAME_ASC(true, R.string.main_sort_name),
    NAME_DSC(false, R.string.main_sort_name),
    CREATED_ASC(true, R.string.main_sort_created),
    CREATED_DSC(false, R.string.main_sort_created),
    UPDATED_ASC(true, R.string.main_sort_updated),
    UPDATED_DSC(false, R.string.main_sort_updated);

    // Can't forward-reference, so we define the opposites here instead
    static {
        NAME_ASC.opposite = NAME_DSC;
        NAME_DSC.opposite = NAME_ASC;
        CREATED_ASC.opposite = CREATED_DSC;
        CREATED_DSC.opposite = CREATED_ASC;
        UPDATED_ASC.opposite = UPDATED_DSC;
        UPDATED_DSC.opposite = UPDATED_ASC;
    }

    private final boolean isAscending;
    private final int stringRes;
    private /*final*/ SortingCriteria opposite;

    SortingCriteria(boolean isAscending, int stringRes) {
        this.isAscending = isAscending;
        this.stringRes = stringRes;
    }

    public boolean isAscending() {
        return isAscending;
    }

    public SortingCriteria getOpposite() {
        return opposite;
    }

    public int getStringResourceId() {
        return stringRes;
    }
}
