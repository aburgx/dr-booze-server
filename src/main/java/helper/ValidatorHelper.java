package helper;

import entities.PersonBO;
import entities.UserBO;
import org.json.JSONObject;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("Duplicates")
public class ValidatorHelper {

    private final Validator validator;

    public ValidatorHelper() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        this.validator = vf.getValidator();
    }

    public String validateUser(UserBO user) {
        Set<ConstraintViolation<UserBO>> userViolations = validator.validate(user);
        if (userViolations.size() > 0) {
            List<JSONObject> jsonList = new ArrayList<>();

            userViolations.forEach(violation -> {
                JSONObject errorJson = new JSONObject();
                errorJson.accumulate("error_code", violation.getMessage());
                errorJson.accumulate("error_reason", violation.getPropertyPath());
                jsonList.add(errorJson);
            });

            // return the violations
            JSONObject json = new JSONObject();
            json.put("error", jsonList);
            String jsonString = json.toString();
            System.out.println("Violations: " + jsonString);
            return jsonString;
        }
        return null;
    }

    public String validatePerson(PersonBO person) {
        Set<ConstraintViolation<PersonBO>> personViolations = validator.validate(person);
        if (personViolations.size() > 0) {
            List<JSONObject> jsonList = new ArrayList<>();

            personViolations.forEach(violation -> {
                JSONObject errorJson = new JSONObject();
                errorJson.accumulate("error_code", violation.getMessage());
                errorJson.accumulate("error_reason", violation.getPropertyPath());
                jsonList.add(errorJson);
            });

            // return the violations
            JSONObject json = new JSONObject();
            json.put("error", jsonList);
            String jsonString = json.toString();
            System.out.println("Violations: " + jsonString);
            return jsonString;
        }
        return null;
    }
}
