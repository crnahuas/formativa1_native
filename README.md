# Formativa 1 Cloud Native - CDY2204

Microservicio REST en Spring Boot para la actividad formativa "Desplegando aplicaciones en la nube".

## Endpoints requeridos

| Metodo | Ruta | Funcion |
| --- | --- | --- |
| GET | `/cursos` | Consulta cursos disponibles con nombre, instructor, duracion y costo. |
| POST | `/cursos` | Agrega un nuevo curso y lo persiste en Oracle Cloud. |
| POST | `/inscripciones` | Inscribe un estudiante en uno o mas cursos, calcula el total y persiste la inscripcion en Oracle Cloud. |

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

## Ejecutar localmente

Configurar variables de entorno:

```bash
mkdir -p wallet
unzip -o Wallet_ProcesoBaseDatos.zip -d wallet

export ORACLE_WALLET_PATH='./wallet'
export ORACLE_DB_URL='jdbc:oracle:thin:@procesobasedatos_high?TNS_ADMIN=./wallet'
export ORACLE_DB_USERNAME='ADMIN'
export ORACLE_DB_PASSWORD='TU_PASSWORD_ORACLE'
```

Compilar:

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

## Docker local

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

La guia completa de arquitectura, configuracion cloud, pipeline, Postman, evidencia y checklist de rubrica esta en:

[docs/ENTREGA.md](docs/ENTREGA.md)
