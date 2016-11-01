package com.deepak.jsonandlistviewtask;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyFragment extends Fragment {
    Button b;
    ListView lv;
    ArrayList<Movies> al;
    //ArrayList<String> alname = new ArrayList<String>();
    //ArrayList<String> alchar = new ArrayList<String>();
    StringBuilder alname = new StringBuilder();
    StringBuilder alchar = new StringBuilder();
    MyAdapter myAdapter;
    MyTask myTask;

    public class MyTask extends AsyncTask<String, Void, String>{

        URL myurl;
        HttpURLConnection connection;
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        String line;
        StringBuilder result;

        @Override
        protected String doInBackground(String... p1) {
            try {
                myurl = new URL(p1[0]);
                connection = (HttpURLConnection) myurl.openConnection();
                inputStream = connection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                result = new StringBuilder();

                line = bufferedReader.readLine();

                while (line != null){
                    result.append(line);
                    line = bufferedReader.readLine();
                }
                return ""+result;

            } catch (MalformedURLException e) {
                Log.d("B33", "Check URL..."+e.getMessage());
                Log.d("B33", "Check URL(Cause)..."+e.getCause());
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("B33", "Message.."+e.getMessage());
                Log.d("B33", "Cause.."+e.getCause());
                e.printStackTrace();
            }finally {
                if(connection != null){
                    connection.disconnect();
                    if (inputStream != null){
                        try {
                            inputStream.close();
                            if(inputStreamReader != null){
                                inputStreamReader.close();
                                if(bufferedReader != null){
                                    bufferedReader.close();
                                }
                            }
                        } catch (IOException e) {
                            Log.d("B33", "PROBLEM IN CLOSING CONNECTION");
                            e.printStackTrace();
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            if(s == null){
                Toast.makeText(getActivity(), "NETWORK ISSUE, FIX", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                JSONObject j = new JSONObject(s);
                JSONArray arr = j.getJSONArray("movies");

                for(int i = 0; i < arr.length(); i++) {
                    JSONObject temp = arr.getJSONObject(i);
                    String title = temp.getString("title");

                    JSONObject release = temp.getJSONObject("release_dates");
                    String theaterDate = "";
                    if(release.has("theater")) {
                        theaterDate = release.getString("theater");
                    }else{
                        theaterDate = "Not Available";
                    }

                    JSONArray cast = temp.getJSONArray("abridged_cast");
                    String name = null;
                    String character = null;
                    Movies m = new Movies();

                    if(cast.length() >= 0) {

                        for (int k = 0; k < cast.length(); k++) {

                            JSONObject temp1 = cast.getJSONObject(k);
                            if(temp1.has("name")) {
                                name = temp1.getString("name");
                            }else name = "Unknown";

                            //Toast.makeText(getActivity(), ""+temp1.getString("name")+"\n"+temp1.getJSONArray("characters").getString(0), Toast.LENGTH_SHORT).show();

                            if(temp1.has("characters")) {
                                character = temp1.getJSONArray("characters").getString(0);
                            } else character = "Unknown";

                            alname.append(name +" : "+ character + "\n");
                            //alchar.append(character + "\n");
                        }
                    }
                    //Toast.makeText(getActivity(), ""+alname, Toast.LENGTH_SHORT).show();

                    m.setMtitle(title);
                    m.setMtheatre(theaterDate);
                    m.setMname(alname.toString());
                    //m.setMcharacters(alchar.toString());

                    //alname.clear(); alchar.clear();
                    alname.setLength(0);alchar.setLength(0);

                    al.add(m);
                    myAdapter.notifyDataSetChanged();
                }

            } catch (JSONException e) {
                Log.d("B33", "JSON Exception.."+ e.getMessage());
                Log.d("B33", "JSON Cause.."+ e.getCause());
                e.printStackTrace();
            }

            super.onPostExecute(s);
        }
    }

    public class MyAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return al.size();
        }

        @Override
        public Object getItem(int position) {
            return al.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Movies m = al.get(position);

            View v = getActivity().getLayoutInflater().inflate(R.layout.row, null);
            TextView tv1 = (TextView) v.findViewById(R.id.textView1);
            TextView tv2 = (TextView) v.findViewById(R.id.textView2);
            TextView tv3 = (TextView) v.findViewById(R.id.textView3);
            //TextView tv4 = (TextView) v.findViewById(R.id.textView4);

            tv1.setText(m.getMtitle());
            tv2.setText(m.getMtheatre());
            if(m.getMname() != null)
            tv3.setText(m.getMname());
            else tv3.setVisibility(View.INVISIBLE);

            /*if(m.getMcharacters() != null)
            tv4.setText(m.getMcharacters());
            else tv4.setVisibility(View.INVISIBLE);*/

            return v;
        }
    }

    public MyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my, container, false);
        b = (Button) view.findViewById(R.id.button1);
        lv = (ListView) view.findViewById(R.id.listView1);
        al = new ArrayList<Movies>();
        myAdapter = new MyAdapter();
        myTask = new MyTask();
        lv.setAdapter(myAdapter);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInternet() == true){
                    myTask.execute("http://api.rottentomatoes.com/api/public/v1.0/movies.json?q=titanic&apikey=ny97sdcpqetasj8a4v2na8va");
                }else{
                    Toast.makeText(getActivity(), "NO INTERNET", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    public boolean checkInternet(){
        ConnectivityManager conn = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conn != null){
            NetworkInfo info = conn.getActiveNetworkInfo();
            if(info != null && info.isConnected()){
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }
}