package com.example.tunisiaguide;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DestinationAdapter adapter;
    private List<Destination> destinationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Guidini - Guide Touristique");
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        destinationList = new ArrayList<>();
        loadDestinations();

        adapter = new DestinationAdapter(this, destinationList, destination -> {
            Toast.makeText(this, "Sélectionné : " + destination.getTitle(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadDestinations() {
        destinationList.add(new Destination(
                "Sidi Bou Said",
                "Famous picturesque blue and white town overlooking the Mediterranean Sea near Tunis.",
                "Tunis / Carthage",
                "Médina / Plage",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/8/82/Sidi_Bou_Said_-_Tunisia_2.jpg/800px-Sidi_Bou_Said_-_Tunisia_2.jpg",
                36.8708, 10.3416
        ));
        destinationList.add(new Destination(
                "Carthage Archaeological Site",
                "Ancient Phoenician and Roman ruins with rich historical significance near Tunis.",
                "Tunis",
                "Historique",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/6/62/Carthage_Byrsa_01.jpg/800px-Carthage_Byrsa_01.jpg",
                36.8528, 10.3236
        ));
        destinationList.add(new Destination(
                "El Djem Amphitheatre",
                "One of the best-preserved Roman amphitheatre ruins in the world, located in Mahdia governorate.",
                "Mahdia / Sousse",
                "Historique",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5a/El_Jem_Amphitheatre_-_Tunisia.jpg/800px-El_Jem_Amphitheatre_-_Tunisia.jpg",
                35.2992, 10.7056
        ));
        destinationList.add(new Destination(
                "The Sahara Desert (Douz)",
                "Known as the 'Gateway to the Sahara', offering camel treks and vast sand dunes.",
                "Douz / Kébili",
                "Désert",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e4/Sahara_dunes_%28Douz%2C_Tunisia%2C_2005%2C_by_Carlo_Nicoletti%29.jpg/800px-Sahara_dunes_%28Douz%2C_Tunisia%2C_2005%2C_by_Carlo_Nicoletti%29.jpg",
                33.4667, 9.0167
        ));
        destinationList.add(new Destination(
                "Djerba Island",
                "An island renowned for its pristine beaches, whitewashed architecture, and vibrant culture.",
                "Djerba / Medenine",
                "Plage",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Djerba_houmt_souk.jpg/800px-Djerba_houmt_souk.jpg",
                33.8076, 10.8523
        ));
        destinationList.add(new Destination(
                "Sbeïtla (Sufetula)",
                "Remarkable Roman ruins featuring well-preserved temples and a triumphal arch near Kasserine / Sidi Bouzid.",
                "Sidi Bouzid / Kasserine",
                "Historique",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Sbeitla_Capitol.jpg/800px-Sbeitla_Capitol.jpg",
                35.2417, 9.1258
        ));
    }
}
