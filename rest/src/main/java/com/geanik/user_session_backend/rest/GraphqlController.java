package com.geanik.user_session_backend.rest;

import com.geanik.user_session_backend.graphql.UserService;
import com.geanik.user_session_backend.logic.BusinessLogic;
import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class GraphqlController {

    private static Logger log = LoggerFactory.getLogger(GraphqlController.class);

    private BusinessLogic logic;

    private GraphQL graphQL;

    public GraphqlController(BusinessLogic logic) {
        this.logic = logic;
    }

    @PostConstruct
    private void init() {
        UserService userService = new UserService(logic); // instantiate the service (or inject by Spring or another framework)
        GraphQLSchema schema = new GraphQLSchemaGenerator()
            .withOperationsFromSingleton(userService) // register the service
            .generate(); // done

        graphQL = GraphQL.newGraphQL(schema).build();
    }

    @RequestMapping(value = "/graphql", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> graph(@RequestBody Map<String, Object> request) {

        ExecutionInput.Builder builder = ExecutionInput.newExecutionInput();

        if (request.get("query") != null) {
            builder = builder.query(request.get("query").toString());

            if (request.get("operationName") != null) {
                builder = builder.operationName(request.get("operationName").toString());

                if (request.get("variables") != null) {
                    try {
                        builder = builder.variables((Map<String, Object>) request.get("variables"));
                    } catch (ClassCastException e) {
                        return null;
                    }
                }
            }
        }

        return graphQL.execute(builder.build()).toSpecification();
    }
}
