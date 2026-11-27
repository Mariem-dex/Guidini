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
        List<Destination> listCopy = new ArrayList<>(allDestinations);

        Collections.sort(listCopy, (d1, d2) -> {
            float[] results1 = new float[1];
            Location.distanceBetween(userLat, userLng, d1.getLatitude(), d1.getLongitude(), results1);

            float[] results2 = new float[1];
            Location.distanceBetween(userLat, userLng, d2.getLatitude(), d2.getLongitude(), results2);

            return Float.compare(results1[0], results2[0]);
        });

        destinationList.clear();
        destinationList.addAll(listCopy);
        adapter.notifyDataSetChanged();

        float[] closestDist = new float[1];
        Location.distanceBetween(userLat, userLng, destinationList.get(0).getLatitude(), destinationList.get(0).getLongitude(), closestDist);
        int km = Math.round(closestDist[0] / 1000f);

        Toast.makeText(this, "Lieux triés par proximité ! Le plus proche : " + destinationList.get(0).getTitle() + " (" + km + " km)", Toast.LENGTH_LONG).show();
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
        
        List<Integer> sbsImages = Arrays.asList(R.drawable.sidibousaid, R.drawable.sidibousaid_folder_1, R.drawable.sidibousaid_folder_2, R.drawable.sidibousaid_folder_3, R.drawable.sidibousaid_folder_4);
        List<Integer> carthageImages = Arrays.asList(R.drawable.carthage, R.drawable.carthage_folder_1, R.drawable.carthage_folder_2, R.drawable.carthage_folder_3, R.drawable.carthage_folder_4);
        List<Integer> eljemImages = Arrays.asList(R.drawable.eljem);
        List<Integer> djerbaImages = Arrays.asList(R.drawable.djerba, R.drawable.djerba_folder_1, R.drawable.djerba_folder_2, R.drawable.djerba_folder_3, R.drawable.djerba_folder_4);
        List<Integer> hammametImages = Arrays.asList(R.drawable.hammamet);
        List<Integer> tozeurImages = Arrays.asList(R.drawable.tozeur, R.drawable.tozeur_folder_1, R.drawable.tozeur_folder_2, R.drawable.tozeur_folder_3, R.drawable.tozeur_folder_4, R.drawable.tozeur_folder_5);
        List<Integer> kairouanImages = Arrays.asList(R.drawable.kairouan, R.drawable.kairouan_1);
        List<Integer> tabarkaImages = Arrays.asList(R.drawable.tabarka, R.drawable.tabarka_1, R.drawable.tabarka_2, R.drawable.tabarka_3, R.drawable.tabarka_4, R.drawable.tabarka_5);
        
        List<Integer> matmataImages = Arrays.asList(R.drawable.matmata_1, R.drawable.matmata_2, R.drawable.matmata_3, R.drawable.matmata_4);
        List<Integer> bizerteImages = Arrays.asList(R.drawable.bizerte_1, R.drawable.bizerte_2, R.drawable.bizerte_3, R.drawable.bizerte_4, R.drawable.bizerte_5);
        List<Integer> sousseImages = Arrays.asList(R.drawable.sousse_1, R.drawable.sousse_2, R.drawable.sousse_3, R.drawable.sousse_4, R.drawable.sousse_5);
        List<Integer> monastirImages = Arrays.asList(R.drawable.monastir_1, R.drawable.monastir_2, R.drawable.monastir_3, R.drawable.monastir_4, R.drawable.monastir_5);

        if (lang.equals("EN")) {
            allDestinations.add(new Destination("Sidi Bou Said", "Famous picturesque blue and white town overlooking the Mediterranean Sea.", "Tunis / Carthage", "Medina / Beach", sbsImages, R.drawable.sidibousaid_folder_plat, 36.8708, 10.3416, 
                "- Ennejma Ezzahra Palace (Center of Arab and Mediterranean Music).\n- Café des Nattes and Café Sidi Chabaane.\n- Dar El Annabi, 18th-century traditional house.\n- Lighthouse and panoramic views of the Gulf of Tunis.\n- Marina and the zaouïa of Sidi Bou Saïd.", 
                "- Wander through paved alleys and photograph studded blue doors.\n- Drink mint tea with pine nuts overlooking the sea.\n- Taste a hot Bambalouni (Tunisian doughnut).\n- Look for decorative birdcages and local crafts.\n- Admire the sunset from the terraces.", 
                "Traditional Dish: Bambalouni (Crispy Tunisian doughnut)."));
            
            allDestinations.add(new Destination("Carthage Archaeological Site", "Ancient Phoenician and Roman historical ruins near Tunis.", "Tunis", "Historical", carthageImages, 0, 36.8528, 10.3236, 
                "- Antonine Baths by the sea.\n- Byrsa Hill and National Museum of Carthage.\n- Saint Louis Cathedral (Acropolium).\n- Punic ports and Tophet sanctuary.\n- Roman theater and Roman villas.", 
                "- Take the TGM train to Carthage stations and explore on foot.\n- Discover Punic and Roman history at Byrsa Museum.\n- Climb Byrsa Hill for views over the gulf and ruins.\n- Attend a concert or exhibition at the Acropolium.\n- End the visit watching sunset at Antonine Baths.", 
                ""));
            
            allDestinations.add(new Destination("El Djem Amphitheatre", "One of the best-preserved Roman amphitheatres in the world.", "Mahdia / Sousse", "Historical", eljemImages, 0, 35.2992, 10.7056, 
                "- Roman Amphitheatre (3rd century, UNESCO World Heritage).\n- El Djem Archaeological Museum and mosaics.\n- House of Africa Roman villa.\n- Vestiges of ancient Thysdrus around the city.", 
                "- Climb the tiers for a view over the city.\n- Descend into the underground where gladiators and beasts waited.\n- Admire Roman mosaics at the museum.\n- Attend the International Symphonic Music Festival in summer.\n- Photograph the amphitheatre at sunset.", 
                ""));
            
            allDestinations.add(new Destination("Djerba Island", "Island renowned for pristine beaches and whitewashed architecture.", "Djerba / Medenine", "Beach", djerbaImages, 0, 33.8076, 10.8523, 
                "- Houmt Souk market and Borj El Kebir fort.\n- El Ghriba Synagogue (Erriadh).\n- Erriadh village and Djerbahood street art.\n- Guellala Museum (popular traditions).\n- Ras Taguermess lighthouse.", 
                "- Swim at Sidi Mahrez beach.\n- Participate in a pottery workshop in Guellala.\n- Observe flamingos in the lagoon.\n- Take a horse or camel ride.\n- Bargain in the souks of Houmt Souk.", 
                ""));
            
            allDestinations.add(new Destination("Hammamet", "Premier seaside resort famous for fine sandy beaches and medina.", "Nabeul / Hammamet", "Beach / Medina", hammametImages, 0, 36.4000, 10.6167, 
                "- Medina and Kasbah (15th-century fort).\n- George Sebastian International Cultural Center (Villa Sebastian).\n- Yasmine Hammamet and Medina Mediterranea.\n- Carthageland amusement park.\n- Fine sandy bay beaches.", 
                "- Relax on fine sandy beaches.\n- Try jet-skiing or paddleboarding.\n- Enjoy thalassotherapy wellness.\n- Play golf in Yasmine Hammamet.\n- Attend the International Hammamet Festival in summer.", 
                ""));
            
            allDestinations.add(new Destination("Tozeur & Sahara Oasis", "Magnificent date palm oases and gateway to the desert.", "Tozeur / Southern Tunisia", "Desert / Nature", tozeurImages, R.drawable.tozeur_plat, 33.9197, 8.1335, 
                "- Ouled El Hadef medina with brick architecture.\n- Tozeur palm grove.\n- Dar Cherait Museum.\n- Chott el-Jerid immense salt lake.\n- Mountain oases of Chebika, Tamerza, and Mides.", 
                "- Explore the palm grove by horse-drawn carriage.\n- Take a 4x4 excursion to Chebika, Tamerza, and Mides.\n- Discover Ong Jmel, Star Wars set near Nefta.\n- Take a camel ride in the dunes.\n- Admire the sunset over Chott el-Jerid.", 
                "Traditional Dish: Fresh Deglet Nour Dates & Sand-baked Bread."));
            
            allDestinations.add(new Destination("Kairouan", "Holy city of Islam, famous for its Great Mosque.", "Kairouan", "Historical / Culture", kairouanImages, R.drawable.kairouan_plat, 35.6781, 10.0963, 
                "- Great Mosque of Kairouan (Sidi Okba).\n- Aghlabid Basins.\n- Mosque of the Three Doors.\n- Zaouia of Sidi Sahab (Barber Mosque).\n- Barrouta Well.", 
                "- Walk through the UNESCO-listed medina.\n- Discover carpet weaving and copper craft workshops.\n- See Barrouta Well and its traditional drawing system.\n- Visit Raqqada Museum of Islamic Art.\n- Wear modest clothing to enter places of worship.", 
                "Traditional Dish: Makroudh (Famous Kairouan pastry)."));
            
            allDestinations.add(new Destination("Tabarka", "Known for rocky needles and Genoese castle.", "Jendouba / Northwest", "Nature / Beach", tabarkaImages, 0, 36.9544, 8.7581, 
                "- Tabarka Needles (rock formations sculpted by sea).\n- Genoese Fort on the islet.\n- Tabarka Archaeological Museum.\n- Ain Draham cork oak forest.\n- City port and beach.", 
                "- Scuba dive or snorkel to discover marine life.\n- Hike in the forests of Ain Draham.\n- Play seaside golf.\n- Climb the Genoese fort for sunset.\n- Swim at the city beach.", 
                ""));
            
            allDestinations.add(new Destination("Matmata Troglodyte Village", "Unique underground cave dwellings carved into mountainsides.", "Gabès / Matmata", "Desert / Culture", matmataImages, R.drawable.matmata_plat, 33.5417, 9.9658, 
                "- Traditional troglodyte houses.\n- Hotel Sidi Driss (Star Wars filming location).\n- Inhabited Berber house (home visit).\n- Berber village of Tamezret.\n- Village of Toujane.", 
                "- Descend into circular courtyards and discover subterranean architecture.\n- Have lunch in a troglodyte house.\n- Meet Berber families and taste tabouna bread.\n- Hike in the mountains towards Tamezret and Toujane.\n- Photograph lunar landscapes.", 
                "Traditional Dish: Mlaoui bread & Tabouna."));
            
            allDestinations.add(new Destination("Bizerte Old Port", "Charming northern port city with majestic forts and crystal beaches.", "Bizerte", "Beach / Nature", bizerteImages, R.drawable.bizerte_plat, 37.2744, 9.8739, 
                "- Old Port and its colorful quays.\n- Kasbah and Spanish Fort.\n- Medina and its alleys.\n- Oceanographic Museum.\n- Ichkeul National Park (UNESCO).", 
                "- Walk along the Old Port quay at sunset.\n- Swim at Rimel beach or corniche.\n- Observe migratory birds at Ichkeul in winter.\n- Go to Cap Angela, northernmost point of Africa.\n- Enjoy fresh fish lunch at the port.", 
                "Traditional Dish: Sandwich Lablabi & Octopus."));
            
            allDestinations.add(new Destination("Sousse Ribat & Medina", "Historic coastal city featuring a millennium-old Ribat.", "Sousse", "Historical / Beach", sousseImages, 0, 35.8256, 10.6369, 
                "- Medina (UNESCO) and its souks.\n- Ribat (8th century).\n- Great Mosque.\n- Archaeological Museum in Kasbah and Khalef Tower.\n- Catacombs.", 
                "- Climb Ribat tower for medina views.\n- Bargain in the souks.\n- Swim at Boujaafar beach.\n- Walk and dine at Port El Kantaoui.\n- Admire Roman mosaics at Kasbah Museum.", 
                ""));
            
            allDestinations.add(new Destination("Monastir Ribat & Mausoleum", "Stunning coastal city with a massive fortress.", "Monastir", "Historical / Beach", monastirImages, R.drawable.monastir_plat, 35.7643, 10.8113, 
                "- Ribat of Monastir (8th century).\n- Habib Bourguiba Mausoleum.\n- Great Mosque.\n- Traditional Costume Museum.\n- Marina and corniche.", 
                "- Climb Ribat tower for panoramic views.\n- Stroll along corniche and marina.\n- Swim at Skanes beach.\n- Take a boat trip to Kuriat Islands.\n- Discover the medina and its market.", 
                "Traditional Dish: Couscous Cherkaw."));
        } else if (lang.equals("AR")) {
            allDestinations.add(new Destination("سيدي بو سعيد", "قرية ساحرة ذات واجهات زرقاء وبيضاء مطلة على البحر الأبيض المتوسط.", "تونس / قرطاج", "مدينة / شاطئ", sbsImages, R.drawable.sidibousaid_folder_plat, 36.8708, 10.3416, 
                "- قصر النجمة الزاهرة (مركز الموسيقى العربية والتونسية).\n- مقهى النات ومقهى سيدي الشباغي.\n- دار العنابي (منزل تقليدي من القرن 18).\n- المنارة والمناظر البانورامية على خليج تونس.\n- الميناء الترفيهي وزاوية سيدي بو سعيد.", 
                "- التمشية في الأزقة المرصوفة وتصوير الأبواب الزرقاء.\n- شرب الشاي بالبندق مع إطلالة على البحر.\n- تذوق البمبالوني الساخن (الزلابية التونسية).\n- البحث عن قفاص العصافير التقليدية والصناعات اليدوية.\n- الاستمتاع بغروب الشمس من الشرفات المطلة على البحر.", 
                "طبق تقليدي: البمبالوني التونسي والأسماك المشوية."));
            
            allDestinations.add(new Destination("موقع قرطاج الأثري", "آثار تاريخية بونية ورومانية ذات أهمية كبرى قرب تونس العاصمة.", "تونس", "تاريخي", carthageImages, 0, 36.8528, 10.3236, 
                "- حمامات أنطونيوس الرومانية على شاطئ البحر.\n- هضبة بيرسا ومتحف قرطاج الوطني.\n- كاتدرائية القديس لويس.\n- الموانئ البونية ومعبد توفيت.\n- المسرح الروماني والفيلا الرومانية.", 
                "- ركوب قطار TGM حتى محطات قرطاج وزيارة الآثار سيراً على الأقدام.\n- اكتشاف التاريخ البوني والروماني في متحف بيرسا.\n- الصعود إلى هضبة بيرسا للاستمتاع بالإطلالة على الخليج.\n- حضور حفلات أو معارض في الأكروبوليوم.\n- إنهاء الجولة بمشاهدة غروب الشمس في حمامات أنطونيوس.", 
                ""));
            
            allDestinations.add(new Destination("مسرح الجم الأثري", "من أروع المسارح الرومانية وأفضلها حفظاً في العالم.", "مهدية / سوسة", "تاريخي", eljemImages, 0, 35.2992, 10.7056, 
                "- قصر الجم الروماني (الكولوسيوم من القرن الثالث).\n- متحف الجم الأثري وفسيفسائه الفريدة.\n- منزل أفريقيا (فيلا رومانية).\n- بقايا مدينة ثيسدروس القديمة.", 
                "- الصعود إلى المدرجات للاستمتاع برؤية المدينة.\n- النزول إلى الأقبية حيث كان ينتظر المصارعون والوحوش.\n- إعجاب بالفسيفساء الرومانية في المتحف.\n- حضور مهرجان الموسيقى السفونية الدولي صيفاً.\n- زيارة الموقع بالقطار والتقاط صور للغروب.", 
                ""));
            
            allDestinations.add(new Destination("جزيرة جربة", "جزيرة شهيرة بشواطئها الساحرة، هندستها البيضاء وثقافتها العريقة.", "جربة / مدنين", "شاطئ", djerbaImages, 0, 33.8076, 10.8523, 
                "- حومة السوق: السوق التقليدي وبرج الكبير.\n- كنيسة الغريبة اليهودية (الرياض).\n- قرية الرياض ولوحات جربة هود الفنية.\n- متحف قلالة (التقاليد الشعبية).\n- منارة راس تقورمس.", 
                "- السباحة في شاطئ سيدي محرز.\n- المشاركة في ورشة صناعة الفخار بقلالة.\n- مراقبة طيور الفلامينغو في السبخة.\n- القيام بجولة على الأقواس أو ركوب الجمال.\n- التسوق في أسواق حومة السوق.", 
                ""));
            
            allDestinations.add(new Destination("الحمامات", "مدينة سياحية رائدة تشتهر بشواطئها الذهبية ومدينتها العتيقة.", "نابل / حمامات", "شاطئ / مدينة عتيقة", hammametImages, 0, 36.4000, 10.6167, 
                "- المدينة العتيقة والقصبة التاريخية (قرن 15).\n- المركز الثقافي الدولي (فيلا جورج سيباستيان).\n- ياسمين الحمامات والمدينة المتوسطية.\n- مدينة الترفيه كارثاج لاند.\n- شواطئ الخليج الرملية الناعمة.", 
                "- الاسترخاء على الشواطئ الرملية.\n- ممارسة الرياضات المائية كالجيت سكي.\n- الاستمتاع بالعلاجات في مراكز اللاسوتيرابي.\n- لعب الجولف في ياسمين الحمامات.\n- حضور مهرجان الحمامات الدولي صيفاً.", 
                ""));
            
            allDestinations.add(new Destination("توزر واحات الصحراء", "واحات نخيل خلابة وعمارة بالطوب الطيني وبوابة الصحراء الكبرى.", "توزر / الجنوب التونسي", "صحراء / طبيعة", tozeurImages, R.drawable.tozeur_plat, 33.9197, 8.1335, 
                "- مدينة أولاد الهادف العتيقة وعماراتها الفريدة.\n- واحة توزر الكبرى.\n- متحف دار الشرايط.\n- شط الجريد (بحيرة ملحية عملاقة).\n- واحات الجبال بالشبيكة، تامرزة وميداس.", 
                "- استكشاف واحة النخيل بالعربة التقليدية (الكلّيشة).\n- القيام برحلة رباعية الدفع نحو الشبيكة وتامرزة.\n- اكتشاف أونق الجمل موقع تصوير حرب النجوم.\n- ركوب الجمال وسط الكثبان الرملية.\n- مراقبة غروب الشمس الساحر على شط الجريد.", 
                "طبق تقليدي: تمر دقلة النور الطازج والخبز المبشور في الرمال."));
            
            allDestinations.add(new Destination("القيروان", "المدينة المقدسة للإسلام، تشتهر بجامع عقبة بن نافع ومعالمها التاريخية.", "القيروان", "تاريخي / ثقافة", kairouanImages, R.drawable.kairouan_plat, 35.6781, 10.0963, 
                "- جامع عقبة بن نافع الكبير (سيدي عقبة).\n- فساقية الأغالبة.\n- جامع الأبواب الثلاثة.\n- زاوية سيدي الصحبي (جامع الباربي).\n- بئر باروطة.", 
                "- التمشية في المدينة العتيقة المسجلة في اليونسكو.\n- اكتشاف ورش الزرابي والصناعات النحاسية التقليدية.\n- زيارة بئر باروطة ونظام الجلب التقليدي للمياه.\n- زيارة متحف الفن الإسلامي برقادة.\n- ارتداء لباس محتشم عند دخول المعالم الدينية.", 
                "طبق تقليدي: المقروض القيرواني بالعسل والتمر."));
            
            allDestinations.add(new Destination("طبرقة", "تشتهر بالصخور الإبرية، قاع البحر الاستثنائي وقلعتها الجنووية المنيعة.", "جندوبة / الشمال الغربي", "طبيعة / شاطئ", tabarkaImages, 0, 36.9544, 8.7581, 
                "- صخور الإبر (التكوينات الصخرية المنحوتة بالبحر).\n- القلعة الجنووية فوق الجزيرة الصغيرة.\n- متحف طبرقة الأثري.\n- غابات الفلين في عين دراهم.\n- الميناء وشاطئ المدينة.", 
                "- الغوص لاكتشاف الحياة البحرية والشعاب المرجانية.\n- التنزه في غابات عين دراهم الخضراء.\n- لعب الجولف قرب البحر.\n- صعود القلعة الجنووية وقت غروب الشمس.\n- السباحة في شاطئ المدينة.", 
                ""));
            
            allDestinations.add(new Destination("مطماطة", "منازل تحت الأرض منحوتة في الصخور، موقع تصوير شهير لأفلام حرب النجوم.", "قابس / مطماطة", "صحراء / ثقافة", matmataImages, R.drawable.matmata_plat, 33.5417, 9.9658, 
                "- البيوت الكهفية التقليدية المنحوتة تحت الأرض.\n- فندق سيدي إدريس (موقع تصوير حرب النجوم).\n- منزل بربري مأهول (زيارة عائلية).\n- القرية الأمازيغية تمازرت.\n- قرية توجان الجبلية.", 
                "- النزول إلى الحافات الدائرية واكتشاف الهندسة تحت الأرض.\n- تناول الغداء في منزل كهفي تقليدي.\n- لقاء العائلات الأمازيغية وتذوق خبز الطبونة.\n- التنزه في الجبال نحو تمازرت وتوجان.\n- تصوير المناظر الطبيعية الفريدة.", 
                "طبق تقليدي: الملاوي بزيت الزيتون والخبز التقليدي (الطبونة)."));
            
            allDestinations.add(new Destination("بنزرت", "مدينة ساحلية شمالية ساحرة تضم ميناءً قديمًا قديماً وقلاعاً حربية.", "بنزرت", "شاطئ / طبيعة", bizerteImages, R.drawable.bizerte_plat, 37.2744, 9.8739, 
                "- الميناء القديم بأرصفته الملونة.\n- القصبة والحصن الإسباني.\n- المدينة العتيقة وأزقتها.\n- المتحف الأوقيانوغرافي.\n- حديقة إشكل الوطنية (اليونسكو).", 
                "- التمشية على رصيف الميناء القديم عند غروب الشمس.\n- السباحة في شاطئ الرمال أو الكورنيش.\n- مراقبة الطيور المهاجرة في إشكل شتاءً.\n- زيارة رأس أنجلة أقصى نقطة شمال إفريقيا.\n- تناول الأسماك الطازجة بالميناء.", 
                "طبق تقليدي: سندويش لبلابي وأطباق الأخطبوط البحري."));
            
            allDestinations.add(new Destination("سوسة", "مدينة ساحلية تاريخية تضم رباطاً يعود للقرون الوسطى ومدينتها العتيقة.", "سوسة", "تاريخي / شاطئ", sousseImages, 0, 35.8256, 10.6369, 
                "- المدينة العتيقة المسجلة في اليونسكو.\n- الرباط التاريخي (القرن الثامن).\n- الجامع الكبير.\n- المتحف الأثري وبرج خلف.\n- السراديب التاريخية.", 
                "- الصعود لبرج الرباط لمشاهدة المدينة.\n- التسوق في الأسواق التقليدية.\n- السباحة في شاطئ بوجعفر.\n- التنزه وتناول العشاء في بورت القنطاوي.\n- الإعجاب بفسيفساء متحف القصبة.", 
                ""));
            
            allDestinations.add(new Destination("المنستير", "مدينة ساحلية رائعة تضم قلعة ضخمة وضريح الرئيس الحبيب بورقيبة.", "Monastir", "Historical / Beach", monastirImages, R.drawable.monastir_plat, 35.7643, 10.8113, 
                "- رباط المنستير التاريخي (القرن الثامن).\n- ضريح الزعيم الحبيب بورقيبة.\n- الجامع الكبير.\n- متحف اللباس التقليدي.\n- المارينا والكورنيش.", 
                "- صعود برج الرباط للإطلالة البانورامية.\n- المشي على الكورنيش والمارينا.\n- السباحة في شاطئ السكانيس.\n- القيام برحلة بحرية نحو جزر الكوريانا.\n- اكتشاف المدينة العتيقة وسوقها.", 
                "طبق تقليدي: كسكسي شركاو التقليدي."));
        } else {
            // Default French
            allDestinations.add(new Destination("Sidi Bou Said", "Célèbre village pittoresque aux façades bleues et blanches.", "Tunis / Carthage", "Médina / Plage", sbsImages, R.drawable.sidibousaid_folder_plat, 36.8708, 10.3416, 
                "- Le Palais Ennejma Ezzahra (Centre des musiques arabes et méditerranéennes).\n- Le Café des Nattes et le Café Sidi Chabaane.\n- Dar El Annabi, maison traditionnelle du XVIIIe siècle.\n- Le phare et les vues panoramiques sur le golfe de Tunis.\n- Le port de plaisance et la zaouïa de Sidi Bou Saïd.", 
                "- Flâner dans les ruelles pavées et photographier les portes bleues cloutées.\n- Boire un thé aux pignons avec vue sur la mer.\n- Goûter un bambalouni chaud (beignet tunisien).\n- Chercher des cages à oiseaux décoratives et de l'artisanat local.\n- Admirer le coucher de soleil depuis les terrasses.", 
                "Plat Traditionnel : Bambalouni (beignet) & poisson frais."));
            
            allDestinations.add(new Destination("Site Archéologique de Carthage", "Vestiges historiques phéniciens et romains près de Tunis.", "Tunis", "Historique", carthageImages, 0, 36.8528, 10.3236, 
                "- Les thermes d'Antonin en bord de mer.\n- La colline de Byrsa et le Musée national de Carthage.\n- La cathédrale Saint-Louis (Acropolium).\n- Les ports puniques et le tophet.\n- Le théâtre romain et les villas romaines.", 
                "- Prendre le TGM jusqu'aux stations de Carthage et relier les sites à pied.\n- Découvrir l'histoire punique et romaine au musée de Byrsa.\n- Monter sur la colline de Byrsa pour la vue sur le golfe et les ruines.\n- Assister à un concert ou à une exposition à l'Acropolium.\n- Terminer la visite par le coucher de soleil aux thermes d'Antonin.", 
                ""));
            
            allDestinations.add(new Destination("Amphithéâtre d'El Djem", "L'un des amphithéâtres romains les mieux conservés.", "Mahdia / Sousse", "Historique", eljemImages, 0, 35.2992, 10.7056, 
                "- L'amphithéâtre romain (IIIe siècle, classé à l'UNESCO).\n- Le musée archéologique d'El Djem et ses mosaïques.\n- La Maison d'Africa, villa romaine.\n- Les vestiges de l'ancienne Thysdrus autour de la ville.", 
                "- Monter dans les gradins pour la vue sur la ville.\n- Descendre dans les souterrains où attendaient gladiateurs et fauves.\n- Admirer les mosaïques romaines au musée.\n- Assister, en été, au Festival international de musique symphonique.\n- Venir en train ou en louage depuis Sousse ou Mahdia et photographier l'amphithéâtre au coucher du soleil.", 
                ""));
            
            allDestinations.add(new Destination("Île de Djerba", "Île renommée pour ses plages paradisiaques.", "Djerba / Médenine", "Plage", djerbaImages, 0, 33.8076, 10.8523, 
                "- Houmt Souk : le marché et le fort Borj El Kebir.\n- La synagogue de la Ghriba (Erriadh).\n- Erriadh et ses fresques Djerbahood.\n- Le musée de Guellala (traditions populaires).\n- Le phare de Ras Taguermess.", 
                "- Se baigner à la plage de Sidi Mahrez.\n- Participer à un atelier de poterie à Guellala.\n- Observer les flamants roses dans la lagune.\n- Faire une balade à cheval ou à dos de dromadaire.\n- Marchander dans les souks de Houmt Souk.", 
                ""));
            
            allDestinations.add(new Destination("Hammamet", "Station balnéaire célèbre pour ses plages et sa médina.", "Nabeul / Hammamet", "Plage / Médina", hammametImages, 0, 36.4000, 10.6167, 
                "- La médina et la Kasbah (fort du XVe siècle).\n- Le Centre culturel international (villa Sebastian).\n- Yasmine Hammamet et sa Medina Mediterranea.\n- Carthageland, parc d'attractions.\n- Les plages de sable fin de la baie.", 
                "- Se détendre sur les plages de sable fin.\n- Faire du jet-ski ou du paddle.\n- Se faire chouchouter en thalassothérapie.\n- Jouer au golf à Yasmine Hammamet.\n- Assister au Festival international de Hammamet (en été) ou offrir un bouquet de jasmin.", 
                ""));
            
            allDestinations.add(new Destination("Tozeur & Oasis du Sahara", "Superbes oasis de palmiers dattiers et portes du désert.", "Tozeur / Sud Tunisien", "Désert / Nature", tozeurImages, R.drawable.tozeur_plat, 33.9197, 8.1335, 
                "- La médina Ouled El Hadef et son architecture en briques.\n- La palmeraie de Tozeur.\n- Le musée Dar Cherait.\n- Chott el-Jérid, immense lac salé.\n- Les oasis de montagne de Chebika, Tamerza et Mides.", 
                "- Explorer la palmeraie en calèche.\n- Partir en excursion 4x4 vers Chebika, Tamerza et Mides.\n- Découvrir Ong Jmel, décor de Star Wars (près de Nefta).\n- Faire une balade à dos de dromadaire.\n- Admirer le coucher de soleil sur le Chott el-Jérid.", 
                "Plat Traditionnel : Dattes Deglet Nour fraîches & Pain cuit dans le sable."));
            
            allDestinations.add(new Destination("Kairouan", "Ville sainte de l'Islam, célèbre pour sa Grande Mosquée.", "Kairouan", "Historique / Culture", kairouanImages, R.drawable.kairouan_plat, 35.6781, 10.0963, 
                "- La Grande Mosquée de Kairouan (Sidi Okba).\n- Les bassins des Aghlabides.\n- La mosquée des Trois Portes.\n- La zaouïa de Sidi Sahab (mosquée du Barbier).\n- Le puits Barrouta.", 
                "- Marcher dans la médina, classée à l'UNESCO.\n- Découvrir les ateliers de tapis et d'artisanat du cuivre.\n- Voir le puits Barrouta et son système traditionnel de puisage.\n- Visiter le musée d'art islamique de Raqqada.\n- Porter une tenue correcte pour entrer dans les lieux de culte.", 
                "Plat Traditionnel : Makroudh de Kairouan (pâtisserie)."));
            
            allDestinations.add(new Destination("Tabarka", "Connue pour ses aiguilles rocheuses et son château génois.", "Jendouba / Nord-Ouest", "Nature / Plage", tabarkaImages, 0, 36.9544, 8.7581, 
                "- Les Aiguilles de Tabarka (rochers sculptés par la mer).\n- Le fort génois sur l'îlot.\n- Le musée archéologique de Tabarka.\n- La forêt de chênes-lièges d'Aïn Draham.\n- Le port et la plage de la ville.", 
                "- Plonger ou faire du snorkeling pour découvrir la vie sous-marine.\n- Randonner dans les forêts d'Aïn Draham.\n- Jouer au golf en bord de mer.\n- Monter au fort génois pour le coucher de soleil.\n- Se baigner sur la plage de la ville.", 
                ""));
            
            allDestinations.add(new Destination("Matmata", "Maisons troglodytes souterraines uniques, décor de Star Wars.", "Gabès / Matmata", "Désert / Culture", matmataImages, R.drawable.matmata_plat, 33.5417, 9.9658, 
                "- Les maisons troglodytes.\n- L'hôtel Sidi Driss (décor de Star Wars).\n- Une maison berbère habitée (visite chez l'habitant).\n- Le village berbère de Tamezret.\n- Le village de Toujane.", 
                "- Descendre dans les cours circulaires et découvrir l'architecture souterraine.\n- Déjeuner dans une maison troglodyte.\n- Rencontrer des familles berbères et goûter le pain tabouna.\n- Randonner dans les montagnes vers Tamezret et Toujane.\n- Photographier les paysages lunaires des environs.", 
                "Plat Traditionnel : Pain Tabouna & Mlaoui."));
            
            allDestinations.add(new Destination("Bizerte", "Charmante ville portuaire du nord avec son vieux port.", "Bizerte", "Plage / Nature", bizerteImages, R.drawable.bizerte_plat, 37.2744, 9.8739, 
                "- Le Vieux Port et ses quais colorés.\n- La Kasbah et le fort d'Espagne.\n- La médina et ses ruelles.\n- Le musée océanographique.\n- Le parc national de l'Ichkeul (UNESCO).", 
                "- Se promener sur le quai du Vieux Port au coucher du soleil.\n- Se baigner à la plage de Rimel ou sur la corniche.\n- Observer les oiseaux migrateurs à l'Ichkeul (en hiver).\n- Aller jusqu'au Cap Angela, point le plus au nord de l'Afrique.\n- Déjeuner de poisson frais sur le port.", 
                "Plat Traditionnel : Sandwich Lablabi & ragoût d'poulpe."));
            
            allDestinations.add(new Destination("Sousse", "Ville côtière historique avec un Ribat millénaire.", "Sousse", "Historical / Beach", sousseImages, 0, 35.8256, 10.6369, 
                "- La médina (UNESCO) et ses souks.\n- Le Ribat (VIIIe siècle).\n- La Grande Mosquée.\n- Le musée archéologique (dans la Kasbah) et la tour Khalef.\n- Les catacombes.", 
                "- Monter à la tour du Ribat pour la vue sur la médina.\n- Marchander dans les souks.\n- Se baigner à la plage de Boujaafar.\n- Se promener et dîner à Port El Kantaoui.\n- Admirer les mosaïques romaines du musée de la Kasbah.", 
                ""));
            
            allDestinations.add(new Destination("Monastir", "Superbe cité côtière avec une immense forteresse.", "Monastir", "Historical / Beach", monastirImages, R.drawable.monastir_plat, 35.7643, 10.8113, 
                "- Le Ribat de Monastir (VIIIe siècle).\n- Le mausolée Habib Bourguiba.\n- La Grande Mosquée.\n- Le musée du costume traditionnel.\n- La marina et la corniche.", 
                "- Grimper dans la tour du Ribat pour la vue panoramique.\n- Se promener sur la corniche et à la marina.\n- Se baigner à la plage de Skanès.\n- Faire une excursion en bateau vers les îles Kuriat.\n- Découvrir la médina et son marché.", 
                "Plat Traditionnel : Couscous Cherkaw (couscous cherkaw)."));
        }
        destinationList.clear();
        destinationList.addAll(allDestinations);
    }
}
