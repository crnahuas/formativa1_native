# Formativa 1 Cloud Native - CDY2204

Este repositorio contiene una solucion cloud native para una plataforma educativa de cursos virtuales. El objetivo fue construir un microservicio REST simple y demostrable: listar cursos, agregar cursos, inscribir estudiantes, calcular el total de inscripcion y persistir la informacion en Oracle Cloud.

La aplicacion no incluye frontend ni autenticacion. El foco del trabajo esta en el backend, la persistencia cloud, la imagen Docker y el despliegue automatico hacia AWS EC2 mediante GitHub Actions.

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
- Oracle JDBC
- Docker
- Docker Hub
- GitHub Actions
- AWS EC2
- Oracle Cloud Database
- Postman

La eleccion de estas tecnologias mantiene el proyecto enfocado: Spring Boot y Maven para el microservicio, Oracle Cloud para persistencia, Docker/Docker Hub para empaquetado y publicacion, GitHub Actions para CI/CD y AWS EC2 para ejecutar el contenedor publicado.

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

Crear curso:

```bash
curl --location 'http://localhost:8080/cursos' \
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
curl --location 'http://localhost:8080/cursos'
```

Inscribir estudiante:

```bash
curl --location 'http://localhost:8080/inscripciones' \
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
  --output resumen.txt
```

Subir el resumen generado a S3. El objeto queda en la clave `1/resumen.txt`:

```bash
curl --request POST 'http://localhost:8080/s3/uploadResumen?numeroResumen=1'
```

Actualizar, descargar y eliminar el resumen en S3:

```bash
curl --request PUT 'http://localhost:8080/s3/updateResumen?numeroResumen=1'
curl --location 'http://localhost:8080/s3/downloadResumen?numeroResumen=1' --output resumen-s3.txt
curl --request DELETE 'http://localhost:8080/s3/deleteResumen?numeroResumen=1'
```

Si se quiere subir o reemplazar un archivo editado manualmente desde el computador, usar `multipart/form-data` con el campo `file`:

```bash
curl --request PUT 'http://localhost:8080/s3/updateResumen?numeroResumen=1' \
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
  formativa-cloud-native:1.0
```

Para crear una imagen manual compatible con una EC2 x86_64 desde Mac Apple Silicon:

```bash
docker build --platform linux/amd64 -t formativa-cloud-native:1.0 .
```

## Documentacion de entrega

La guia completa de arquitectura, configuracion cloud, pipeline, pruebas Postman, evidencias recomendadas y checklist tecnico esta en:

[docs/ENTREGA.md](docs/ENTREGA.md)

La guia puntual para capturar evidencias en Postman esta en:

[docs/EVIDENCIAS_POSTMAN.md](docs/EVIDENCIAS_POSTMAN.md)

La documentacion de Semana 4 para configurar Azure AD B2C, AWS API Gateway, autorizador JWT y pruebas por Postman esta en:

[docs/SEMANA4_IDAAS_API_MANAGER.md](docs/SEMANA4_IDAAS_API_MANAGER.md)

Version Word para adjuntar en la entrega:

[docs/SEMANA4_IDAAS_API_MANAGER.docx](docs/SEMANA4_IDAAS_API_MANAGER.docx)

Comprimido de documentacion para adjuntar:

[docs/semana4_documentacion_idaas_api_manager.zip](docs/semana4_documentacion_idaas_api_manager.zip)

Coleccion Postman especifica para probar los endpoints publicados en API Gateway:

[docs/postman_api_gateway_semana4_collection.json](docs/postman_api_gateway_semana4_collection.json)

Checklist de evidencias finales para Word y video:

[docs/EVIDENCIAS_SEMANA4_CHECKLIST.md](docs/EVIDENCIAS_SEMANA4_CHECKLIST.md)

Version Word del checklist de evidencias:

[docs/EVIDENCIAS_SEMANA4_CHECKLIST.docx](docs/EVIDENCIAS_SEMANA4_CHECKLIST.docx)

Informe Word final con evidencias integradas:

[docs/Informe_Formativa_Semana4_IDaaS_API_Manager.docx](docs/Informe_Formativa_Semana4_IDaaS_API_Manager.docx)

El guion sugerido para grabar el video de entrega esta en:

[docs/GUION_VIDEO.md](docs/GUION_VIDEO.md)

Version HTML del guion:

[docs/guion_video.html](docs/guion_video.html)
