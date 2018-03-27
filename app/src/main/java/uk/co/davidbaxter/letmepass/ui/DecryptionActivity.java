package uk.co.davidbaxter.letmepass.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.databinding.ActivityDecryptionBinding;
import uk.co.davidbaxter.letmepass.presentation.DecryptionViewModel;

public class DecryptionActivity extends AppCompatActivity {

    private DecryptionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityDecryptionBinding binding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_decryption);
        binding.setLifecycleOwner(this);

        // Setup viewmodel
        this.viewModel = ViewModelProviders.of(this).get(DecryptionViewModel.class);
        binding.setViewModel(this.viewModel);
    }
}
