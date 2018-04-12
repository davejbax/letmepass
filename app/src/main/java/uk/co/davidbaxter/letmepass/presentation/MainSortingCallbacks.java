package uk.co.davidbaxter.letmepass.presentation;

import android.view.View;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.util.Triplet;

/**
 * Sorting callbacks for the sort feature of the main view
 */
public class MainSortingCallbacks {

    private final MainViewModel viewModel;

    public MainSortingCallbacks(MainViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Handles a switch (changing from ascending-descending and vice-versa) of sort order
     */
    public void onSortSwitch() {
        // Change sorting criteria to opposite (i.e. ascending if descending and vice-versa)
        if (viewModel.sortingCriteria.getValue() != null) {
            viewModel.sortingCriteria.postValue(viewModel.sortingCriteria.getValue().getOpposite());
            viewModel.entries.postValue(viewModel.entries.getValue()); // Resort
        }
    }

    /**
     * Handles opening a popup menu of sorting options
     * @param v View to attach menu to
     * @return True (boolean required for long press binding)
     */
    public boolean onSortMenuOpen(View v) {
        viewModel.popupMenu.postValue(new Triplet<View, Integer, Object>(
                v, R.menu.menu_main_sort_popup, null));
        return true;
    }

    /**
     * Handles a sorting of entries by some sorting criteria
     * @param sortingCriteria Sorting criteria to use: ASC/DSC will be disregarded and set to the
     *                        current order in the view instead.
     */
    public void onSort(SortingCriteria sortingCriteria) {
        // If the criteria is not of the right order (ascending or descending), flip it.
        if (viewModel.sortingCriteria.getValue() != null
                && sortingCriteria.isAscending() !=
                    viewModel.sortingCriteria.getValue().isAscending()) {
            sortingCriteria = sortingCriteria.getOpposite();
        }

        // Set the sorting criteria in viewModel and resort entries
        viewModel.sortingCriteria.postValue(sortingCriteria);
        viewModel.entries.postValue(viewModel.entries.getValue());
    }

}
