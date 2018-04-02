package uk.co.davidbaxter.letmepass.ui;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.databinding.FragmentCreationStep2Binding;

public class CreationStep2Fragment extends CreationStepFragmentBase<FragmentCreationStep2Binding> {

    @Override
    protected final int getLayoutId() {
        return R.layout.fragment_creation_step2;
    }

    @Override
    protected void initDataBinding(FragmentCreationStep2Binding binding) {
        binding.setViewModel(viewModel);
    }
}
