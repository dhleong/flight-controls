package net.dhleong.ctrlf.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import net.dhleong.ctrlf.App;
import net.dhleong.ctrlf.model.EngineStatus;
import net.dhleong.ctrlf.model.SimEvent;
import net.dhleong.ctrlf.ui.MagnetoSwitchView;
import net.dhleong.ctrlf.ui.MagnetoSwitchView.MagnetoMode;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Contains and manages the Magnetos switches
 *  for the current aircraft (if any)
 *
 * @author dhleong
 */
public class MagnetosView
        extends LinearLayout
        implements Action1<EngineStatus> {

    @Inject Observer<SimEvent> magnetoSetter;
    @Inject Observable<EngineStatus> engineStatus;

    final CompositeSubscription subscriptions = new CompositeSubscription();

    public MagnetosView(final Context context) {
        this(context, null);
    }

    public MagnetosView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        App.provideComponent(this)
           .newEngineComponent()
           .inject(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        initSubscriptions();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        subscriptions.unsubscribe();
    }

    @Override
    public void call(final EngineStatus engineStatus) {
        switch (engineStatus.type) {
        case JET:
        case NONE:
            // these won't have magnetos
            // TODO others?
            setVisibility(GONE);
            removeAllViews();
            return;
        }

        if (getChildCount() > 0) {
            // we already have kids! reset subscriptions
            //  and clear them out
            subscriptions.clear();
            initSubscriptions();
            removeAllViews();
            return;
        }

        for (int i=0; i < engineStatus.engines; i++) {
            final MagnetoSwitchView view = new MagnetoSwitchView(getContext());
            view.setCurrentMode(engineStatus.getMagnetoMode(i));

            final int magnetoIndex = i;
            subscriptions.add(
                view.modeChanges()
                    .flatMapIterable(new Func1<MagnetoMode, Collection<SimEvent>>() {
                        @Override
                        public Collection<SimEvent> call(final MagnetoMode magnetoMode) {
                            return SimEvent.getMagnetoEvents(magnetoIndex, magnetoMode);
                        }
                    })
                    .subscribe(magnetoSetter)
            );

            addView(view);
        }

        // if we have no kids, there were no engines;
        //  adjust visibility accordingly
        setVisibility(getChildCount() == 0 ? GONE : VISIBLE);
    }

    private void initSubscriptions() {
        subscriptions.add(
                engineStatus.observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this)
        );
    }

}
