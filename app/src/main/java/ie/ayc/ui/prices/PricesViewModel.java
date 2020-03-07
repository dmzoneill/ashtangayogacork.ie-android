package ie.ayc.ui.prices;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PricesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PricesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is prices fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}