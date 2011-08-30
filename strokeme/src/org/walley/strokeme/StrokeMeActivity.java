package org.walley.strokeme;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.lang.Math;

import jama.Matrix;
import jkalman.JKalman;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class StrokeMeActivity extends Activity implements OnClickListener {
    // For logging and debugging purposes
    private static final String TAG = "StrokeMeActivity";
    
    private static final double resetPeriod = 3.0;
    private static final int maxHistory = 3;
	
	private TextView rateView;
	private ListView historyView;
	
	private JKalman kalman;
	
    Matrix state; // state [x, dx]        
    Matrix correction; // corrected state [x, dx]                 
    Matrix measurement; // measurement [z]
	
	int numStrokes; // Number of strokes recorded in current sequence
	
	double strokeRate; // The current estimate of the stroke rate
	
	private List<String> history; // Array holding history of most recent stroke rates
	private ArrayAdapter<String> historyAdapter;
	
	long lastTime; // time of last stroke

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        rateView = (TextView)findViewById(R.id.rate);
        rateView.setOnClickListener(this);
        
        historyView = (ListView)findViewById(R.id.history);
        history = new ArrayList<String>();
        historyAdapter = new ArrayAdapter<String>(this, R.layout.history_item, history);
        historyView.setAdapter(historyAdapter);
        
        numStrokes = 0; // Just started. No taps recorded yet
        
        lastTime = System.currentTimeMillis(); // Need a valid value but this is never used to estimate stroke rate

        // Instantiate Kalman filter implementation
        try {
			kalman = new JKalman(2, 1);
            kalman.setProcess_noise_cov(kalman.getProcess_noise_cov().identity());
            kalman.setMeasurement_noise_cov(kalman.getMeasurement_noise_cov().identity());	
		} catch (Exception e) {
            Log.e(TAG, e.getMessage());
		}
        
        state = new Matrix(2, 1); // state [x, dx]        
        measurement = new Matrix(1, 1); // measurement [z]
    }
    
    
    // Tap received
    public void onClick(View v) {   	
    	numStrokes++;
    	
    	// Calculate time since last stroke (in seconds)
    	long currentTime = System.currentTimeMillis();
    	double dt = (double)(currentTime-lastTime)/1000.0;
    	
    	// If sufficient time has passed we treat this stroke as the first in a new sequence
    	if (dt>resetPeriod & numStrokes>2) {
    		numStrokes = 1;
    		
    		DateFormat formatter = new SimpleDateFormat("h:mm a");
    		Calendar calendar = Calendar.getInstance();

    		formatter.format(calendar.getTime());    		
    		
    		// Really should be a bounded Queue but this works
    		historyAdapter.insert(String.format("%2.0f - %s", strokeRate, formatter.format(calendar.getTime())), 0); // Insert at beginning of list
    		if (historyAdapter.getCount()>maxHistory) {
    			historyAdapter.remove(historyAdapter.getItem(maxHistory-1)); // Remove oldest history item if over limit
    		}
    		
    	}
    	
    	switch (numStrokes) {
    	case 1:
    		state.set(0, 0, 0.0);
    		state.set(1, 0, Double.NaN);
    		Log.i(TAG, "dx:" + state.get(1, 0));
    		break;    		
    	case 2:
    		state.set(0, 0, 1.0);
    		state.set(1, 0, 1.0/dt);
    		// Seed filter with initial state estimate. Assumes first two measurements are unbiased
    		kalman.setState_post(state);
    		Log.i(TAG, "dx:" + state.get(1, 0));
    		break;
    	default:
			// Set up filter
            kalman.setTransition_matrix(new Matrix(new double[][] { {1, dt}, {0, 1} }));
            
            kalman.Predict();
            
            measurement.set(0, 0, (double)numStrokes-1.0);
            
            state = kalman.Correct(measurement);
            
            Log.i(TAG, "dx:" + state.get(1, 0));
    	}
    	
    	strokeRate = 60.0*state.get(1, 0);
    	    	
    	if (Double.isNaN(strokeRate)) {
    		rateView.setText("..."); 
    	} else {
    		rateView.setText(String.format("%2.0f", strokeRate)); 
    	}
    	
    	lastTime = currentTime;
    	
    }
}