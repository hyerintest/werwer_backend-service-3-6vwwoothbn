package com.tlc.test.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TagSorter implements OpenApiCustomiser {
    @Getter
    @Setter
    @AllArgsConstructor
    private static class TagOrder{
        private SwaggerMarkdown tag;
        private int order = 0;
    }

    @Override
    public void customise(OpenAPI openApi) {
        List<TagOrder> orders = Arrays.asList(
                new TagOrder(SwaggerMarkdown.ACTUATOR, 0),
                new TagOrder(SwaggerMarkdown.LOGIN, 1),
                new TagOrder(SwaggerMarkdown.DESCRIPTION, 2)
        );
        Map<String, Integer> orderMap = new HashMap<>();
        for (TagOrder order : orders) {
            orderMap.put(order.getTag().getName(), order.getOrder());
        }

        openApi.setTags(openApi.getTags().stream().sorted((a, b) -> {
            int aOrder = orderMap.get(a.getName());
            int bOrder = orderMap.get(b.getName());
            return aOrder - bOrder;
        }).collect(Collectors.toList()));
    }
}
