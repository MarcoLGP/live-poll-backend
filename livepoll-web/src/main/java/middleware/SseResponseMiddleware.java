package middleware;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SseResponseMiddleware implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        String contentType = res.getMediaType() != null ? res.getMediaType().toString() : "";

        if (contentType.contains("text/event-stream")
                || req.getUriInfo().getPath().contains("/stream")) {
            res.getHeaders().putSingle("X-Accel-Buffering", "no");
            res.getHeaders().putSingle("Cache-Control", "no-cache, no-transform");
            res.getHeaders().putSingle("Connection", "keep-alive");
        }
    }
}
