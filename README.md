# Spring Boot Boilerplate

(server only)

Auth and user management API. Register, login, and admin user CRUD.

## Tech

- [Spring Boot] 4.1.1
- [Java] 21
- [PostgreSQL]
- [Java-JWT]
- [MapStruct]
- [springdoc] OpenAPI

## Checkout

```sh
git clone https://github.com/hqyuh/spring-boot-boilerplate
```

## Build

Needs JDK 21.

```sh
mvn clean package -DskipTests
```

The jar is `target/boilerplate-0.0.1-SNAPSHOT.jar`. Main class is `com.hqh.boilerplate.BoilerplateApplication`.

## Run

PostgreSQL must be running, and the database `quiz` must already exist. Each profile file has its own connection settings (`jdbc:postgresql://localhost:5432/quiz`, user `postgres`).

| Profile | File | Port |
| ------- | ---- | ---- |
| `dev` | `src/main/resources/application-dev.yml` | 8081 |
| `prod` | `src/main/resources/application-prod.yml` | 9090 |

Both files are packaged into the jar. Choose one with the active profile. There is no `application.yml`, so a start without a profile does not load the datasource or the port.

Dev, from source:

```sh
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

On PowerShell, quote that property:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

After the build above, prod from the jar:

```sh
java -jar target/boilerplate-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

The same jar with dev:

```sh
java -jar target/boilerplate-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

The startup log names the profile, for example `The following 1 profile is active: "dev"`. `No active profile set` means neither file was loaded.

Dev base URL: `http://localhost:8081/api/v1`

Dev Swagger: `http://localhost:8081/api/v1/swagger-ui.html`

Prod uses port `9090` with the same paths.

Tests use H2 (`src/test/resources/application-test.yml`):

```sh
mvn test
```



## Auth

Public:


| Method | Path             |
| ------ | ---------------- |
| POST   | `/auth/register` |
| POST   | `/auth/login`    |


Register body:

```json
{
  "firstName": "Admin",
  "lastName": "User",
  "username": "adminuser",
  "email": "admin@test.local",
  "password": "quiz1234",
  "roles": "ROLE_ADMIN"
}
```

`roles` is `ROLE_USER`, `ROLE_ADMIN`, or `ROLE_TEACHER`. Password needs at least 8 characters, one lowercase letter, and one digit. Username starts with a letter. First and last name start with a capital letter.

Login body is `email` and `password`. The response has `tokenType`, `accessToken`, and `expireAt`. Send later calls as `Authorization: Bearer <accessToken>`.

## User CRUD

All of these need `ROLE_ADMIN`.


| Method | Path                | Notes                               |
| ------ | ------------------- | ----------------------------------- |
| POST   | `/user/add`         | Returns the generated password once |
| GET    | `/user/list`        |                                     |
| GET    | `/user/find/{id}`   | 400 if the id does not exist        |
| POST   | `/user/update`      | Body includes `currentUsername`     |
| DELETE | `/user/delete/{id}` | 400 if the id does not exist        |


Add and update body:

```json
{
  "currentUsername": "adminuser",
  "firstName": "Admin",
  "lastName": "User",
  "username": "adminuser",
  "email": "admin@test.local",
  "roles": "ROLE_ADMIN",
  "isActive": true,
  "isNonLocked": true
}
```

`currentUsername` is required only on update. A duplicate username or email returns 400 `USERNAME OR EMAIL ALREADY EXISTS`. A missing user returns 400 `NO USER FOUND BY ID`. `ROLE_USER` calling these routes gets 403.

## License

MIT