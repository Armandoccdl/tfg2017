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
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class RestaurantsRecommendedActivity extends Activity {

    String user = "";
    String idRestaurant;

    ListView list;
    HttpClient httpclient = new DefaultHttpClient();
    HttpPost httppost;
    ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
    ArrayList<Integer> ids = new ArrayList<>();
    Restaurant restaurant;
    ArrayList<NameValuePair> nameValuePairs;
    ArrayList<Object> info = new ArrayList<>();
    Activity ctx = this;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurants_recommended);

        Bundle b = getIntent().getExtras();
        String u = b.getString("User");
        user = u;

        new RestaurantsRecommendedActivity.List(RestaurantsRecommendedActivity.this).execute();
        new RestaurantsRecommendedActivity.List2(RestaurantsRecommendedActivity.this).execute();

        list = (ListView) findViewById(R.id.listRestaurantsRecommended);



        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Restaurant restaurant = (Restaurant) list.getAdapter().getItem(position);
                idRestaurant = String.valueOf(restaurant.getId());
                Intent intent = new Intent(ctx, RestaurantRecommendActivity.class);
                Bundle b = getIntent().getExtras();
                b.putString("Id", idRestaurant);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });


    }






    //Solicitamos los restaurantes del filtrado colaborativo
    public String log() {
        httppost = new HttpPost("http://armconcaltfg.esy.es/php/getRestaurantsColabFilter.php");
        nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("user", user));
        HttpResponse response;
        String result = "";

        try {
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
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

    //Ordenamos la informacion
    private boolean filter(){
        String data = log();
        if(!data.equalsIgnoreCase("")){
            JSONObject json;
            try{
                json = new JSONObject(data);
                JSONArray jsonArray = json.optJSONArray("info");
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonArrayChild = jsonArray.getJSONObject(i);
                    ids.add(jsonArrayChild.optInt("restaurantId"));
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    //Conseguimos los restaurantes con las ids de los filtrado colaborativo
    public String log2() {
        if(filter()){
            httppost = new HttpPost("http://armconcaltfg.esy.es/php/getFilterRestaurants.php");
            nameValuePairs = new ArrayList<NameValuePair>(4);
            nameValuePairs.add(new BasicNameValuePair("id1", ids.get(0).toString()));
            nameValuePairs.add(new BasicNameValuePair("id2", ids.get(1).toString()));
            nameValuePairs.add(new BasicNameValuePair("id3", ids.get(2).toString()));
            nameValuePairs.add(new BasicNameValuePair("id4", ids.get(3).toString()));
            HttpResponse response;
            String result = "";

            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
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
       return "";
    }

    //Creamos una lista de restaurantes con esos restaurantes
    public boolean getRestaurantsList(){
        restaurants.clear();
        String data = log2();
        if(!data.equalsIgnoreCase("")){
            JSONObject json;
            try{
                json = new JSONObject(data);
                JSONArray jsonArray = json.optJSONArray("info");
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonArrayChild = jsonArray.getJSONObject(i);
                    restaurant = new Restaurant(jsonArrayChild.optInt("id"),jsonArrayChild.optInt("likes"),jsonArrayChild.optInt("dislikes"),jsonArrayChild.optString("name"),jsonArrayChild.optString("phone"));
                    restaurants.add(restaurant);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    //Conseguimos los restaurantes del filtrado por conocimiento
    public String log3() {
        if(filter()){
            httppost = new HttpPost("http://armconcaltfg.esy.es/php/getRestaurantsKnowledgeFilter.php");
            nameValuePairs = new ArrayList<NameValuePair>(4);
            nameValuePairs.add(new BasicNameValuePair("user", user));
            HttpResponse response;
            String result = "";

            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
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
        return "";
    }

    //hacemos una lista de restaurantes con los restaurantes del filtrado por conocimiento
    public boolean getRestaurantsList2(){
        String data = log3();
        ArrayList <Integer>  in = new ArrayList<>();
        if(!data.equalsIgnoreCase("")){
            JSONObject json;
            try{
                json = new JSONObject(data);
                JSONArray jsonArray = json.optJSONArray("info");
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonArrayChild = jsonArray.getJSONObject(i);
                    restaurant = new Restaurant(jsonArrayChild.optInt("id"),jsonArrayChild.optInt("likes"),jsonArrayChild.optInt("dislikes"),jsonArrayChild.optString("name"),jsonArrayChild.optString("phone"));
                    for(Restaurant r : restaurants){
                        in.add(r.getId());
                    }
                    if(!in.contains(restaurant.getId())){
                        restaurants.add(restaurant);
                    }
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    //Se muestran los restaurantes del filtrado colaborativo
    public class List extends AsyncTask<String, Float, String> {

        private Activity ctx;

        List(Activity ctx){
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            if(getRestaurantsList()){
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.setAdapter(new RestaurantsAdapter(RestaurantsRecommendedActivity.this, R.layout.row_restaurant, restaurants) {
                            @Override
                            public void onEntrance(Object entrance, View view) {
                                if (entrance != null) {
                                    TextView nameR = (TextView) view.findViewById(R.id.txtRowRestaurantName);
                                    if (nameR != null)
                                        nameR.setText(""+((Restaurant) entrance).getName());

                                    TextView phoneR = (TextView) view.findViewById(R.id.txtRowRestaurantPhone);
                                    if (phoneR != null)
                                        phoneR.setText(""+ ((Restaurant) entrance).getPhone());

                                    TextView likesR = (TextView) view.findViewById(R.id.txtRowRestaurantLikes);
                                    if (likesR != null)
                                        likesR.setText(""+((Restaurant) entrance).getLikes());

                                    TextView dislikesR = (TextView) view.findViewById(R.id.txtRowRestaurantDislikes);
                                    if (dislikesR != null)
                                        dislikesR.setText(""+((Restaurant) entrance).getDislikes());
                                }
                            }
                        });
                    }
                });
            }
            return null;
        }
    }

    //Se muestran los restaurantes del filtrado por conocimiento
    public class List2 extends AsyncTask<String, Float, String> {

        private Activity ctx;

        List2(Activity ctx){
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            if(getRestaurantsList2()){
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.setAdapter(new RestaurantsAdapter(RestaurantsRecommendedActivity.this, R.layout.row_restaurant, restaurants) {
                            @Override
                            public void onEntrance(Object entrance, View view) {
                                if (entrance != null) {
                                    TextView nameR = (TextView) view.findViewById(R.id.txtRowRestaurantName);
                                    if (nameR != null)
                                        nameR.setText(""+((Restaurant) entrance).getName());

                                    TextView phoneR = (TextView) view.findViewById(R.id.txtRowRestaurantPhone);
                                    if (phoneR != null)
                                        phoneR.setText(""+ ((Restaurant) entrance).getPhone());

                                    TextView likesR = (TextView) view.findViewById(R.id.txtRowRestaurantLikes);
                                    if (likesR != null)
                                        likesR.setText(""+((Restaurant) entrance).getLikes());

                                    TextView dislikesR = (TextView) view.findViewById(R.id.txtRowRestaurantDislikes);
                                    if (dislikesR != null)
                                        dislikesR.setText(""+((Restaurant) entrance).getDislikes());
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
                    Intent intent = new Intent(ctx, RestaurantsActivity.class);
                    Bundle b = getIntent().getExtras();
                    b.putString("Id", idRestaurant);
                    b.putString("User", user);
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                }
            });

            return null;
        }
    }


    public void onBackPressed() {
        new RestaurantsRecommendedActivity.Back(RestaurantsRecommendedActivity.this).execute();
    }




}
