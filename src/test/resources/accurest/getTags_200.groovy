io.codearte.accurest.dsl.GroovyDsl.make {
    request {
        method 'GET'
        urlPath '/api/tags'
    }
    response {
        status 200
        body("""["Java", "Java8", "Spring", "SpringBoot", "Stream"]""")
        headers {
            header('Content-Type': 'application/json;charset=UTF-8')
        }
    }
}