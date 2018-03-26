package uk.co.davidbaxter.letmepass.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.databinding.ActivityIntroBinding;
import uk.co.davidbaxter.letmepass.presentation.IntroViewModel;

public class IntroActivity extends AppCompatActivity {

    private IntroViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityIntroBinding binding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_intro);

        // Setup viewmodel
        this.viewModel = ViewModelProviders.of(this).get(IntroViewModel.class);
        binding.setViewModel(this.viewModel);
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
