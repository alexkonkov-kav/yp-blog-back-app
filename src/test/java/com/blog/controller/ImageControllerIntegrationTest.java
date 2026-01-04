package com.blog.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ImageControllerIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        jdbcTemplate.execute("DELETE FROM post");
        jdbcTemplate.execute("ALTER TABLE post ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("""
                    INSERT INTO post (title, text)
                    VALUES ('Test title 1','Test text 1')
                """);
        jdbcTemplate.execute("""
                    INSERT INTO post (title, text)
                    VALUES ('Test title 2','Test text 2')
                """);
    }

    @Test
    void uploadAndDownloadImage_success() throws Exception {
        byte[] pngStub = new byte[]{(byte) 137, 80, 78, 71};
        MockMultipartFile file = new MockMultipartFile("image", "image.png", "image/png", pngStub);

        mockMvc.perform(multipart("/api/posts/{id}/image", 1L).file(file).with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isCreated())
                .andExpect(content().string("ok"));

        mockMvc.perform(get("/api/posts/{id}/image", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().bytes(pngStub));
    }

    @Test
    void uploadImage_emptyFile_badRequest() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("image", "image.png", "image/png", new byte[0]);

        mockMvc.perform(multipart("/api/posts/{id}/image", 1L).file(empty).with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("empty image"));
    }

    @Test
    void uploadImage_postNotFound_404() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "image.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/posts/{id}/image", 999L).file(file).with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isNotFound());
    }

    @Test
    void getImage_postHasNoImage_404() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/image", 2L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getImage_postNotFound_404() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/image", 777L))
                .andExpect(status().isNotFound());
    }
}
