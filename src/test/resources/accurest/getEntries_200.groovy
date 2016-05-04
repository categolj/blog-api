io.codearte.accurest.dsl.GroovyDsl.make {
    request {
        method 'GET'
        urlPath '/api/entries'
    }
    response {
        status 200
        body(
                content: [
                        [entryId    : 2,
                         content    : 'Spring Boot!',
                         created    : [name: 'making', date: $(client('2016-05-04T19:09:28.272+09:00'), server(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{1,3}\\+[0-9]{2}:[0-9]{2}')))],
                         updated    : [name: 'making', date: $(client('2016-05-04T19:09:28.272+09:00'), server(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{1,3}\\+[0-9]{2}:[0-9]{2}')))],
                         frontMatter: [title: 'Hello Spring Boot', tags: ['Java', 'Spring', 'SpringBoot'], categories: ['Programming', 'Java', 'Spring', 'Boot']]],
                        [entryId    : 1,
                         content    : 'Java8!',
                         created    : [name: 'making', date: $(client('2016-05-04T19:09:28.272+09:00'), server(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{1,3}\\+[0-9]{2}:[0-9]{2}')))],
                         updated    : [name: 'making', date: $(client('2016-05-04T19:09:28.272+09:00'), server(regex('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{1,3}\\+[0-9]{2}:[0-9]{2}')))],
                         frontMatter: [title: 'Hello Java8', tags: ['Java', 'Java8', 'Stream'], categories: ['Programming']]]
                ],
                size: 10,
                number: 0,
                sort: null,
                first: true,
                last: true,
                totalPages: 1,
                totalElements: 2,
                numberOfElements: 2)
        headers {
            header('Content-Type': 'application/json;charset=UTF-8')
        }
    }
}