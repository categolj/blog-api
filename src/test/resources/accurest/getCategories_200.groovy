io.codearte.accurest.dsl.GroovyDsl.make {
    request {
        method 'GET'
        urlPath '/api/categories'
    }
    response {
        status 200
        body([["Programming", "Java"], ["Programming", "Java", "Spring", "Boot"]])
        headers {
            header('Content-Type': 'application/json;charset=UTF-8')
        }
    }
}