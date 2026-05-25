package com.example.bitbusters.activities.asesor;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter que provee los tres fragments de citas al ViewPager2.
 *
 * Soporta dos constructores:
 * - {@link FragmentActivity}: para usar en una Activity directamente.
 * - {@link Fragment}: para usar cuando el ViewPager2 vive dentro de un Fragment
 *   (usa childFragmentManager, evitando fugas de lifecycle).
 */
public class CitasPagerAdapter extends FragmentStateAdapter {

    /** Usar cuando el ViewPager2 está directamente en una Activity. */
    public CitasPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /** Usar cuando el ViewPager2 está dentro de un Fragment (Navigation Component). */
    public CitasPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @Override
    public int getItemCount() {
        return 3;  // Pendientes · Confirmadas · Pasadas
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:  return new ConfirmadasFragment();
            case 2:  return new PasadasFragment();
            default: return new PendientesFragment();
        }
    }
}
