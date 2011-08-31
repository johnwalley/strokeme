package org.walley.strokeme.test;

import org.walley.strokeme.StrokeMeActivity;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

public class StrokeMeTest extends
		ActivityInstrumentationTestCase2<StrokeMeActivity> {
	
    private StrokeMeActivity mActivity;
    private TextView mView;
    private String resourceString;

	public StrokeMeTest() {
		super("org.walley.strokeme", StrokeMeActivity.class);
		// TODO Auto-generated constructor stub
	}
	
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = this.getActivity();
        mView = (TextView) mActivity.findViewById(org.walley.strokeme.R.id.rate);
        resourceString = mActivity.getString(org.walley.strokeme.R.string.rate);
    }
    
    public void testPreconditions() {
        assertNotNull(mView);
    }
    
    public void testText() {
        assertEquals(resourceString,(String)mView.getText());
    }

}
