package centraline;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

public class Estrai {
    public static void main(String[] args) throws Exception {
        // Creazione Array per contenere i nome delle centraline
        ArrayList centraline = new ArrayList();
        
        for (Object st : AirqinoStationData()) {
            JSONObject station = (JSONObject) st;
            String name = station.get("name").toString();
            centraline.add(name);
        }
        Collections.sort(centraline);

        for (Object c : centraline) {
            try {
                // Creazione dell'URL per la chiamata
                URL url = new URL("https://airqino-api.magentalab.it/getLastValuesRaw/" + c);

                // Apertura della connessione HTTP
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                // Impostazione del metodo di richiesta
                con.setRequestMethod("GET");

                // Lettura dello status
                int status = con.getResponseCode();

                if (status < 200) { 
                    System.err.println("Errore nella risposta dello status per " + c);
                    System.err.println("Status risposta: " + status);
                    System.err.println("-------------------");
                } else {
                    // Creazione stringa Json
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer content = new StringBuffer();
                    while ((inputLine = br.readLine()) != null) {
                        content.append(inputLine);
                    }
                    br.close();
                    con.disconnect();

                    if(content.toString().equals("{    \"values\": []}")){
                        System.err.println("La centralina " + c + ": non è in funzione");
                        System.out.println("-------------------");
                    }else{
                        // Estrai valori (sensor, value) dal Json
                        JsonObject desObject = Jsoner.deserialize(content.toString(), new JsonObject());
                        JsonArray valuesArray = (JsonArray) desObject.get("values");
                        JsonArray valori = new JsonArray();

                        for (Object valueObj : valuesArray) {
                            JsonObject values = (JsonObject) valueObj;
                            String tutto = values.get("sensor").toString() + ":" + values.get("value").toString();
                            valori.add(tutto);
                        }

                        // Imposta data
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String data = sdf.format(new Date());
                        LocalDateTime dataCorrente = LocalDateTime.parse(data, formatter);
                
                        // Trova last_timestamp
                        String lastTimestamp = content.substring(24, 43);
                        LocalDateTime dataUltimaTrasmissione = LocalDateTime.parse(lastTimestamp, formatter);

                        // Stampa risultati
                        System.out.println("Data odierna: " + data);
                        System.out.println("Status risposta: " + status);
                        System.out.print("Centralina " + c + ": ");
                        System.out.print("Ultima tramissione --> " + lastTimestamp + ", ");
                        System.out.print("Valori sensori --> ");
                        for (Object i : valori) System.out.print(i + "; ");
                        System.out.print("Posizione --> ");
                        for (Object a : AirqinoStationData()) {
                            JSONObject station = (JSONObject) a;
                            Double lat = station.getDouble("latitude");
                            Double lon = station.getDouble("longitude");                        
                            System.out.print("latitude: " + lat + " - longitude: " + lon);
                            break;
                        }
                        System.out.println();
                        trovaDifferenzaData(dataUltimaTrasmissione, dataCorrente);
                        System.out.println("-------------------");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Metodo per trovare la differenza tra due date
    static void trovaDifferenzaData(LocalDateTime start_date, LocalDateTime end_date) {
        if (end_date.compareTo(start_date) > 0) {
            int difference_Years = end_date.getYear() - start_date.getYear();

            int difference_Month = end_date.getMonthValue() - start_date.getMonthValue();
            if (difference_Month < 0) difference_Month += 12;

            int difference_Days = end_date.getDayOfMonth() - start_date.getDayOfMonth();
            if (difference_Days < 0) difference_Days += 30;

            int difference_Hours = end_date.getHour() - start_date.getHour();
            if (difference_Hours < 0) difference_Hours += 24;  

            int difference_Minutes = end_date.getMinute() - start_date.getMinute();
            if (difference_Minutes < 0) difference_Minutes += 60;

            int difference_Seconds = end_date.getSecond() - start_date.getSecond();
            if (difference_Seconds < 0) difference_Seconds += 60;
            
            System.out.print("La centralina non trasmette da: ");
            System.out.println(difference_Years + " anni/o "+ "- " + difference_Month + " mesi/e " + "- "+ difference_Days + " giorni/o  " +
            difference_Hours + " ore/a " + ": " + difference_Minutes + " minuti/o " + ": " + difference_Seconds + " secondi "); 
        } else {
            System.err.println("Errore: La data della centralina supera la data odierna");
        }
    }

    // Metodo per creare l'Array delle stazioni
    static JSONArray AirqinoStationData() {
        String urlString = "https://airqino-backend.magentalab.it/api/station";
        
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            conn.disconnect();
            
            // analizzare le stringa di origine JSON
            JSONTokener jtoken = new JSONTokener(response.toString());
            JSONArray stationsArray = new JSONArray(jtoken);

            return stationsArray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}