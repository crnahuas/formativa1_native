# Semana 9 - Preparacion EFT

## Objetivo

Preparar la entrega final transversal usando el proyecto existente. No se
requiere rehacer la aplicacion desde cero: el backend ya cubre cursos,
inscripciones, seguridad JWT, Oracle Cloud, S3, RabbitMQ, Docker y despliegue.

## Brechas corregidas en esta etapa

- Se agrega una guia de IDaaS y API Manager en
  `docs/SEMANA5_IDAAS_API_MANAGER_SECURITY.md`.
- Se agrega un frontend separado en `frontend/index.html`, publicado como
  contenedor independiente para demostrar consumo de endpoints con token JWT.
- Se agrega un endpoint productor explicito para RabbitMQ:
  `POST /inscripciones/{numeroResumen}/resumenes-mq/producir`.
- Se alinea el pipeline para desplegar con `docker-compose.ec2.yml`, de modo que
  la app y RabbitMQ queden levantados juntos.

## Flujo final recomendado

1. Obtener un JWT vigente desde Azure AD B2C.
2. Importar `docs/postman_eft_semana9_collection.json` en Postman.
3. Configurar `accessToken` con el JWT vigente.
4. Configurar `baseUrl` con EC2 o API Gateway.
5. Crear un curso.
6. Crear una inscripcion.
7. Generar el resumen.
8. Publicar el resumen en RabbitMQ.
9. Consumir el mensaje y guardar el resultado en Oracle Cloud.
10. Subir, descargar o eliminar el resumen en S3.

Como cliente visual opcional tambien se puede abrir `frontend/index.html` en el
navegador o usar el contenedor `formativa-frontend` en el puerto `3000`.

## Endpoints finales

| Metodo | Ruta | Evidencia |
| --- | --- | --- |
| GET | `/cursos` | JSON con cursos disponibles |
| POST | `/cursos` | Curso persistido en Oracle |
| POST | `/inscripciones` | Inscripcion persistida en Oracle |
| GET | `/inscripciones/{numeroResumen}/resumen` | Archivo `resumen.txt` |
| POST | `/inscripciones/{numeroResumen}/resumenes-mq/producir` | Mensaje enviado a RabbitMQ |
| POST | `/inscripciones/resumenes-mq/consumir` | Mensaje consumido y guardado en Oracle |
| POST | `/s3/uploadResumen` | Objeto creado en S3 |
| PUT | `/s3/updateResumen` | Objeto reemplazado en S3 |
| GET | `/s3/downloadResumen` | Objeto descargado desde S3 |
| DELETE | `/s3/deleteResumen` | Objeto eliminado desde S3 |

## Servicios desplegados

| Servicio | Contenedor | Puerto |
| --- | --- | --- |
| Frontend | `formativa-frontend` | `3000` |
| Backend Spring Boot | `formativa-cloud-native` | `8080` |
| RabbitMQ Management | `formativa-rabbitmq` | `15672` |

## Evidencia para la pauta

- Spring Boot: mostrar codigo y respuesta JSON de `/cursos`.
- IDaaS: mostrar Azure AD B2C y llamada con JWT.
- RabbitMQ: mostrar cola, mensaje pendiente, consumo y registro en Oracle.
- API Manager: mostrar rutas en API Gateway y prueba por URL Gateway.
- S3: mostrar bucket y objeto `numeroResumen/resumen.txt`.
- Frontend: mostrar `http://IP_EC2:3000` consumiendo el backend con JWT.
- CI/CD: mostrar GitHub Actions desplegando backend, frontend y RabbitMQ en EC2.
- Postman: usar `docs/postman_eft_semana9_collection.json` como flujo final.
- Documentacion: incluir este archivo y las guias de configuracion.
- Video: recorrer el flujo final en menos de 10 minutos.
