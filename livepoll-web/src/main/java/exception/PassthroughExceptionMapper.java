package exception;

import jakarta.annotation.Priority;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

@Priority(100)
public class PassthroughExceptionMapper implements ResponseExceptionMapper<WebApplicationException> {
    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status >= 400;
    }

    @Override
    public WebApplicationException toThrowable(Response response) {
        return new WebApplicationException(response);
    }
}