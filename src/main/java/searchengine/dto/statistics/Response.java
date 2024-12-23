package searchengine.dto.statistics;

import lombok.Data;
import lombok.Setter;

@Data
public class Response {
    private boolean result;

    public Response(){
        this.result = true;
    }
}
