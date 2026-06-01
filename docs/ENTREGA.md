# Documentacion tecnica CDY2204

Este documento resume la solucion implementada para el despliegue cloud native de una plataforma educativa. La propuesta se desarrollo priorizando una demostracion directa: endpoints REST funcionando, datos persistidos en Oracle Cloud, imagen publicada en Docker Hub y despliegue automatico en una instancia EC2.

La solucion se mantuvo acotada al alcance definido para el microservicio. No se agrego frontend, autenticacion ni componentes extra, porque el objetivo es evidenciar el flujo cloud native y no aumentar complejidad innecesaria.

## 1. Arquitectura completa del proyecto

La solucion corresponde a un microservicio REST desarrollado con Spring Boot. El servicio concentra las operaciones principales de cursos e inscripciones y expone tres endpoints:

- `GET /cursos`: consulta cursos disponibles.
- `POST /cursos`: agrega cursos y los persiste en Oracle Cloud.
- `POST /inscripciones`: inscribe estudiantes en uno o mas cursos, genera resumen, calcula total y persiste en Oracle Cloud.
- `GET /inscripciones/{numeroResumen}/resumen`: genera un archivo fisico `resumen.txt` y lo descarga.
- `POST /s3/uploadResumen?numeroResumen=1`: sube el resumen a AWS S3.
- `PUT /s3/updateResumen?numeroResumen=1`: reemplaza un resumen existente en AWS S3.
- `GET /s3/downloadResumen?numeroResumen=1`: descarga el resumen desde AWS S3.
- `DELETE /s3/deleteResumen?numeroResumen=1`: elimina el resumen desde AWS S3.

El flujo cloud implementado es el siguiente:

```text
Postman -> AWS EC2:8080 -> Contenedor Docker -> Spring Boot -> Oracle Cloud Database
Postman -> AWS EC2:8080 -> Contenedor Docker -> Spring Boot -> AWS S3 Bucket
GitHub main -> GitHub Actions -> Docker Hub -> AWS EC2 -> Contenedor actualizado
```

Esta arquitectura permite demostrar el funcionamiento completo desde Postman y, al mismo tiempo, evidenciar el despliegue continuo del servicio.

## 2. Explicacion tecnica

La aplicacion usa Java 21, Spring Boot y Maven para construir el microservicio. Spring Web publica los endpoints REST y Spring Data JPA administra la persistencia en Oracle Cloud mediante el driver JDBC de Oracle.

Docker se utiliza para empaquetar el microservicio junto con el wallet de Oracle Cloud. La imagen se publica en Docker Hub y luego es descargada desde EC2 durante el despliegue. GitHub Actions automatiza el proceso completo al hacer push a `main`: compila el proyecto, ejecuta pruebas, construye la imagen, la publica y actualiza el contenedor en AWS.

El diseno busca que la solucion sea simple de explicar y facil de verificar durante la presentacion. Cada tecnologia tiene una responsabilidad concreta dentro del flujo de entrega.

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

El codigo se separo en capas para mantener responsabilidades claras:

- Entidades: representan las tablas `CURSOS`, `INSCRIPCIONES` e `INSCRIPCION_CURSOS`.
- Repositorios: encapsulan el acceso a datos con Spring Data JPA.
- Servicios: contienen la logica de negocio, como crear cursos y calcular el total de una inscripcion.
- Servicio de resumen: genera el archivo fisico `resumen.txt` dentro de `resumenes/{numeroResumen}/`.
- Integracion S3: `StorageConfig`, `S3Repository`, `AwsService` y `AwsController` gestionan subida, reemplazo, descarga y eliminacion.
- Controladores: exponen los endpoints REST requeridos.
- DTOs: separan los datos recibidos y respondidos por la API.
- Manejo de errores: entrega respuestas controladas para validaciones y recursos inexistentes.
- Pruebas automaticas: validan la creacion de cursos y el calculo de inscripciones.

## 5. application.properties

La configuracion usa variables de entorno para evitar guardar credenciales en el repositorio. El usuario por defecto es `ADMIN`, pero la password se entrega solo por secret o variable local.

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

app.resumenes.path=${APP_RESUMENES_PATH:./resumenes}

aws.region=${AWS_REGION:us-east-1}
aws.access-key=${AWS_ACCESS_KEY_ID:}
aws.secret-key=${AWS_SECRET_ACCESS_KEY:}
aws.session-token=${AWS_SESSION_TOKEN:}
aws.s3.bucket-name=${AWS_S3_BUCKET_NAME:}
```

El valor `ORACLE_WALLET_PATH` permite que la misma aplicacion funcione localmente y dentro del contenedor. En EC2 el wallet queda en `/app/wallet`.

## 6. Dockerfile

El `Dockerfile` se construyo en dos etapas. La primera compila la aplicacion con Maven y extrae el wallet. La segunda genera una imagen de ejecucion mas directa con Java 21, el JAR y los archivos necesarios para conectarse a Oracle Cloud.

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

Incluye las dependencias necesarias para el microservicio:

- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `software.amazon.awssdk:s3`
- `ojdbc11`
- `oraclepki`
- `osdt_core`
- `osdt_cert`
- `spring-boot-starter-test`

## 8. Workflow GitHub Actions

Archivo: `.github/workflows/main.yml`.

El workflow se llama `Build and Push Docker Image`. Se preparo con un flujo directo de CI/CD: checkout, autenticacion en Docker Hub, build de imagen, push a Docker Hub, configuracion AWS, llave SSH y despliegue remoto.

Adicionalmente se incorporaron dos ajustes necesarios para este proyecto:

- Compilacion y pruebas Maven antes de construir la imagen.
- Plataforma `linux/amd64`, para asegurar compatibilidad con EC2.

En pull request el workflow valida build. En push a `main` publica la imagen y despliega en EC2.

## 9. Secrets GitHub

Secrets usados por el pipeline:

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
AWS_REGION
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_SESSION_TOKEN
AWS_S3_BUCKET_NAME
```

Para esta entrega, `ORACLE_DB_USERNAME` corresponde a `ADMIN`. La password debe guardarse solo como secret y no escribirse en archivos del repositorio.

Ejemplo de `ORACLE_DB_URL`:

```text
jdbc:oracle:thin:@procesobasedatos_high?TNS_ADMIN=/app/wallet
```

## 10.1 Pasos exactos AWS S3

1. Ingresar a AWS Academy y activar el Learner Lab.
2. Entrar a AWS Details y copiar `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` y `AWS_SESSION_TOKEN`.
3. Abrir Amazon S3 y crear un bucket en la region definida para el laboratorio, por ejemplo `us-east-1`.
4. Deshabilitar el bloqueo de acceso publico segun la guia de la semana, si el docente lo solicita.
5. Configurar las variables de entorno:

```bash
export AWS_REGION='us-east-1'
export AWS_ACCESS_KEY_ID='valor_aws_academy'
export AWS_SECRET_ACCESS_KEY='valor_aws_academy'
export AWS_SESSION_TOKEN='valor_aws_academy'
export AWS_S3_BUCKET_NAME='bucket-formativa-duoc'
```

Alternativa con GitHub Actions:

1. Confirmar que existen los secrets `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` y `AWS_SESSION_TOKEN`.
2. Ejecutar manualmente el workflow `Create AWS S3 Bucket`.
3. Ingresar el nombre del bucket y region.
4. Una vez creado, agregar el secret `AWS_S3_BUCKET_NAME` con el nombre usado.
5. El workflow principal inyecta las variables AWS al contenedor durante el despliegue.

La organizacion obligatoria del bucket queda asi:

```text
1/
  resumen.txt
2/
  resumen.txt
1001/
  resumen.txt
```

La carpeta siempre corresponde al `numeroResumen`, que en esta solucion es el ID de la inscripcion.

## 10. Pasos exactos Oracle Cloud

Para la persistencia se uso una base Oracle Cloud configurada con wallet. El contenedor no necesita abrir conexiones sin cifrado hacia Oracle.

1. Entrar a Oracle Cloud.
2. Usar la base configurada para el proyecto.
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

7. Ejecutar la aplicacion. Con `spring.jpa.hibernate.ddl-auto=update`, Hibernate crea o actualiza las tablas al iniciar.
8. Si la base ya tenia tablas antiguas con otra estructura, limpiarlas antes de la demo o usar el script `docs/oracle_schema.sql`.

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
8. Se detiene cualquier contenedor anterior que use el puerto `8080`.
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

El pipeline cumple el requerimiento de despliegue automatico porque se ejecuta con `push` a `main`. La etapa Maven valida que el proyecto compile y ejecuta pruebas automaticas de creacion de cursos, calculo total de inscripcion y error por curso inexistente.

Despues, Docker genera una imagen reproducible llamada `my-app`. Esta imagen se publica en Docker Hub para que EC2 pueda descargarla. Finalmente, la etapa SSH actualiza el contenedor en AWS EC2 sin intervencion manual. Antes de iniciar el nuevo contenedor, el workflow libera el puerto `8080` para evitar errores de despliegue cuando queda una ejecucion anterior activa.

## 20. Estructura de documentacion final

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

## 21. Checklist tecnico

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

## 22. Rol de cada componente

- Java/Spring Boot/Maven: implementan el microservicio REST.
- Oracle Cloud: almacena cursos e inscripciones.
- Docker: genera imagen ejecutable del proyecto.
- Docker Hub: almacena la imagen publicada por CI/CD.
- GitHub Actions: automatiza build, push y deploy.
- AWS EC2: ejecuta el contenedor publicado.
- Postman: demuestra las tres funcionalidades principales.

## 23. Errores comunes y solucion

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
