package uk.co.davidbaxter.letmepass.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.databinding.ActivityMainBinding;
import uk.co.davidbaxter.letmepass.databinding.ComponentNavHeaderBinding;
import uk.co.davidbaxter.letmepass.presentation.DisplayMode;
import uk.co.davidbaxter.letmepass.presentation.MainViewModel;
import uk.co.davidbaxter.letmepass.presentation.PasswordDatabaseEntryContainer;
import uk.co.davidbaxter.letmepass.presentation.SortingCriteria;
import uk.co.davidbaxter.letmepass.util.Triplet;

public class MainActivity extends AppCompatActivity implements
        SearchView.OnQueryTextListener,
        MenuItem.OnActionExpandListener,
        NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout = null;

    private RecyclerView recyclerView = null;

    private PasswordRecyclerAdapter passwordRecyclerAdapter = null;

    private MainViewModel viewModel;

    private boolean largeScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set whether we are a large screen/device or not
        this.largeScreen = getResources().getBoolean(R.bool.large_layout);

        // Get viewmodel
        this.viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        // Inflate layout and set viewModel
        ActivityMainBinding binding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_main);
        binding.setLifecycleOwner(this);
        binding.setViewModel(this.viewModel);

        // Setup actionbar
        this.setupActionBar();

        // Setup navigation drawer
        this.setupNavDrawer();

        // Setup recycler view
        this.setupRecyclerView();

        // Setup popup menus
        this.setupPopupMenus();

        // Setup dialogs
        this.setupDialogs();

        // Setup clipboard
        this.setupClipboard();

        // Setup snackbar
        this.setupSnackbar();

        // Listen to close events to close activity if necessary
        this.viewModel.getCloseEvent().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void aVoid) {
                MainActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing())
            viewModel.onFinish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu using our menu XML; this will add it to the ActionBar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_toolbar, menu);

        // Set up our search option by listening for queries
        MenuItem searchItem = menu.findItem(R.id.itemSearch);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchItem.setOnActionExpandListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle options selected in the ActionBar

        switch (item.getItemId()) {
            // 'Home' button, repurposed as navigation drawer open button
            case android.R.id.home:
                if (viewModel.getCanGoBack().getValue() != null
                        && viewModel.getCanGoBack().getValue()) {
                    this.viewModel.getNavigationCallbacks().onGoBack();
                } else {
                    if (drawerLayout == null)
                        throw new RuntimeException("Drawer layout is null; was nav drawer setup?");

                    drawerLayout.openDrawer(GravityCompat.START);
                    return true; // We processed the menu ourselves
                }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        this.viewModel.getSearchCallbacks().onSearchSubmit(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        this.viewModel.getSearchCallbacks().onSearchChange(newText);
        return true;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        // Don't handle expansion events
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        // Process search close event
        if (item.getItemId() == R.id.itemSearch)
            this.viewModel.getSearchCallbacks().onSearchClose();

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DisplayMode mode = null;
        switch (item.getItemId()) {
            case R.id.itemExplore:
                mode = DisplayMode.EXPLORE;
                break;
            case R.id.itemFavorites:
                mode = DisplayMode.FAVORITES;
                break;
            case R.id.itemAllPasswords:
                mode = DisplayMode.ALL_PASSWORDS;
                break;
            case R.id.itemConfigure:
                this.viewModel.getNavigationCallbacks().onConfigure();
                break;
            case R.id.itemClose:
                this.viewModel.getNavigationCallbacks().onCloseDatabase();
                break;
            default:
                throw new RuntimeException("Invalid navigation item selected");
        }

        if (mode != null) {
            // Pass the selected -mode- to the viewmodel. We do this so that the VM does not have to
            // hardcode any reference to any part of the view -- such as menu item IDs.
            this.viewModel.getNavigationCallbacks().onModeSelected(mode);
        }

        // TODO: only close sometimes?
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true; // Show item as selected
    }

    @Override
    public void onBackPressed() {
        if (this.viewModel.getCanGoBack().getValue() != null
                && this.viewModel.getCanGoBack().getValue())
            this.viewModel.getNavigationCallbacks().onGoBack();
        else {
            // Show a 'are you sure' dialog to stop users accidentally closing the DB when they
            // don't want to
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.main_close_dialog_title)
                    .setMessage(R.string.main_close_dialog_message)
                    .setPositiveButton(R.string.main_close_dialog_ok,
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.main_close_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .create();
            dialog.show();
        }
    }

    private void setupPopupMenus() {
        this.viewModel.getPopupMenu().observe(this, new Observer<Triplet<View, Integer, Object>>() {
            @Override
            public void onChanged(@Nullable final Triplet<View, Integer, Object> triplet) {
                if (triplet == null)
                    return;

                // Create and show a popup, anchored on the view [first], using the menu
                // resource as specified by [second]
                PopupMenu popup = new PopupMenu(MainActivity.this, triplet.first, Gravity.END);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.itemSortName:
                                // Sort by asc: we do not actually care if it is ASC or not, the
                                // callback will choose the correct order.
                                // The same is done below.
                                viewModel.getSortingCallbacks().onSort(SortingCriteria.NAME_ASC);
                                break;
                            case R.id.itemSortCreated:
                                viewModel.getSortingCallbacks().onSort(SortingCriteria.CREATED_ASC);
                                break;
                            case R.id.itemSortUpdated:
                                viewModel.getSortingCallbacks().onSort(SortingCriteria.UPDATED_ASC);
                                break;
                            case R.id.itemToggleFavorite:
                                viewModel.getEntryCallbacks().onToggleFavorite(
                                        (PasswordDatabaseEntryContainer)triplet.third);
                                break;
                            case R.id.itemEditEntry:
                                viewModel.getEntryCallbacks().onEditEntry(
                                        (PasswordDatabaseEntryContainer)triplet.third);
                                break;
                            case R.id.itemDeleteEntry:
                                viewModel.getEntryCallbacks().onDeleteEntry(
                                        (PasswordDatabaseEntryContainer)triplet.third);
                                break;
                        }

                        return true;
                    }
                });
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(triplet.second, popup.getMenu());
                popup.show();
            }
        });
    }

    private void setupActionBar() {
        // Set support action bar, so that the action bar integrates properly and works with older
        // devices.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        // Retrieve the ActionBar (support library handles casting automatically, rather than using
        // our Toolbar reference), and add the button to open the navigation drawer. This works by
        // repurposing a 'home' button, which would usually navigate backwards. We set this 'home'
        // button to an 'up' (i.e. go up in nav hierarchy) button so we can change its icon.
        // (N.B. - this is the official recommended method)
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true); // Show the home button as an 'up' (nav) button
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp); // Set 'up' icon to menu icon

        // Subscribe to any changes that we should show in the ActionBar's title. These changes will
        // be emitted if a user submits a search query, changes the display of passwords to
        // 'favourites', etc.
        this.viewModel.getScreenTitle().observe(this, new Observer<Pair<Integer, Object[]>>() {
            @Override
            public void onChanged(@Nullable Pair<Integer, Object[]> pair) {
                if (pair == null)
                    return;

                actionBar.setTitle(getString(
                        pair.first,
                        pair.second == null ? new Object[]{} : pair.second // Null coalescing
                ));
            }
        });

        // Subscribe to any changes of whether we can go back: we do this to set our home button
        // to either a back arrow or a nav drawer button
        this.viewModel.getCanGoBack().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean canGoBack) {
                if (canGoBack != null && canGoBack) {
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
                } else {
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
                }
            }
        });
    }

    private void setupNavDrawer() {
        // Retrieve the drawer reference so we can open it via action bar
        this.drawerLayout = findViewById(R.id.drawerLayoutMain);

        // Separately inflate the header: we do this so we can have data bound to it (e.g. for DB
        // title). We then need to add this to our navigation view (drawer)
        NavigationView navView = findViewById(R.id.navMain);
        ComponentNavHeaderBinding binding = DataBindingUtil.inflate(
                getLayoutInflater(),
                R.layout.component_nav_header,
                navView,
                false);
        navView.addHeaderView(binding.getRoot());
        navView.setNavigationItemSelectedListener(this);
    }

    private void setupRecyclerView() {
        // Retrieve containers, and replace with blank list if null
        List<PasswordDatabaseEntryContainer> containers =
                this.viewModel.getContainers().getValue();
        if (containers == null)
            containers = new ArrayList<PasswordDatabaseEntryContainer>();

        // Initialize adapter
        this.passwordRecyclerAdapter = new PasswordRecyclerAdapter(containers, this.viewModel);

        // Setup adapter to use linear layout, and our adapter
        this.recyclerView = findViewById(R.id.recyclerMain);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Use LinearLayout for list
        this.recyclerView.setAdapter(this.passwordRecyclerAdapter);

        // Listen to scroll
        this.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                // Update the stuck divider while scrolling
                sendStuckDividerUpdate();
            }
        });

        // Subscribe to changes in containers so that we can update RecyclerView
        this.viewModel.getContainers().observe(this, new Observer<List<PasswordDatabaseEntryContainer>>() {
            @Override
            public void onChanged(@Nullable List<PasswordDatabaseEntryContainer> entries) {
                // We set the list to an immutable list so that we do not share a reference. This is
                // so updates to the list are always synchronized with updates to the view; the view
                // may simply be updated by calling setValue/postValue on the entries LiveData with
                // the same reference, as it is invoked regardless of reference change.
                if (entries == null)
                    MainActivity.this.passwordRecyclerAdapter.setEntries(
                            Collections.<PasswordDatabaseEntryContainer>emptyList()
                    );
                else
                    MainActivity.this.passwordRecyclerAdapter.setEntries(
                            Collections.unmodifiableList(entries)
                    );

                // Update the stuck divider since our new entries may have different dividers
                sendStuckDividerUpdate();
            }
        });

        // Subscribe to changes in specific containers so we can update individual RecyclerView
        // entries
        this.viewModel.getContainerUpdate().observe(this,
                new Observer<PasswordDatabaseEntryContainer>() {
            @Override
            public void onChanged(@Nullable PasswordDatabaseEntryContainer container) {
                passwordRecyclerAdapter.notifyContainerChanged(container);
            }
        });
    }

    private void setupDialogs() {
        this.viewModel.getDialog().observe(this, new Observer<Pair<PasswordDatabaseEntryContainer, Boolean>>() {
            @Override
            public void onChanged(@Nullable Pair<PasswordDatabaseEntryContainer, Boolean> pair) {
                if (pair == null || pair.first.getEntry() == null)
                    return;

                // Prepare arguments -- we can't instantiate a fragment with constructor args as
                // this breaks fragments
                Bundle args = new Bundle();
                args.putSerializable(EntryDialogFragment.TAG_CONTAINER, pair.first);
                args.putBoolean(EntryDialogFragment.TAG_EDITABLE, pair.second);

                // Create the fragment and show the dialog
                EntryDialogFragment dialogFragment = new EntryDialogFragment();
                dialogFragment.setArguments(args);

                if (!largeScreen) { // Show as fullscreen if we are a small screen
                    getSupportFragmentManager()
                            .beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.drawerLayoutMain, dialogFragment)
                            .addToBackStack(null) // Makes transaction reversible
                            .commit();
                } else {
                    dialogFragment.show(getSupportFragmentManager(), "EntryDialogFragment");
                }
            }
        });
    }

    private void setupClipboard() {
        final ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);

        this.viewModel.getCopyToClipboard().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                ClipData clip = ClipData.newPlainText("password", s);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(
                    MainActivity.this,
                    R.string.main_copied_clipboard,
                    Toast.LENGTH_SHORT)
                .show();
            }
        });
    }

    private void setupSnackbar() {
        this.viewModel.getSnackBarMessage().observe(this, new Observer<Pair<Integer, Object[]>>() {
            @Override
            public void onChanged(@Nullable Pair<Integer, Object[]> integerPair) {
                if (integerPair == null)
                    return;

                Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(integerPair.first, integerPair.second),
                        Snackbar.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void sendStuckDividerUpdate() {
        // Get the first item in the recycler view that is -visible-
        View firstItem = recyclerView.findChildViewUnder(0, 0);

        if (firstItem == null)
            return;

        // Get the index of the first item; update the stuck divider accordingly (stuck
        // divider depends on the index of showed items, since a divider covers a range of
        // indices; once the index is outside of this range, the stuck divider should
        // change)
        int firstItemIndex = recyclerView.getChildAdapterPosition(firstItem);

        // If we didn't find the item in the adapter, pretend we did. It's probably the first item.
        // This is to resolve some glitches to do with the way the RecyclerView works, causing the
        // null stuck header to be displayed when it shouldn't be -- because the index was negative.
        if (firstItemIndex < 0)
            firstItemIndex = 0;

        viewModel.onStuckDividerUpdate(firstItemIndex);
    }

}
