Start [eureka-server](../eureka-server), then

    $ mvn clean package -Dmaven.test.skip=true
    $ cf login -a api.<your CF target>
    $ cf create-user-provided-service eureka-service -p '{"uri":"http://eureka:<password>@eureka-server.<domain>"}'
    $ cf create-user-provided-service blog-api-log -l syslog://<your log manager>
    $ cf create-service searchly starter blog-es
    $ cf create-service rediscloud 30mb blog-redis
    $ cf push

In case of PCF

    $ cf create-user-provided-service blog-es -p '{"sslUri": "https://...", "uri": "http://..."}'
    $ cf create-service p-redis shared-vm blog-redis
    $ ...