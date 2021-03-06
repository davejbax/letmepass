package uk.co.davidbaxter.letmepass.ui;

import android.databinding.ViewDataBinding;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.databinding.FragmentCreationStep1Binding;
import uk.co.davidbaxter.letmepass.presentation.CreationViewModel;

public class CreationStep1Fragment extends CreationStepFragmentBase<FragmentCreationStep1Binding> {

    @Override
    protected final int getLayoutId() {
        return R.layout.fragment_creation_step1;
    }

    @Override
    protected void initDataBinding(FragmentCreationStep1Binding binding) {
        binding.setViewModel(viewModel);
    }

    @Override
    protected int getStepId() {
        return CreationViewModel.ID_STEP_1;
    }
}
