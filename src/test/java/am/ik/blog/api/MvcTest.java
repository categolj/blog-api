package am.ik.blog.api;

import am.ik.categolj3.api.category.CategoryRestController;
import am.ik.categolj3.api.category.InMemoryCategoryService;
import am.ik.categolj3.api.entry.*;
import am.ik.categolj3.api.event.EntryPutEvent;
import am.ik.categolj3.api.tag.InMemoryTagService;
import am.ik.categolj3.api.tag.TagRestController;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class MvcTest {
    OffsetDateTime now = OffsetDateTime.now();
    List<Entry> mockEntries = Arrays.asList(
            Entry.builder()
                    .entryId(2L)
                    .content("Spring Boot!")
                    .frontMatter(FrontMatter.builder()
                            .title("Hello Spring Boot")
                            .categories(Arrays.asList("Programming", "Java", "Spring", "Boot"))
                            .tags(Arrays.asList("Java", "Spring", "SpringBoot"))
                            .build())
                    .created(Author.builder().name("making").date(now).build())
                    .updated(Author.builder().name("making").date(now).build())
                    .build(),
            Entry.builder()
                    .entryId(1L)
                    .content("Java8!")
                    .frontMatter(FrontMatter.builder()
                            .title("Hello Java8")
                            .categories(Arrays.asList("Programming", "Java"))
                            .tags(Arrays.asList("Java", "Java8", "Stream"))
                            .build())
                    .created(Author.builder().name("making").date(now).build())
                    .updated(Author.builder().name("making").date(now).build())
                    .build());
    List<Entry> mockEntriesNoContent = mockEntries.stream()
            .map(x -> {
                Entry e = new Entry();
                BeanUtils.copyProperties(x, e, "content");
                return e;
            })
            .collect(Collectors.toList());

    MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter(new Jackson2ObjectMapperBuilder()
            .dateFormat(new StdDateFormat())
            .indentOutput(true)
            .build());

    @Before
    public void setup() {
        EntryRestController entryRestController = new EntryRestController();
        EntryService entryService = mock(EntryService.class);
        ReflectionTestUtils.setField(entryRestController, "entryProperties", new EntryProperties());
        ReflectionTestUtils.setField(entryRestController, "entryService", entryService);

        TagRestController tagRestController = new TagRestController();
        InMemoryTagService tagService = new InMemoryTagService();
        tagService.handlePutEntry(new EntryPutEvent.Bulk(mockEntries.stream().map(EntryPutEvent::new).collect(Collectors.toList())));
        ReflectionTestUtils.setField(tagRestController, "tagService", tagService);

        CategoryRestController categoryRestController = new CategoryRestController();
        InMemoryCategoryService categoryService = new InMemoryCategoryService();
        categoryService.handlePutEntry(new EntryPutEvent.Bulk(mockEntries.stream().map(EntryPutEvent::new).collect(Collectors.toList())));
        ReflectionTestUtils.setField(categoryRestController, "categoryService", categoryService);


        RestAssuredMockMvc.standaloneSetup(MockMvcBuilders
                .standaloneSetup(entryRestController, tagRestController, categoryRestController)
                .alwaysDo(print())
                .setMessageConverters(jackson2HttpMessageConverter)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()));

        when(entryService.findAll(anyObject()))
                .thenReturn(new PageImpl<>(mockEntries, new PageRequest(0, 10), mockEntries.size()));
        when(entryService.findAll(anyObject(), anyObject()))
                .thenReturn(new PageImpl<>(mockEntriesNoContent, new PageRequest(0, 10), mockEntries.size()));
        when(entryService.findOne(eq(2L)))
                .thenReturn(mockEntries.get(0));
        when(entryService.findOne(eq(1L)))
                .thenReturn(mockEntries.get(1));
    }
}
