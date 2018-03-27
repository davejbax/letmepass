package uk.co.davidbaxter.letmepass.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.databinding.ActivityIntroBinding;
import uk.co.davidbaxter.letmepass.presentation.IntroViewModel;

public class IntroActivity extends AppCompatActivity {

    private IntroViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the layout using the data binding utility library: this allows for binding the
        // viewmodel, so that buttons etc. in the layout can invoke viewmodel methods, and fields
        // (such as text fields) can be bound to the viewmodel
        ActivityIntroBinding binding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_intro);

        // Setup viewmodel
        this.viewModel = ViewModelProviders.of(this).get(IntroViewModel.class);
        binding.setViewModel(this.viewModel);

        // Setup viewmodel events
        this.setupEvents();
    }

    private void setupEvents() {
        // Observe for changes to toast text
        this.viewModel.getToastText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                if (s == null) return;
                Toast.makeText(IntroActivity.this, s, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

}
