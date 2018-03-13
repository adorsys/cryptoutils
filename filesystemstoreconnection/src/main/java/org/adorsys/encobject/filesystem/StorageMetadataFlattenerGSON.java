package org.adorsys.encobject.filesystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.adorsys.encobject.domain.Location;
import org.adorsys.encobject.domain.StorageMetadata;
import org.adorsys.encobject.service.impl.SimpleLocationImpl;
import org.adorsys.encobject.service.impl.SimpleStorageMetadataImpl;

import java.lang.reflect.Type;

/**
 * Created by peter on 21.02.18 at 20:08.
 */
public class StorageMetadataFlattenerGSON {
    private Gson gson = new GsonBuilder().setPrettyPrinting()
            .registerTypeAdapter(Location.class, new InterfaceAdapter<SimpleLocationImpl>())
            .create();

    public StorageMetadata fromJson(String jsonString) {
        return gson.fromJson(jsonString, SimpleStorageMetadataImpl.class);
    }

    public String toJson(StorageMetadata storageMetadata) {
        return gson.toJson(storageMetadata);
    }


    /**
     * Created by peter on 21.02.18 at 08:58.
     * Author: Mauricio Silva Manrique
     * http://technology.finra.org/code/serialize-deserialize-interfaces-in-java.html
     */
    private static class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

        private static final String CLASSNAME = "CLASSNAME";
        private static final String DATA = "DATA";

        @Override
        public T deserialize(JsonElement jsonElement, Type type,
                             JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
            String className = prim.getAsString();
            Class klass = getObjectClass(className);
            return jsonDeserializationContext.deserialize(jsonObject.get(DATA), klass);
        }
        @Override
        public JsonElement serialize(T jsonElement, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(CLASSNAME, jsonElement.getClass().getName());
            jsonObject.add(DATA, jsonSerializationContext.serialize(jsonElement));
            return jsonObject;
        }

        /****** Helper method to get the className of the object to be deserialized *****/
        public Class getObjectClass(String className) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                //e.printStackTrace();
                throw new JsonParseException(e.getMessage());
            }
        }
    }

}
