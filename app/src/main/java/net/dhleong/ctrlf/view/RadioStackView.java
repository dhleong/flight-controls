package net.dhleong.ctrlf.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.ui.NavComView;
import net.dhleong.ctrlf.util.Named;
import rx.Observer;
import rx.functions.Action1;

import javax.inject.Inject;

/**
 * @author dhleong
 */
public class RadioStackView extends LinearLayout {

    @InjectView(R.id.navcom1) NavComView navCom1;

    @Inject @Named("COM1") Observer<Integer> com1Observer;

    public RadioStackView(final Context context) {
        this(context, null);
    }

    public RadioStackView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setOrientation(VERTICAL);
    }

    @Override
    protected void onFinishInflate() {
        // inject
        ButterKnife.inject(this);
        App.provideComponent(this)
           .newRadioStackComponent()
           .inject(this);

        // bind/init
        navCom1.comStandbyFrequencies()
                .doOnNext(new Action1<Integer>() {
                    @Override
                    public void call(final Integer integer) {
                        Log.v("RSV", "Notifying com1 standby: " + integer);
                    }
                })
               .subscribe(com1Observer);

        Log.v("RSV", "forward to: " + com1Observer);
    }
}
