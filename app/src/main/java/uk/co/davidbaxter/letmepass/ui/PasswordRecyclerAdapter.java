package uk.co.davidbaxter.letmepass.ui;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import uk.co.davidbaxter.letmepass.BR;
import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.presentation.MainViewModel;
import uk.co.davidbaxter.letmepass.presentation.PasswordDatabaseEntryContainer;

/**
 * A RecyclerView.Adapter class for passwords/folders/data displayed in a vertical list. This view
 * takes {@link uk.co.davidbaxter.letmepass.presentation.PasswordDatabaseEntryContainer} objects so
 * that there is as little logic as possible contained within the view. The logical processing and
 * presentation of models such as password entries should instead take place in the presentation
 * layer.
 */
public class PasswordRecyclerAdapter
        extends RecyclerView.Adapter<PasswordRecyclerAdapter.ViewHolder> {

    private static final int VIEW_TYPE_ENTRY = 0; // View type for password entries
    private static final int VIEW_TYPE_DIVIDER = 1; // View type for dividers

    private List<PasswordDatabaseEntryContainer> entries;
    private MainViewModel viewModel;

    /**
     * Construct a new PasswordRecyclerAdapter
     * @param entries List of password database entry containers to display in view. When updating,
     *                it is necessary to use the notify*() methods to update the RecyclerView. If
     *                this list reference is later discarded and the user wishes to set a new list
     *                of entries, the {@link #setEntries(List)} method can be used to set this new
     *                list.
     * @param viewModel The viewmodel instance to bind to password database entries (so that its
     *                  methods can be called, e.g.)
     */
    public PasswordRecyclerAdapter(@NonNull List<PasswordDatabaseEntryContainer> entries,
                                   MainViewModel viewModel) {
        this.entries = entries;
        this.viewModel = viewModel;
    }

    /**
     * Sets a new list of entries. Note that this must be distinct from the current reference for
     * any changes to be detected: if the reference held by the adapter is modified (e.g. if items
     * are inserted or removed from the {@link List} itself) and then this method is called, there
     * will be no changes detected. For these changes, use the notify*() methods of this class.
     * <p>
     * <b>This method should be used when the entire list changes.</b> For other changes, the notify
     * methods should be used instead, as the list reference stored by this class is not inherently
     * immutable.
     *
     * @param newEntries The new list of entries to display in the RecyclerView
     */
    public void setEntries(@NonNull final List<PasswordDatabaseEntryContainer> newEntries) {
        // Diff the entries so we know the changes that we must notify for
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return entries.size();
            }

            @Override
            public int getNewListSize() {
                return newEntries.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                // Compare object references
                return entries.get(oldItemPosition).getEntry()
                        == newEntries.get(newItemPosition).getEntry();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                // Compare object contents using (hopefully overridden) equals method
                return entries.get(oldItemPosition).equals(newEntries.get(newItemPosition));
            }
        });

        // Update entries and notify of change so view is updated
        this.entries = newEntries;
        result.dispatchUpdatesTo(this);
    }

    /**
     * Updates a single container in the view, if it exists. This will re-bind its contents.
     * @param container Container to update
     */
    public void notifyContainerChanged(PasswordDatabaseEntryContainer container) {
        int index = this.entries.indexOf(container);
        if (index > -1)
            this.notifyItemChanged(index);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate view using data binding library, and store this binding in the ViewHolder
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(
                inflater,
                viewType == VIEW_TYPE_ENTRY ? R.layout.component_pwd_list_entry
                        : R.layout.component_pwd_list_divider,
                parent,
                false);

        return new ViewHolder(binding, viewModel);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(entries.get(position));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    @Override
    public int getItemViewType(int position) {
        return entries.get(position).isDivider() ? VIEW_TYPE_DIVIDER : VIEW_TYPE_ENTRY;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding binding;
        private MainViewModel viewModel;
        private PasswordDatabaseEntryContainer container;

        public ViewHolder(ViewDataBinding binding, MainViewModel viewModel) {
            super(binding.getRoot());

            this.binding = binding;
            this.viewModel = viewModel;
        }

        public void bind(PasswordDatabaseEntryContainer entry) {
            this.container = entry;
            this.binding.setVariable(BR.entryContainer, entry);
            this.binding.setVariable(BR.viewModel, viewModel);

            // Execute pending bindings. This is important because the RecyclerView will calculate
            // proportions after calling this method, but the data binding lib will not set bindings
            // until the next frame. This could lead to incorrectly determined proportions.
            this.binding.executePendingBindings();
        }

    }

}
