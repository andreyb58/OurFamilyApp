package com.example.killer.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.killer.R;
import com.example.killer.adapters.FamilyMemberAdapter;
import com.example.killer.auth.AuthManager;
import com.example.killer.dialogs.AddFamilyDialog;
import com.example.killer.dialogs.JoinFamilyDialog;
import com.example.killer.managers.FamilyManager;
import com.example.killer.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FamilyFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private View noFamilyView;
    private MaterialCardView familyInfoCard;
    private TextView tvFamilyName, tvInviteCode, tvMemberCount;
    private MaterialButton btnCreateFamily, btnJoinFamily, btnInviteMember, btnLeaveFamily;
    private ImageButton btnCopyInvite;
    private RecyclerView membersRecyclerView;
    private ProgressBar progressBar;

    private FamilyMemberAdapter memberAdapter;
    private final List<Map<String, Object>> members = new ArrayList<>();
    private AuthManager authManager;
    private FamilyManager familyManager;
    private Map<String, Object> currentFamily;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authManager  = AuthManager.getInstance(requireContext());
        familyManager = FamilyManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_family_new, container, false);
        initViews(view);
        setupRecyclerView();
        // ИСПРАВЛЕНО: всегда идём на сервер, не доверяем локальному family_id
        loadFamilyFromServer();
        return view;
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        noFamilyView    = view.findViewById(R.id.no_family_view);
        familyInfoCard  = view.findViewById(R.id.family_info_card);
        tvFamilyName    = view.findViewById(R.id.tv_family_name);
        tvInviteCode    = view.findViewById(R.id.tv_invite_code);
        tvMemberCount   = view.findViewById(R.id.tv_member_count);
        btnCreateFamily = view.findViewById(R.id.btn_create_family);
        btnJoinFamily   = view.findViewById(R.id.btn_join_family);
        btnInviteMember = view.findViewById(R.id.btn_invite_member);
        btnLeaveFamily  = view.findViewById(R.id.btn_leave_family);
        btnCopyInvite   = view.findViewById(R.id.btn_copy_invite);
        membersRecyclerView = view.findViewById(R.id.members_recycler_view);
        progressBar     = view.findViewById(R.id.progress_bar);

        btnCreateFamily.setOnClickListener(v -> showCreateFamilyDialog());
        btnJoinFamily.setOnClickListener(v -> showJoinFamilyDialog());
        if (btnInviteMember != null) btnInviteMember.setOnClickListener(v -> showInviteDialog());
        if (btnLeaveFamily  != null) btnLeaveFamily.setOnClickListener(v -> confirmLeave());
        if (btnCopyInvite   != null) btnCopyInvite.setOnClickListener(v -> copyInviteCode());
    }

    private void setupRecyclerView() {
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        memberAdapter = new FamilyMemberAdapter(members, requireContext());
        membersRecyclerView.setAdapter(memberAdapter);
    }

    /** ИСПРАВЛЕНО: всегда запрашиваем семью с сервера — не смотрим на локальный family_id */
    private void loadFamilyFromServer() {
        showLoading(true);
        familyManager.getMyFamily(new FamilyManager.FamilyCallback() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);

                    if (data == null || data.isEmpty() || data.get("id") == null) {
                        // Сервер сказал — семьи нет
                        clearLocalFamilyId();
                        showNoFamilyView();
                        return;
                    }

                    currentFamily = data;

                    // Обновляем family_id в AuthManager из ответа сервера
                    int familyId = extractInt(data.get("id"));
                    if (familyId > 0) {
                        authManager.updateFamilyId(familyId);
                        User user = authManager.getCurrentUser();
                        if (user != null) {
                            user.setFamilyId(familyId);
                            authManager.saveAuthData(authManager.getToken(), user);
                        }
                    }

                    showFamilyInfo(data);

                    // Участники
                    Object membersObj = data.get("members");
                    if (membersObj instanceof List) {
                        members.clear();
                        members.addAll((List<Map<String, Object>>) membersObj);
                        memberAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);
                    // Любая ошибка (включая "не в семье") → показываем экран без семьи
                    clearLocalFamilyId();
                    showNoFamilyView();
                });
            }
        });
    }

    /** Сбрасываем family_id локально если сервер говорит что семьи нет */
    private void clearLocalFamilyId() {
        User user = authManager.getCurrentUser();
        if (user != null && user.getFamilyId() != 0) {
            user.setFamilyId(0);
            authManager.saveAuthData(authManager.getToken(), user);
        }
        authManager.updateFamilyId(0);
    }

    private void showFamilyInfo(Map<String, Object> family) {
        noFamilyView.setVisibility(View.GONE);
        familyInfoCard.setVisibility(View.VISIBLE);

        String name = family.get("name") != null ? family.get("name").toString() : "Моя семья";
        String code = family.get("inviteCode") != null ? family.get("inviteCode").toString() : "------";
        int cnt = members.size();

        tvFamilyName.setText(name);
        tvInviteCode.setText("Код: " + code);
        tvMemberCount.setText("Участников: " + cnt);

        if (btnInviteMember != null) btnInviteMember.setVisibility(View.VISIBLE);
        if (btnLeaveFamily  != null) btnLeaveFamily.setVisibility(View.VISIBLE);
        if (btnCopyInvite   != null) btnCopyInvite.setVisibility(View.VISIBLE);
    }

    private void showNoFamilyView() {
        familyInfoCard.setVisibility(View.GONE);
        noFamilyView.setVisibility(View.VISIBLE);
        members.clear();
        if (memberAdapter != null) memberAdapter.notifyDataSetChanged();
        currentFamily = null;
        if (btnInviteMember != null) btnInviteMember.setVisibility(View.GONE);
        if (btnLeaveFamily  != null) btnLeaveFamily.setVisibility(View.GONE);
        if (btnCopyInvite   != null) btnCopyInvite.setVisibility(View.GONE);
    }

    private void showLoading(boolean loading) {
        if (progressBar != null)
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showCreateFamilyDialog() {
        AddFamilyDialog dlg = new AddFamilyDialog();
        dlg.setOnFamilyCreatedListener(() -> {
            loadFamilyFromServer();
            Toast.makeText(getContext(), "🏠 Семья создана!", Toast.LENGTH_SHORT).show();
        });
        dlg.show(getParentFragmentManager(), "AddFamilyDialog");
    }

    private void showJoinFamilyDialog() {
        JoinFamilyDialog dlg = new JoinFamilyDialog();
        dlg.setOnFamilyJoinedListener(() -> {
            loadFamilyFromServer();
            Toast.makeText(getContext(), "✅ Вы присоединились к семье!", Toast.LENGTH_SHORT).show();
        });
        dlg.show(getParentFragmentManager(), "JoinFamilyDialog");
    }

    private void showInviteDialog() {
        if (currentFamily == null) return;
        String code = currentFamily.get("inviteCode") != null ?
                currentFamily.get("inviteCode").toString() : "------";
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("👥 Пригласить участника")
                .setMessage("Код приглашения: " + code + "\n\nПоделитесь этим кодом с членом семьи.")
                .setPositiveButton("Копировать", (d, w) -> copyInviteCode())
                .setNegativeButton("Закрыть", null)
                .show();
    }

    private void confirmLeave() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("⚠️ Выход из семьи")
                .setMessage("Вы уверены, что хотите покинуть семью?")
                .setPositiveButton("Выйти", (d, w) -> leaveFamily())
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void leaveFamily() {
        if (currentFamily == null) return;
        int familyId = extractInt(currentFamily.get("id"));
        if (familyId == 0) return;

        showLoading(true);
        familyManager.leaveFamily(familyId, new FamilyManager.FamilyCallback() {
            @Override
            public void onSuccess(Map<String, Object> data) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    clearLocalFamilyId();
                    showNoFamilyView();
                    Toast.makeText(getContext(), "Вы покинули семью", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Ошибка: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void copyInviteCode() {
        String text = tvInviteCode.getText().toString();
        String code = text.startsWith("Код: ") ? text.substring(5) : text;
        ClipboardManager cm = (ClipboardManager) requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("InviteCode", code));
        Toast.makeText(getContext(), "✅ Код скопирован: " + code, Toast.LENGTH_SHORT).show();
    }

    private int extractInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Integer) return (Integer) v;
        if (v instanceof Double)  return ((Double) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; }
    }

    @Override
    public void onRefresh() { loadFamilyFromServer(); }
}
