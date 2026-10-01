package com.olehprukhnytskyi.macrotrackernotificationservice.push;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PushMessageCatalog {
    private static final Map<String, Messages> MESSAGES = Map.ofEntries(
            Map.entry("en", new Messages(
                    "Close today’s diary 🎯",
                    "Log your remaining calories to keep your progress and calculations accurate.",
                    "AI is ready again 🤖",
                    "Your free attempts have reset for quick tracking today.",
                    "Your weekly report is ready 📊",
                    "See your weight trend and updated recommendations.")),
            Map.entry("uk", new Messages(
                    "Закрий щоденник за сьогодні 🎯",
                    "Внеси залишок калорій, щоб не переривати прогрес і точність розрахунків.",
                    "AI знову готовий до роботи 🤖",
                    "Твої безкоштовні спроби відновлено для швидкого трекінгу сьогодні.",
                    "Твій звіт за тиждень готовий 📊",
                    "Поглянь на динаміку ваги та оновлені рекомендації.")),
            Map.entry("ru", new Messages(
                    "Закрой дневник за сегодня 🎯",
                    "Внеси оставшиеся калории, чтобы сохранить прогресс и точность расчётов.",
                    "AI снова готов к работе 🤖",
                    "Бесплатные попытки восстановлены для быстрого трекинга сегодня.",
                    "Твой недельный отчёт готов 📊",
                    "Посмотри динамику веса и обновлённые рекомендации.")),
            Map.entry("de", new Messages(
                    "Schließe dein Tagebuch für heute 🎯",
                    "Trage die restlichen Kalorien ein, damit Fortschritt "
                            + "und Berechnungen genau bleiben.",
                    "Die KI ist wieder bereit 🤖",
                    "Deine kostenlosen Versuche wurden für das schnelle Tracking heute erneuert.",
                    "Dein Wochenbericht ist fertig 📊",
                    "Sieh dir deinen Gewichtsverlauf und aktualisierte Empfehlungen an.")),
            Map.entry("el", new Messages(
                    "Κλείσε το ημερολόγιο της ημέρας 🎯",
                    "Κατάγραψε τις υπόλοιπες θερμίδες για συνεχή πρόοδο και ακριβείς υπολογισμούς.",
                    "Το AI είναι ξανά έτοιμο 🤖",
                    "Οι δωρεάν προσπάθειές σου ανανεώθηκαν για γρήγορη καταγραφή σήμερα.",
                    "Η εβδομαδιαία αναφορά σου είναι έτοιμη 📊",
                    "Δες την πορεία του βάρους σου και τις ενημερωμένες προτάσεις.")),
            Map.entry("es", new Messages(
                    "Cierra el diario de hoy 🎯",
                    "Registra las calorías restantes para mantener el progreso "
                            + "y la precisión de los cálculos.",
                    "La IA está lista de nuevo 🤖",
                    "Tus intentos gratuitos se han renovado para registrar rápidamente hoy.",
                    "Tu informe semanal está listo 📊",
                    "Consulta la evolución de tu peso y las recomendaciones actualizadas.")),
            Map.entry("fr", new Messages(
                    "Termine ton journal d’aujourd’hui 🎯",
                    "Ajoute les calories restantes pour préserver tes progrès "
                            + "et la précision des calculs.",
                    "L’IA est de nouveau prête 🤖",
                    "Tes essais gratuits ont été renouvelés pour un suivi rapide aujourd’hui.",
                    "Ton rapport hebdomadaire est prêt 📊",
                    "Consulte l’évolution de ton poids et les recommandations mises à jour.")),
            Map.entry("hi", new Messages(
                    "आज की डायरी पूरी करें 🎯",
                    "प्रगति और गणना की सटीकता बनाए रखने के लिए बची कैलोरी दर्ज करें।",
                    "AI फिर से तैयार है 🤖",
                    "आज तेज़ ट्रैकिंग के लिए आपके मुफ़्त प्रयास फिर से उपलब्ध हैं।",
                    "आपकी साप्ताहिक रिपोर्ट तैयार है 📊",
                    "वज़न का रुझान और नई सलाह देखें।")),
            Map.entry("id", new Messages(
                    "Selesaikan catatan hari ini 🎯",
                    "Catat sisa kalori agar progres dan perhitungan tetap akurat.",
                    "AI siap digunakan lagi 🤖",
                    "Percobaan gratismu sudah diperbarui untuk pelacakan cepat hari ini.",
                    "Laporan mingguanmu sudah siap 📊",
                    "Lihat tren berat badan dan rekomendasi terbaru.")),
            Map.entry("pl", new Messages(
                    "Zamknij dzisiejszy dziennik 🎯",
                    "Dodaj pozostałe kalorie, aby zachować postępy i dokładność obliczeń.",
                    "AI jest znów gotowe 🤖",
                    "Darmowe próby zostały odnowione na szybkie śledzenie dzisiaj.",
                    "Twój raport tygodniowy jest gotowy 📊",
                    "Sprawdź trend masy ciała i zaktualizowane zalecenia.")),
            Map.entry("pt", new Messages(
                    "Fecha o diário de hoje 🎯",
                    "Regista as calorias restantes para manter o progresso "
                            + "e a precisão dos cálculos.",
                    "A IA está pronta novamente 🤖",
                    "As tuas tentativas gratuitas foram renovadas para um registo rápido hoje.",
                    "O teu relatório semanal está pronto 📊",
                    "Vê a evolução do peso e as recomendações atualizadas.")),
            Map.entry("tr", new Messages(
                    "Bugünün günlüğünü tamamla 🎯",
                    "İlerlemeni ve hesaplamaların doğruluğunu korumak için kalan kalorileri ekle.",
                    "AI yeniden hazır 🤖",
                    "Bugün hızlı takip için ücretsiz denemelerin yenilendi.",
                    "Haftalık raporun hazır 📊",
                    "Kilo değişimini ve güncellenen önerileri incele.")),
            Map.entry("ar", new Messages(
                    "أكمل سجل اليوم 🎯",
                    "سجّل السعرات المتبقية للحفاظ على تقدمك ودقة الحسابات.",
                    "الذكاء الاصطناعي جاهز من جديد 🤖",
                    "تجددت محاولاتك المجانية للتتبع السريع اليوم.",
                    "تقريرك الأسبوعي جاهز 📊",
                    "اطّلع على تغيّر وزنك والتوصيات المحدّثة.")));

    public PushCopy message(String locale, NotificationType type) {
        Messages value = MESSAGES.getOrDefault(locale, MESSAGES.get("en"));
        return switch (type) {
            case DINNER -> new PushCopy(value.dinnerTitle(), value.dinnerBody());
            case AI_LIMIT_RESET -> new PushCopy(value.aiTitle(), value.aiBody());
            case WEEKLY_REPORT -> new PushCopy(value.weeklyTitle(), value.weeklyBody());
        };
    }

    public enum NotificationType {
        DINNER("/home"),
        AI_LIMIT_RESET("/home"),
        WEEKLY_REPORT("/insights");

        private final String route;

        NotificationType(String route) {
            this.route = route;
        }

        public String route() {
            return route;
        }
    }

    public record PushCopy(String title, String body) {
    }

    private record Messages(String dinnerTitle, String dinnerBody,
                            String aiTitle, String aiBody,
                            String weeklyTitle, String weeklyBody) {
    }
}
