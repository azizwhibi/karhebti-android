#!/usr/bin/env python3
"""
Script de test pour vérifier les demandes SOS
"""

import requests
import sys
import json

# Configuration
BASE_URL = "http://172.18.1.246:3000"
TOKEN = ""  # À remplir avec un vrai token

def test_breakdowns_endpoint():
    """Test de l'endpoint /breakdowns"""
    print("🧪 Test: Récupération des demandes SOS")
    print("=" * 50)

    if not TOKEN:
        print("❌ Erreur: TOKEN non fourni")
        print("   Veuillez définir le TOKEN dans le script")
        return False

    headers = {
        "Authorization": f"Bearer {TOKEN}",
        "Content-Type": "application/json"
    }

    try:
        # Test 1: Récupérer toutes les demandes
        print("\n📋 Test 1: GET /breakdowns")
        response = requests.get(f"{BASE_URL}/breakdowns", headers=headers, timeout=10)
        print(f"   Status Code: {response.status_code}")

        if response.status_code == 200:
            data = response.json()
            print(f"   ✅ Succès!")

            # Analyser la réponse
            if "breakdowns" in data:
                breakdowns = data["breakdowns"]
                print(f"   📊 Nombre de demandes: {len(breakdowns)}")

                if len(breakdowns) > 0:
                    print(f"\n   🔍 Détails des demandes:")
                    for idx, bd in enumerate(breakdowns, 1):
                        print(f"      {idx}. ID: {bd.get('_id')}")
                        print(f"         Status: {bd.get('status')}")
                        print(f"         Type: {bd.get('type')}")
                        print(f"         AssignedTo: {bd.get('assignedTo')}")
                        print(f"         UserID: {bd.get('userId')}")
                        print()
                else:
                    print("   ⚠️  Aucune demande SOS trouvée dans la base")
                    print("   💡 Conseil: Créez une demande SOS de test")
            elif "data" in data:
                breakdowns = data["data"]
                print(f"   📊 Nombre de demandes: {len(breakdowns)}")
            else:
                print(f"   ⚠️  Format de réponse inattendu: {data}")

            print(f"\n   📄 Réponse complète:")
            print(f"   {json.dumps(data, indent=2)}")

        elif response.status_code == 401:
            print(f"   ❌ Erreur 401: Non authentifié")
            print(f"   💡 Le token est peut-être expiré ou invalide")
        elif response.status_code == 403:
            print(f"   ❌ Erreur 403: Non autorisé")
            print(f"   💡 L'utilisateur n'a peut-être pas le rôle 'propGarage'")
        else:
            print(f"   ❌ Erreur: {response.text}")

        # Test 2: Récupérer avec filtre status=pending
        print(f"\n📋 Test 2: GET /breakdowns?status=pending")
        response2 = requests.get(
            f"{BASE_URL}/breakdowns",
            params={"status": "pending"},
            headers=headers,
            timeout=10
        )
        print(f"   Status Code: {response2.status_code}")
        if response2.status_code == 200:
            data2 = response2.json()
            breakdowns2 = data2.get("breakdowns", data2.get("data", []))
            print(f"   ✅ Demandes 'pending': {len(breakdowns2)}")

        # Test 3: Récupérer avec filtre status=PENDING (majuscules)
        print(f"\n📋 Test 3: GET /breakdowns?status=PENDING")
        response3 = requests.get(
            f"{BASE_URL}/breakdowns",
            params={"status": "PENDING"},
            headers=headers,
            timeout=10
        )
        print(f"   Status Code: {response3.status_code}")
        if response3.status_code == 200:
            data3 = response3.json()
            breakdowns3 = data3.get("breakdowns", data3.get("data", []))
            print(f"   ✅ Demandes 'PENDING': {len(breakdowns3)}")

        print("\n" + "=" * 50)
        return True

    except requests.exceptions.ConnectionError:
        print(f"   ❌ Erreur: Impossible de se connecter à {BASE_URL}")
        print(f"   💡 Vérifiez que le backend est démarré")
        return False
    except requests.exceptions.Timeout:
        print(f"   ❌ Erreur: Timeout de la requête")
        return False
    except Exception as e:
        print(f"   ❌ Erreur inattendue: {e}")
        return False

def main():
    print("🔧 OUTIL DE TEST - Demandes SOS")
    print("=" * 50)
    print(f"Backend URL: {BASE_URL}")
    print()

    if not TOKEN:
        print("⚠️  TOKEN non défini!")
        print()
        print("📝 Comment obtenir un token:")
        print("   1. Se connecter à l'application")
        print("   2. Vérifier les logs: adb logcat | grep 'Token'")
        print("   3. Ou utiliser Postman/curl pour POST /auth/login")
        print()
        print("📝 Exemple d'utilisation:")
        print(f"   python {sys.argv[0]}")
        print("   Puis modifier TOKEN dans le script")
        print()
        return

    # Exécuter les tests
    test_breakdowns_endpoint()

if __name__ == "__main__":
    main()

