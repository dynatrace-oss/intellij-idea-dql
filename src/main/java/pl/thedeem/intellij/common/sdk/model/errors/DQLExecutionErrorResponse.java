package pl.thedeem.intellij.common.sdk.model.errors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import pl.thedeem.intellij.common.sdk.model.DQLSyntaxErrorPositionDetails;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DQLExecutionErrorResponse {
    public String message;
    public DQLErrorDetails details;
    public Integer code;

    public static final class DQLErrorDetails {
        public String exceptionType;
        public String errorType;
        public String errorMessage;
        public List<String> arguments;
        public String queryString;
        public List<String> errorMessageFormatSpecifierTypes;
        public String errorMessageFormat;
        public String queryId;
        public DQLSyntaxErrorPositionDetails syntaxErrorPosition;

        public String getExceptionType() {
            return exceptionType;
        }

        public String getErrorType() {
            return errorType;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public List<String> getArguments() {
            return arguments;
        }

        public String getQueryString() {
            return queryString;
        }

        public List<String> getErrorMessageFormatSpecifierTypes() {
            return errorMessageFormatSpecifierTypes;
        }

        public String getErrorMessageFormat() {
            return errorMessageFormat;
        }

        public String getQueryId() {
            return queryId;
        }

        public DQLSyntaxErrorPositionDetails getSyntaxErrorPosition() {
            return syntaxErrorPosition;
        }
    }
}
