Usage rapide - Intégration Jitsi Meet SDK

1) Ajouter la dépendance (app/build.gradle):

implementation('org.jitsi.react:jitsi-meet-sdk:3.10.2') { transitive = true }

Remarque: vérifiez la version la plus récente sur Maven central ou la doc Jitsi.

2) Autorisations (déjà présentes dans `AndroidManifest.xml`):
- INTERNET
- CAMERA
- RECORD_AUDIO

3) Activity ajoutée: `com.example.karhebti_android.ui.screens.JitsiCallActivity`
- Lancez-la avec un Intent depuis votre écran SOS lorsque la panne est acceptée par le garage.

Exemple d'appel depuis un composable / activity:

val ctx = LocalContext.current
val intent = Intent(ctx, JitsiCallActivity::class.java)
intent.putExtra("room", "sos-12345")
ctx.startActivity(intent)

4) Notes:
- Jitsi utilise WebRTC. Sur l'émulateur Android, la caméra peut ne pas fonctionner correctement — testez sur un vrai appareil.
- Vérifiez que votre clé Play Store / restrictions API n'empêchent pas l'accès réseau.

5) Fonctionnement souhaité côté backend:
- Quand un SOS est accepté, backend envoie la roomId (ex: sos-690f5...) au client.
- Le client ouvre `JitsiCallActivity` et rejoint la room.

Si tu veux je peux aussi injecter un bouton "Appel vidéo" dans `BreakdownHistoryScreen` ou `BreakdownDetailScreen` et lancer automatiquement l'Activity avec la room fournie par l'API.

