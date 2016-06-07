package sixkiller.sample.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by ala on 17.5.16.
 */
public class OnPopStateRedirectFilter extends OncePerRequestFilter {

    @Autowired
    private ApplicationConfigurationProperties configuration;

    /**
     * Filter redirect from configured angular's (and not necessarily only angulars') urls
     * which was reached by onpopstate HTML5 event.
     * It enables browser to reload and find a valid content.
     * @param request Servlet request
     * @param response Servlet response
     * @param filterChain Filter chain to control filters' flow
     * @throws ServletException Standard filter exception, not thrown
     * @throws IOException Standard filter exception, not thrown
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (isHtmlRequest(request) && startsWith(request.getServletPath(), configuration.getOnPopStateUrls())) {
            response.sendRedirect("/");
        } else {
            filterChain.doFilter(request, response);
        }

    }

    private boolean isHtmlRequest(HttpServletRequest request) {
        String accepptHeader = request.getHeader(HttpHeaders.ACCEPT);

        if (accepptHeader == null) {
            return false;
        }

        return request.getHeader(HttpHeaders.ACCEPT).contains(MediaType.TEXT_HTML.toString());
    }

    private boolean startsWith(String url, String[] matchUrlParts) {
        if (matchUrlParts == null) {
            return false;
        }
        return Arrays.stream(matchUrlParts).anyMatch(
                s -> url.startsWith(s)
        );
    }

}
