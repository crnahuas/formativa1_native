# Informe EFT - Desarrollo Cloud Native CDY2204

## 1. Descripcion del proyecto

El proyecto corresponde a una plataforma cloud native para gestion de cursos en
linea. La solucion permite administrar cursos, registrar inscripciones de
estudiantes, generar resumenes de inscripcion, publicar y consumir mensajes en
RabbitMQ, almacenar archivos en AWS S3 y proteger los endpoints mediante JWT
emitidos por Azure AD B2C.

La aplicacion esta compuesta por un backend desarrollado en Spring Boot, un
frontend HTML separado para la demostracion, una cola RabbitMQ ejecutada en
Docker, persistencia en Oracle Cloud, almacenamiento de archivos en AWS S3,
exposicion de endpoints mediante API Gateway y despliegue automatizado en AWS
EC2 con GitHub Actions.

## 2. Arquitectura propuesta

La arquitectura de la solucion se organiza en los siguientes componentes:

| Componente | Funcion |
| --- | --- |
| Frontend | Cliente web para ejecutar el flujo funcional con JWT. |
| Backend Spring Boot | Expone APIs REST para cursos, inscripciones, resumenes, RabbitMQ y S3. |
| Azure AD B2C | Servicio IDaaS encargado de emitir los tokens JWT. |
| AWS API Gateway | API Manager para publicar y administrar los endpoints. |
| RabbitMQ | Servicio de colas para procesar resumenes de inscripcion. |
| Oracle Cloud Database | Base de datos para persistir cursos, inscripciones y mensajes consumidos. |
| AWS S3 | Almacenamiento cloud de archivos `resumen.txt`. |
| GitHub Actions | Pipeline CI/CD para construir imagenes Docker y desplegar en EC2. |
| AWS EC2 | Servidor cloud donde se ejecutan los contenedores. |

## 3. Cumplimiento de la pauta

| Criterio de evaluacion | Cumplimiento en el proyecto | Evidencia asociada |
| --- | --- | --- |
| Microservicios backend en Spring Boot | Backend Spring Boot con APIs REST para cursos, inscripciones, resumenes, RabbitMQ y S3. | Codigo fuente, Postman, respuestas JSON. |
| IDaaS para autenticacion y autorizacion | Azure AD B2C emite JWT y Spring Security valida el token en todos los endpoints. | Token JWT, `401` sin token, `200` con token, secret `AZURE_B2C_ISSUER_URI`. |
| RabbitMQ en Docker | RabbitMQ se ejecuta como contenedor `formativa-rabbitmq` y la app usa productor y consumidor. | Consola RabbitMQ, cola, exchange, productor y consumidor. |
| API Manager | AWS API Gateway publica las rutas del backend hacia EC2. | Captura de API Gateway con rutas e integracion HTTP. |
| Almacenamiento cloud | AWS S3 almacena los archivos `numeroResumen/resumen.txt`. | Respuesta Postman y objeto visible en bucket S3. |
| Despliegue CI/CD | GitHub Actions construye imagenes Docker y despliega en EC2 con Docker Compose. | Workflow verde y `docker ps` en EC2. |
| Documentacion con evidencias | El informe, checklist, Postman y script Oracle quedan en `docs/`. | Archivos de documentacion y capturas. |
| Video explicativo | El video debe mostrar arquitectura, flujo funcional e integraciones. | Link Kaltura y capturas usadas en la grabacion. |

## 4. Alcance funcional del caso

El caso solicitado habla de una plataforma de cursos en linea. En esta entrega
el alcance implementado se concentra en el flujo backend evaluable:

- Administracion basica de cursos.
- Inscripcion de estudiantes en cursos.
- Generacion de resumen de inscripcion.
- Publicacion del resumen en RabbitMQ.
- Consumo del resumen desde RabbitMQ y persistencia en Oracle.
- Almacenamiento del resumen en AWS S3.
- Cliente frontend para ejecutar el flujo con JWT.

El backend Spring Boot cumple el rol de BFF para la demostracion, ya que expone
los endpoints consumidos por el frontend y orquesta las operaciones hacia
RabbitMQ, Oracle y S3.

Para IDaaS, el proyecto valida autenticacion con JWT. Si la evaluacion exige
roles o permisos diferenciados, se debe complementar la evidencia de Azure AD
B2C mostrando los claims o grupos configurados para el usuario de prueba.

## 5. Microservicio backend

El backend fue desarrollado con Java 21 y Spring Boot. Expone endpoints REST que
responden en formato JSON para las operaciones principales del caso.

Endpoints principales:

| Metodo | Endpoint | Descripcion |
| --- | --- | --- |
| GET | `/cursos` | Lista los cursos disponibles. |
| POST | `/cursos` | Crea un curso y lo persiste en Oracle Cloud. |
| POST | `/inscripciones` | Registra una inscripcion de estudiante. |
| GET | `/inscripciones/{numeroResumen}/resumen` | Genera y descarga el resumen de inscripcion. |
| POST | `/inscripciones/{numeroResumen}/resumenes-mq/producir` | Publica un resumen en RabbitMQ. |
| POST | `/inscripciones/resumenes-mq/consumir` | Consume un mensaje desde RabbitMQ y lo guarda en Oracle. |
| POST | `/s3/uploadResumen` | Sube el resumen generado a AWS S3. |
| PUT | `/s3/updateResumen` | Actualiza el resumen almacenado en S3. |
| GET | `/s3/downloadResumen` | Descarga el resumen desde S3. |
| DELETE | `/s3/deleteResumen` | Elimina el resumen desde S3. |

## 6. Seguridad con IDaaS

La seguridad del backend se implemento con Spring Security y OAuth2 Resource
Server. Todos los endpoints requieren un token JWT valido emitido por Azure AD
B2C. El backend valida el issuer configurado mediante la variable
`AZURE_B2C_ISSUER_URI`.

Para demostrar la seguridad se consideran dos pruebas:

- Solicitud sin token: el backend responde `401 Unauthorized`.
- Solicitud con JWT valido: el backend responde correctamente, por ejemplo
  `200 OK` en `GET /cursos`.

## 7. API Manager

Los endpoints del backend se publican mediante AWS API Gateway. API Gateway
funciona como punto de entrada administrado hacia el backend desplegado en EC2.

Las pruebas funcionales pueden ejecutarse contra la URL directa de EC2 o contra
la URL de API Gateway. Para la evidencia final se recomienda mostrar ambas:

- EC2 directo para demostrar que el contenedor backend esta operativo.
- API Gateway para demostrar la administracion de endpoints solicitada por la
  pauta.

## 8. RabbitMQ

RabbitMQ se ejecuta en Docker usando la imagen `rabbitmq:3-management`. La
aplicacion utiliza una cola para procesar mensajes relacionados con resumenes de
inscripcion.

Configuracion usada:

| Elemento | Valor |
| --- | --- |
| Cola | `resumen.inscripcion.queue` |
| Exchange | `resumen.inscripcion.exchange` |
| Routing key | `resumen.inscripcion.key` |
| Contenedor | `formativa-rabbitmq` |
| Consola | `http://IP_EC2:15672` |
| Conexion backend | `rabbitmq:5672` dentro de la red Docker |

Variables configuradas:

| Variable | Valor usado |
| --- | --- |
| `RABBITMQ_HOST` | `rabbitmq` en EC2 |
| `RABBITMQ_PORT` | `5672` |
| `RABBITMQ_USERNAME` | Secret o `guest` |
| `RABBITMQ_PASSWORD` | Secret o `guest` |
| `RABBITMQ_RESUMEN_QUEUE` | `resumen.inscripcion.queue` |
| `RABBITMQ_RESUMEN_EXCHANGE` | `resumen.inscripcion.exchange` |
| `RABBITMQ_RESUMEN_ROUTING_KEY` | `resumen.inscripcion.key` |

Para evitar que la configuracion dependa solamente del arranque del backend,
RabbitMQ carga el archivo `rabbitmq/definitions.json`, donde se declaran la
cola, el exchange y el binding con la routing key requerida.

El flujo demostrado es:

1. Crear curso.
2. Crear inscripcion.
3. Generar resumen.
4. Publicar el resumen en RabbitMQ.
5. Revisar la cola en RabbitMQ Management.
6. Consumir el mensaje desde el backend.
7. Verificar en Oracle la fila creada en `RESUMENES_INSCRIPCION_MQ`.

## 9. Almacenamiento cloud con S3

AWS S3 se utiliza para almacenar los archivos de resumen de inscripcion. El
archivo se guarda con la estructura:

```text
numeroResumen/resumen.txt
```

Las operaciones disponibles son subir, actualizar, descargar y eliminar el
resumen. La evidencia debe mostrar la respuesta exitosa desde Postman y el
objeto visible en el bucket S3.

## 10. Persistencia Oracle Cloud

Oracle Cloud Database se utiliza para persistir la informacion de cursos,
inscripciones y resumenes procesados desde RabbitMQ.

Tablas principales:

| Tabla | Uso |
| --- | --- |
| `CURSOS` | Cursos disponibles. |
| `INSCRIPCIONES` | Inscripciones realizadas por estudiantes. |
| `INSCRIPCION_CURSOS` | Relacion entre inscripciones y cursos. |
| `RESUMENES_INSCRIPCION_MQ` | Mensajes consumidos desde RabbitMQ. |

El script base se encuentra en `docs/oracle_schema.sql`.

## 11. Frontend

El frontend se encuentra separado del backend en la carpeta `frontend/`. Se
publica como una imagen Docker independiente y se despliega como contenedor
`formativa-frontend`.

En EC2 queda disponible en:

```text
http://IP_PUBLICA_EC2:3000
```

El frontend permite ingresar la URL del backend, pegar el JWT de Azure AD B2C y
ejecutar el flujo de prueba: listar cursos, crear curso, crear inscripcion,
generar resumen, producir/consumir RabbitMQ y probar S3.

## 12. CI/CD y despliegue

El despliegue se automatiza con GitHub Actions. El pipeline realiza las
siguientes acciones:

1. Descarga el codigo fuente.
2. Configura Java 21.
3. Ejecuta `mvn clean package`.
4. Construye la imagen Docker del backend.
5. Construye la imagen Docker del frontend.
6. Publica ambas imagenes en Docker Hub.
7. Se conecta a EC2 por SSH.
8. Copia `docker-compose.ec2.yml`.
9. Levanta los contenedores `formativa-cloud-native`, `formativa-frontend` y
   `formativa-rabbitmq`.

Servicios desplegados:

| Servicio | Contenedor | Puerto |
| --- | --- | --- |
| Frontend | `formativa-frontend` | `3000` |
| Backend | `formativa-cloud-native` | `8080` |
| RabbitMQ Management | `formativa-rabbitmq` | `15672` |
| RabbitMQ AMQP | `formativa-rabbitmq` | `5672` interno Docker |

## 13. Pruebas

La coleccion Postman final se encuentra en:

```text
docs/postman_eft_semana9_collection.json
```

La coleccion incluye pruebas de seguridad, flujo funcional completo, RabbitMQ
Management, S3 y errores controlados.

Tambien se ejecutaron pruebas automatizadas del backend con Maven:

```text
mvn test
```

Resultado esperado:

```text
Tests run: 10, Failures: 0, Errors: 0
```

## 14. Entregables

Para la entrega final se debe incluir:

- Link al repositorio GitHub.
- Informe final en Word o PDF usando este documento como base.
- Coleccion `docs/postman_eft_semana9_collection.json`.
- Script `docs/oracle_schema.sql`.
- Evidencias/capturas indicadas en `docs/EVIDENCIAS_EFT.md`.
- Link del video Kaltura.

## 15. Conclusiones

La solucion cumple con los requerimientos centrales de la Evaluacion Final
Transversal: backend Spring Boot, autenticacion con IDaaS, API Manager,
RabbitMQ en Docker con productor y consumidor, almacenamiento cloud en S3,
persistencia en Oracle Cloud, frontend separado, despliegue en la nube y
pipeline CI/CD.

La evidencia final debe centrarse en demostrar el flujo completo funcionando en
EC2/API Gateway, junto con las capturas de Azure AD B2C, RabbitMQ, S3, Oracle,
GitHub Actions y el frontend.
