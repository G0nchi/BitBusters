package com.example.bitbusters.activities.asesor;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bitbusters.R;
import com.example.bitbusters.databinding.FragmentCitasViewpagerBinding;
import com.example.bitbusters.utils.AsesorStorage;
import com.google.android.material.button.MaterialButton;

/**
 * Fragment que aloja el ViewPager2 con los tres tabs de citas.
 *
 * Es el destino inicial del nav_citas.xml. Al hacer clic en "Ver detalle"
 * dentro de un tab, NavController navega a VerDetalleCitaFragment.
 *
 * Usa childFragmentManager para el FragmentStateAdapter (patrón correcto
 * cuando ViewPager2 vive dentro de un Fragment).
 */
public class CitasViewPagerFragment extends Fragment {

    private FragmentCitasViewpagerBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCitasViewpagerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Adapter usa `this` (Fragment) → childFragmentManager internamente
        CitasPagerAdapter adapter = new CitasPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        // Sincronizar tab buttons con swipe
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                highlightTab(position);
                AsesorStorage.saveCitaTab(requireContext(), position);
            }
        });

        // Tab buttons → cambiar página
        binding.tabPendientes.setOnClickListener(v  -> binding.viewPager.setCurrentItem(0, true));
        binding.tabConfirmadas.setOnClickListener(v -> binding.viewPager.setCurrentItem(1, true));
        binding.tabPasadas.setOnClickListener(v     -> binding.viewPager.setCurrentItem(2, true));

        // Restaurar último tab
        int lastTab = AsesorStorage.getCitaTab(requireContext());
        binding.viewPager.setCurrentItem(lastTab, false);
        highlightTab(lastTab);
    }

    private void highlightTab(int tab) {
        int activeColor   = Color.parseColor("#252B5C");
        int inactiveColor = Color.TRANSPARENT;
        int activeText    = Color.WHITE;
        int inactiveText  = Color.parseColor("#AACDE0");

        applyTab(binding.tabPendientes,  tab == 0, activeColor, inactiveColor, activeText, inactiveText);
        applyTab(binding.tabConfirmadas, tab == 1, activeColor, inactiveColor, activeText, inactiveText);
        applyTab(binding.tabPasadas,     tab == 2, activeColor, inactiveColor, activeText, inactiveText);
    }

    private void applyTab(MaterialButton btn, boolean active,
                          int activeBg, int inactiveBg, int activeText, int inactiveText) {
        btn.setBackgroundTintList(ColorStateList.valueOf(active ? activeBg : inactiveBg));
        btn.setTextColor(active ? activeText : inactiveText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
