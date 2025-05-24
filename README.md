1° FASE: 
1. installare curl per windows: https://curl.se/windows/
  Curl permette di effettuare una chiamata a un servizio http.
  Nel caso di airqino, per una centralina si può recuperare l'ultimo valore trasmesso come segue: curl https://airqino-api.magentalab.it/getLastValuesRaw/SMART512
  Viene restituito un pacchetto JSON

3. Eseguire la chiamata curl per ogni stazione dell'elenco fornito nel file excel allegato che contiene tutte le centraline attualmente installate.. 

4. Per ogni centralina, inserire nel file excel la colonna relativa all'ultimo timestamp e una colonna per ogni dato ricevuto con l'indicazione del dato stesso.
   Inserire inoltre una colonna calcolata che indica da quante ore la centralina non sta trasmettendo. Puoi usare i fogli di google per fare l'excel, così ci puoi lavorare da qualsiasi pc. 

2° FASE:
1. scrivere un programma a linea di comando che prende in ingresso la lista di centraline e produce il report
  1.1 Leggere in ingresso la lista. Inizialmente puoi usare un array pre-popolato con tutte le centraline nel codice. Poi se ci resta tempo lo leggiamo da file esterno.
      Provare inizialmente con un numero limitato di centraline.
  1.2 Per ogni centralina, effettuare una chiamata alle API analoga a quella fatta con curl. Esempio (https://www.baeldung.com/java-http-request):
      URL url = new URL("http://example.com");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.setDoOutput(true); DataOutputStream out = new DataOutputStream(con.getOutputStream()); out.flush(); out.close();

2. Interpretare la risposta:
  int status = con.getResponseCode();
  BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
  String inputLine;
  StringBuffer content = new StringBuffer();
  while ((inputLine = in.readLine()) != null) {
      content.append(inputLine);
  }
  in.close();
  con.disconnect();

3. Come prima versione, stampare solo il timestamp di trasmissione della centralina a scherma.
   Poi calcolare la differenza in ore (https://www.geeksforgeeks.org/find-the-duration-of-difference-between-two-dates-in-java/).
   Poi stampare l'output a schermo.

3° FASE: 
1. il prossimo passo è quello di recuperare le centraline al momento del lancio del programma.
  La chiamata che restituisce l'elenco delle centraline è questa (puoi provarla con curl): https://airqino-backend.magentalab.it/api/station
  viene restituito un json array di element fatti in questo modo:
  {
          "id": 283249436,
          "name": "SMART193",
          "latitude": 43.6058349609375,
          "longitude": 11.085165977478027,
          "legacy": null,
          "note": "16, Via Colle Montalbino, Colle di Sopra, Montespertoli, Unione dei comuni Circondario dell'Empolese Valdelsa, Firenze, Toscana, 50025, Italia",
          "stationType": {
              "id": 9,
              "name": "AIRQINO_POSITIONAL",
              "sensors": [],
              "category": null,
              "initialCollocation": null,
              "finalCollocation": null,
              "estimatedPosition": null,
              "activationDate": null
          },
          "projects": [
              {
                "id": 483544927,
                "name": "Montalbino",
                "icon": "Pin_192658.png",
                "projectPic": null,
                "zoomLevel": 10,
                "visible": true
             }
           ],
          "imageUrl": [],
          "dataSessions": []
      }
  I campi che ci interessano sono "name", "latitude", "longitude" (che devono far parte della riga di output).
