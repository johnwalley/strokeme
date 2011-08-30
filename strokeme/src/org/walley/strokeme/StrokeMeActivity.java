package org.walley.strokeme;

import jama.Matrix;
import jkalman.JKalman;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class StrokeMeActivity extends Activity implements OnClickListener {
	
	private TextView rateView;
	private JKalman kalman;
	
	int numStrokes; // Number of strokes recorded in current sequence
	
	double strokeRate; // The current estimate of the stroke rate
	
	long lastTime; // time of last stroke
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        rateView = (TextView)findViewById(R.id.rate);
        rateView.setOnClickListener(this);
        
        numStrokes = 0; // Just started. No taps recorded yet

        try {
			kalman = new JKalman(2, 1);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
    }
    
    
    // Tap received
    public void onClick(View v) {
    	numStrokes++;
    	
    	// Calculate time since last stroke (in seconds)
    	long currentTime = System.currentTimeMillis();
    	double dt = (double)(currentTime-lastTime)/1000.0;
    	
    	switch (numStrokes) {
    	case 1:
    		break;    		
    	default:
    		strokeRate = 60.0/dt;
    		rateView.setText(String.format("%2.0f", strokeRate));
    	}
    	
    	lastTime = currentTime;
    	
    	
      
    }
}