package sixkiller.sample.restapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import sixkiller.sample.restapi.resource.RootResource;

/**
 * Created by ala on 9.5.16.
 */
@RestController
@RequestMapping("/api")
public class RootRestController {

    @RequestMapping(method = RequestMethod.GET)
    public RootResource getRoot() {
        return new RootResource();
    }

}
