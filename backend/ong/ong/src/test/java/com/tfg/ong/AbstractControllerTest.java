package com.tfg.ong;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractControllerTest {

	@Autowired
	protected MockMvc mockMvc;
	
	@Autowired
	protected ObjectMapper objectMapper;
	
	protected void assertResponseBodyIsOk(MvcResult mvcResult, Object objeto) throws Exception {
		
		String responseBodyEsperado = objectMapper.writeValueAsString(objeto);
		String responseBodyRecibido = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
		
		assertThat(responseBodyRecibido).isEqualToIgnoringWhitespace(responseBodyEsperado);
	}
}