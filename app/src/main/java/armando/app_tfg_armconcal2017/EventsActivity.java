package armando.app_tfg_armconcal2017;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class EventsActivity extends Activity{
    String user = "";
    String idEvent;

    ListView list;
    HttpClient httpclient = new DefaultHttpClient();
    HttpPost httppost;
    ArrayList<EventList> events = new ArrayList<EventList>();
    EventList event;
    Activity ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        if(getIntent().getExtras() != null){
            Bundle b = getIntent().getExtras();
            String u = b.getString("User");
            user = u;
        }

        new List(EventsActivity.this).execute();

        list = (ListView) findViewById(R.id.listEvents);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final EventList event = (EventList) list.getAdapter().getItem(position);
                idEvent = String.valueOf(event.getId());
                Intent intent = new Intent(ctx, EventActivity.class);
                Bundle b = getIntent().getExtras();
                b.putString("Id", idEvent);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });
    }

    public boolean getEventsList(){
        events.clear();
        String data = log();
        if(!data.equalsIgnoreCase("")){
            JSONObject json;
            try{
                json = new JSONObject(data);
                JSONArray jsonArray = json.optJSONArray("info");
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonArrayChild = jsonArray.getJSONObject(i);
                    event = new EventList(jsonArrayChild.optInt("id"),jsonArrayChild.optInt("restaurant"),jsonArrayChild.optString("name"),jsonArrayChild.optString("Date"),jsonArrayChild.optDouble("price"));
                    events.add(event);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public String log() {
        httppost = new HttpPost("http://armconcaltfg.esy.es/php/getRestaurants.php");
        HttpResponse response;
        String result = "";

        try {
            response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            result = convertStreamToString(instream);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public class List extends AsyncTask<String, Float, String> {

        private Activity ctx;

        List(Activity ctx){
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            if(getEventsList()){
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.setAdapter(new EventsAdapter(EventsActivity.this, R.layout.row_event, events) {
                            @Override
                            public void onEntrance(Object entrance, View view) {
                                if (entrance != null) {
                                    TextView nameR = (TextView) view.findViewById(R.id.txtRowRestaurantName);
                                    if (nameR != null)
                                        nameR.setText(""+((RestaurantList) entrance).getName());

                                    TextView phoneR = (TextView) view.findViewById(R.id.txtRowRestaurantPhone);
                                    if (phoneR != null)
                                        phoneR.setText(""+ ((RestaurantList) entrance).getPhone());

                                    TextView likesR = (TextView) view.findViewById(R.id.txtRowRestaurantLikes);
                                    if (likesR != null)
                                        likesR.setText(""+((RestaurantList) entrance).getLikes());

                                    TextView dislikesR = (TextView) view.findViewById(R.id.txtRowRestaurantDislikes);
                                    if (dislikesR != null)
                                        dislikesR.setText(""+((RestaurantList) entrance).getDislikes());
                                }
                            }
                        });
                    }
                });
            }
            return null;
        }
    }

    public String convertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        }else{
            return "";
        }
    }


    public class Back extends AsyncTask<String, Float, String> {

        private Activity ctx;


        Back(Activity ctx){
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            ctx.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(ctx, MenuActivity.class);
                    Bundle b = getIntent().getExtras();
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                }
            });

            return null;
        }
    }

    public void onBackPressed() {
        new Back(EventsActivity.this).execute();
    }

}