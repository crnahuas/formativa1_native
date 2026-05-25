# Entrega actividad formativa CDY2204

## 1. Arquitectura completa del proyecto

La solucion corresponde a un microservicio REST desarrollado con Spring Boot. Expone tres endpoints requeridos por la pauta:

- `GET /cursos`: consulta cursos disponibles.
- `POST /cursos`: agrega cursos y los persiste en Oracle Cloud.
- `POST /inscripciones`: inscribe estudiantes en uno o mas cursos, genera resumen, calcula total y persiste en Oracle Cloud.

Flujo cloud:

```text
Postman -> AWS EC2:8080 -> Contenedor Docker -> Spring Boot -> Oracle Cloud Database
GitHub main -> GitHub Actions -> Docker Hub -> AWS EC2 -> Contenedor actualizado
```

No se incluye frontend, login, JWT ni microservicios adicionales porque la pauta no los solicita.

## 2. Explicacion tecnica alineada a la pauta

La aplicacion usa Java, Spring Boot y Maven para construir el microservicio. Spring Web publica los endpoints REST. Spring Data JPA administra la persistencia en Oracle Cloud mediante el driver JDBC de Oracle. Docker empaqueta el microservicio como imagen portable. GitHub Actions automatiza compilacion, construccion de imagen, publicacion en Docker Hub y despliegue en EC2 al hacer push a `main`.

## 3. Estructura de carpetas

```text
.
├── .github/workflows/main.yml
├── Dockerfile
├── README.md
├── docs
│   ├── ENTREGA.md
│   ├── oracle_schema.sql
│   └── postman_collection.json
├── pom.xml
└── src/main
    ├── java/cl/duoc/cdy2204/formativa
    │   ├── FormativaCloudNativeApplication.java
    │   ├── controller
    │   ├── dto
    │   ├── entity
    │   ├── exception
    │   ├── repository
    │   └── service
    └── resources/application.properties
```

## 4. Codigo completo

Archivos principales:

- Entidades: `Curso.java`, `Inscripcion.java`
- Repositorios: `CursoRepository.java`, `InscripcionRepository.java`
- Servicios: `CursoService.java`, `InscripcionService.java`
- Controladores: `CursoController.java`, `InscripcionController.java`
- DTOs: `CursoRequest.java`, `CursoResponse.java`, `InscripcionRequest.java`, `InscripcionResponse.java`, `ErrorResponse.java`
- Errores: `GlobalExceptionHandler.java`, `RecursoNoEncontradoException.java`
- Configuracion: `application.properties`
- Pruebas automaticas: `CursoServiceTest.java`, `InscripcionServiceTest.java`

## 5. application.properties

```properties
spring.application.name=formativa-cloud-native
server.port=${SERVER_PORT:8080}

oracle.wallet.path=${ORACLE_WALLET_PATH:./wallet}

spring.datasource.url=${ORACLE_DB_URL:jdbc:oracle:thin:@procesobasedatos_high?TNS_ADMIN=${oracle.wallet.path}}
spring.datasource.username=${ORACLE_DB_USERNAME:ADMIN}
spring.datasource.password=${ORACLE_DB_PASSWORD}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
```

Las credenciales no se escriben en el codigo. Se entregan como variables de entorno locales o como GitHub Secrets.

## 6. Dockerfile

El `Dockerfile` compila con Maven y ejecuta el JAR con Java 21:

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY Wallet_ProcesoBaseDatos.zip /tmp/wallet.zip
RUN mkdir -p /wallet && cd /wallet && jar xf /tmp/wallet.zip
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV ORACLE_WALLET_PATH=/app/wallet
COPY --from=build /wallet /app/wallet
COPY --from=build /app/target/formativa-cloud-native-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 7. pom.xml

Incluye las dependencias obligatorias para la pauta:

- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `ojdbc11`
- `oraclepki`
- `osdt_core`
- `osdt_cert`
- `spring-boot-starter-test`

## 8. Workflow GitHub Actions

Archivo: `.github/workflows/main.yml`.

El workflow se llama `Build and Push Docker Image`. Se activa con push a `main`, pull request a `main` y ejecucion manual con `workflow_dispatch`. En pull request solo valida build; en push a `main` compila con Maven, crea imagen Docker `linux/amd64`, publica en Docker Hub y despliega en EC2 por SSH.

## 9. Secrets GitHub

Secrets obligatorios segun la actividad:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_SESSION_TOKEN
DOCKERHUB_TOKEN
DOCKERHUB_USERNAME
EC2_HOST
EC2_SSH_KEY
USER_SERVER
```

Secrets tecnicos necesarios para que el contenedor se conecte a Oracle Cloud:

```text
ORACLE_DB_URL
ORACLE_DB_USERNAME
ORACLE_DB_PASSWORD
```

Para esta entrega, `ORACLE_DB_USERNAME` corresponde a `ADMIN`. La password debe guardarse solo como secret y no escribirse en archivos del repositorio.

Ejemplo de `ORACLE_DB_URL`:

```text
jdbc:oracle:thin:@procesobasedatos_high?TNS_ADMIN=/app/wallet
```

## 10. Pasos exactos Oracle Cloud

1. Entrar a Oracle Cloud.
2. Usar la base configurada para la actividad.
3. Descargar el wallet de Oracle Cloud.
4. Agregar el wallet al proyecto como `Wallet_ProcesoBaseDatos.zip` para la demostracion.
5. Confirmar el alias del wallet en `tnsnames.ora`. Para esta entrega se usa `procesobasedatos_high`.
6. Crear las variables:

```bash
mkdir -p wallet
unzip -o Wallet_ProcesoBaseDatos.zip -d wallet

export ORACLE_WALLET_PATH='./wallet'
export ORACLE_DB_URL='jdbc:oracle:thin:@procesobasedatos_high?TNS_ADMIN=./wallet'
export ORACLE_DB_USERNAME='ADMIN'
export ORACLE_DB_PASSWORD='TU_PASSWORD_ORACLE'
```

7. Ejecutar la aplicacion. Con `spring.jpa.hibernate.ddl-auto=update`, Hibernate crea las tablas al iniciar.
8. Si se requiere crear manualmente las tablas, usar `docs/oracle_schema.sql`.

## 11. Pasos exactos Docker Hub

1. Crear cuenta en Docker Hub.
2. Ir a `Account settings` -> `Personal access tokens`.
3. Crear token con permisos de lectura y escritura.
4. Registrar en GitHub Secrets:

```text
DOCKERHUB_USERNAME=<usuario_dockerhub>
DOCKERHUB_TOKEN=<token_generado>
```

Prueba manual:

```bash
docker login
docker build --platform linux/amd64 -t USUARIO_DOCKERHUB/my-app:latest .
docker push USUARIO_DOCKERHUB/my-app:latest
```

## 12. Pasos exactos AWS EC2

1. Ingresar a AWS Academy o consola AWS.
2. Iniciar laboratorio si corresponde.
3. Crear instancia EC2 Amazon Linux 2 o Amazon Linux 2023.
4. Crear y descargar par de llaves `.pem`.
5. Asociar IP elastica a la instancia.
6. Guardar IP en secret `EC2_HOST`.
7. Guardar usuario SSH en `USER_SERVER`. En Amazon Linux normalmente es `ec2-user`.
8. Guardar el contenido completo del `.pem` en `EC2_SSH_KEY`.

Conectar:

```bash
chmod 600 llave.pem
ssh -i llave.pem ec2-user@IP_ELASTICA
```

## 13. Configuracion Docker en EC2

Amazon Linux 2:

```bash
sudo yum update -y
sudo yum install docker -y
sudo service docker start
sudo usermod -a -G docker ec2-user
sudo chkconfig docker on
sudo docker ps
```

Amazon Linux 2023:

```bash
sudo dnf update -y
sudo dnf install docker -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user
sudo docker ps
```

Cerrar sesion SSH y volver a entrar para que el grupo `docker` aplique.

## 14. Security Groups AWS

Reglas de entrada recomendadas:

```text
Tipo: SSH
Protocolo: TCP
Puerto: 22
Origen: Mi IP

Tipo: TCP personalizado
Protocolo: TCP
Puerto: 8080
Origen: 0.0.0.0/0
```

Para una demo academica se puede abrir `8080` a cualquier IPv4. En un ambiente productivo se restringe el origen.

## 15. Flujo CI/CD completo

1. El equipo sube cambios a `main`.
2. GitHub Actions se activa automaticamente.
3. Maven ejecuta `mvn clean package`, incluyendo pruebas automaticas de servicios.
4. Docker construye la imagen para plataforma `linux/amd64`, compatible con EC2 x86_64.
5. La imagen se publica en Docker Hub con tags `latest` y SHA del commit.
6. GitHub Actions se conecta por SSH a EC2.
7. EC2 descarga la ultima imagen desde Docker Hub.
8. Se detiene el contenedor anterior.
9. Se inicia el contenedor nuevo en el puerto `8080`.
10. Postman prueba los endpoints contra `http://IP_ELASTICA:8080`.

## 16. Comandos terminal exactos

Compilar:

```bash
mvn clean package
```

Ejecutar local:

```bash
mkdir -p wallet
unzip -o Wallet_ProcesoBaseDatos.zip -d wallet

export ORACLE_WALLET_PATH='./wallet'
export ORACLE_DB_URL='jdbc:oracle:thin:@procesobasedatos_high?TNS_ADMIN=./wallet'
export ORACLE_DB_USERNAME='ADMIN'
export ORACLE_DB_PASSWORD='TU_PASSWORD_ORACLE'
java -jar target/formativa-cloud-native-0.0.1-SNAPSHOT.jar
```

Construir imagen:

```bash
docker build -t formativa-cloud-native:1.0 .
```

Ejecutar contenedor:

```bash
docker run -d \
  --name formativa-cloud-native \
  -p 8080:8080 \
  -e ORACLE_DB_USERNAME='ADMIN' \
  -e ORACLE_DB_PASSWORD='TU_PASSWORD_ORACLE' \
  formativa-cloud-native:1.0
```

Si se construye manualmente desde un Mac con Apple Silicon y se va a ejecutar en una EC2 x86_64, usar:

```bash
docker build --platform linux/amd64 -t formativa-cloud-native:1.0 .
```

Ver logs:

```bash
docker logs formativa-cloud-native
```

## 17. Ejemplos Postman y JSON

### POST /cursos

Request:

```json
{
  "nombre": "Spring Boot Cloud Native",
  "instructor": "Docente CDY2204",
  "duracion": "24 horas",
  "costo": 120000
}
```

Response `201 Created`:

```json
{
  "id": 1,
  "nombre": "Spring Boot Cloud Native",
  "instructor": "Docente CDY2204",
  "duracion": "24 horas",
  "costo": 120000
}
```

### GET /cursos

Response `200 OK`:

```json
[
  {
    "id": 1,
    "nombre": "Spring Boot Cloud Native",
    "instructor": "Docente CDY2204",
    "duracion": "24 horas",
    "costo": 120000
  }
]
```

### POST /inscripciones

Request:

```json
{
  "estudianteNombre": "Maria Perez",
  "estudianteEmail": "maria.perez@duocuc.cl",
  "cursoIds": [1]
}
```

Response `201 Created`:

```json
{
  "inscripcionId": 1,
  "estudianteNombre": "Maria Perez",
  "estudianteEmail": "maria.perez@duocuc.cl",
  "fechaInscripcion": "2026-05-23T17:30:00",
  "cursosSeleccionados": [
    {
      "id": 1,
      "nombre": "Spring Boot Cloud Native",
      "instructor": "Docente CDY2204",
      "duracion": "24 horas",
      "costo": 120000
    }
  ],
  "totalPagar": 120000
}
```

## 18. Manejo basico de errores

Validacion de curso sin nombre:

```json
{
  "fecha": "2026-05-23T17:30:00",
  "estado": 400,
  "error": "Solicitud invalida",
  "detalles": [
    "nombre: El nombre del curso es obligatorio"
  ]
}
```

Curso inexistente en inscripcion:

```json
{
  "fecha": "2026-05-23T17:30:00",
  "estado": 404,
  "error": "Recurso no encontrado",
  "detalles": [
    "Uno o mas cursos seleccionados no existen"
  ]
}
```

## 19. Explicacion del pipeline

El pipeline cumple el requerimiento de despliegue automatico porque se ejecuta con `push` a `main`. La etapa Maven valida que el proyecto compile y ejecuta pruebas automaticas de creacion de cursos, calculo total de inscripcion y error por curso inexistente. La etapa Docker genera una imagen reproducible llamada `my-app`. La etapa Docker Hub publica la imagen para que EC2 pueda descargarla. La etapa SSH actualiza el contenedor `my-app` en AWS EC2 sin intervencion manual.

## 20. Evidencias recomendadas para grabar

Mostrar en Teams:

1. Repositorio GitHub con codigo.
2. Archivo `.github/workflows/main.yml`.
3. Secrets configurados, ocultando valores.
4. Ejecucion exitosa de GitHub Actions.
5. Imagen publicada en Docker Hub.
6. Instancia EC2 activa.
7. Security Group con puerto `8080`.
8. Contenedor corriendo en EC2 con `docker ps`.
9. Postman creando curso.
10. Postman listando cursos.
11. Postman creando inscripcion y mostrando total.
12. Evidencia en Oracle Cloud de tablas con datos.

## 21. Guion tecnico para presentacion Teams

1. Presentar objetivo: microservicio para cursos e inscripciones de plataforma educativa.
2. Explicar arquitectura: Spring Boot, Oracle Cloud, Docker, GitHub Actions, Docker Hub y EC2.
3. Mostrar estructura del proyecto.
4. Mostrar entidades `Curso` e `Inscripcion`.
5. Mostrar endpoints en controladores.
6. Mostrar `application.properties` con variables de entorno.
7. Mostrar `Dockerfile`.
8. Mostrar workflow CI/CD.
9. Ejecutar o mostrar Action exitoso.
10. Abrir Docker Hub y confirmar imagen.
11. Abrir EC2 y confirmar instancia.
12. Probar `POST /cursos` en Postman.
13. Probar `GET /cursos` en Postman.
14. Probar `POST /inscripciones` en Postman.
15. Explicar que el total se calcula sumando los costos de los cursos seleccionados.
16. Mostrar persistencia en Oracle Cloud.
17. Cerrar con checklist de cumplimiento.

## 22. Estructura de documentacion final

Entregar:

```text
repositorio-github/
zip-o-rar-documentacion/
├── README.md
├── docs/ENTREGA.md
├── docs/postman_collection.json
├── docs/oracle_schema.sql
└── link-video-teams.txt
```

## 23. Checklist de cumplimiento de rubrica

| Criterio | Cumplimiento |
| --- | --- |
| Pipeline automatico en la nube | GitHub Actions con push a main, Docker Hub y despliegue EC2. |
| Pruebas automaticas | `mvn clean package` ejecuta tests unitarios de servicios en GitHub Actions. |
| Listado de cursos | `GET /cursos` consulta Oracle y devuelve nombre, instructor, duracion y costo. |
| Agregar cursos | `POST /cursos` valida datos y persiste en Oracle. |
| Resumen y calculo final | `POST /inscripciones` devuelve cursos seleccionados, costo individual y total. |
| Persistencia de inscripcion | Entidad `Inscripcion` y tabla relacional con cursos inscritos. |
| Video explicativo | Guion y evidencias definidos para Teams. |
| Manejo de errores | Validaciones 400 y curso inexistente 404. |

## 24. Como cada parte cumple la pauta

- Java/Spring Boot/Maven: implementan el microservicio solicitado.
- Oracle Cloud: almacena cursos e inscripciones.
- Docker: genera imagen ejecutable del proyecto.
- Docker Hub: almacena la imagen publicada por CI/CD.
- GitHub Actions: automatiza build, push y deploy.
- AWS EC2: ejecuta el contenedor publicado.
- Postman: demuestra las tres funcionalidades requeridas.

## 25. Errores comunes y solucion

Error: no encuentra el wallet de Oracle

Solucion:

```bash
mkdir -p wallet
unzip -o Wallet_ProcesoBaseDatos.zip -d wallet
export ORACLE_WALLET_PATH='./wallet'
export ORACLE_DB_URL='jdbc:oracle:thin:@procesobasedatos_high?TNS_ADMIN=./wallet'
```

Error: `ORA-01017 invalid username/password`

Solucion: revisar `ORACLE_DB_USERNAME` y `ORACLE_DB_PASSWORD`.

Error: `Connection refused` desde Postman.

Solucion: verificar que el contenedor este activo y que Security Group permita puerto `8080`.

```bash
docker ps
docker logs my-app
```

Error: GitHub Actions no conecta por SSH.

Solucion: revisar `EC2_HOST`, `USER_SERVER`, `EC2_SSH_KEY` y regla SSH puerto `22`.

Error: Docker no ejecuta sin `sudo`.

Solucion:

```bash
sudo usermod -a -G docker ec2-user
exit
ssh -i llave.pem ec2-user@IP_ELASTICA
```

Error: `docker: command not found`.

Solucion: instalar Docker en EC2 usando los comandos de la seccion 13.

Error: la inscripcion retorna 404.

Solucion: crear primero cursos con `POST /cursos` y usar IDs existentes en `cursoIds`.
