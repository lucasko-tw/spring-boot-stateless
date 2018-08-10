## Stateless Web Application

1. 建立兩個 Stateless Web Application。
未來不論向哪個Web Application請求，其 SESSION與資料都是共用的。

2. 未來可作為 k8s的範例。

### Docker Runs MySQL
透過Docker啟用一個MySQL，會將使用者帳號密碼儲存在 user table中。

```docker
docker pull mysql:5.6

docker run \
--name my-mysql \
-p 3306:3306  \
-e MYSQL_DATABASE=mydb \
-e MYSQL_ROOT_PASSWORD=1234 \
-d mysql:5.6 
```


### Docker Runs Redis
1. 透過Docker啟用一個Redis，會將 Spring 的Session儲存在 Redis中。

2. 儲存在Redis的目的是，讓兩個不同的Web application可以共用同一個session來源。

```sh

docker run --name my-redis  -p 6379:6379 -d redis

```


### Run Apps
1. 建立兩個 spring web application：一個是 **app1** 另一個是 **app2**。

 * **app1** 建立在 [http://localhost:8081](http://localhost:8081)

 * **app2** 建立在 [http://localhost:8082](http://localhost:8082)

2. 由於兩個web app的session都是儲存在 redis中，因此這兩個web app都是屬於 stateless的 web app.

```sh
cd spring-boot-stateless/


docker run --name app1 --link my-mysql:my-mysql --link my-redis:my-redis -it -v ~/.m2:/root/.m2  -v $PWD:/opt -p8081:8080 -d lucasko/springboot

docker run --name app2 --link my-mysql:my-mysql --link my-redis:my-redis -it -v ~/.m2:/root/.m2  -v $PWD:/opt -p8082:8080 -d lucasko/springboot
```



### Signup Account
在app1(8081 port)註冊一組帳號，帳號資訊會儲存在 MySQL中的 user table

```sh
curl  --header "Content-Type: application/json" -d '{ "firstname" : "Lucas",  "lastname" : "Ko",  "account" : "lucasko.tw@test.com" , "password" : "123456789"}' http://localhost:8081/stateless/api/pub/signup
```


### Login

在app1(8081 port)登入，登入後可以在response的header中看到有 SESSION。

```sh

curl -v  --header "Content-Type: application/json" -d '{  "account" : "lucasko.tw@test.com" , "password" : "123456789"}' http://localhost:8081/stateless/api/pub/login

*   Trying 127.0.0.1...
* TCP_NODELAY set
* Connected to localhost (127.0.0.1) port 8081 (#0)
> POST /api/pub/login HTTP/1.1
> Host: localhost:8081
> User-Agent: curl/7.55.1
> Accept: */*
> Content-Type: application/json
> Content-Length: 64
> 
* upload completely sent off: 64 out of 64 bytes
< HTTP/1.1 200 
< X-Content-Type-Options: nosniff
< X-XSS-Protection: 1; mode=block
< Cache-Control: no-cache, no-store, max-age=0, must-revalidate
< Pragma: no-cache
< Expires: 0
< X-Frame-Options: DENY
< Set-Cookie: SESSION=7914d879-c6ef-4764-bf7e-ed27ff468516; Path=/; HttpOnly
< Content-Type: application/json;charset=UTF-8
< Content-Length: 16
< Date: Fri, 10 Aug 2018 03:59:08 GMT
< 
* Connection #0 to host localhost left intact
{"success":true}
```



### Get Data
1. 將**app1**所取得的SESSION給**app2** (8082 port)使用，送出請求到**app2**，取得資料成功。

2. 雖然網站是分別架設在 8081 port 與 8082 port，但是SESSION是架在Redis上共用的。

```
 curl  --cookie "SESSION=7914d879-c6ef-4764-bf7e-ed27ff468516"   http://localhost:8082/stateless/api/user/profile/get
 
{"firstname":"Lucas","role":"ROLE_USER","account":"lucasko.tw@test.com","lastname":"Ko"}
```

