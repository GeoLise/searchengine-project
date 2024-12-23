package searchengine.dto.statistics;

import lombok.Data;

@Data
public class ErrorResponse {
    private boolean result;
    private String error;

    public ErrorResponse(String error){
        this.result = false;
        this.error = error;
    }
}
