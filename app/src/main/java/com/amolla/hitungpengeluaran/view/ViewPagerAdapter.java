package com.amolla.hitungpengeluaran.view;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.amolla.hitungpengeluaran.view.fragment.pemasukan.PemasukanFragment;
import com.amolla.hitungpengeluaran.view.fragment.pengeluaran.PengeluaranFragment;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

   public ViewPagerAdapter(FragmentManager manager) {
      super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
   }

   @Override
   public Fragment getItem(int position) {
      Fragment fragment = null;

      switch (position) {
         case 0:
            fragment = new PengeluaranFragment();
            break;
         case 1:
            fragment = new PemasukanFragment();
            break;
      }
      return fragment;
   }

   @Override
   public int getCount() {
      return 2;
   }

   @Override
   public CharSequence getPageTitle(int position) {
      String strTitle = "";
      switch (position) {
         case 0:
            strTitle = "Pengeluaran";
            break;
         case 1:
            strTitle = "Pemasukan";
            break;
      }
      return strTitle;
   }
}