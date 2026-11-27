package com.example.tunisiaguide;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_LOCATION_CODE = 1001;

    private RecyclerView recyclerView;
    private DestinationAdapter adapter;
    private List<Destination> destinationList;
    private List<Destination> allDestinations;

    private Button btnFr, btnEn, btnAr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("TuniGuide");
        }

        ImageButton btnMainThemeToggle = findViewById(R.id.btnMainThemeToggle);
        boolean isDarkMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        updateThemeIcon(btnMainThemeToggle, isDarkMode);
        btnMainThemeToggle.setOnClickListener(v -> {
            boolean dark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
            int newMode = dark ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
            AppCompatDelegate.setDefaultNightMode(newMode);
            saveThemePref(newMode);
            recreate();
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        destinationList = new ArrayList<>();
        allDestinations = new ArrayList<>();
        loadDestinations("FR");

        adapter = new DestinationAdapter(this, destinationList, destination -> {
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("destination", destination);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        btnFr = findViewById(R.id.btnFr);
        btnEn = findViewById(R.id.btnEn);
        btnAr = findViewById(R.id.btnAr);
        Button btnNearby = findViewById(R.id.btnNearby);

        updateLanguageButtons("FR");

        btnFr.setOnClickListener(v -> {
            loadDestinations("FR");
            adapter.notifyDataSetChanged();
            updateLanguageButtons("FR");
            Toast.makeText(this, "Langue : Français", Toast.LENGTH_SHORT).show();
        });

        btnEn.setOnClickListener(v -> {
            loadDestinations("EN");
            adapter.notifyDataSetChanged();
            updateLanguageButtons("EN");
            Toast.makeText(this, "Language: English", Toast.LENGTH_SHORT).show();
        });

        btnAr.setOnClickListener(v -> {
            loadDestinations("AR");
            adapter.notifyDataSetChanged();
            updateLanguageButtons("AR");
            Toast.makeText(this, "اللغة : العربية", Toast.LENGTH_SHORT).show();
        });

        btnNearby.setOnClickListener(v -> checkPermissionAndGetLocation());
    }

    private void checkPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_LOCATION_CODE);
        } else {
            getAutomaticGpsLocationAndSort();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_LOCATION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getAutomaticGpsLocationAndSort();
        } else {
            Toast.makeText(this, "Permission GPS requise pour calculer la distance exacte.", Toast.LENGTH_SHORT).show();
            sortDestinationsByGps(36.8065, 10.1815);
        }
    }

    private void getAutomaticGpsLocationAndSort() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location lastKnown = null;
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnown == null) {
                    lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
        }

        double userLat = (lastKnown != null) ? lastKnown.getLatitude() : 36.8065;
        double userLng = (lastKnown != null) ? lastKnown.getLongitude() : 10.1815;

        sortDestinationsByGps(userLat, userLng);
    }

    private void sortDestinationsByGps(double userLat, double userLng) {
        class DestinationDistance implements Comparable<DestinationDistance> {
            Destination destination;
            float distanceKm;

            DestinationDistance(Destination destination, float distanceKm) {
                this.destination = destination;
                this.distanceKm = distanceKm;
            }

            @Override
            public int compareTo(DestinationDistance o) {
                return Float.compare(this.distanceKm, o.distanceKm);
            }
        }

        List<DestinationDistance> listWithDistance = new ArrayList<>();
        for (Destination d : allDestinations) {
            float[] results = new float[1];
            Location.distanceBetween(userLat, userLng, d.getLatitude(), d.getLongitude(), results);
            float distKm = results[0] / 1000f;
            listWithDistance.add(new DestinationDistance(d, distKm));
        }

        Collections.sort(listWithDistance);

        destinationList.clear();
        for (DestinationDistance item : listWithDistance) {
            destinationList.add(item.destination);
        }

        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Localisation GPS automatique réussie ! Lieux triés du plus proche au plus éloigné.", Toast.LENGTH_LONG).show();
    }

    private void updateLanguageButtons(String activeLang) {
        int redColor = ContextCompat.getColor(this, R.color.harissa_red);
        int lightBg = ContextCompat.getColor(this, R.color.tunisian_primary_light);
        int textPrimary = ContextCompat.getColor(this, R.color.text_primary);

        btnFr.setBackgroundTintList(ColorStateList.valueOf(activeLang.equals("FR") ? redColor : lightBg));
        btnFr.setTextColor(activeLang.equals("FR") ? Color.WHITE : textPrimary);

        btnEn.setBackgroundTintList(ColorStateList.valueOf(activeLang.equals("EN") ? redColor : lightBg));
        btnEn.setTextColor(activeLang.equals("EN") ? Color.WHITE : textPrimary);

        btnAr.setBackgroundTintList(ColorStateList.valueOf(activeLang.equals("AR") ? redColor : lightBg));
        btnAr.setTextColor(activeLang.equals("AR") ? Color.WHITE : textPrimary);
    }

    private void updateThemeIcon(ImageButton button, boolean isDarkMode) {
        if (isDarkMode) {
            button.setImageResource(R.drawable.ic_light_mode);
            button.setContentDescription("Passer en mode clair");
        } else {
            button.setImageResource(R.drawable.ic_dark_mode);
            button.setContentDescription("Passer en mode sombre");
        }
    }

    private void saveThemePref(int mode) {
        SharedPreferences prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        prefs.edit().putInt("night_mode", mode).apply();
    }

    private void loadDestinations(String lang) {
        allDestinations.clear();
        
        List<Integer> sbsImages = Arrays.asList(R.drawable.sidibousaid_folder_1, R.drawable.sidibousaid_folder_2, R.drawable.sidibousaid_folder_3, R.drawable.sidibousaid_folder_4);
        List<Integer> carthageImages = Arrays.asList(R.drawable.carthage_folder_1, R.drawable.carthage_folder_2, R.drawable.carthage_folder_3, R.drawable.carthage_folder_4);
        List<Integer> eljemImages = Arrays.asList(R.drawable.eljem, R.drawable.carthage, R.drawable.sousse_1);
        List<Integer> djerbaImages = Arrays.asList(R.drawable.djerba_folder_1, R.drawable.djerba_folder_2, R.drawable.djerba_folder_3, R.drawable.djerba_folder_4);
        List<Integer> hammametImages = Arrays.asList(R.drawable.hammamet, R.drawable.djerba_folder_1, R.drawable.sidibousaid_folder_1);
        List<Integer> tozeurImages = Arrays.asList(R.drawable.tozeur_folder_1, R.drawable.tozeur_folder_2, R.drawable.tozeur_folder_3, R.drawable.tozeur_folder_4, R.drawable.tozeur_folder_5);
        List<Integer> kairouanImages = Arrays.asList(R.drawable.kairouan_1, R.drawable.kairouan);
        List<Integer> tabarkaImages = Arrays.asList(R.drawable.tabarka_1, R.drawable.tabarka_2, R.drawable.tabarka_3, R.drawable.tabarka_4, R.drawable.tabarka_5);
        
        List<Integer> matmataImages = Arrays.asList(R.drawable.matmata_1, R.drawable.matmata_2, R.drawable.matmata_3, R.drawable.matmata_4);
        List<Integer> bizerteImages = Arrays.asList(R.drawable.bizerte_1, R.drawable.bizerte_2, R.drawable.bizerte_3, R.drawable.bizerte_4, R.drawable.bizerte_5);
        List<Integer> sousseImages = Arrays.asList(R.drawable.sousse_1, R.drawable.sousse_2, R.drawable.sousse_3, R.drawable.sousse_4, R.drawable.sousse_5);
        List<Integer> monastirImages = Arrays.asList(R.drawable.monastir_1, R.drawable.monastir_2, R.drawable.monastir_3, R.drawable.monastir_4, R.drawable.monastir_5);

        if (lang.equals("EN")) {
            allDestinations.add(new Destination("Sidi Bou Said", "Famous picturesque blue and white town overlooking the Mediterranean Sea.", "Tunis / Carthage", "Medina / Beach", sbsImages, R.drawable.sidibousaid_folder_plat, 36.8708, 10.3416, "- Ennejma Ezzahra Palace.\n- Lighthouse and panoramic views.", "- Walk through paved alleys.\n- Drink mint tea at Café des Nattes.", "Traditional Dish: Bambalouni (Crispy Tunisian doughnut)."));
            allDestinations.add(new Destination("Carthage Archaeological Site", "Ancient Phoenician and Roman historical ruins near Tunis.", "Tunis", "Historical", carthageImages, 0, 36.8528, 10.3236, "- Antonine Baths.\n- Carthage National Museum on Byrsa Hill.", "- Explore Carthaginian history.\n- Visit St. Louis Cathedral.", "Traditional Dish: Tunisian Couscous with vegetables."));
            allDestinations.add(new Destination("El Djem Amphitheatre", "One of the best-preserved Roman amphitheatres in the world.", "Mahdia / Sousse", "Historical", eljemImages, 0, 35.2992, 10.7056, "- Roman Colosseum.\n- Archaeological Museum.", "- Descend into gladiators' arenas.\n- Climb to the top for city views.", "Traditional Dish: Sahel Fish Couscous & Mloukhia."));
            allDestinations.add(new Destination("Djerba Island", "Island renowned for pristine beaches and whitewashed architecture.", "Djerba / Medenine", "Beach", djerbaImages, 0, 33.8076, 10.8523, "- Houmt Souk market.\n- Djerba Hood street art.", "- Enjoy white sandy beaches.\n- Pottery workshops.", "Traditional Dish: Djerbian Brik with eggs."));
            allDestinations.add(new Destination("Hammamet", "Premier seaside resort famous for fine sandy beaches and medina.", "Nabeul / Hammamet", "Beach / Medina", hammametImages, 0, 36.4000, 10.6167, "- Historic Medina.\n- Waterfront Kasbah.", "- Relax on golden beaches.\n- Wander through medina alleys.", "Traditional Dish: Grilled Rock Fish."));
            allDestinations.add(new Destination("Tozeur & Sahara Oasis", "Magnificent date palm oases and gateway to the desert.", "Tozeur / Southern Tunisia", "Desert / Nature", tozeurImages, 0, 33.9197, 8.1335, "- Old medina.\n- Mountain oases (Chebika).", "- 4x4 desert excursion.\n- Sunset over salt lake.", "Traditional Dish: Fresh Deglet Nour Dates."));
            allDestinations.add(new Destination("Kairouan", "Holy city of Islam, famous for its Great Mosque.", "Kairouan", "Historical / Culture", kairouanImages, R.drawable.kairouan_plat, 35.6781, 10.0963, "- Great Mosque of Okba.\n- Aghlabid Basins.", "- Discover handmade carpets.", "Traditional Dish: Makroudh (Famous Kairouan pastry)."));
            allDestinations.add(new Destination("Tabarka", "Known for rocky needles and Genoese castle.", "Jendouba / Northwest", "Nature / Beach", tabarkaImages, 0, 36.9544, 8.7581, "- Tabarka Needles.\n- Genoese Fort.", "- Scuba diving.\n- Hiking in forests.", "Traditional Dish: Fresh Sea Urchins."));
            allDestinations.add(new Destination("Matmata Troglodyte Village", "Unique underground cave dwellings carved into mountainsides.", "Gabès / Matmata", "Desert / Culture", matmataImages, R.drawable.matmata_plat, 33.5417, 9.9658, "- Troglodyte underground houses.\n- Hotel Sidi Driss (Star Wars).", "- Explore subterranean architecture.\n- Meet Berber families.", "Traditional Dish: Mlaoui bread & Tabouna."));
            allDestinations.add(new Destination("Bizerte Old Port", "Charming northern port city with majestic forts and crystal beaches.", "Bizerte", "Beach / Nature", bizerteImages, R.drawable.bizerte_plat, 37.2744, 9.8739, "- Old fishing port.\n- Spanish Fort.", "- Walk along marina.\n- Swim at Cap Angela.", "Traditional Dish: Sandwich Lablabi & Octopus."));
            allDestinations.add(new Destination("Sousse Ribat & Medina", "Historic coastal city featuring a millennium-old Ribat.", "Sousse", "Historical / Beach", sousseImages, 0, 35.8256, 10.6369, "- Sousse Ribat.\n- Great Mosque.", "- Shop in traditional souks.\n- Relax on Bou Jaafar beach.", "Traditional Dish: Sousse Fish Couscous."));
            allDestinations.add(new Destination("Monastir Ribat & Mausoleum", "Stunning coastal city with a massive fortress.", "Monastir", "Historical / Beach", monastirImages, R.drawable.monastir_plat, 35.7643, 10.8113, "- Ribat of Harthhema.\n- Habib Bourguiba Mausoleum.", "- Climb Ribat tower.\n- Stroll along corniche.", "Traditional Dish: Couscous Cherkaw."));
        } else if (lang.equals("AR")) {
            allDestinations.add(new Destination("سيدي بو سعيد", "قرية ساحرة ذات واجهات زرقاء وبيضاء مطلة على البحر الأبيض المتوسط.", "تونس / قرطاج", "مدينة / شاطئ", sbsImages, R.drawable.sidibousaid_folder_plat, 36.8708, 10.3416, "- قصر النجمة الزاهرة.\n- المنارة والمناظر البانورامية.", "- التمشية في الأزقة المرصوفة.\n- شرب الشاي بمقهى النات.", "طبق تقليدي: البمبالوني التونسي والأسماك المشوية."));
            allDestinations.add(new Destination("موقع قرطاج الأثري", "آثار تاريخية بونية ورومانية قرب تونس العاصمة.", "تونس", "تاريخي", carthageImages, 0, 36.8528, 10.3236, "- حمامات أنطونيوس.\n- متحف قرطاج الوطني.", "- استكشاف تاريخ قرطاج وروما.\n- زيارة كاتدرائية القديس لويس.", "طبق تقليدي: الكسكسي التونسي الأصيل."));
            allDestinations.add(new Destination("مسرح الجم الأثري", "من أروع المسارح الرومانية وأفضلها حفظاً في العالم.", "مهدية / سوسة", "تاريخي", eljemImages, 0, 35.2992, 10.7056, "- قصر الجم الروماني.\n- متحف الجم الأثري.", "- النزول لأقبية المصارعين.\n- صعود المدرجات.", "طبق تقليدي: كسكسي السمك الساحلي والملوخية."));
            allDestinations.add(new Destination("جزيرة جربة", "جزيرة شهيرة بشواطئها الساحرة وهندستها البيضاء.", "جربة / مدنين", "شاطئ", djerbaImages, 0, 33.8076, 10.8523, "- حومة السوق والأسواق.\n- جربة هود للفن.", "- الاستمتاع بالشواطئ الرملية.\n- رحلة بحرية.", "طبق تقليدي: البريك الجربي بالبيض."));
            allDestinations.add(new Destination("الحمامات", "مدينة سياحية رائدة تشتهر بشواطئها الذهبية.", "نابل / حمامات", "شاطئ / مدينة عتيقة", hammametImages, 0, 36.4000, 10.6167, "- المدينة العتيقة.\n- القصبة التاريخية.", "- الاسترخاء على الشواطئ.\n- التجول بالأزقة.", "طبق تقليدي: الأسماك المشوية."));
            allDestinations.add(new Destination("توزر واحات الصحراء", "واحات نخيل خلابة وعمارة بالطوب الطيني.", "توزر / الجنوب التونسي", "صحراء / طبيعة", tozeurImages, 0, 33.9197, 8.1335, "- واحات الجبال الخلابة.\n- ديكور حرب النجوم.", "- رحلة 4x4 في الصحراء.\n- جولة بالعرّابة.", "طبق تقليدي: تمر دقلة النور الطازج."));
            allDestinations.add(new Destination("القيروان", "المدينة المقدسة للإسلام وجامع عقبة.", "القيروان", "تاريخي / ثقافة", kairouanImages, R.drawable.kairouan_plat, 35.6781, 10.0963, "- جامع عقبة بن نافع.\n- فساقية الأغالبة.", "- استكشاف الزجاج القيرواني.", "طبق تقليدي: المقروض القيرواني."));
            allDestinations.add(new Destination("طبرقة", "تشتهر بالصخور الإبرية وقلعة جارية.", "جندوبة / الشمال الغربي", "طبيعة / شاطئ", tabarkaImages, 0, 36.9544, 8.7581, "- صخور الإبر.\n- القلعة الجنووية.", "- الغوص تحت الماء.\n- التنزه بالغابات.", "طبق تقليدي: ثمار البحر الطازجة."));
            allDestinations.add(new Destination("مطماطة", "منازل تحت الأرض منحوتة في الصخور.", "قابس / مطماطة", "صحراء / ثقافة", matmataImages, R.drawable.matmata_plat, 33.5417, 9.9658, "- البيوت الكهفية.\n- فندق ستار وورز.", "- استكشاف الهندسة التحت أرضية.", "طبق تقليدي: الملاوي والخبز التقليدي."));
            allDestinations.add(new Destination("بنزرت", "مدينة ساحلية شمالية ساحرة بميناء قديم.", "بنزرت", "شاطئ / طبيعة", bizerteImages, R.drawable.bizerte_plat, 37.2744, 9.8739, "- الميناء القديم.\n- القصبة.", "- التنزه على الكورنيش.", "طبق تقليدي: سندويش لبلابي وأخطبوط."));
            allDestinations.add(new Destination("سوسة", "مدينة ساحلية تاريخية تضم رباطاً عريقاً.", "سوسة", "تاريخي / شاطئ", sousseImages, 0, 35.8256, 10.6369, "- رباط سوسة.\n- الجامع الكبير.", "- التسوق بالأسواق العتيقة.", "طبق تقليدي: كسكسي السمك."));
            allDestinations.add(new Destination("المنستير", "مدينة ساحلية رائعة وضريح بورقيبة.", "المنستير", "تاريخي / شاطئ", monastirImages, R.drawable.monastir_plat, 35.7643, 10.8113, "- رباط هرثمة.\n- ضريح بورقيبة.", "- صعود برج الرباط.", "طبق تقليدي: Couscous Cherkaw (كسكسي شركاو)."));
        } else {
            // Default French
            allDestinations.add(new Destination("Sidi Bou Said", "Célèbre village pittoresque aux façades bleues et blanches.", "Tunis / Carthage", "Médina / Plage", sbsImages, R.drawable.sidibousaid_folder_plat, 36.8708, 10.3416, "- Le Palais Ennejma Ezzahra.\n- Vues panoramiques.", "- Se promener dans les ruelles.\n- Prendre un thé au Café des Nattes.", "Plat Traditionnel : Bambalouni (beignet) & poisson frais."));
            allDestinations.add(new Destination("Site Archéologique de Carthage", "Vestiges historiques phéniciens et romains près de Tunis.", "Tunis", "Historique", carthageImages, 0, 36.8528, 10.3236, "- Les Thermes d'Antonin.\n- Le Musée national.", "- Explorer l'histoire carthaginoise.\n- Visiter la cathédrale.", "Plat Traditionnel : Couscous tunisien traditionnel."));
            allDestinations.add(new Destination("Amphithéâtre d'El Djem", "L'un des amphithéâtres romains les mieux conservés.", "Mahdia / Sousse", "Historique", eljemImages, 0, 35.2992, 10.7056, "- Le Colisée romain.\n- Le Musée archéologique.", "- Descendre dans les arènes.\n- Monter au sommet.", "Plat Traditionnel : Couscous au poisson & Mloukhia."));
            allDestinations.add(new Destination("Île de Djerba", "Île renommée pour ses plages paradisiaques.", "Djerba / Médenine", "Plage", djerbaImages, 0, 33.8076, 10.8523, "- Houmt Souk et ses souks.\n- Djerba Hood street art.", "- Profiter des plages de sable.\n- Balade en bateau.", "Plat Traditionnel : Brik djerbienne aux œufs."));
            allDestinations.add(new Destination("Hammamet", "Station balnéaire célèbre pour ses plages et sa médina.", "Nabeul / Hammamet", "Plage / Médina", hammametImages, 0, 36.4000, 10.6167, "- La Médina historique.\n- La Kasbah fortifiée.", "- Se détendre sur la plage.\n- Flâner dans les souks.", "Plat Traditionnel : Poisson grillé."));
            allDestinations.add(new Destination("Tozeur & Oasis du Sahara", "Superbes oasis de palmiers dattiers et portes du désert.", "Tozeur / Sud Tunisien", "Désert / Nature", tozeurImages, 0, 33.9197, 8.1335, "- La vieille médina.\n- Oasis de montagne.", "- Excursion en 4x4.\n- Calèche dans la palmeraie.", "Plat Traditionnel : Dattes Deglet Nour fraîches."));
            allDestinations.add(new Destination("Kairouan", "Ville sainte de l'Islam, célèbre pour sa Grande Mosquée.", "Kairouan", "Historique / Culture", kairouanImages, R.drawable.kairouan_plat, 35.6781, 10.0963, "- La Grande Mosquée d'Okba.\n- Bassins des Aghlabides.", "- Explorer l'artisanat des tapis.", "Plat Traditionnel : Makroudh de Kairouan (pâtisserie)."));
            allDestinations.add(new Destination("Tabarka", "Connue pour ses aiguilles rocheuses et son château génois.", "Jendouba / Nord-Ouest", "Nature / Plage", tabarkaImages, 0, 36.9544, 8.7581, "- Les Aiguilles de Tabarka.\n- Le Fort Génois.", "- Plongée sous-marine.\n- Randonnée en forêt.", "Plat Traditionnel : Oursins frais & fruits de mer."));
            allDestinations.add(new Destination("Matmata", "Maisons troglodytes souterraines uniques, décor de Star Wars.", "Gabès / Matmata", "Désert / Culture", matmataImages, R.drawable.matmata_plat, 33.5417, 9.9658, "- Maisons troglodytes.\n- Hôtel Sidi Driss (Star Wars).", "- Explorer l'architecture sous terre.\n- Rencontrer les Berbères.", "Plat Traditionnel : Pain Tabouna & Mlaoui."));
            allDestinations.add(new Destination("Bizerte", "Charmante ville portuaire du nord avec son vieux port.", "Bizerte", "Plage / Nature", bizerteImages, R.drawable.bizerte_plat, 37.2744, 9.8739, "- Le vieux port de pêche.\n- La Kasbah.", "- Se promener le long de la marina.\n- Se baigner à Cap Angela.", "Plat Traditionnel : Sandwich Lablabi & ragoût d'poulpe."));
            allDestinations.add(new Destination("Sousse", "Ville côtière historique avec un Ribat millénaire.", "Sousse", "Historical / Beach", sousseImages, 0, 35.8256, 10.6369, "- Le Ribat de Sousse.\n- La Grande Mosquée.", "- Shopping dans les souks.\n- Plage de Bou Jaafar.", "Plat Traditionnel : Couscous au poisson de Sousse."));
            allDestinations.add(new Destination("Monastir", "Superbe cité côtière avec une immense forteresse.", "Monastir", "Historical / Beach", monastirImages, R.drawable.monastir_plat, 35.7643, 10.8113, "- Le Ribat de Harthhema.\n- Mausolée Bourguiba.", "- Grimper sur la tour du Ribat.\n- Promenade sur la corniche.", "Plat Traditionnel : Couscous Cherkaw (couscous cherkaw)."));
        }
        destinationList.clear();
        destinationList.addAll(allDestinations);
    }
}
