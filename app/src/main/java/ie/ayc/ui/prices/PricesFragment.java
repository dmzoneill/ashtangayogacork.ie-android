package ie.ayc.ui.prices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import ie.ayc.R;

public class PricesFragment extends Fragment {

    private PricesViewModel pricesViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        pricesViewModel =
                ViewModelProviders.of(this).get(PricesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_prices, container, false);
        final TextView textView = root.findViewById(R.id.nav_prices);
        pricesViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}
