package tn.esprit.forumservice.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostValidator {

    // Limite de posts par minute
    private static final int MAX_POSTS_PER_MINUTE = 3;

    private static final List<String> SPAM_WORDS = List.of(
            // Argent & gains
            "gagner de l'argent", "gagner de l'argent facilement", "revenu passif",
            "devenir riche", "argent rapide", "travailler de chez soi",
            "100% gratuit", "sans investissement",
            // Marketing agressif
            "offre spéciale", "offre limitée", "offre exclusive",
            "promotion exceptionnelle", "promotion", "soldes flash",
            "prix incroyable", "meilleur prix garanti", "livraison gratuite", "cashback",
            // Call to action trompeur
            "cliquez ici", "cliquez maintenant", "inscrivez-vous maintenant",
            "rejoignez-nous", "téléchargez gratuitement", "accès immédiat",
            // Phishing & fraude
            "vérifiez votre compte", "confirmer vos informations",
            "votre compte a été suspendu", "mise à jour requise",
            "connectez-vous pour sécuriser", "mot de passe expiré",
            "activité suspecte détectée", "cliquez pour débloquer",
            "gagnez un iphone", "vous avez été sélectionné",
            "félicitations vous avez gagné"
    );

    private static final List<String> BLOCKED_DOMAINS = List.of(
            // Malware & exploit
            "badsite.com", "malware.org", "exploit-kit.net", "ransomware-dl.com",
            "drive-by-download.net", "trojan-host.ru", "virus-spread.cc",
            // Phishing bancaire & crypto
            "paypal-secure-login.com", "amazon-account-verify.net",
            "secure-bank-update.com", "crypto-profit-fast.com",
            "bitcoin-doubler.net", "invest-crypto-now.io", "free-btc-generator.com",
            // Spam & scam
            "make-money-online.biz", "easy-cash-daily.net", "work-from-home-scam.com",
            "get-rich-quick.info", "free-iphone-winner.com", "survey-reward-fake.com",
            "prize-claim-now.net", "lottery-winner-alert.com",
            // TLDs & hébergeurs douteux
            "spamlink.ru", "click-bait.tk", "adware-host.cn", "shady-redirect.top",
            "fake-news-site.xyz", "deepfake-content.cc", "piracy-stream.ws",
            "illegal-download.to", "darkweb-market.onion.ws"
    );

    // Vérifie si l'utilisateur poste trop rapidement
    public static boolean isFlood(LocalDateTime lastPostTime) {
        if (lastPostTime == null) return false;

        Duration duration = Duration.between(lastPostTime, LocalDateTime.now());
        return duration.toSeconds() < 30; // 30 secondes
    }


    // Vérifie si le texte contient des mots spam
    public static boolean containsSpam(String text) {
        String lower = text.toLowerCase();
        return SPAM_WORDS.stream().anyMatch(lower::contains);
    }

    // Vérifie si le texte contient un lien bloqué
    public static boolean containsBlockedLink(String text) {
        Pattern urlPattern = Pattern.compile("https?://\\S+");
        Matcher matcher = urlPattern.matcher(text);
        while (matcher.find()) {
            String url = matcher.group();
            for (String domain : BLOCKED_DOMAINS) {
                if (url.contains(domain)) return true;
            }
        }
        return false;
    }

    // Vérifie si le texte est similaire à un autre (Levenshtein distance simple)
    public static boolean isDuplicate(String newText, String oldText) {
        if (newText == null || oldText == null) return false;
        newText = newText.trim().toLowerCase();
        oldText = oldText.trim().toLowerCase();
        return newText.equals(oldText); // simple égalité, tu peux faire plus complexe
    }
}
