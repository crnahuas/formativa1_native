# Checklist de evidencias EFT

Usar esta lista como orden definitivo para el informe y el video. Las capturas
nuevas deben ir agrupadas por tema, con una explicacion breve antes o despues de
cada imagen. La idea es que cada evidencia responda una pregunta concreta de la
pauta: seguridad, roles, API Gateway, mensajeria, persistencia, almacenamiento,
frontend, infraestructura y CI/CD.

Las capturas nuevas reemplazan las evidencias antiguas que mostraban fallas
intermedias de configuracion. En el informe final solo deben quedar evidencias
del estado corregido.

## 00 - Seguridad y roles

Objetivo de la seccion: demostrar que el backend esta protegido con JWT y que
los permisos cambian segun el rol entregado por Azure AD B2C.

| Archivo | Que demuestra |
| --- | --- |
| `00.1 EC2 sin token debe responder 401.png` | El backend en EC2 rechaza acceso sin JWT. |
| `00.2 ESTUDIANTE lista cursos debe responder 200.png` | Un estudiante autenticado puede consultar cursos. |
| `00.3 ESTUDIANTE crea curso debe responder 403.png` | Un estudiante no puede ejecutar una accion de instructor. |
| `00.4 INSTRUCTOR crea curso debe responder 201.png` | Un instructor puede crear cursos correctamente. |

## 01 - Funcionalidad academica

Objetivo de la seccion: demostrar que el backend cubre el caso academico
principal, no solo operaciones basicas de cursos. Estas capturas muestran
creacion y consulta de contenidos, examenes, inscripciones, intentos de examen
y calificaciones.

| Archivo | Que demuestra |
| --- | --- |
| `01.1 Crear contenido de curso.png` | Creacion de contenido asociado a curso. |
| `01.2 Listar contenidos de curso.png` | Consulta de contenidos persistidos. |
| `01.3 Crear examen de curso.png` | Creacion de examen asociado a curso. |
| `01.4 Listar examenes de curso.png` | Consulta de examenes persistidos. |
| `01.5 Crear inscripcion de estudiante.png` | Inscripcion de estudiante y calculo de total. |
| `01.6 Registrar calificacion.png` | Registro de calificacion, reemplaza la evidencia anterior con `404`. |
| `01.7 Listar calificaciones por inscripcion.png` | Consulta de calificaciones por inscripcion. |
| `01.8 ESTUDIANTE inicia intento de examen.png` | El estudiante inicia un intento real asociado a una inscripcion y examen. |
| `01.9 ESTUDIANTE finaliza examen y registra calificacion.png` | El estudiante envia respuestas, finaliza el intento y se genera calificacion. |
| `01.10 Listar intentos por inscripcion.png` | Trazabilidad de intentos realizados por la inscripcion. |

## 02 - API Gateway

Objetivo de la seccion: demostrar que las rutas fueron publicadas en AWS API
Gateway, protegidas con autorizacion JWT e integradas con el backend en EC2.

| Archivo | Que demuestra |
| --- | --- |
| `02.1 API Gateway sin token debe responder 401.png` | API Gateway rechaza solicitudes sin token. |
| `02.2 API Gateway con token debe responder 200.png` | API Gateway integra correctamente con `/cursos`. |
| `02.3 API Gateway contenidos con token debe responder 200.png` | API Gateway expone contenidos con JWT valido. |
| `02.4 API Gateway calificaciones con token debe responder 200.png` | API Gateway expone calificaciones con JWT valido. |
| `02.5 API Gateway iniciar intento con token debe responder 201.png` | API Gateway expone el inicio de intento de examen. |
| `02.6 API Gateway listar intentos con token debe responder 200.png` | API Gateway expone la trazabilidad de intentos. |
| `api.png` | Rutas registradas en API Gateway. |
| `api-seguridad.png` | Rutas protegidas con JWT Auth. |
| `api-asignación.png` | Metodos HTTP asignados a las rutas. |
| `integracion.png` | Integraciones HTTP hacia el backend en EC2. |

## 03 - RabbitMQ

Objetivo de la seccion: demostrar el flujo minimo exigido de mensajeria:
generar resumen, publicar mensaje, verificarlo en RabbitMQ Management y
consumirlo desde el backend.

| Archivo | Que demuestra |
| --- | --- |
| `03.1 Generar resumen local.png` | Generacion del resumen antes de enviarlo a mensajeria. |
| `03.2 Producir mensaje RabbitMQ.png` | Productor publica mensaje con cola, exchange y routing key. |
| `03.2 Producir mensaje RabbitMQ Management.png` | El mensaje publicado se observa desde RabbitMQ Management. |
| `03.3 RabbitMQ Management - detalle cola.png` | Detalle de la cola `resumen.inscripcion.queue`. |
| `03.4 Consumir mensaje RabbitMQ.png` | Consumidor procesa el mensaje y responde correctamente. |

Configuracion que debe coincidir:

```text
Queue: resumen.inscripcion.queue
Exchange: resumen.inscripcion.exchange
Routing key: resumen.inscripcion.key
```

## 04 - AWS S3

Objetivo de la seccion: demostrar que el resumen de inscripcion puede guardarse
como archivo en S3 y recuperarse posteriormente desde el backend.

| Archivo | Que demuestra |
| --- | --- |
| `04.1 Subir resumen a S3.png` | Endpoint sube `resumen.txt` al bucket S3. |
| `04.1 Subir resumen a S3 Bucket.png` | El objeto/carpeta existe en la consola de AWS S3. |
| `04.2 Descargar resumen desde S3.png` | Endpoint descarga el resumen desde S3. |
| `04.2 Descargar resumen desde S3 File.png` | Archivo descargado contiene el resumen esperado. |

## 05 - Oracle Cloud

Objetivo de la seccion: demostrar que los datos quedan persistidos en Oracle
Cloud y que las operaciones realizadas por API y RabbitMQ tienen respaldo en la
base de datos.

| Archivo | Que demuestra |
| --- | --- |
| `oracle 01.png` | Registros en `CONTENIDOS_CURSO`, `EXAMENES` y `CALIFICACIONES`. |
| `oracle 02.png` | Registros en `CURSOS`, `INSCRIPCIONES` y `RESUMENES_INSCRIPCION_MQ`. |

Consultas usadas:

```sql
SELECT * FROM CONTENIDOS_CURSO ORDER BY ID DESC;
SELECT * FROM EXAMENES ORDER BY ID DESC;
SELECT * FROM CALIFICACIONES ORDER BY ID DESC;

SELECT * FROM CURSOS ORDER BY ID DESC;
SELECT * FROM INSCRIPCIONES ORDER BY ID DESC;
SELECT * FROM RESUMENES_INSCRIPCION_MQ ORDER BY ID DESC;
```

## 06 - Frontend e infraestructura

Objetivo de la seccion: demostrar que la solucion no depende solo de Postman.
El frontend esta separado del backend y la infraestructura permite acceder a los
servicios desplegados en EC2.

| Archivo | Que demuestra |
| --- | --- |
| `Frontend.png` | Cliente frontend separado configurado contra backend. |
| `Frontend login JWT Azure B2C.png` | Pantalla de ingreso del frontend usando JWT emitido por Azure AD B2C. |
| `operacion-ok.png` | Operacion exitosa ejecutada desde el frontend. |
| `Frontend examen finalizado.png` | Intento de examen finalizado desde frontend con token obtenido por login. |
| `grupo-seguridad.png` | Grupo de seguridad con puertos necesarios para la entrega. |
| `GitHub.png` | Repositorio GitHub del proyecto. |
| `GitHub_Actions.png` | Pipeline de GitHub Actions. |
| `nuevo_secret-repositorio.png` | Secrets configurados para despliegue e integraciones. |

## Evidencias reemplazadas

No usar capturas donde aparezcan estos estados como resultado final:

- Error de autorizacion al ingresar a RabbitMQ Management.
- `404` al registrar calificacion.
- `500` o `502` en productor/consumidor RabbitMQ.
- `201 Created` cuando el usuario ESTUDIANTE intenta crear curso.
- Pruebas con `TU_EC2`, `TU_TOKEN` o URLs de ejemplo.

Esas capturas pueden guardarse solo como historial de depuracion, pero no como
evidencia principal de cumplimiento. Para el informe se deben usar las capturas
corregidas listadas en las secciones anteriores.
