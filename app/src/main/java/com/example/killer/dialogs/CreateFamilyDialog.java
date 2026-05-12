package com.example.killer.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.killer.R;
import com.example.killer.managers.FamilyManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Map;

public class CreateFamilyDialog extends DialogFragment {

    private OnFamilyCreatedListener listener;
    private TextInputEditText etFamilyName, etDescription;
    private Button btnCreate, btnCancel;
    private ProgressBar progressBar;
    private TextView tvTitle;
    private FamilyManager familyManager;

    public interface OnFamilyCreatedListener {
        void onFamilyCreated();
    }

    public void setOnFamilyCreatedListener(OnFamilyCreatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.Theme_FamilyPlanner_Dialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_create_family_new, container, false);

        familyManager = FamilyManager.getInstance(requireContext());
        initViews(view);

        return view;
    }

    private void initViews(View view) {
        tvTitle = view.findViewById(R.id.tv_title);
        etFamilyName = view.findViewById(R.id.et_family_name);
        etDescription = view.findViewById(R.id.et_description);
        progressBar = view.findViewById(R.id.progress_bar);
        btnCreate = view.findViewById(R.id.btn_create);
        btnCancel = view.findViewById(R.id.btn_cancel);

        btnCancel.setOnClickListener(v -> dismiss());

        btnCreate.setOnClickListener(v -> {
            String name = etFamilyName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (name.isEmpty()) {
                etFamilyName.setError("Введите название семьи");
                etFamilyName.requestFocus();
                return;
            }

            createFamily(name, description);
        });
    }

    private void createFamily(String name, String description) {
        showLoading(true);

        familyManager.createFamily(name, description, new FamilyManager.FamilyCallback() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                showLoading(false);
                Toast.makeText(requireContext(), "Семья создана!", Toast.LENGTH_SHORT).show();

                if (listener != null) {
                    listener.onFamilyCreated();
                }
                dismiss();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(requireContext(), "Ошибка: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            btnCreate.setEnabled(false);
            btnCancel.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnCreate.setEnabled(true);
            btnCancel.setEnabled(true);
        }
    }
}