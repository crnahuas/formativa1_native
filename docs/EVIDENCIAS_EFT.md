# Checklist de evidencias EFT

Usar esta lista para preparar las capturas del informe y el video. Conviene
nombrar las imagenes con el numero indicado para que el orden sea claro.

## 1. Repositorio y codigo

| N | Evidencia | Que debe mostrar |
| --- | --- | --- |
| 01 | Repositorio GitHub | Codigo fuente, carpetas `src`, `frontend`, `docs`, `.github`. |
| 02 | Backend Spring Boot | Controladores o endpoints principales. |
| 03 | Frontend separado | Carpeta `frontend/` con `index.html` y `Dockerfile`. |
| 04 | Docker Compose EC2 | Servicios `app`, `frontend` y `rabbitmq`. |
| 05 | Script Oracle | Archivo `docs/oracle_schema.sql`. |

## 2. IDaaS y seguridad

| N | Evidencia | Que debe mostrar |
| --- | --- | --- |
| 06 | Azure AD B2C | Tenant, app o user flow usado para emitir JWT. |
| 07 | App registrada | Client ID o configuracion de la aplicacion, sin mostrar secretos. |
| 08 | Token JWT | Claims principales, especialmente `iss`, sin exponer datos sensibles si no es necesario. |
| 09 | Roles o permisos | Claims, grupos o configuracion de permisos si estan definidos en Azure AD B2C. |
| 10 | GitHub Secret IDaaS | Secret `AZURE_B2C_ISSUER_URI` creado, sin mostrar valor. |
| 11 | Prueba sin token | `GET /cursos` respondiendo `401 Unauthorized`. |
| 12 | Prueba con token | `GET /cursos` respondiendo `200 OK`. |

## 3. API Gateway

| N | Evidencia | Que debe mostrar |
| --- | --- | --- |
| 13 | API Gateway | API creada en AWS. |
| 14 | Rutas registradas | Rutas de cursos, inscripciones, RabbitMQ y S3. |
| 15 | Integracion HTTP | Integracion hacia EC2/backend. |
| 16 | Prueba por API Gateway | Request con JWT respondiendo correctamente. |

## 4. Despliegue y contenedores

| N | Evidencia | Que debe mostrar |
| --- | --- | --- |
| 17 | GitHub Actions verde | Workflow final ejecutado correctamente. |
| 18 | Build backend/frontend | Pasos donde se construyen y publican ambas imagenes. |
| 19 | EC2 `docker ps` | Contenedores `formativa-cloud-native`, `formativa-frontend`, `formativa-rabbitmq`. |
| 20 | Backend EC2 | `http://IP_EC2:8080/cursos` funcionando con JWT. |
| 21 | Frontend EC2 | `http://IP_EC2:3000` cargando la interfaz. |
| 22 | Security Group | Puertos necesarios abiertos: `8080`, `3000`, `15672` y `22`. |
| 22B | Variables RabbitMQ | `RABBITMQ_HOST=rabbitmq`, `RABBITMQ_PORT=5672`, cola, exchange y routing key en secrets/env. |

## 5. Flujo funcional Postman

Usar `docs/postman_eft_semana9_collection.json`.

| N | Evidencia | Request | Resultado esperado |
| --- | --- | --- | --- |
| 23 | Crear curso | `POST /cursos` | `201 Created` con `id`. |
| 24 | Listar cursos | `GET /cursos` | `200 OK` con lista JSON. |
| 25 | Crear inscripcion | `POST /inscripciones` | `201 Created` con `numeroResumen` o `id`. |
| 26 | Generar resumen | `GET /inscripciones/{numeroResumen}/resumen` | `200 OK` y archivo de texto. |
| 27 | Producir RabbitMQ | `POST /inscripciones/{numeroResumen}/resumenes-mq/producir` | `200 OK` con cola/exchange/routing key. |
| 28 | Consumir RabbitMQ | `POST /inscripciones/resumenes-mq/consumir` | `200 OK` con registro procesado. |
| 29 | Subir S3 | `POST /s3/uploadResumen` | `200 OK` con clave `numeroResumen/resumen.txt`. |
| 30 | Actualizar S3 | `PUT /s3/updateResumen` | `200 OK`. |
| 31 | Descargar S3 | `GET /s3/downloadResumen` | `200 OK` con contenido del resumen. |

La eliminacion de S3 es opcional para limpieza. Si se necesita capturar el
objeto en el bucket, tomar la captura antes de ejecutar `DELETE /s3/deleteResumen`.

## 6. RabbitMQ

| N | Evidencia | Que debe mostrar |
| --- | --- | --- |
| 32 | RabbitMQ Overview | Consola `http://IP_EC2:15672` activa. |
| 33 | Cola | `resumen.inscripcion.queue`. |
| 34 | Exchange | `resumen.inscripcion.exchange`. |
| 35 | Binding | Routing key `resumen.inscripcion.key`. |
| 36 | Mensaje pendiente | Mensaje publicado antes de consumir. |
| 37 | Mensaje consumido | Cola vacia o conteo actualizado despues del consumo. |
| 37B | Definiciones RabbitMQ | Archivo `rabbitmq/definitions.json` con cola, exchange y binding. |

Valores esperados para la captura de configuracion:

```text
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_RESUMEN_QUEUE=resumen.inscripcion.queue
RABBITMQ_RESUMEN_EXCHANGE=resumen.inscripcion.exchange
RABBITMQ_RESUMEN_ROUTING_KEY=resumen.inscripcion.key
```

## 7. Oracle Cloud

| N | Evidencia | Que debe mostrar |
| --- | --- | --- |
| 38 | Tabla cursos | Registros creados en `CURSOS`. |
| 39 | Tabla inscripciones | Registro creado en `INSCRIPCIONES`. |
| 40 | Tabla RabbitMQ | Registro creado en `RESUMENES_INSCRIPCION_MQ`. |

Consulta sugerida:

```sql
SELECT * FROM RESUMENES_INSCRIPCION_MQ ORDER BY ID DESC;
```

## 8. AWS S3

| N | Evidencia | Que debe mostrar |
| --- | --- | --- |
| 41 | Bucket S3 | Bucket configurado para la entrega. |
| 42 | Objeto resumen | Archivo `numeroResumen/resumen.txt`. |
| 43 | Descarga Postman | Resumen descargado desde `/s3/downloadResumen`. |

## 9. Video

El video debe durar menos de 10 minutos y mostrar:

1. Arquitectura general.
2. Azure AD B2C y token.
3. API Gateway.
4. Frontend consumiendo backend.
5. Flujo Postman principal.
6. RabbitMQ con mensaje.
7. Oracle con registro consumido.
8. S3 con objeto.
9. GitHub Actions y EC2 con contenedores.

## 10. Archivos a incluir en ZIP/RAR

| Archivo | Proposito |
| --- | --- |
| Link del repositorio GitHub | Codigo fuente del proyecto. |
| `docs/INFORME_EFT.md` o version Word | Informe final. |
| `docs/EVIDENCIAS_EFT.md` | Checklist de capturas. |
| `docs/postman_eft_semana9_collection.json` | Coleccion final de pruebas. |
| `docs/oracle_schema.sql` | Script de base de datos. |
| Link del video Kaltura | Presentacion final. |
