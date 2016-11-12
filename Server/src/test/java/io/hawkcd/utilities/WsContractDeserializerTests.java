package io.hawkcd.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.hawkcd.utilities.deserializers.WsContractDeserializer;
import io.hawkcd.model.dto.ConversionObject;
import io.hawkcd.model.dto.WsContractDto;
import io.hawkcd.model.enums.NotificationType;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

public class WsContractDeserializerTests {
    private Gson jsonConverter;
    private WsContractDeserializer deserializer;

    @Before
    public void setUp() {
        this.jsonConverter = new Gson();
        this.deserializer = new WsContractDeserializer();
    }

    @Test
    public void deserialize_WithValidJson() {
        //Arrange
        String jsonAsString = "{\n" +
                "\"className\": \"testClass\",\n" +
                "\"packageName\": \"testPackage\",\n" +
                "\"methodName\": \"testMethod\",\n" +
                "\"result\": \"testResult\",\n" +
                "\"notificationType\": \"SUCCESS\",\n" +
                "\"errorMessage\": \"testErrorMessage\",\n" +
                "\"args\": [{\n" +
                "\"packageName\": \"testPackage\",\n" +
                "\"object\": {\"testObject\" : \"someValue\"\n}" +
                "}]\n" +
                "}";

        JsonElement jsonElement = this.jsonConverter.fromJson(jsonAsString, JsonElement.class);

        WsContractDto expectedResult = new WsContractDto();
        expectedResult.setClassName("testClass");
        expectedResult.setPackageName("testPackage");
        expectedResult.setMethodName("testMethod");
        expectedResult.setResult("testResult");
		expectedResult.setNotificationType(NotificationType.SUCCESS);
        expectedResult.setErrorMessage("testErrorMessage");
        ConversionObject[] args = {new ConversionObject()};
        args[0].setPackageName("testPackage");
        args[0].setObject("{\"testObject\":\"someValue\"}");
        expectedResult.setArgs(args);

        //Act
        WsContractDto actualResult = this.deserializer.deserialize(jsonElement, null, null);

        //Assert
        ReflectionAssert.assertReflectionEquals(expectedResult, actualResult);
    }

    @Test(expected = JsonParseException.class)
    public void deserialize_JsonWithMissingField() {
        //Arrange
        String jsonAsString = "{\n" +
                "\"packageNameError\": \"testPackage\",\n" +
                "\"methodName\": \"testMethod\",\n" +
                "\"result\": \"testResult\",\n" +
                "\"error\": \"testError\",\n" +
                "\"errorMessage\": \"testErrorMessage\",\n" +
                "\"args\": [{\n" +
                "\"packageName\": \"testPackage\",\n" +
                "\"object\": \"testObject\"\n" +
                "}]\n" +
                "}";
        JsonElement jsonElement = this.jsonConverter.fromJson(jsonAsString, JsonElement.class);

        //Act
        this.deserializer.deserialize(jsonElement, null, null);
    }

    @Test(expected = JsonParseException.class)
    public void deserialize_JsonWithMisspelledField() {
        //Arrange
        String jsonAsString = "{\n" +
                "\"packageNameError\": \"testPackage\",\n" +
                "\"methodName\": \"testMethod\",\n" +
                "\"result\": \"testResult\",\n" +
                "\"error\": \"testError\",\n" +
                "\"errorMessage\": \"testErrorMessage\",\n" +
                "\"args\": [{\n" +
                "\"packageName\": \"testPackage\",\n" +
                "\"object\": \"testObject\"\n" +
                "}]\n" +
                "}";
        JsonElement jsonElement = this.jsonConverter.fromJson(jsonAsString, JsonElement.class);

        //Act
        this.deserializer.deserialize(jsonElement, null, null);
    }
}