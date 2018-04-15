package uk.co.davidbaxter.letmepass.presentation;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.util.Pair;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.model.DataEntry;
import uk.co.davidbaxter.letmepass.model.FolderEntry;
import uk.co.davidbaxter.letmepass.model.PasswordDatabaseEntry;
import uk.co.davidbaxter.letmepass.model.PasswordEntry;
import uk.co.davidbaxter.letmepass.util.SingleLiveEvent;
import uk.co.davidbaxter.letmepass.util.Triplet;

public class MainViewModel extends ViewModel {

    //________________________________CONSTANTS________________________________
    /**
     * Empty container constant to be used wherever an empty container is required, such as when
     * an empty divider is needed.
     */
    private static final PasswordDatabaseEntryContainer NULL_DIVIDER_CONTAINER
            = new PasswordDatabaseEntryContainer(R.string.main_divider_empty);

    //________________________________BOUND LIVEDATA________________________________
    /**
     * LiveData object representing the currently 'stuck' container; this is the one that will be
     * displayed at the top of the list, without moving when scrolling.
     */
    public final MutableLiveData<PasswordDatabaseEntryContainer> stuckContainer
            = new MutableLiveData<>();

    /**
     * The current sorting criteria, as a LiveData object
     */
    public final MutableLiveData<SortingCriteria> sortingCriteria = new MutableLiveData<>();

    //________________________________OBSERVABLE LIVEDATA________________________________
    /**
     * Internal entries LiveData; updating this will also update containers via transformations
     */
    final MutableLiveData<List<PasswordDatabaseEntry>> entries = new MutableLiveData<>();

    /**
     * Current title (to be displayed) of the screen shown in the view, as a pair of a string
     * resource ID and optional/nullable String formatting parameters.
     */
    final MutableLiveData<Pair<Integer, Object[]>> screenTitle = new MutableLiveData<>();

    final MutableLiveData<Boolean> canGoBack = new MutableLiveData<>();

    /**
     * Triplet (LiveData) of the view to bind a popup to, the menu resource ID for the popup menu,
     * and any extra data to pass to the menu -- for instance, containers, if a container menu was
     * opened.
     */
    final SingleLiveEvent<Triplet<View, Integer, Object>> popupMenu
            = new SingleLiveEvent<>();

    /**
     * Dialog show event, to show a dialog to edit or view an entry; the emitted object in this case
     * is a pair of the password container to edit/view, and whether it should be editable.
     */
    final SingleLiveEvent<Pair<PasswordDatabaseEntryContainer, Boolean>> dialog
            = new SingleLiveEvent<>();

    /**
     * Event to update a single container in the view (rather than updating every container).
     */
    final SingleLiveEvent<PasswordDatabaseEntryContainer> updateContainer = new SingleLiveEvent<>();

    /**
     * Event to copy text to the clipboard
     */
    final SingleLiveEvent<String> copyToClipboard = new SingleLiveEvent<>();

    //________________________________INTERNALS________________________________
    /**
     * Current display mode -- this does NOT include search results
     */
    DisplayMode currentMode = DisplayMode.EXPLORE;

    // TODO: replace with real model
    DummyModel model = new DummyModel();

    DummyModelNavigator navigator = new DummyModelNavigator(model);

    /**
     * List of containers of password entries -- these are transformed from model-returned password
     * entries. This list consists of only -displayed- containers.
     */
    private final LiveData<List<PasswordDatabaseEntryContainer>> containers;

    /**
     * Comparator for entry containers, which compares based on sorting state
     * @see #makeEntryContainerComparator()
     */
    private final Comparator<PasswordDatabaseEntryContainer> entryContainerComparator =
            makeEntryContainerComparator();

    private final MainEntryCallbacks entryCallbacks = new MainEntryCallbacks(this);

    private final MainNavigationCallbacks navigationCallbacks = new MainNavigationCallbacks(this);

    private final MainSearchCallbacks searchCallbacks = new MainSearchCallbacks(this);

    private final MainSortingCallbacks sortingCallbacks = new MainSortingCallbacks(this);


    public MainViewModel() {
        this.sortingCriteria.setValue(SortingCriteria.NAME_ASC);
        this.containers = transformIntoContainers(entries);
        this.entries.postValue(this.model.getEntries());
        this.stuckContainer.setValue(new PasswordDatabaseEntryContainer(R.string.main_divider_passwords));
    }

    /**
     * Get the list of containers to display in the view
     * @return Live list of containers
     */
    public LiveData<List<PasswordDatabaseEntryContainer>> getContainers() {
        return containers;
    }

    /**
     * Get the current screen title, as a (live) pair of string resource ID, and (nullable) String
     * formatting parameters.
     *
     * @return Live pair of string resource ID & formatting parameters
     */
    public LiveData<Pair<Integer, Object[]>> getScreenTitle() {
        return screenTitle;
    }

    /**
     * Gets an observable for popup menu events. These events will be emitted when a popup menu must
     * be spawned by the view, and will pass a triple of:
     * (view to attach to, menu resource ID, extra data)
     * @return Popup menu live data
     */
    public LiveData<Triplet<View, Integer, Object>> getPopupMenu() {
        return popupMenu;
    }

    /**
     * Gets the LiveData event for password dialogs to be shown on screen; this is a pair of
     * the PDE container, and a boolean which is true if the user wishes to edit a password.
     *
     * @return Dialog live data
     */
    public LiveData<Pair<PasswordDatabaseEntryContainer, Boolean>> getDialog() {
        return dialog;
    }

    public LiveData<PasswordDatabaseEntryContainer> getContainerUpdate() {
        return updateContainer;
    }

    public LiveData<Boolean> getCanGoBack() {
        return canGoBack;
    }

    public LiveData<String> getCopyToClipboard() {
        return this.copyToClipboard;
    }

    public MainEntryCallbacks getEntryCallbacks() {
        return entryCallbacks;
    }

    public MainNavigationCallbacks getNavigationCallbacks() {
        return navigationCallbacks;
    }

    public MainSearchCallbacks getSearchCallbacks() {
        return searchCallbacks;
    }

    public MainSortingCallbacks getSortingCallbacks() {
        return sortingCallbacks;
    }

    /**
     * Updates the 'stuck' divider. This is a divider shown at the top of the screen, which stays
     * in place instead of scrolling with the list. The divider is intended to display the title for
     * the first visible items in the list, so the index of the first visible item in the list is
     * required.
     *
     * @param firstVisibleIndex The index of the first visible item in the list view of containers
     */
    public void onStuckDividerUpdate(int firstVisibleIndex) {
        PasswordDatabaseEntryContainer stuckContainer = null;

        // Find the first container that is a divider, and has a lesser than or equal to index
        List<PasswordDatabaseEntryContainer> containersList = containers.getValue();

        // Iterate backwards to find the first divider with an index lower than/equal to the first
        // visible index (this one will be 'stuck')
        for (int i = firstVisibleIndex; i >= 0 && containersList.size() > 0; i--) {
            if (containersList.get(i).isDivider()) {
                stuckContainer = containersList.get(i);
                break;
            }
        }

        // If we found one, set the stuck container
        if (stuckContainer != null)
            this.stuckContainer.postValue(stuckContainer);
        // Otherwise, assume no entries/dividers
        else
            this.stuckContainer.postValue(NULL_DIVIDER_CONTAINER);
    }

    /**
     * 'Refreshes' the view by re-setting title appropriately, re-fetching entries from the model,
     * and updating the list of displayed entries.
     * <p>
     * Note that this method will replace search results if the displayed screen is currently search
     * results, instead returning to whatever mode was selected before search.
     */
    public void refreshView() {
        switch (this.currentMode) {
            case EXPLORE:
                int stringId = navigator.isAtRoot() ? R.string.main_title_explore
                        : R.string.main_title_exploring;
                this.screenTitle.postValue(new Pair<Integer, Object[]>(
                        stringId,
                        new String[] { navigator.getFolderTitle() } // No format params
                ));
                this.entries.postValue(this.navigator.getCurrentEntries());
                break;

            case FAVORITES:
                this.screenTitle.postValue(new Pair<Integer, Object[]>(
                        R.string.main_title_favorites,
                        null // No format params
                ));
                this.entries.postValue(this.model.getFavorites());
                break;

            case ALL_PASSWORDS:
                this.screenTitle.postValue(new Pair<Integer, Object[]>(
                        R.string.main_title_all_passwords,
                        null // No format params
                ));
                this.entries.postValue(this.model.getAllPasswords());
                break;
        }
    }

    /**
     * Transforms a LiveData of a list of password database entries (raw from the model) into a list
     * of the view-consumable password database entry containers.
     *
     * @param data Data to transform
     * @return Transformed LiveData of list of containers
     */
    private LiveData<List<PasswordDatabaseEntryContainer>> transformIntoContainers(
            LiveData<List<PasswordDatabaseEntry>> data) {

        return Transformations.map(data, new Function<List<PasswordDatabaseEntry>,
                List<PasswordDatabaseEntryContainer>>() {

            @Override
            public List<PasswordDatabaseEntryContainer> apply(List<PasswordDatabaseEntry> input) {
                // Separate input into folders and others
                List<PasswordDatabaseEntryContainer> folders = new ArrayList<>();
                List<PasswordDatabaseEntryContainer> others = new ArrayList<>();

                for (PasswordDatabaseEntry entry : input) {
                    if (entry.getType().equals(FolderEntry.TYPE)) // Is folder type?
                        folders.add(new PasswordDatabaseEntryContainer(entry, sortingCriteria));
                    else // All other types (password, data, etc.)
                        others.add(new PasswordDatabaseEntryContainer(entry, sortingCriteria));
                }

                // Sort each subdivision by -current sorting criteria-
                Collections.sort(folders, entryContainerComparator);
                Collections.sort(others, entryContainerComparator);

                // In final list of containers, add a folder divider if there are folders, then
                // add folders; do the same for others.
                List<PasswordDatabaseEntryContainer> containers = new ArrayList<>();

                if (folders.size() > 0)
                    containers.add(new PasswordDatabaseEntryContainer(
                            R.string.main_divider_folders));
                containers.addAll(folders);

                if (others.size() > 0)
                    containers.add(new PasswordDatabaseEntryContainer(
                            R.string.main_divider_passwords));
                containers.addAll(others);

                // Add an empty divider below everything to make room for FAB
                if (input.size() > 1) {
                    containers.add(new PasswordDatabaseEntryContainer(R.string.empty_string));
                    containers.add(new PasswordDatabaseEntryContainer(R.string.empty_string));
                }

                return containers;
            }

        });

    }

    /**
     * Construct a new entry container comparator. This is a Comparator object that will compare
     * containers based on some criteria. In this case, the criteria will change depending on the
     * currently set sorting criteria in the view. This comparator can therefore be used to sort the
     * final list of containers.
     * <p>
     * Note that divider containers have no stored information that can be sorted on; therefore,
     * this comparator is better used <b>before</b> divider containers have been added to a list.
     *
     * @return Entry container comparator
     */
    private Comparator<PasswordDatabaseEntryContainer> makeEntryContainerComparator() {
        return new Comparator<PasswordDatabaseEntryContainer>() {
            @Override
            public int compare(PasswordDatabaseEntryContainer a, PasswordDatabaseEntryContainer b) {
                if (a.isDivider()) {
                    return b.isDivider() ? 0 : -1; // 0 if both dividers, a 'lesser' if a divider
                } else if (b.isDivider()) {
                    return 1; // a is not divider => a is 'greater'
                } else {
                    SortingCriteria criteria = sortingCriteria.getValue();
                    if (criteria == null)
                        return 0;

                    // If descending, multiply comparison by -1.
                    int mul = criteria.isAscending() ? 1 : -1;

                    // Compare based on current sorting criteria
                    switch (criteria) {
                        case NAME_ASC:
                        case NAME_DSC:
                            return a.getEntry().name.compareTo(b.getEntry().name) * mul;
                        case CREATED_ASC:
                        case CREATED_DSC:
                            return Long.valueOf(a.getEntry().created)
                                    .compareTo(b.getEntry().created) * mul;
                        case UPDATED_ASC:
                        case UPDATED_DSC:
                            return Long.valueOf(a.getEntry().updated)
                                    .compareTo(b.getEntry().updated) * mul;
                    }
                }

                return 0;
            }
        };
    }

    public static class DummyModel {

        private List<PasswordDatabaseEntry> entries = new ArrayList<>();

        public DummyModel() {
            PasswordEntry passwordEntry = new PasswordEntry("Netflix", "johnsmith", "xxxx", "", "");
            FolderEntry folderEntry = new FolderEntry("More");
            DataEntry dataEntry = new DataEntry("Data!", "");

            entries.add(passwordEntry);
            entries.add(folderEntry);
            entries.add(dataEntry);
            for (char a : "abcdef".toCharArray()) {
                entries.add(new PasswordEntry(a + "", "person_" + a, "xxxxx", "", ""));
            }
        }

        public List<PasswordDatabaseEntry> getEntries() {
            return entries;
        }

        public List<PasswordDatabaseEntry> getFavorites() {
            List<PasswordDatabaseEntry> matching = new ArrayList<>();
            for (PasswordDatabaseEntry entry : entries)
                if (entry.favorite)
                    matching.add(entry);
            return matching;
        }

        public List<PasswordDatabaseEntry> getAllPasswords() {
            List<PasswordDatabaseEntry> allPasswords = new ArrayList<>();
            for (PasswordDatabaseEntry entry : entries)
                if (!entry.getType().equals(FolderEntry.TYPE))
                    allPasswords.add(entry);
            return allPasswords;
        }

        public List<PasswordDatabaseEntry> search(String query) {
            query = query.toLowerCase();

            List<PasswordDatabaseEntry> matching = new ArrayList<>();
            // Add all entries containing the search query (in lowercase) to matches
            for (PasswordDatabaseEntry entry : entries) {
                if (entry.name.toLowerCase().contains(query))
                    matching.add(entry);
            }

            return matching;
        }

        public void saveEntry(PasswordDatabaseEntry entry) {
            if (entries.contains(entry)) {
                // Dummy model does not implement saving; real model will
                return;
            } else {
                entries.add(entry);
            }
        }

        public void saveToFolder(FolderEntry folder, PasswordDatabaseEntry entry) {
            if (!folder.children.contains(entry))
                folder.children.add(entry);
        }

        public void deleteEntry(PasswordDatabaseEntry entry) {
            deleteRecursively(null, entry);
        }

        private boolean deleteRecursively(FolderEntry folder, PasswordDatabaseEntry target) {
            List<PasswordDatabaseEntry> entries;
            if (folder == null)
                entries = this.entries;
            else
                entries = folder.children;

            boolean found = false;

            for (PasswordDatabaseEntry entry : entries) {
                if (entry == target) {
                    found = true;
                    break;
                } else if (entry.getType().equals(FolderEntry.TYPE)) {
                    if (deleteRecursively((FolderEntry) entry, target))
                        return true;
                }
            }

            if (found) {
                if (folder == null)
                    this.entries.remove(target);
                else
                    folder.children.remove(target);

                return true;
            }

            return false;
        }

    }

    public static class DummyModelNavigator {

        private DummyModel model;
        private Stack<FolderEntry> hierarchy = new Stack<>();

        public DummyModelNavigator(DummyModel model) {
            this.model = model;
        }

        public boolean isAtRoot() {
            return hierarchy.isEmpty();
        }

        public List<PasswordDatabaseEntry> getCurrentEntries() {
            FolderEntry currentFolder = hierarchy.isEmpty() ? null : hierarchy.peek();
            if (currentFolder == null)
                return model.getEntries();
            else
                return currentFolder.children;
        }

        public String getFolderTitle() {
            return hierarchy.isEmpty() ? null : hierarchy.peek().name;
        }

        public FolderEntry getFolder() {
            return hierarchy.isEmpty() ? null : hierarchy.peek();
        }

        public boolean openFolder(PasswordDatabaseEntry entry) {
            if (!(entry instanceof FolderEntry))
                return false;

            hierarchy.push((FolderEntry)entry);
            return true;
        }

        public void closeFolder() {
            hierarchy.pop();
        }

    }

}
