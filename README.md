# Formativa 1 Cloud Native - CDY2204

Este repositorio contiene una solucion cloud native para una plataforma educativa de cursos virtuales. La Semana 5 reutiliza el backend y la configuracion de API Gateway/IDaaS trabajada en Semana 4, agregando Spring Security para proteger los endpoints del microservicio.

La aplicacion no incluye frontend. El foco del trabajo esta en el backend, la persistencia cloud, la imagen Docker, el despliegue automatico hacia AWS EC2 mediante GitHub Actions y la autenticacion JWT exigida en Semana 5.

## Endpoints requeridos

| Metodo | Ruta | Funcion |
| --- | --- | --- |
| GET | `/cursos` | Consulta cursos disponibles con nombre, instructor, duracion y costo. |
| POST | `/cursos` | Agrega un nuevo curso y lo persiste en Oracle Cloud. |
| POST | `/inscripciones` | Inscribe un estudiante en uno o mas cursos, calcula el total y persiste la inscripcion en Oracle Cloud. |
| GET | `/inscripciones/{numeroResumen}/resumen` | Genera y descarga el archivo fisico `resumen.txt` de una inscripcion. |
| POST | `/s3/uploadResumen?numeroResumen=1` | Sube el resumen a AWS S3 en la carpeta `numeroResumen/`. |
| PUT | `/s3/updateResumen?numeroResumen=1` | Reemplaza un resumen existente en AWS S3. |
| GET | `/s3/downloadResumen?numeroResumen=1` | Descarga el resumen almacenado en AWS S3. |
| DELETE | `/s3/deleteResumen?numeroResumen=1` | Elimina el resumen almacenado en AWS S3. |

El flujo de uso esperado es crear uno o mas cursos, revisar la lista disponible y luego generar una inscripcion utilizando los IDs de los cursos existentes.

## Tecnologias

- Java 21
- Spring Boot 4.0.6
- Maven
- Spring Web
- Spring Data JPA
- Spring Security
- OAuth2 Resource Server JWT
- Oracle JDBC
- Docker
- Docker Hub
- GitHub Actions
- AWS EC2
- AWS API Gateway
- Azure AD B2C como IDaaS
- Oracle Cloud Database
- Postman

La eleccion de estas tecnologias mantiene el proyecto enfocado: Spring Boot y Maven para el microservicio, Oracle Cloud para persistencia, Docker/Docker Hub para empaquetado y publicacion, GitHub Actions para CI/CD, AWS EC2 para ejecutar el contenedor publicado, AWS API Gateway para exponer los endpoints y Azure AD B2C para emitir los JWT.

## Ejecutar localmente

Para ejecutar localmente se debe tener disponible el wallet de Oracle Cloud. En este proyecto se utiliza el alias `procesobasedatos_high`, definido dentro del archivo `tnsnames.ora` del wallet.

Configurar variables de entorno:

```bash
mkdir -p wallet
unzip -o Wallet_ProcesoBaseDatos.zip -d wallet

export ORACLE_WALLET_PATH='./wallet'
export ORACLE_DB_URL='jdbc:oracle:thin:@procesobasedatos_high?TNS_ADMIN=./wallet'
export ORACLE_DB_USERNAME='ADMIN'
export ORACLE_DB_PASSWORD='TU_PASSWORD_ORACLE'

export AWS_REGION='us-east-1'
export AWS_ACCESS_KEY_ID='TU_ACCESS_KEY_AWS_ACADEMY'
export AWS_SECRET_ACCESS_KEY='TU_SECRET_KEY_AWS_ACADEMY'
export AWS_SESSION_TOKEN='TU_SESSION_TOKEN_AWS_ACADEMY'
export AWS_S3_BUCKET_NAME='TU_BUCKET_S3'

export AZURE_B2C_ISSUER_URI='https://TU_TENANT.b2clogin.com/TU_TENANT.onmicrosoft.com/TU_POLITICA/v2.0/'
```

Si las credenciales AWS ya estan guardadas como GitHub Secrets, el bucket se puede crear desde GitHub Actions:

1. Ir al repositorio en GitHub.
2. Entrar a `Actions`.
3. Ejecutar manualmente `Create AWS S3 Bucket`.
4. Usar el nombre `bucket-formativa-duoc`.
5. Agregar el secreto `AWS_S3_BUCKET_NAME` con el nombre del bucket creado.

Para el despliegue automatico tambien deben existir estos secrets:

```text
AWS_REGION
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_SESSION_TOKEN
AWS_S3_BUCKET_NAME
AZURE_B2C_ISSUER_URI
```

Compilar y ejecutar pruebas:

```bash
mvn clean package
```

Ejecutar:

```bash
java -jar target/formativa-cloud-native-0.0.1-SNAPSHOT.jar
```

## Probar con curl

Todos los endpoints requieren un token JWT valido. Para pruebas locales, define primero:

```bash
export ACCESS_TOKEN='JWT_EMITIDO_POR_AZURE_AD_B2C'
```

Crear curso:

```bash
curl --location 'http://localhost:8080/cursos' \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --header 'Content-Type: application/json' \
  --data '{
    "nombre": "Spring Boot Cloud Native",
    "instructor": "Docente CDY2204",
    "duracion": "24 horas",
    "costo": 120000
  }'
```

Listar cursos:

```bash
curl --location 'http://localhost:8080/cursos' \
  --header "Authorization: Bearer $ACCESS_TOKEN"
```

Inscribir estudiante:

```bash
curl --location 'http://localhost:8080/inscripciones' \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --header 'Content-Type: application/json' \
  --data '{
    "estudianteNombre": "Maria Perez",
    "estudianteEmail": "maria.perez@duocuc.cl",
    "cursoIds": [1]
  }'
```

Generar y descargar el resumen fisico:

```bash
curl --location 'http://localhost:8080/inscripciones/1/resumen' \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --output resumen.txt
```

Subir el resumen generado a S3. El objeto queda en la clave `1/resumen.txt`:

```bash
curl --request POST 'http://localhost:8080/s3/uploadResumen?numeroResumen=1' \
  --header "Authorization: Bearer $ACCESS_TOKEN"
```

Actualizar, descargar y eliminar el resumen en S3:

```bash
curl --request PUT 'http://localhost:8080/s3/updateResumen?numeroResumen=1' \
  --header "Authorization: Bearer $ACCESS_TOKEN"
curl --location 'http://localhost:8080/s3/downloadResumen?numeroResumen=1' \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --output resumen-s3.txt
curl --request DELETE 'http://localhost:8080/s3/deleteResumen?numeroResumen=1' \
  --header "Authorization: Bearer $ACCESS_TOKEN"
```

Si se quiere subir o reemplazar un archivo editado manualmente desde el computador, usar `multipart/form-data` con el campo `file`:

```bash
curl --request PUT 'http://localhost:8080/s3/updateResumen?numeroResumen=1' \
  --header "Authorization: Bearer $ACCESS_TOKEN" \
  --form 'file=@resumen.txt'
```

## Docker local

La imagen Docker incluye el JAR de la aplicacion y el wallet necesario para conectarse a Oracle Cloud. La password se entrega siempre por variable de entorno.

```bash
docker build -t formativa-cloud-native:1.0 .
docker run -d \
  --name formativa-cloud-native \
  -p 8080:8080 \
  -e ORACLE_DB_USERNAME='ADMIN' \
  -e ORACLE_DB_PASSWORD='TU_PASSWORD_ORACLE' \
  -e AZURE_B2C_ISSUER_URI='https://TU_TENANT.b2clogin.com/TU_TENANT.onmicrosoft.com/TU_POLITICA/v2.0/' \
  formativa-cloud-native:1.0
```

Para crear una imagen manual compatible con una EC2 x86_64 desde Mac Apple Silicon:

```bash
docker build --platform linux/amd64 -t formativa-cloud-native:1.0 .
```

## Documentacion de entrega Semana 5

La guia de aplicacion de la pauta Semana 5, basada en la reutilizacion de Semana 4, esta en:

[docs/SEMANA5_IDAAS_API_MANAGER_SECURITY.md](docs/SEMANA5_IDAAS_API_MANAGER_SECURITY.md)

Coleccion Postman especifica para probar los endpoints publicados en API Gateway:

[docs/postman_api_gateway_semana5_collection.json](docs/postman_api_gateway_semana5_collection.json)
