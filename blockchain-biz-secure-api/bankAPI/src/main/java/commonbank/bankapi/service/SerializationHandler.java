package commonbank.bankapi.service;
import commonbank.bankapi.model.Confirmation;

import com.owlike.genson.Genson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;


public final class SerializationHandler {
    private SerializationHandler(){
    }
    /**
     * Metoda pomocnicza do serializacji obiektu do JSON
     */
    public static byte[] marshal(final Object obj) {
        try {
            String json = new Genson().serialize(obj);
            System.out.println("Serialized JSON (Java): " + json);
            return json.getBytes(UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error during serialization", e);
        }
    }
    public static String marshalString(final Object obj) {
        String json = new Genson().serialize(obj);
        System.out.println("Serialized JSON (Java): " + json);
        return json;
    }


    public static String returnSerializedString(final Confirmation confirmation, final String encryptedHash, final String hash) {
        try {
            Map<String, Object> jsonWithSignedHash = new HashMap<>();
            jsonWithSignedHash.put("encryptedHash", encryptedHash);
            jsonWithSignedHash.put("hash", hash);
            jsonWithSignedHash.put("data", confirmation);

            String jsonOutput = new Genson().serialize(jsonWithSignedHash);
            return jsonOutput;

        } catch (Exception e) {
            throw new RuntimeException("Error saving JSON to file", e);
        }
    }

    /**
     * Odczytanie pliku JSON, zdeserializowanie do mapy, a następnie zbudowanie obiektu Confirmation.
     * Zwraca obiekt Confirmation wczytany z JSON-a.
     */
    public static Confirmation loadConfirmationFromJson(String jsonContent) {
        try {
            Genson genson = new Genson();
            Map jsonMap = genson.deserialize(jsonContent, Map.class);

            if (!jsonMap.containsKey("data")) {
                throw new IllegalArgumentException("JSON does not contain 'data' key");
            }

            // Pominięty krok sprawdzenia poprawności klucza...

            Confirmation confirmation = genson.deserialize(genson.serialize(jsonMap.get("data")), Confirmation.class);

            System.out.println("Confirmation loaded: " + confirmation);

            return confirmation;
        } catch (ClassCastException e) {
            throw new RuntimeException("Error casting 'data' to expected type", e);
        } catch (NullPointerException e) {
            throw new RuntimeException("Key 'data' is missing in JSON", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid JSON structure", e);
        }
    }
}
