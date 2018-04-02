package uk.co.davidbaxter.letmepass.ui;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.databinding.FragmentCreationStep4Binding;

public class CreationStep4Fragment extends CreationStepFragmentBase<FragmentCreationStep4Binding> {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_creation_step4;
    }

    @Override
    protected void initDataBinding(FragmentCreationStep4Binding binding) {
        binding.setViewModel(viewModel);
    }

}
