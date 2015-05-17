package net.dhleong.ctrlf.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.R;
import net.dhleong.ctrlf.model.RadioStatus;
import net.dhleong.ctrlf.ui.NavComView;
import net.dhleong.ctrlf.util.Named;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

/**
 * @author dhleong
 */
public class RadioStackView
        extends LinearLayout
        implements Action1<RadioStatus> {

    @InjectView(R.id.navcom1) NavComView navCom1;

    @Inject Observable<RadioStatus> radioStatus;
    @Inject @Named("COM1Swap") Observer<Void> com1SwapObserver;
    @Inject @Named("COM1Standby") Observer<Integer> com1Observer;

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
                       .subscribe(com1Observer)
        );
        subscriptions.add(
                navCom1.comFrequencySwaps()
                       .subscribe(com1SwapObserver)
        );
        subscriptions.add(
                radioStatus.observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this)
        );
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        subscriptions.unsubscribe();
    }

    @Override
    public void call(final RadioStatus radioStatus) {
        navCom1.setComFrequency(radioStatus.com1Active);
        navCom1.setComStandbyFrequency(radioStatus.com1Standby);
        navCom1.setEnabled(radioStatus.avionicsMaster);
    }
}
