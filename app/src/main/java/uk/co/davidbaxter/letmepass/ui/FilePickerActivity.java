package uk.co.davidbaxter.letmepass.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.davidbaxter.letmepass.R;
import uk.co.davidbaxter.letmepass.databinding.ComponentFileEntryBinding;
import uk.co.davidbaxter.letmepass.presentation.FilePickerViewModel;

public class FilePickerActivity extends AppCompatActivity {

    public static final String EXTRA_EXTENSION = "extension";
    public static final String EXTRA_PATH = "path";

    private FilePickerViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.viewModel = ViewModelProviders.of(this).get(FilePickerViewModel.class);

        // Set our content view
        setContentView(R.layout.activity_file_picker);

        // Generate our containers
        String extension = null;
        if (getIntent().getExtras() != null)
            extension = getIntent().getExtras().getString(EXTRA_EXTENSION);
        this.viewModel.generateContainers(getFilesDir(), extension);

        // Setup recycler
        FilePickerAdapter recyclerAdapter = new FilePickerAdapter(viewModel.getContainers());
        RecyclerView recyclerView = findViewById(R.id.recyclerFilePicker);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(recyclerAdapter);

        // Setup file pick event
        this.viewModel.getFilePickEvent().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String file) {
                if (file != null) {
                    // Get a File object, convert this to a Uri, and pass back to the calling
                    // activity with a RESULT_OK
                    Intent result = new Intent();
                    result.putExtra(EXTRA_PATH, getFileStreamPath(file).getPath());
                    setResult(RESULT_OK, result);
                } else {
                    // We have no file: probably cancelled
                    setResult(RESULT_CANCELED);
                }

                // End the activity
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // No file chosen: we set result to cancelled and then end the activity
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    /**
     * A basic, static RecyclerAdapter to display {@link FilePickerViewModel.Container} objects
     */
    private class FilePickerAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<FilePickerViewModel.Container> containers;

        FilePickerAdapter(List<FilePickerViewModel.Container> containers) {
            super();
            this.containers = containers;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ComponentFileEntryBinding binding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.component_file_entry,
                    parent,
                    false
            );
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(containers.get(position));
        }

        @Override
        public int getItemCount() {
            return containers.size();
        }
    }

    /**
     * A basic viewholder to hold a file entry component view in a RecyclerView
     */
    private class ViewHolder extends RecyclerView.ViewHolder {

        private ComponentFileEntryBinding binding;

        ViewHolder(ComponentFileEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FilePickerViewModel.Container container) {
            this.binding.setContainer(container);
            this.binding.setViewModel(viewModel);
        }

    }

}
