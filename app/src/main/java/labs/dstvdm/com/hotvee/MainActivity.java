package labs.dstvdm.com.hotvee;


import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {

    private int NUM_PAGES = 0;
    HotVeePagerAdapter mDemoCollectionPagerAdapter;
    // Progress dialog
    private ProgressDialog pDialog;
    private RequestQueue queue;
    private Bitmap bmp;
    List<Program> progList = new ArrayList<Program>();


    /**
     * The {@link android.support.v4.view.ViewPager} that will display the object collection.
     */
    private ViewPager mViewPager;
    private static String TAG = "HotVee";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_collection_demo);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading items...");
        pDialog.setCancelable(false);

        mDemoCollectionPagerAdapter = new HotVeePagerAdapter(getSupportFragmentManager());

        final ActionBar actionBar = getActionBar();

        // Set up the ViewPager, attaching the adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is not part of the application's task, so create a new task
                    // with a synthesized back stack.
                    TaskStackBuilder.from(this)
                            // If there are ancestor activities, they should be added here.
                            .addNextIntent(upIntent)
                            .startActivities();
                    finish();
                } else {
                    // This activity is part of the application's task, so simply
                    // navigate up to the hierarchical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }



    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public class HotVeePagerAdapter extends FragmentStatePagerAdapter implements Serializable {

        public HotVeePagerAdapter(FragmentManager fm) {
            super(fm);
            getJSONData();
        }

        @Override
        public Fragment getItem(int i) {
//            Program prog = progList.get(i);
//            Log.i(TAG, prog.toString());
            Fragment fragment = new HotVeeObjectFragment();
            Bundle args = new Bundle();
//            args.putSerializable("prog", prog);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            //return progList.size();
            Toast.makeText(getApplicationContext(), String.valueOf(NUM_PAGES), Toast.LENGTH_SHORT).show();
            return NUM_PAGES; //300; //mDemoCollectionPagerAdapter.getCount();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }

        /**
         * Method to make json object request where json response starts wtih {
         */
        private void getJSONData() {
            queue = Volley.newRequestQueue(getApplicationContext());
            showpDialog();
            String url = "http://10.50.100.171:8080/getitems";

            RequestFuture<JSONArray> future = RequestFuture.newFuture();
            JsonArrayRequest req = new JsonArrayRequest(url,new Response.Listener<JSONArray>() {

                @Override
                public void onResponse(JSONArray response) {
                    try {

                        int count = response.length();
                        for (int i = 0; i < count; i++) {
                            JSONObject rec = response.getJSONObject(i);
                            String dbid = rec.getString("id");
                            if (rec.has("program") && !rec.isNull("program")) {
                                JSONObject program = rec.getJSONObject("program");
                                String synopsis = program.getString("synopsis");
                                String title = program.getString("title");
                                JSONObject images = program.getJSONObject("images");
                                JSONObject poster = images.getJSONObject("poster");
                                String imageUrl = poster.getString("MEDIUM");
                                Bitmap image = downloadImage(imageUrl);
                                Program p = new Program(image, synopsis, title);
                                if(!p.equals(null)) {
                                    progList.add(p);
                                    NUM_PAGES += 1;
                                    mDemoCollectionPagerAdapter.notifyDataSetChanged();


                                }

                            }
                        }
                        hidepDialog();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.e("Error: ", error.getMessage());
                    error.printStackTrace();
                    hidepDialog();
                }
            });
            queue.add(req);
        }

        private Bitmap downloadImage(String imageUrl) {
            // Retrieves an image specified by the URL, displays it in the UI.
            ImageRequest request = new ImageRequest(imageUrl,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            bmp = bitmap;
                        }
                    }, 0, 0, null,
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            bmp = null;

                        }
                    });
            queue.add(request);
            return bmp;
        }


    }
}

