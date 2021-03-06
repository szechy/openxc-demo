package com.openxc.openxcstarter;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.openxc.VehicleManager;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.UnrecognizedMeasurementTypeException;
import com.openxc.measurements.EngineSpeed;
import com.openxc.remote.VehicleServiceException;

import de.passsy.holocircularprogressbar.HoloCircularProgressBar;

public class StarterActivity extends Activity {
    private static final String TAG = "StarterActivity";

    private VehicleManager mVehicleManager;
    private TextView mEngineSpeedView;
    private TextView mFuelPercentage;
    private ProgressBar pb;
    private ObjectAnimator mProgressBarAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        // grab a reference to the engine speed text object in the UI, so we can
        // manipulate its value later from Java code
        mEngineSpeedView = (TextView) findViewById(R.id.vehicle_speed);
        mFuelPercentage = (TextView) findViewById(R.id.fuel_percentage);
        pb = (ProgressBar) findViewById(R.id.circularProgBar);
        //Animation an = new RotateAnimation(0.0f, 135.0f, 250f, 273f);
        //an.setFillAfter(true);
       // pb.startAnimation(an);
    }

    @Override
    public void onPause() {
        super.onPause();
        // When the activity goes into the background or exits, we want to make
        // sure to unbind from the service to avoid leaking memory
        if(mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");
            try {
                // Remember to remove your listeners, in typical Android
                // fashion.
                mVehicleManager.removeListener(EngineSpeed.class, mSpeedListener);
                mVehicleManager.removeListener(FuelLevel.class, mFuelListener);
            } catch (VehicleServiceException e) {
                e.printStackTrace();
            }
            unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the activity starts up or returns from the background,
        // re-connect to the VehicleManager so we can receive updates.
        if(mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /* This is an OpenXC measurement listener object - the type is recognized
     * by the VehicleManager as something that can receive measurement updates.
     * Later in the file, we'll ask the VehicleManager to call the receive()
     * function here whenever a new EngineSpeed value arrives.
     */
    EngineSpeed.Listener mSpeedListener = new EngineSpeed.Listener() {
        public void receive(Measurement measurement) {
            // When we receive a new EngineSpeed value from the car, we want to
            // update the UI to display the new value. First we cast the generic
            // Measurement back to the type we know it to be, an EngineSpeed.
            final EngineSpeed speed = (EngineSpeed) measurement;
            // In order to modify the UI, we have to make sure the code is
            // running on the "UI thread" - Google around for this, it's an
            // important concept in Android.
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    // Finally, we've got a new value and we're running on the
                    // UI thread - we set the text of the EngineSpeed view to
                    // the latest value
                    mEngineSpeedView.setText("Engine speed (RPM): "
                            + (float)speed.getValue().doubleValue());
                    pb.setProgress(speed.getValue().intValue());
                }
            });
        }
    };
    
    FuelLevel.Listener mFuelListener = new FuelLevel.Listener() {

		@Override
		//After receiving new FuelLevel value, update UI to display new value
		//Cast pack to FuelLevel, then jump to UI thread to modify UI
		public void receive(Measurement measurement) {
			final FuelLevel fuel = (FuelLevel) measurement;
			
			StarterActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					mFuelPercentage.setText("Fuel percentage is at: " + 
							(float)fuel.getValue().intValue());
					//set TextView to latest percentage value
				}
			});
			
		}
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the VehicleManager service is established, i.e. bound.
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            // When the VehicleManager starts up, we store a reference to it
            // here in "mVehicleManager" so we can call functions on it
            // elsewhere in our code.
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            // We want to receive updates whenever the EngineSpeed changes. We
            // have an EngineSpeed.Listener (see above, mSpeedListener) and here
            // we request that the VehicleManager call its receive() method
            // whenever the EngineSpeed changes
            try {
                mVehicleManager.addListener(EngineSpeed.class, mSpeedListener);
            } catch (VehicleServiceException e) {
                e.printStackTrace();
            } catch (UnrecognizedMeasurementTypeException e) {
                e.printStackTrace();
            }
            
            try {
            	mVehicleManager.addListener(FuelLevel.class, mFuelListener);
            }
            catch (VehicleServiceException e) {
            	e.printStackTrace();
            }
            catch (UnrecognizedMeasurementTypeException e) {
            	e.printStackTrace();
            }
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.starter, menu);
        return true;
    }

	/**
	 * Animate.
	 * 
	 * @param progressBar
	 *            the progress bar
	 * @param listener
	 *            the listener
	 */
	private void animate(final HoloCircularProgressBar progressBar, final AnimatorListener listener) {
		final float progress = (float) (Math.random() * 2);
		int duration = 3000;
		animate(progressBar, listener, progress, duration);
	}
	
	private void animate(final HoloCircularProgressBar progressBar, final AnimatorListener listener,
			final float progress, final int duration) {
	
		mProgressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", progress);
		mProgressBarAnimator.setDuration(duration);
	
		mProgressBarAnimator.addListener(new AnimatorListener() {
	
			@Override
			public void onAnimationCancel(final Animator animation) {
			}
	
			@Override
			public void onAnimationEnd(final Animator animation) {
				progressBar.setProgress(progress);
			}
	
			@Override
			public void onAnimationRepeat(final Animator animation) {
			}
	
			@Override
			public void onAnimationStart(final Animator animation) {
			}
		});
		if (listener != null) {
			mProgressBarAnimator.addListener(listener);
		}
		mProgressBarAnimator.reverse();
		mProgressBarAnimator.addUpdateListener(new AnimatorUpdateListener() {
	
			@Override
			public void onAnimationUpdate(final ValueAnimator animation) {
				progressBar.setProgress((Float) animation.getAnimatedValue());
			}
		});
		progressBar.setMarkerProgress(progress);
		mProgressBarAnimator.start();
	}
}
