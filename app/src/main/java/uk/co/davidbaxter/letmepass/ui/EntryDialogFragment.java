package uk.co.davidbaxter.letmepass.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import uk.co.davidbaxter.letmepass.BR;
import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.databinding.FragmentEntryDialogBinding;
import uk.co.davidbaxter.letmepass.model.DataEntry;
import uk.co.davidbaxter.letmepass.model.FolderEntry;
import uk.co.davidbaxter.letmepass.model.PasswordEntry;
import uk.co.davidbaxter.letmepass.presentation.BreachAction;
import uk.co.davidbaxter.letmepass.presentation.MainViewModel;
import uk.co.davidbaxter.letmepass.presentation.PasswordDatabaseEntryContainer;
import uk.co.davidbaxter.letmepass.presentation.EntryDialogViewModel;
import uk.co.davidbaxter.letmepass.security.PasswordGeneratorService;
import uk.co.davidbaxter.letmepass.security.SecurityServices;

import static android.app.Activity.RESULT_OK;

/**
 * A dialog to display or allow editing of {@link PasswordDatabaseEntryContainer} objects that hold
 * entries.
 */
public class EntryDialogFragment extends DialogFragment {

    public static final String TAG_CONTAINER = "container";
    public static final String TAG_EDITABLE = "editable";

    private static final int REQUEST_PERM_REQUEST_THEN_BREACH_CHECK = 1;

    private MainViewModel mainViewModel;
    private EntryDialogViewModel viewModel;
    private PasswordDatabaseEntryContainer container;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PERM_REQUEST_THEN_BREACH_CHECK:
                // If the permission was accepted successfully, re-carry out the breach check
                if (resultCode == RESULT_OK)
                    viewModel.onBreachCheck();
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the security services for use in the viewmodel
        SecurityServices.initialize(getContext().getApplicationContext());

        // Get arguments
        this.container = (PasswordDatabaseEntryContainer)
                getArguments().getSerializable(TAG_CONTAINER);
        boolean editable = getArguments().getBoolean(TAG_EDITABLE);

        // Obtain main view model bound to activity: we need this because we need to communicate
        // any changes that take place within this fragment to the main screen - similar to a
        // 'return value', but passed instead as a callback on the VM
        this.mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);

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

        // Observe for breach check events
        this.viewModel.getBreachActionEvent().observe(this,
                new Observer<Pair<BreachAction, Object>>() {
            @Override
            public void onChanged(@Nullable Pair<BreachAction, Object> value) {
                BreachCheckCommon.handleBreachCheck(getActivity(), value,
                        REQUEST_PERM_REQUEST_THEN_BREACH_CHECK);
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
        final ViewDataBinding childBinding = DataBindingUtil.inflate(
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

        // Observe for validation errors
        this.viewModel.getValidationError().observe(this, new Observer<EntryDialogViewModel.ValidationError>() {
            @Override
            public void onChanged(@Nullable EntryDialogViewModel.ValidationError validationError) {
                TextInputLayout nameInput = (TextInputLayout)
                        root.findViewById(R.id.editLayoutEntryName);
                TextInputLayout passwordInput = null;

                // Check to see if we're editing a password
                if (EntryDialogFragment.this.container.getEntry().getType().equals(
                        PasswordEntry.TYPE)) {
                    // Set password input layout if we have one
                    passwordInput = (TextInputLayout) childBinding.getRoot().findViewById(
                            R.id.editLayoutEntryPassword);
                }

                // If we have a null error, clear the error
                if (validationError == null) {
                    nameInput.setError(null);
                    nameInput.setErrorEnabled(false);
                    if (passwordInput != null) {
                        passwordInput.setError(null);
                        passwordInput.setErrorEnabled(false);
                    }
                    return;
                }

                switch (validationError) {
                    case ENTRY_NAME_BLANK:
                        nameInput.setErrorEnabled(true);
                        nameInput.setError(getString(R.string.entry_dialog_name_empty));
                        break;
                    case PASSWORD_BLANK:
                        if (passwordInput == null)
                            break;
                        passwordInput.setErrorEnabled(true);
                        passwordInput.setError(getString(R.string.entry_dialog_password_empty));
                        break;
                }
            }
        });

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
