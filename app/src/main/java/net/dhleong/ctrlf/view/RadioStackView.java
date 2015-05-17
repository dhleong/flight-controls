package net.dhleong.ctrlf.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.ui.NavComView;
import net.dhleong.ctrlf.util.Named;
import rx.Observer;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

/**
 * @author dhleong
 */
public class RadioStackView extends LinearLayout {

    @InjectView(R.id.navcom1) NavComView navCom1;

    @Inject @Named("COM1") Observer<Integer> com1Observer;

    private CompositeSubscription subscriptions = new CompositeSubscription();

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
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // bind/init
        subscriptions.add(
                navCom1.comStandbyFrequencies()
                       .subscribe(com1Observer));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        subscriptions.unsubscribe();
    }
}
