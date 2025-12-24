package com.example.gonote.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.gonote.R
import com.example.gonote.data.local.UserPreferences
import com.example.gonote.data.local.entity.UserEntity
import com.example.gonote.data.model.Note
import com.example.gonote.data.repository.NoteRepository
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DemoDataManager(
    private val context: Context,
    private val repository: NoteRepository,
    private val userPreferences: UserPreferences
) {
    companion object {
        private const val TAG = "DemoDataManager"
        private const val DEMO_USER_EMAIL = "semihalkanli@gmail.com"
        private const val DEMO_USER_ID = "demo_user_semih_001"
        
        // Demo users for admin panel presentation
        private val DEMO_USERS = listOf(
            Pair("semihalkanli@gmail.com", "Semih Alkanli"),  // Primary demo user
            Pair("john.doe@example.com", "John Doe"),
            Pair("jane.smith@example.com", "Jane Smith"),
            Pair("alex.johnson@example.com", "Alex Johnson"),
            Pair("emily.wilson@example.com", "Emily Wilson"),
            Pair("michael.brown@example.com", "Michael Brown"),
            Pair("sarah.davis@example.com", "Sarah Davis"),
            Pair("david.miller@example.com", "David Miller"),
            Pair("lisa.garcia@example.com", "Lisa Garcia"),
            Pair("james.martinez@example.com", "James Martinez"),
            Pair("emma.anderson@example.com", "Emma Anderson"),
            Pair("robert.taylor@example.com", "Robert Taylor"),
            Pair("olivia.thomas@example.com", "Olivia Thomas"),
            Pair("william.jackson@example.com", "William Jackson"),
            Pair("sophia.white@example.com", "Sophia White"),
            Pair("daniel.harris@example.com", "Daniel Harris"),
            Pair("ava.martin@example.com", "Ava Martin"),
            Pair("matthew.thompson@example.com", "Matthew Thompson"),
            Pair("isabella.moore@example.com", "Isabella Moore"),
            Pair("ethan.clark@example.com", "Ethan Clark")
        )
    }

    suspend fun loadDemoDataIfNeeded() {
        try {
            // Demo veriler SADECE demo kullanıcı için yüklenir
            // Diğer kullanıcılar sıfırdan başlar
            
            // First, create demo users for admin panel
            loadDemoUsersForAdminPanel()
            
            // Check if demo data already loaded for demo user
            val noteCount = repository.getTotalNotesCount(DEMO_USER_ID).first()
            if (noteCount > 0) {
                return
            }

            createAndInsertDemoNotes(DEMO_USER_ID)

            // Mark as loaded
            userPreferences.setDemoDataLoaded(true)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to load demo data", e)
            throw e
        }
    }

    private suspend fun createAndInsertDemoNotes(userId: String) {
        val demoNotes = listOf(
            // 1. Ayasofya Ziyareti - Travel, İstanbul, 3 photos, favorite, 50 days ago
            Note(
                title = "Ayasofya Ziyareti",
                content = "1500 yıllık mimari harikası! Mozaikler inanılmaz detaylı. İç kubbedeki süslemeler büyüleyici. Bizans ve Osmanlı mimarisinin birleşimi muhteşem.",
                latitude = 41.0086,
                longitude = 28.9802,
                locationName = "Ayasofya Camii",
                city = "İstanbul",
                country = "Turkey",
                timestamp = getDaysAgoTimestamp(50),
                userId = userId,
                isFavorite = true,
                photos = createDemoPhotos(listOf(R.drawable.ayasofya_1, R.drawable.ayasofya_2, R.drawable.ayasofya_3)),
                category = "Travel"
            ),

            // 2. Sultanahmet Meydanı - Travel, İstanbul, 2 photos, favorite, 48 days ago
            Note(
                title = "Sultanahmet Meydanı",
                content = "Mavi Cami'nin incelikli süslemeleri büyüleyici! Altı minare ile eşsiz bir görünüm. İç mekandaki mavi çiniler nefes kesici.",
                latitude = 41.0054,
                longitude = 28.9768,
                locationName = "Sultanahmet Meydanı",
                city = "İstanbul",
                country = "Turkey",
                timestamp = getDaysAgoTimestamp(48),
                userId = userId,
                isFavorite = true,
                photos = createDemoPhotos(listOf(R.drawable.blue_mosque_1, R.drawable.blue_mosque_2)),
                category = "Travel"
            ),

            // 3. Kapadokya Balon Turu - Travel, Nevşehir, 4 photos, favorite, 42 days ago
            Note(
                title = "Kapadokya Balon Turu",
                content = "Gün doğumunda yüzlerce balon havada! Eşsiz manzara. Peribacalarının üstünden uçmak inanılmazdı. Hayatımın en güzel deneyimlerinden biri.",
                latitude = 38.6431,
                longitude = 34.8286,
                locationName = "Göreme",
                city = "Nevşehir",
                country = "Turkey",
                timestamp = getDaysAgoTimestamp(42),
                userId = userId,
                isFavorite = true,
                photos = createDemoPhotos(listOf(R.drawable.cappadocia_1, R.drawable.cappadocia_2, R.drawable.cappadocia_3, R.drawable.cappadocia_4)),
                category = "Travel"
            ),

            // 4. Pamukkale Travertenleri - Travel, Denizli, 3 photos, favorite, 38 days ago
            Note(
                title = "Pamukkale Travertenleri",
                content = "Doğal beyaz teraslar ve termal sular. UNESCO dünya mirası! Termal suda yürümek çok keyifliydi. Hierapolis antik kenti de muhteşem.",
                latitude = 37.9208,
                longitude = 29.1211,
                locationName = "Pamukkale",
                city = "Denizli",
                country = "Turkey",
                timestamp = getDaysAgoTimestamp(38),
                userId = userId,
                isFavorite = true,
                photos = createDemoPhotos(listOf(R.drawable.pamukkale_1, R.drawable.pamukkale_2, R.drawable.pamukkale_3)),
                category = "Travel"
            ),

            // 5. Efes Antik Kenti - Travel, İzmir, 3 photos, favorite, 35 days ago
            Note(
                title = "Efes Antik Kenti",
                content = "2000 yıllık Roma antik kenti! Celsus Kütüphanesi muhteşem. Büyük tiyatro inanılmaz korunmuş. Tarihi hissetmek çok etkileyiciydi.",
                latitude = 37.9395,
                longitude = 27.3408,
                locationName = "Efes Antik Kenti",
                city = "Selçuk",
                country = "Turkey",
                timestamp = getDaysAgoTimestamp(35),
                userId = userId,
                isFavorite = true,
                photos = createDemoPhotos(listOf(R.drawable.ephesus_1, R.drawable.ephesus_2, R.drawable.ephesus_3)),
                category = "Travel"
            ),

            // 6. Kapalı Çarşı - Shopping, İstanbul, 2 photos, 30 days ago
            Note(
                title = "Kapalı Çarşı",
                content = "Dünyanın en eski kapalı çarşısı! 4000'den fazla dükkan. Renkli halılar, geleneksel el sanatları, antika objeler. Kaybolmak bile keyifli.",
                latitude = 41.0106,
                longitude = 28.9681,
                locationName = "Kapalı Çarşı",
                city = "İstanbul",
                country = "Turkey",
                timestamp = getDaysAgoTimestamp(30),
                userId = userId,
                isFavorite = false,
                photos = createDemoPhotos(listOf(R.drawable.grand_bazaar_1, R.drawable.grand_bazaar_2)),
                category = "Shopping"
            ),

            // 7. Boğaz Turu - Travel, İstanbul, 3 photos, favorite, 25 days ago
            Note(
                title = "Boğaz Turu",
                content = "İki kıtayı birleştiren Boğaz'da tekne turu! Sunset harika. Köprülerin altından geçmek muhteşem bir duygu. Kız Kulesi'ni yakından gördük.",
                latitude = 41.0422,
                longitude = 29.0075,
                locationName = "Boğaziçi",
                city = "İstanbul",
                country = "Turkey",
                timestamp = getDaysAgoTimestamp(25),
                userId = userId,
                isFavorite = true,
                photos = createDemoPhotos(listOf(R.drawable.bosphorus_1, R.drawable.bosphorus_2, R.drawable.bosphorus_3)),
                category = "Travel"
            ),

            // 8. Türk Kahvesi Keyfi - Food, İstanbul, 2 photos, 20 days ago
            Note(
                title = "Türk Kahvesi Keyfi",
                content = "UNESCO somut olmayan kültürel miras! Geleneksel Türk kahvesi deneyimi. Köpük ve aroma mükemmel. Fal keyfi ayrı bir güzellik.",
                latitude = 41.0255,
                longitude = 28.9742,
                locationName = "Tarihi Kahvehane",
                city = "İstanbul",
                country = "Turkey",
                timestamp = getDaysAgoTimestamp(20),
                userId = userId,
                isFavorite = false,
                photos = createDemoPhotos(listOf(R.drawable.turkish_coffee_1, R.drawable.turkish_coffee_2)),
                category = "Food"
            ),

            // 9. Köfte Salonu - Food, İstanbul, 2 photos, 15 days ago
            Note(
                title = "Köfte Salonu",
                content = "İstanbul'un en meşhur köftecisi! 60 yıllık aile işletmesi. Izgara köfte ve pilav kombinasyonu harika. Her lokmada lezzet patlaması.",
                latitude = 41.0095,
                longitude = 28.9743,
                locationName = "Tarihi Köfteci",
                city = "İstanbul",
                country = "Turkey",
                timestamp = getDaysAgoTimestamp(15),
                userId = userId,
                isFavorite = false,
                photos = createDemoPhotos(listOf(R.drawable.kofte_1, R.drawable.kofte_2)),
                category = "Food"
            ),

            // 10. Anıtkabir Ziyareti - Travel, Ankara, 2 photos, favorite, 28 days ago
            Note(
                title = "Anıtkabir Ziyareti",
                content = "Atatürk'ün anıt mezarı. Etkileyici mimari ve tarihi önemi. Aslan heykelleri muhteşem. Müze bölümünde çok şey öğrendim.",
                latitude = 39.9255,
                longitude = 32.8366,
                locationName = "Anıtkabir",
                city = "Ankara",
                country = "Turkey",
                timestamp = getDaysAgoTimestamp(28),
                userId = userId,
                isFavorite = true,
                photos = createDemoPhotos(listOf(R.drawable.anitkabir_1, R.drawable.anitkabir_2)),
                category = "Travel"
            ),

            // 11. Eyfel Kulesi - Travel, Paris, France, 3 photos, favorite, 60 days ago
            Note(
                title = "Eyfel Kulesi",
                content = "Işıl ışıl kule! Her saat başı ışık gösterisi muhteşem. Paris'in simgesi. Tepesine çıktık, Paris panoraması nefes kesici!",
                latitude = 48.8584,
                longitude = 2.2945,
                locationName = "Eiffel Tower",
                city = "Paris",
                country = "France",
                timestamp = getDaysAgoTimestamp(60),
                userId = userId,
                isFavorite = true,
                photos = createDemoPhotos(listOf(R.drawable.eiffel_1, R.drawable.eiffel_2, R.drawable.eiffel_3)),
                category = "Travel"
            ),

            // 12. Kolezyum - Travel, Roma, Italy, 3 photos, favorite, 55 days ago
            Note(
                title = "Kolezyum",
                content = "2000 yıllık antik amfi tiyatro! Gladyatör dövüşlerinin yapıldığı yer. İnanılmaz! İç arenaya girdik, tarihi hissettik.",
                latitude = 41.8902,
                longitude = 12.4922,
                locationName = "Colosseum",
                city = "Roma",
                country = "Italy",
                timestamp = getDaysAgoTimestamp(55),
                userId = userId,
                isFavorite = true,
                photos = createDemoPhotos(listOf(R.drawable.colosseum_1, R.drawable.colosseum_2, R.drawable.colosseum_3)),
                category = "Travel"
            ),

            // 13. Times Square - Travel, New York, USA, 3 photos, 50 days ago
            Note(
                title = "Times Square",
                content = "New York'un kalbi! Milyonlarca LED ekran ve inanılmaz enerji. Gece hayatı muhteşem. Şehir hiç uyumuyor!",
                latitude = 40.7580,
                longitude = -73.9855,
                locationName = "Times Square",
                city = "New York",
                country = "USA",
                timestamp = getDaysAgoTimestamp(50),
                userId = userId,
                isFavorite = false,
                photos = createDemoPhotos(listOf(R.drawable.times_square_1, R.drawable.times_square_2, R.drawable.times_square_3)),
                category = "Travel"
            ),

            // 14. Burj Khalifa - Travel, Dubai, UAE, 3 photos, favorite, 45 days ago
            Note(
                title = "Burj Khalifa",
                content = "Dünyanın en yüksek binası! 828 metre. Tepesinden Dubai'yi izlemek paha biçilmez. Gün batımı manzarası inanılmazdı.",
                latitude = 25.1972,
                longitude = 55.2744,
                locationName = "Burj Khalifa",
                city = "Dubai",
                country = "UAE",
                timestamp = getDaysAgoTimestamp(45),
                userId = userId,
                isFavorite = true,
                photos = createDemoPhotos(listOf(R.drawable.burj_khalifa_1, R.drawable.burj_khalifa_2, R.drawable.burj_khalifa_3)),
                category = "Travel"
            ),

            // 15. Santorini Sunset - Travel, Santorini, Greece, 3 photos, favorite, 40 days ago
            Note(
                title = "Santorini Sunset",
                content = "Dünyaca ünlü gün batımı! Beyaz evler ve mavi kubbeler masalsı. Oia'da izlediğimiz sunset hayatımın en güzel anıydı.",
                latitude = 36.3932,
                longitude = 25.4615,
                locationName = "Oia",
                city = "Santorini",
                country = "Greece",
                timestamp = getDaysAgoTimestamp(40),
                userId = userId,
                isFavorite = true,
                photos = createDemoPhotos(listOf(R.drawable.santorini_1, R.drawable.santorini_2, R.drawable.santorini_3)),
                category = "Travel"
            ),

            // 16. Neuschwanstein Kalesi - Travel, Bavaria, Germany, 2 photos, favorite, 35 days ago
            Note(
                title = "Neuschwanstein Kalesi",
                content = "Disney'in ilham aldığı peri masalı kalesi! Ormanların içinde saklı. Bavyera Alpleri manzarası eşsiz. Gerçek bir masal dünyası.",
                latitude = 47.5576,
                longitude = 10.7498,
                locationName = "Neuschwanstein Castle",
                city = "Schwangau",
                country = "Germany",
                timestamp = getDaysAgoTimestamp(35),
                userId = userId,
                isFavorite = true,
                photos = createDemoPhotos(listOf(R.drawable.neuschwanstein_1, R.drawable.neuschwanstein_2)),
                category = "Travel"
            ),

            // 17. Pizza Margherita - Food, Naples, Italy, 2 photos, 30 days ago
            Note(
                title = "Pizza Margherita",
                content = "Napoli'nin orijinal pizza'sı! Fırın taş ocakta, fesleğen muhteşem. İtalya'nın en lezzetli yemeği. Hamur kıvamı mükemmel!",
                latitude = 40.8518,
                longitude = 14.2681,
                locationName = "L'Antica Pizzeria da Michele",
                city = "Naples",
                country = "Italy",
                timestamp = getDaysAgoTimestamp(30),
                userId = userId,
                isFavorite = false,
                photos = createDemoPhotos(listOf(R.drawable.pizza_1, R.drawable.pizza_2)),
                category = "Food"
            ),

            // 18. Starbucks Reserve - Food, Seattle, USA, 2 photos, 25 days ago
            Note(
                title = "Starbucks Reserve",
                content = "Starbucks'ın doğduğu şehir! Reserve kahveleri denedim, aroması harika. Latte art muhteşem. Kahve kültürünün merkezi.",
                latitude = 47.6101,
                longitude = -122.3421,
                locationName = "Starbucks Reserve Roastery",
                city = "Seattle",
                country = "USA",
                timestamp = getDaysAgoTimestamp(25),
                userId = userId,
                isFavorite = false,
                photos = createDemoPhotos(listOf(R.drawable.starbucks_1, R.drawable.starbucks_2)),
                category = "Food"
            ),

            // 19. Machu Picchu - Travel, Cusco, Peru, 3 photos, favorite, 65 days ago
            Note(
                title = "Machu Picchu",
                content = "İnka İmparatorluğu'nun kayıp şehri! 2400m yükseklikte bulutların üstünde. Dağ manzarası ve antik yapılar inanılmaz. Hayallerimi aştı!",
                latitude = -13.1631,
                longitude = -72.5450,
                locationName = "Machu Picchu",
                city = "Cusco",
                country = "Peru",
                timestamp = getDaysAgoTimestamp(65),
                userId = userId,
                isFavorite = true,
                photos = createDemoPhotos(listOf(R.drawable.machu_picchu_1, R.drawable.machu_picchu_2, R.drawable.machu_picchu_3)),
                category = "Travel"
            ),

            // 20. Proje Sunumu - Work, İstanbul, 1 photo, favorite, 10 days ago
            Note(
                title = "Proje Sunumu",
                content = "Yeni ürün lansmanı başarılı! Müşteri geri dönüşleri oldukça olumlu. Takım çalışması mükemmeldi. Networking harika fırsatlar yarattı.",
                latitude = 41.0082,
                longitude = 28.9784,
                locationName = "Konferans Merkezi",
                city = "İstanbul",
                country = "Turkey",
                timestamp = getDaysAgoTimestamp(10),
                userId = userId,
                isFavorite = true,
                photos = createDemoPhotos(listOf(R.drawable.office_presentation)),
                category = "Work"
            )
        )

        // Insert all demo notes
        demoNotes.forEach { note ->
            try {
                repository.insertNote(note)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert note: ${note.title}", e)
            }
        }
    }

    private fun createDemoPhotos(drawableIds: List<Int>): List<String> {
        val photos = mutableListOf<String>()

        drawableIds.forEachIndexed { index, drawableId ->
            try {
                val photoPath = createDemoPhoto(drawableId, index)
                photos.add(photoPath)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create demo photo from drawable", e)
            }
        }

        return photos
    }

    private fun createDemoPhoto(drawableId: Int, index: Int): String {
        // Create photos directory if not exists
        val photosDir = File(context.filesDir, "photos")
        if (!photosDir.exists()) {
            photosDir.mkdirs()
        }

        // Load drawable as bitmap
        val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)

        // Save to file
        val timestamp = System.currentTimeMillis() + index
        val fileName = "photo_$timestamp.jpg"
        val file = File(photosDir, fileName)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }

        bitmap.recycle()

        return file.absolutePath
    }

    private fun getDaysAgoTimestamp(daysAgo: Int): Long {
        return System.currentTimeMillis() - TimeUnit.DAYS.toMillis(daysAgo.toLong())
    }
    
    /**
     * Creates 20 demo users with random notes for admin panel presentation.
     * Each user will have 1-5 notes with photos.
     */
    suspend fun loadDemoUsersForAdminPanel() {
        try {
            // Check if demo users already exist
            val existingUsers = repository.getAllUsers().first()
            val existingEmails = existingUsers.map { it.email }.toSet()
            
            // Available demo photos for notes
            val demoPhotos = listOf(
                R.drawable.ayasofya_1, R.drawable.ayasofya_2, R.drawable.ayasofya_3,
                R.drawable.blue_mosque_1, R.drawable.blue_mosque_2,
                R.drawable.cappadocia_1, R.drawable.cappadocia_2, R.drawable.cappadocia_3,
                R.drawable.pamukkale_1, R.drawable.pamukkale_2, R.drawable.pamukkale_3,
                R.drawable.ephesus_1, R.drawable.ephesus_2, R.drawable.ephesus_3,
                R.drawable.bosphorus_1, R.drawable.bosphorus_2, R.drawable.bosphorus_3,
                R.drawable.eiffel_1, R.drawable.eiffel_2, R.drawable.eiffel_3,
                R.drawable.colosseum_1, R.drawable.colosseum_2, R.drawable.colosseum_3,
                R.drawable.santorini_1, R.drawable.santorini_2, R.drawable.santorini_3,
                R.drawable.burj_khalifa_1, R.drawable.burj_khalifa_2, R.drawable.burj_khalifa_3,
                R.drawable.machu_picchu_1, R.drawable.machu_picchu_2, R.drawable.machu_picchu_3
            )
            
            // Note templates
            val noteTemplates = listOf(
                Triple("Summer Vacation", "Amazing trip to the beach! The sunset was breathtaking.", "Travel"),
                Triple("Coffee Shop Find", "Discovered a cozy coffee shop. Great latte art!", "Food"),
                Triple("Mountain Hike", "Challenging but rewarding hike. The view from the top was incredible.", "Travel"),
                Triple("Birthday Celebration", "Had a wonderful birthday party with friends and family.", "Personal"),
                Triple("New Recipe", "Tried a new pasta recipe today. Turned out delicious!", "Food"),
                Triple("City Walk", "Explored the old town. So many historical buildings!", "Travel"),
                Triple("Weekend Getaway", "Short trip to the countryside. So peaceful and relaxing.", "Travel"),
                Triple("Art Gallery Visit", "Saw some amazing contemporary art. Very inspiring!", "Personal"),
                Triple("Local Market", "Found fresh produce and handmade crafts at the farmers market.", "Shopping"),
                Triple("Sunset Point", "Best sunset I've ever seen! Colors were unbelievable.", "Travel"),
                Triple("Beach Day", "Perfect weather for the beach. Waves were great for swimming.", "Travel"),
                Triple("Food Festival", "So many delicious options! Tried cuisines from 5 countries.", "Food"),
                Triple("Historical Tour", "Learned so much about the city's history. Fascinating!", "Travel"),
                Triple("Concert Night", "Amazing live performance! The atmosphere was electric.", "Personal"),
                Triple("Garden Visit", "Beautiful botanical garden. Flowers in full bloom!", "Personal")
            )
            
            // Cities for random location assignment
            val cities = listOf(
                Triple("New York", "USA", Pair(40.7128, -74.0060)),
                Triple("London", "UK", Pair(51.5074, -0.1278)),
                Triple("Paris", "France", Pair(48.8566, 2.3522)),
                Triple("Tokyo", "Japan", Pair(35.6762, 139.6503)),
                Triple("Sydney", "Australia", Pair(-33.8688, 151.2093)),
                Triple("Barcelona", "Spain", Pair(41.3851, 2.1734)),
                Triple("Amsterdam", "Netherlands", Pair(52.3676, 4.9041)),
                Triple("Rome", "Italy", Pair(41.9028, 12.4964)),
                Triple("Berlin", "Germany", Pair(52.5200, 13.4050)),
                Triple("Dubai", "UAE", Pair(25.2048, 55.2708))
            )
            
            var usersCreated = 0
            var notesCreated = 0
            
            DEMO_USERS.forEachIndexed { index, (email, name) ->
                // Skip if user already exists
                if (email in existingEmails) {
                    return@forEachIndexed
                }
                
                val userId = "demo_user_${index + 1}"
                val createdAt = getDaysAgoTimestamp(Random.nextInt(1, 90))
                
                // Create user
                val user = UserEntity(
                    id = userId,
                    email = email,
                    createdAt = createdAt
                )
                repository.registerUser(user)
                usersCreated++
                
                // Create 1-5 random notes for this user
                val noteCount = Random.nextInt(1, 6)
                val shuffledTemplates = noteTemplates.shuffled().take(noteCount)
                
                shuffledTemplates.forEachIndexed { noteIndex, (title, content, category) ->
                    val city = cities.random()
                    val photoCount = Random.nextInt(1, 4)
                    val photos = demoPhotos.shuffled().take(photoCount)
                    
                    val note = Note(
                        title = title,
                        content = content,
                        latitude = city.third.first + Random.nextDouble(-0.05, 0.05),
                        longitude = city.third.second + Random.nextDouble(-0.05, 0.05),
                        locationName = "$title Location",
                        city = city.first,
                        country = city.second,
                        timestamp = getDaysAgoTimestamp(Random.nextInt(1, 60)),
                        userId = userId,
                        isFavorite = Random.nextBoolean(),
                        photos = createDemoPhotos(photos),
                        category = category
                    )
                    
                    repository.insertNote(note)
                    notesCreated++
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create demo users", e)
            throw e
        }
    }
}
