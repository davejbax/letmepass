package uk.co.davidbaxter.letmepass.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

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

        // Setup events
        this.setupEvents();
    }

    private void setupEvents() {
        viewModel.getDecryptionResult().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer errorStringId) {
                if (errorStringId != null) {
                    Toast.makeText(DecryptionActivity.this, errorStringId, Toast.LENGTH_LONG)
                            .show();
                } else {
                    Intent intent = new Intent(DecryptionActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
