package armando.app_tfg_armconcal2017;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class EventRecommendActivity extends Activity {
    String idEvent = "";
    String user = "";
    Button join;
    TextView name, description, restaurant, date, price, assistants;
    HttpClient httpclient = new DefaultHttpClient();
    ArrayList<NameValuePair> nameValuePairs;
    HttpPost httppost;
    ArrayList<Object> info = new ArrayList<>();
    ArrayList<Object> number = new ArrayList<>();
    ArrayList<Object> joinChecked = new ArrayList<>();
    Activity ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        if(getIntent().getExtras() != null){
            Bundle b = getIntent().getExtras();
            String i = b.getString("Id");
            String u = b.getString("User");
            idEvent = i;
            user = u;
        }

        name = (TextView) findViewById(R.id.txtEventName);
        description = (TextView) findViewById(R.id.txtEventDescription);
        restaurant = (TextView) findViewById(R.id.txtEventRestaurant);
        date = (TextView) findViewById(R.id.txtEventDate);
        price = (TextView) findViewById(R.id.txtEventPrice);
        assistants = (TextView) findViewById(R.id.txtEventAssistants);
        join = (Button) findViewById(R.id.btnEventJoin);

        new EventRecommendActivity.Info(EventRecommendActivity.this).execute();
        new EventRecommendActivity.CheckAssistance(EventRecommendActivity.this).execute();



    }

    //Se solicita un evento
    public String log() {
        httppost = new HttpPost("http://armconcaltfg.esy.es/php/getEvent.php");
        nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("id", idEvent));
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

    //Se ordena la informacion
    private boolean filter(){
        String data = log();
        if(!data.equalsIgnoreCase("")){
            JSONObject json;
            try{
                json = new JSONObject(data);
                JSONArray jsonArray = json.optJSONArray("info");
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonArrayChild = jsonArray.getJSONObject(i);
                    info.add(jsonArrayChild.optString("name"));
                    info.add(jsonArrayChild.optString("description"));
                    info.add(jsonArrayChild.optString("restaurant"));
                    info.add(jsonArrayChild.optString("date"));
                    info.add(jsonArrayChild.optString("price"));
                    info.add(jsonArrayChild.optString("assistants"));
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    //Se muestra el evento
    public class Info extends AsyncTask<String, Float, String> {

        private Activity ctx;

        Info(Activity ctx){
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            if(filter()){
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        name.setText(info.get(0).toString());
                        description.setText(info.get(1).toString());
                        restaurant.setText(info.get(2).toString());
                        date.setText(info.get(3).toString());
                        price.setText(info.get(4).toString());
                        assistants.setText(info.get(5).toString());
                    }
                });
            }
            return null;
        }
    }


    //Se solicita la asistencia de ese usuario al evento
    public String checkjoin() {
        httppost = new HttpPost("http://armconcaltfg.esy.es/php/checkAssistance.php");
        nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("idE", idEvent));
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
    //Se ordena la informacion
    private boolean joinStatus(){
        String data = checkjoin();
        if(!data.equals("[]\n")){
            JSONObject json;
            try{
                json = new JSONObject(data);
                JSONArray jsonArray = json.optJSONArray("info");
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonArrayChild = jsonArray.getJSONObject(i);
                    joinChecked.add(jsonArrayChild.optString("user"));
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    //Si el usuario va a ese evento el boton es Uncoil
    public class CheckAssistance extends AsyncTask<String, Float, String> {

        private Activity ctx;

        CheckAssistance(Activity ctx){
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            if(joinStatus()){
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String userId = joinChecked.get(0).toString();
                        if(userId.equals(user)){
                            join.setText(R.string.unjoin);
                            join.setOnClickListener(new View.OnClickListener(){

                                @Override
                                public void onClick(View v) {
                                    new DeleteAssistant(EventRecommendActivity.this).execute();
                                }
                            });

                        }
                    }
                });
            }else{
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        join.setText(R.string.join);
                        join.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View v) {
                                new CreateAssistant(EventRecommendActivity.this).execute();
                            }
                        });
                    }
                });
            }
            return null;
        }
    }

    //Se solicita crear una nueva asistencia
    public boolean assistance(){

        httppost = new HttpPost("http://armconcaltfg.esy.es/php/createAssistant.php");
        nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("idE", idEvent));
        nameValuePairs.add(new BasicNameValuePair("user", user));

        try{
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httpclient.execute(httppost);
            return true;
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }catch (ClientProtocolException e) {
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }
    //Si la asistencia se ha realizado con exito se muestra un mensaje y se vuelve a comprobar la asistencia
    public class CreateAssistant extends AsyncTask<String, String, String> {

        private Activity ctx;

        CreateAssistant(Activity ctx){
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            if(assistance()){
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ctx, "you just Join the event", Toast.LENGTH_LONG).show();
                        new UpdateNumber(EventRecommendActivity.this).execute();
                        new CheckAssistance(EventRecommendActivity.this).execute();
                        join.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View v) {
                                new DeleteAssistant(EventRecommendActivity.this).execute();
                            }
                        });
                    }
                });
            }else{
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ctx, "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }
    }

    //Se solicita borrar una asistencia
    public boolean unassistance(){

        httppost = new HttpPost("http://armconcaltfg.esy.es/php/deleteAssistant.php");
        nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("idE", idEvent));
        nameValuePairs.add(new BasicNameValuePair("user", user));

        try{
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httpclient.execute(httppost);
            return true;
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }catch (ClientProtocolException e) {
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return false;
    }

    //Si se eleminia el evento sale un mensaje de exito y se vuelve a comprobar la asistencia
    public class DeleteAssistant extends AsyncTask<String, String, String> {

        private Activity ctx;

        DeleteAssistant(Activity ctx){
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            if(unassistance()){
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ctx, "you just uncoil the event", Toast.LENGTH_LONG).show();
                        new UpdateNumber(EventRecommendActivity.this).execute();
                        new CheckAssistance(EventRecommendActivity.this).execute();
                        join.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View v) {
                                new CreateAssistant(EventRecommendActivity.this).execute();
                            }
                        });
                    }
                });
            }else{
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ctx, "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }
    }

    //Solicita el evento
    public String number() {
        httppost = new HttpPost("http://armconcaltfg.esy.es/php/getEvent.php");
        nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("id", idEvent));
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

    //Se ordena la informacion
    private boolean filterNumber(){
        String data = number();
        if(!data.equalsIgnoreCase("")){
            JSONObject json;
            try{
                json = new JSONObject(data);
                JSONArray jsonArray = json.optJSONArray("info");
                for(int i = 0; i < jsonArray.length(); i++){
                    JSONObject jsonArrayChild = jsonArray.getJSONObject(i);
                    number.add(jsonArrayChild.optString("assistants"));
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    //Se actualiza el numero de asistentes
    public class UpdateNumber extends AsyncTask<String, Float, String> {

        private Activity ctx;

        UpdateNumber(Activity ctx){
            this.ctx = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            if(filterNumber()){
                ctx.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        assistants.setText(number.get(0).toString());
                        number.clear();
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
                    Intent intent = new Intent(ctx, EventsRecommendedActivity.class);
                    Bundle b = getIntent().getExtras();
                    b.putString("Id", idEvent);
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
        new EventRecommendActivity.Back(EventRecommendActivity.this).execute();
    }




}
