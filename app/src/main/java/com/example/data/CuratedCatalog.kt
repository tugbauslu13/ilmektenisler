package com.example.data

import com.example.R
import com.example.data.model.AtelierProduct
import com.example.data.model.DiscoverItem
import com.example.data.model.SocialFeedItem

object CuratedCatalog {

    val categories = listOf(
        "Tümü",
        "Amigurumi",
        "Giyim & Hırka",
        "Çanta",
        "Anahtarlık & Aksesuar"
    )
    val postCreationCategories = listOf(
        "Amigurumi",
        "Giyim & Hırka",
        "Çanta",
        "Anahtarlık & Aksesuar"
    )
    val difficulties = listOf("Tümü", "Başlangıç", "Orta", "İleri")

    val socialTopics = listOf(
        "Tümü",
        "Örgü Günlüğü",
        "Soru Sor",
        "İp Tavsiyesi",
        "Genel Sohbet"
    )
    val socialComposeTopics = listOf(
        "Örgü Günlüğü",
        "Soru Sor",
        "İp Tavsiyesi",
        "Genel Sohbet"
    )

    val specialtyTags = listOf(
        "Amigurumi Ustası",
        "Kadife İp Sever",
        "Motif Tasarımcısı",
        "Şal & Ajur Tutkunu",
        "Çanta & Makrome",
        "Tığ & Kahve Keyfi",
        "Başlangıç Dostu"
    )

    val atelierProductTags = listOf(
        "Hazır Ürün",
        "Özel Tasarım",
        "Sınırlı Üretim",
        "Atölye Favorisi",
        "Hediye Seçeneği"
    )

    val curatedDiscoverItems = listOf(
        DiscoverItem(
            id = "curated_cardigan_1",
            title = "Kır Çiçeği Motifli Hırka",
            authorName = "Zeynep Yılmaz",
            authorHandle = "@zeynep.ilmek",
            category = "Giyim & Hırka",
            difficulty = "Orta",
            yarnType = "%100 Organik Pamuklu DK (3 numara)",
            hookSize = "3.5 mm tığ",
            estimatedDuration = "14 saat",
            abbreviationsNote = "zn: zincir • x: sık iğne • cc: ilmek kaydırma • dc: ikili trabzan",
            steps = listOf(
                "1. Tur: Sihirli halka içine 3 zn, 11 dc yapıp cc ile birleştirin. (12)",
                "2. Tur: Tarçın rengi ipe geçin. Her yuvaya (2 dc, 1 zn) pıtırcık yapın.",
                "3. Tur: Adaçayı yeşili ile boşluklara 3 dc, köşelere (3 dc, 2 zn, 3 dc) örün.",
                "4. Tur: Krem rengi iple son çerçeveyi tüm kenarlara x + köşelerde 3 zn olarak tamamlayın.",
                "5. Birleştirme: Toplam 42 motif örüp arka beden 16, önler 8'er, kollar 5'er motif olacak şekilde cc ile birleştirin.",
                "6. Kenar Lastik: Yaka ve manşetlere 4 sıra ön-arka kabartmalı trabzan uygulayın."
            ),
            imageKey = "img_crochet_cardigan",
            baseLikes = 148,
            aspectHeightRatio = 1.18f,
            finishedDate = "12 Kas 2026",
            yarnBrand = "Alize Cotton Gold"
        ),
        DiscoverItem(
            id = "curated_bunny_1",
            title = "Atkılı Minik Tavşan Amigurumi",
            authorName = "Elif Karahan",
            authorHandle = "@elifin.yumagi",
            category = "Amigurumi",
            difficulty = "Başlangıç",
            yarnType = "Bebe Pamuklu Amigurumi İpi (Krem & Adaçayı)",
            hookSize = "2.5 mm tığ",
            estimatedDuration = "4 saat",
            abbreviationsNote = "zn: zincir • x: sık iğne • v: artırma • a: eksiltme",
            steps = listOf(
                "1. Kafa - Tur 1: Sihirli halka içine 6x yapın. (6)",
                "2. Kafa - Tur 2: Her ilmeğe v yapın. (12)",
                "3. Kafa - Tur 3: (1x, 1v) x 6 tekrarlayın. (18)",
                "4. Kafa - Tur 4-8: 18x olarak düz örün, 6. sıra aralığına 8 mm kilitli göz takın.",
                "5. Kafa - Tur 9-10: (1x, 1a) x 6 ardından 6a yapıp elyaf doldurun ve kapatın.",
                "6. Kulaklar (2 adet): Sihirli halka 5x, 5v (10), 6 sıra 10x örüp hafifçe katlayın.",
                "7. Tarçın Atkı: 32 zn çekin, her zincire 1x örerek boyuna sarın."
            ),
            imageKey = "img_amigurumi_bunny",
            baseLikes = 214,
            aspectHeightRatio = 0.88f,
            finishedDate = "08 Kas 2026",
            yarnBrand = "YarnArt Jeans"
        ),
        DiscoverItem(
            id = "curated_tote_1",
            title = "Bahar Pazarı File Çanta",
            authorName = "Selin Demir",
            authorHandle = "@selindokuma",
            category = "Çanta",
            difficulty = "Başlangıç",
            yarnType = "Makrome Pamuk Halat İp (2 mm)",
            hookSize = "4.5 mm tığ",
            estimatedDuration = "3.5 saat",
            abbreviationsNote = "zn: zincir • x: sık iğne • cc: ilmek kaydırma • dc: ikili trabzan",
            steps = listOf(
                "1. Taban: Sihirli halka 12 dc, 2. tur 24 dc, 3. tur (1 dc, 1v) toplam 36 dc.",
                "2. Gövde File: (5 zn çekin, 2 ilmek atlayıp 1x) şeklinde 18 sıra döne döne örün.",
                "3. Ağız Çerçevesi: Adaçayı yeşili ipe geçerek 3 sıra sıkı x geçin.",
                "4. Askılar: 55 zn çekip diğer uca cc ile sabitleyin; askı üzerinden 2 sıra x ile güçlendirin."
            ),
            imageKey = "img_knit_tote",
            baseLikes = 96,
            aspectHeightRatio = 1.05f,
            finishedDate = "03 Kas 2026",
            yarnBrand = "Spagettiyarn Pamuk Makrome"
        ),
        DiscoverItem(
            id = "curated_shawl_1",
            title = "Gofret Doku Bebek Battaniyesi",
            authorName = "Merve Aydın",
            authorHandle = "@ilmektenisler",
            category = "Giyim & Hırka",
            difficulty = "Orta",
            yarnType = "Ekstra Yumuşak Merinos Yün Karışımlı",
            hookSize = "4.0 mm tığ",
            estimatedDuration = "11 saat",
            abbreviationsNote = "zn: zincir • dc: ikili trabzan • fpdc: önden kabartma trabzan",
            steps = listOf(
                "1. Başlangıç: 3'ün katları + 2 olacak şekilde 122 zn çekin.",
                "2. Sıra 1: 4. zincirden başlayarak sıra sonuna kadar her zincire 1 dc örün.",
                "3. Sıra 2 (Gofret Ön): 3 zn, *1 fpdc, 2 dc* sıra sonuna kadar tekrarlayın.",
                "4. Sıra 3 (Gofret Arka): 3 zn, *2 fpdc, 1 dc* tekrarlayarak kare petek dokusu oluşturun.",
                "5. Kenar Oyası: 2. ve 3. sıraları 85 cm olana dek tekrarlayın, etrafını tarçın iplikle ters sık iğne geçin."
            ),
            imageKey = "img_atelier_shawl",
            baseLikes = 183,
            aspectHeightRatio = 1.24f,
            finishedDate = "29 Eki 2026",
            yarnBrand = "Nako Elit Baby & Merinos"
        ),
        DiscoverItem(
            id = "curated_cardigan_2",
            title = "Pastel Büyük Kare Bebe Yeleği",
            authorName = "Duygu Çelik",
            authorHandle = "@duygu.elisi",
            category = "Giyim & Hırka",
            difficulty = "Başlangıç",
            yarnType = "Anti-Pilling Bebe Akrilik (Krem & Adaçayı)",
            hookSize = "4.0 mm tığ",
            estimatedDuration = "5 saat",
            abbreviationsNote = "zn: zincir • x: sık iğne • v: artırma • dc: ikili trabzan",
            steps = listOf(
                "1. Altıgen Motif (2 adet): Sihirli halka içine (3 dc, 2 zn) x 6 grup örün.",
                "2. Her sırada köşe 2 zn boşluklarına (3 dc, 2 zn, 3 dc) yaparak 9 sıra büyütün.",
                "3. Katlama: İki altıgeni L formunda katlayıp sırt ve omuz dikişlerini x ile birleştirin.",
                "4. Düğme Patı: Ön kenarlara 3 sıra x örüp ahşap düğme dikin."
            ),
            imageKey = "img_crochet_cardigan",
            baseLikes = 72,
            aspectHeightRatio = 0.92f,
            finishedDate = "24 Eki 2026",
            yarnBrand = "Himalaya Everyday Bebe"
        ),
        DiscoverItem(
            id = "curated_tote_2",
            title = "Çapraz Askılı Günlük Kese Çanta",
            authorName = "Buse Şahin",
            authorHandle = "@buseyleorgu",
            category = "Çanta",
            difficulty = "Orta",
            yarnType = "Organik Kağıt & Pamuk Büküm İp",
            hookSize = "3.5 mm tığ",
            estimatedDuration = "6 saat",
            abbreviationsNote = "zn: zincir • x: sık iğne • v: artırma • a: eksiltme",
            steps = listOf(
                "1. Taban: 18 zn çekin, etrafını oval şekilde v yaparak 3 tur dönün.",
                "2. Gövde: Arka ilmekten (BLO) 1 tur x örüp duvarı dikleştirin.",
                "3. Desen: 20 sıra boyunca kaydırmalı başak sık iğne tekniği ile yükselin.",
                "4. Büzgü ve Askı: Üstten 3. sırada 2 zn delikler bırakın ve kordon ip geçirin."
            ),
            imageKey = "img_knit_tote",
            baseLikes = 129,
            aspectHeightRatio = 1.12f,
            finishedDate = "19 Eki 2026",
            yarnBrand = "La Mia Natural Raffia"
        ),
        DiscoverItem(
            id = "curated_shawl_2",
            title = "Ajurlu Yaprak Dantel Şal",
            authorName = "Nazlı Kılıç",
            authorHandle = "@nazli.dantel",
            category = "Giyim & Hırka",
            difficulty = "İleri",
            yarnType = "İnce Merinos & İpek Dantel İpi (1 numara)",
            hookSize = "3.0 mm bambu şiş",
            estimatedDuration = "22 saat",
            abbreviationsNote = "d: düz • t: ters • a: artırma (dolama) • k: iki ilmek birlikte kesme",
            steps = listOf(
                "1. Başlangıç: 3 ilmekle başlayıp garter tab yöntemiyle 9 ilmeğe çıkarın.",
                "2. Gövde Ajur (Sıra 1-24): Kenarlardan 3 ilmek haraşo, ortadan yaprak ajur diyagramını uygulayın.",
                "3. Artırmalar: Her ön yüzde orta direk sağından ve solundan 1'er gizli artırma yapın.",
                "4. Kenar Dantel: Son 12 sırada yaprak uçlarını pikolu kesme tekniğiyle kapatıp gerdirerek bloklayın."
            ),
            imageKey = "img_atelier_shawl",
            baseLikes = 265,
            aspectHeightRatio = 1.28f,
            finishedDate = "14 Eki 2026",
            yarnBrand = "Gazzal Baby Wool XL"
        ),
        DiscoverItem(
            id = "curated_accessory_1",
            title = "Papatya Çiçekli Çanta Süsü & Anahtarlık",
            authorName = "Seda Kaya",
            authorHandle = "@sedanin.ilmekleri",
            category = "Anahtarlık & Aksesuar",
            difficulty = "Başlangıç",
            yarnType = "Mercerize Pamuk İp (Sarı & Krem)",
            hookSize = "2.2 mm tığ",
            estimatedDuration = "45 dk",
            abbreviationsNote = "sh: sihirli halka • zn: zincir • x: sık iğne • dc: ikili trabzan • cc: ilmek kaydırma",
            steps = listOf(
                "1. Orta Göbek (2 adet): Sarı iple sh içine 6x yapın, 2. tur 6v (12) ile kapatın.",
                "2. Yapraklar: Krem iple her ilmeğe (3 zn, 2 dc, 3 zn, aynı ilmeğe cc) örerek 6 yaprak çıkarın.",
                "3. Askı Halkası: 12 zn çekip anahtarlık halkasından geçirerek cc ile sabitleyin."
            ),
            imageKey = "img_amigurumi_bunny",
            baseLikes = 134,
            aspectHeightRatio = 0.94f,
            finishedDate = "10 Kas 2026",
            yarnBrand = "YarnArt Begonia"
        ),
        DiscoverItem(
            id = "curated_cardigan_3",
            title = "Jakarlı Renk Geçişli Bohem Hırka",
            authorName = "Aslı Erdoğan",
            authorHandle = "@asli.ilmek",
            category = "Giyim & Hırka",
            difficulty = "İleri",
            yarnType = "İnce Alize Cotton & Yün Karışımı (3 renk)",
            hookSize = "3.0 mm tığ",
            estimatedDuration = "26 saat",
            abbreviationsNote = "zn: zincir • x: sık iğne • fpdc: önden kabartma • tapestry: iplik yürütme",
            steps = listOf(
                "1. Arka Beden: Krem, adaçayı ve tarçın iplerini birlikte yürüterek tapestry tekniğiyle 78 ilmek örün.",
                "2. Kol Oyuntusu: 42. sırada her iki yandan kademeli 4-3-2 ilmek eksiltin.",
                "3. Ön Paneller: Sağ ve sol ön parçalarda simetrik çiçek motiflerini gizli dikişle oturtun.",
                "4. Bitiriş: Kolları yuvarlak örgü ile aşağı doğru daraltıp manşetleri 6 sıra lastik tamamlayın."
            ),
            imageKey = "img_crochet_cardigan",
            baseLikes = 192,
            aspectHeightRatio = 1.15f,
            finishedDate = "06 Eki 2026",
            yarnBrand = "Alize Cotton Gold Plus"
        )
    )

    val atelierProducts = listOf(
        AtelierProduct(
            id = "atelier_1",
            title = "Kır Çiçeği El Örgüsü Hırka",
            subtitle = "S-M ve M-L bedenlere uyumlu, tek tek elde birleştirilmiş 42 motifli özel tasarım.",
            priceFormatted = "1.850 ₺",
            tag = "Sınırlı Üretim",
            yarnDetail = "%100 Organik Pamuk • Doğal Boyalı",
            dimensions = "Boy: 58 cm • En: 54 cm",
            imageRes = R.drawable.img_crochet_cardigan,
            shopierUrl = "https://www.shopier.com/ilmektenisler",
            dolapUrl = "https://dolap.com/profil/ilmektenisler",
            inStock = true
        ),
        AtelierProduct(
            id = "atelier_2",
            title = "Heirloom Gofret Bebek Battaniyesi",
            subtitle = "Yeni doğan hassas cildine uygun, nefes alan merinos & pamuk dokuma.",
            priceFormatted = "1.290 ₺",
            tag = "Atölye Favorisi",
            yarnDetail = "Merinos Yün & Organik Pamuk",
            dimensions = "90 cm x 95 cm",
            imageRes = R.drawable.img_atelier_shawl,
            shopierUrl = "https://www.shopier.com/ilmektenisler",
            dolapUrl = "https://dolap.com/profil/ilmektenisler",
            inStock = true
        ),
        AtelierProduct(
            id = "atelier_3",
            title = "Pofuduk Uyku Arkadaşı Tavşan",
            subtitle = "Kilitli güvenli gözler ve çıkarılabilir tarçın atkısıyla el emeği amigurumi.",
            priceFormatted = "640 ₺",
            tag = "Hediye Seçeneği",
            yarnDetail = "%100 Mercerize Pamuk & Boncuk Elyaf",
            dimensions = "Yükseklik: 26 cm",
            imageRes = R.drawable.img_amigurumi_bunny,
            shopierUrl = "https://www.shopier.com/ilmektenisler",
            dolapUrl = "https://dolap.com/profil/ilmektenisler",
            inStock = true
        ),
        AtelierProduct(
            id = "atelier_4",
            title = "Adaçayı File Pazar & Sahil Çantası",
            subtitle = "Genişleyebilen dayanıklı makrome örgü, omuzda iz yapmayan kalın askı.",
            priceFormatted = "590 ₺",
            tag = "Hazır Kargo",
            yarnDetail = "%100 Geri Dönüşümlü Pamuk Halat",
            dimensions = "36 cm x 40 cm",
            imageRes = R.drawable.img_knit_tote,
            shopierUrl = "https://www.shopier.com/ilmektenisler",
            dolapUrl = "https://dolap.com/profil/ilmektenisler",
            inStock = true
        )
    )

    val curatedSocialPosts = listOf(
        SocialFeedItem(
            id = "social_1",
            authorName = "Zeynep Yılmaz",
            authorHandle = "@zeynep.ilmek",
            topic = "Örgü Günlüğü",
            text = "Pazar kahvem ve yarım kalan papatya hırka motiflerimle baş başayım. Bugün hedef 8 motifi daha birleştirip kollara geçmek! Şu an tığınızda ne var?",
            imageKey = "img_crochet_cardigan",
            timeLabel = "25 dk önce",
            baseLikes = 38,
            repliesCount = 6,
            isRemotePost = false
        ),
        SocialFeedItem(
            id = "social_2",
            authorName = "Elif Karahan",
            authorHandle = "@elifin.yumagi",
            topic = "Soru Sor",
            text = "Amigurumi oyuncaklarda boyun kısmının zamanla sağa sola düşmemesi için siz nasıl bir sabitleme yapıyorsunuz? Çift kat örüp elyafı sıkı basmak yeterli oluyor mu?",
            timeLabel = "1 sa önce",
            baseLikes = 24,
            repliesCount = 9,
            isRemotePost = false
        ),
        SocialFeedItem(
            id = "social_3",
            authorName = "Selin Demir",
            authorHandle = "@selindokuma",
            topic = "İp Tavsiyesi",
            text = "Yazlık file çantalarda kağıt ip yerine 2 mm taranabilir pamuk makrome denedim, hem elde örmesi çok yumuşak hem de formu hiç bozulmuyor. Özellikle 4.5 mm tığla harika düşüyor.",
            imageKey = "img_knit_tote",
            timeLabel = "3 sa önce",
            baseLikes = 52,
            repliesCount = 11,
            isRemotePost = false
        ),
        SocialFeedItem(
            id = "social_4",
            authorName = "Merve Aydın",
            authorHandle = "@ilmektenisler",
            topic = "Genel Sohbet",
            text = "Atölyede yeni bebek battaniyesi siparişleri için adaçayı yeşili ve sıcak krem yünleri hazırladık. Renk uyumu gerçekten en huzur verici kısım olabilir.",
            imageKey = "img_atelier_shawl",
            timeLabel = "5 sa önce",
            baseLikes = 67,
            repliesCount = 8,
            isRemotePost = false
        )
    )
}
