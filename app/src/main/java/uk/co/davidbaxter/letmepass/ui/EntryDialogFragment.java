package uk.co.davidbaxter.letmepass.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.davidbaxter.letmepass.BR;
import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.databinding.FragmentEntryDialogBinding;
import uk.co.davidbaxter.letmepass.model.DataEntry;
import uk.co.davidbaxter.letmepass.model.FolderEntry;
import uk.co.davidbaxter.letmepass.model.PasswordEntry;
import uk.co.davidbaxter.letmepass.presentation.MainViewModel;
import uk.co.davidbaxter.letmepass.presentation.PasswordDatabaseEntryContainer;
import uk.co.davidbaxter.letmepass.presentation.EntryDialogViewModel;
import uk.co.davidbaxter.letmepass.security.PasswordGeneratorService;
import uk.co.davidbaxter.letmepass.security.SecurityServices;

public class EntryDialogFragment extends DialogFragment {

    public static final String TAG_CONTAINER = "container";
    public static final String TAG_EDITABLE = "editable";

    private MainViewModel mainViewModel;
    private EntryDialogViewModel viewModel;
    private PasswordDatabaseEntryContainer container;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get arguments
        this.container = (PasswordDatabaseEntryContainer)
                getArguments().getSerializable(TAG_CONTAINER);
        boolean editable = getArguments().getBoolean(TAG_EDITABLE);

        // Obtain main view model bound to activity: we need this because we need to communicate
        // any changes that take place within this fragment to the main screen - similar to a
        // 'return value', but passed instead as a callback on the VM
        this.mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);

        // Initialize the security services for use in the viewmodel
        SecurityServices.initialize(getContext().getApplicationContext());

        // Obtain our own view model to deal with our own presentation and actions; we need to
        // use a factory to get this,
        this.viewModel = ViewModelProviders
                .of(this, new EntryDialogViewModel.Factory(
                        container,
                        editable))
                .get(EntryDialogViewModel.class);

        // Observe for close events, to close the dialog
        this.viewModel.getCloseEvent().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(@Nullable Void v) {
                // Pop the fragment off the stack, to return to the activity that made us
                getFragmentManager().popBackStackImmediate();
            }
        });

        // Observe for save events: we delegate this responsibility to the main viewmodel
        this.viewModel.getSaveEvent().observe(this, new Observer<PasswordDatabaseEntryContainer>() {
            @Override
            public void onChanged(@Nullable PasswordDatabaseEntryContainer container) {
                mainViewModel.getEntryCallbacks().saveEntry(container);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the root view
        FragmentEntryDialogBinding binding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_entry_dialog,
                null,
                false);
        binding.setLifecycleOwner(this);
        binding.setVariable(BR.entry, this.viewModel.getWorkingEntry());
        binding.setViewModel(this.viewModel);
        final View root = binding.getRoot();

        // Remove any views from our editor container (it can only hold one view)
        ViewGroup containerGroup = (ViewGroup) root.findViewById(R.id.fragContainer);
        containerGroup.removeAllViews();

        // Inflate the child view, and attach it to the editor container
        ViewDataBinding childBinding = DataBindingUtil.inflate(
                inflater,
                getChildLayoutId(),
                containerGroup,
                true
        );

        // Set variables: this is a bit of a hack -- we set entry so that the casting is
        // done by the data binding library on this one instance, since we don't specify
        // types. This gets rid of any casts within the layout itself (which would be slow)
        childBinding.setLifecycleOwner(this);
        childBinding.setVariable(BR.entry, this.viewModel.getWorkingEntry());
        childBinding.setVariable(BR.viewModel, this.viewModel);

        return root;
    }

    private int getChildLayoutId() {
        switch (container.getEntry().getType()) {
            case PasswordEntry.TYPE:
                return R.layout.component_editor_password;
            case FolderEntry.TYPE:
                return R.layout.component_editor_folder;
            case DataEntry.TYPE:
                return R.layout.component_editor_data;
        }

        throw new RuntimeException("Unsupported entry type");
    }

}
