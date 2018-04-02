package uk.co.davidbaxter.letmepass.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.VerificationError;

import uk.co.davidbaxter.letmepass.presentation.CreationViewModel;

/**
 * Base class for creation step fragments -- i.e. individual 'step' fragments for the stepper in the
 * creation screen (see: CreationActivity). This implements necessary methods and allows for
 * overriding of desired methods, such as onSelected().
 *
 * Note that this class also handles obtaining a viewModel instance, which is bound to the activity
 * (and so shared across the fragments). This class has a generic type parameter of the binding
 * class to use, such that any required variables can be set by derived classes. It is recommended
 * to set the viewModel with this (by overriding initDataBinding()).
 */
public abstract class CreationStepFragmentBase<T extends ViewDataBinding>
        extends Fragment implements Step {

    // Non-final, because fragments can be re-assigned to activities; viewmodel in this case is
    // bound to the lifecycle of the current activity
    protected CreationViewModel viewModel;

    public CreationStepFragmentBase() {
        // Required empty constructor
    }

    /**
     * Get the layout ID to be used when creating this fragment's view
     * @return Android layout ID
     */
    protected abstract int getLayoutId();

    /**
     * Initialize a data binding by setting any required variables, such as the viewModel.
     * @param binding Binding to set variables on
     */
    protected abstract void initDataBinding(T binding);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get viewmodel of activity so fragments can share data; this also means that it can
        // outlive the fragments themselves, so data is persisted across steps.
        // Note that we set the viewmodel here and not in constructor, because onCreate is called
        // when the activity is changed. Activity can be null when constructed.
        this.viewModel = ViewModelProviders.of(getActivity()).get(CreationViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        // Inflate view using data binding, setting viewmodel and lifecycle owner
        final T binding = DataBindingUtil.inflate(
                inflater,
                this.getLayoutId(),
                group,
                false);

        // Allow derived class to set any variables -- this could include viewModel, e.g.
        this.initDataBinding(binding);

        // Set the lifecycle owner of the binding to enable LiveData objects to work
        binding.setLifecycleOwner(this);

        // Return the data binding 'root' (i.e. the view)
        return binding.getRoot();
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        // Return null for no error/step success
        return null;
    }

    @Override
    public void onSelected() {
        // Do nothing on selection; method required in implementation
    }

    @Override
    public void onError(@NonNull VerificationError error) {
        // TODO
    }

}
