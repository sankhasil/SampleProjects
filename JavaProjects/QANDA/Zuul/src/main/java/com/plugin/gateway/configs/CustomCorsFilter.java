package com.plugin.gateway.configs;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Configuration
public class CustomCorsFilter {

    @Bean
    public FilterRegistrationBean corsFilter() {

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("*", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
    
//    @Bean
//	@Autowired
//	public ObjectMapper mapperConfiguration() {
//    	ObjectMapper om = new ObjectMapper();
//		SimpleModule module = new SimpleModule();
//		module.addSerializer(JsonObject.class,new JsonSerializer<JsonObject>() {
//			@Override
//			public void serialize(JsonObject value, JsonGenerator gen, SerializerProvider serializers)
//					throws IOException {
//				Gson jsonParser = new Gson();
//				String encoded = jsonParser.toJson(value);
//				gen.writeRawValue(encoded);
//			}
//		});
//		module.addDeserializer(JsonObject.class, new JsonDeserializer<JsonObject>() {
//
//			@Override
//			public JsonObject deserialize(JsonParser parser, DeserializationContext context)
//					throws IOException, JsonProcessingException {
//				return new com.google.gson.JsonParser().parse(parser.getCodec().readTree(parser).toString()).getAsJsonObject();
//			}
//		});
//		
//		module.addDeserializer(JsonArray.class, new JsonDeserializer<JsonArray>() {
//
//			@Override
//			public JsonArray deserialize(JsonParser parser, DeserializationContext context)
//					throws IOException, JsonProcessingException {
//				return new com.google.gson.JsonParser().parse(parser.getCodec().readTree(parser).toString()).getAsJsonArray();
//			}
//		});
//		return om;
//    }
}