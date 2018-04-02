package uk.co.davidbaxter.letmepass.ui;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.databinding.FragmentCreationStep3Binding;

public class CreationStep3Fragment extends CreationStepFragmentBase<FragmentCreationStep3Binding> {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_creation_step3;
    }

    @Override
    protected void initDataBinding(FragmentCreationStep3Binding binding) {
        binding.setViewModel(viewModel);
    }
}